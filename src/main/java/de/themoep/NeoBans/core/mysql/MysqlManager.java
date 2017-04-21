package de.themoep.NeoBans.core.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.LogEntry;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TimedPunishmentEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Phoenix616 on 08.03.2015.
 */
public class MysqlManager implements DatabaseManager {

    private final HikariDataSource ds;
    private NeoBansPlugin plugin;

    private String tablePrefix;

    public MysqlManager(NeoBansPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Loading MySQLManager...");
        this.tablePrefix = plugin.getConfig().getString("mysql.tableprefix", "neo_");

        String host = plugin.getConfig().getString("mysql.host", "127.0.0.1");
        String port = plugin.getConfig().getString("mysql.port", "3306");
        String database = plugin.getConfig().getString("mysql.database", "minebench");

        ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        ds.setUsername(plugin.getConfig().getString("mysql.user", "root"));
        ds.setPassword(plugin.getConfig().getString("mysql.pass", ""));
        ds.setConnectionTimeout(5000);

        initializeTables();
        updateTables();
    }

    @Override
    public void initializeTables() {
        try (Connection conn = getConn();
             Statement staBans = conn.createStatement();
             Statement staLog = conn.createStatement();
        ) {
            String sqlBans = "CREATE TABLE IF NOT EXISTS " + getTablePrefix() + "bans ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "type VARCHAR(64),"
                    + "bannedid CHAR(36), INDEX (bannedid),"
                    + "issuerid CHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000',"
                    + "reason TINYTEXT,"
                    + "comment TINYTEXT,"
                    + "time BIGINT(11) NOT NULL,"
                    + "endtime BIGINT(11)"
                    + ")  DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;";

            staBans.execute(sqlBans);

            /*String sqlPlayers = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "players` ("
                    + "playerid VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,"
                    + "name VARCHAR(16) NOT NULL INDEX,"
                    + "firstseen BIGINT(11) NOT NULL,"
                    + "lastseen BIGINT(11) NOT NULL,"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;";

            Statement staPlayers = this.getConn().createStatement();
            staPlayers.execute(sqlPlayers);
            staPlayers.close();*/

            String sqlLog = "CREATE TABLE IF NOT EXISTS " + getTablePrefix() + "log ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "type VARCHAR(64),"
                    + "playerid CHAR(36),"
                    + "issuerid CHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000',"
                    + "msg TEXT,"
                    + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "INDEX (type, playerid)"
                    + ")  DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;";

            staLog.execute(sqlLog);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while initializing the database tables!", e);
        }
    }

    @Override
    public void updateTables() {
        if (plugin.compareVersion("0.4") >= 0) {
            try (Connection conn = getConn();
                 Statement staAddTypeColumn = conn.createStatement();
                 Statement staFillTypeColumn = conn.createStatement();
                 Statement staUpdateBans = conn.createStatement();
                 Statement staUpdateLog = conn.createStatement();
            ) {
                try {
                    staAddTypeColumn.execute("ALTER TABLE " + getTablePrefix() + "bans ADD COLUMN type VARCHAR(64) DEFAULT NULL, ADD INDEX (type, bannedid) ;");
                    staFillTypeColumn.execute("UPDATE TABLE " + getTablePrefix() + "bans SET type = IF(endtime == 0, 'BAN', 'TEMPBAN') WHERE type IS NULL;");
                    plugin.getLogger().log(Level.INFO, "Added type column to " + getTablePrefix() + "bans!");
                } catch (SQLException b) {
                    if (b.getErrorCode() == 1060) {
                        plugin.getLogger().log(Level.INFO, "'type' column already exists in " + getTablePrefix() + "bans!");
                    } else {
                        plugin.getLogger().log(Level.SEVERE, "Error while adding column type to " + getTablePrefix() + "bans!", b);
                    }
                }

                String sqlUpdateBans = "ALTER TABLE " + getTablePrefix() + "bans " +
                        "MODIFY COLUMN bannedid CHAR(36)," +
                        "MODIFY COLUMN issuerid CHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000';";

                staUpdateBans.execute(sqlUpdateBans);

                String sqlUpdateLog = "ALTER TABLE " + getTablePrefix() + "log " +
                        "MODIFY COLUMN type VARCHAR(64)," +
                        "MODIFY COLUMN bannedid CHAR(36)," +
                        "MODIFY COLUMN issuerid CHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000';";

                staUpdateLog.execute(sqlUpdateLog);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while updating the database tables!", e);
            }
        }
    }

    @Override
    public boolean log(EntryType type, UUID playerId, UUID issuerid, String message) {
        String query = "INSERT INTO " + getTablePrefix() + "log (type, playerid, issuerid, msg) values (?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            sta.setString(1, type.toString());
            sta.setString(2, playerId.toString());
            sta.setString(3, issuerid.toString());
            sta.setString(4, message);
            sta.execute();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to insert into the log table!");
            e.printStackTrace();
            return false;
        }
    }

    public List<Entry> getLogEntries(UUID playerId, int page, int amount) {
        int start = page * amount;
        List<Entry> logList = new ArrayList<>();
        String query = "SELECT * FROM " + getTablePrefix() + "log WHERE playerid = ? OR issuerid = ? ORDER BY time DESC LIMIT ?,?";
        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            sta.setString(1, playerId.toString());
            sta.setString(2, playerId.toString());
            sta.setInt(3, start);
            sta.setInt(4, amount);
            ResultSet rs = sta.executeQuery();

            while (rs.next()) {
                String typeStr = rs.getString("type");
                try {
                    EntryType type = EntryType.valueOf(typeStr);
                    UUID entryPlayerId = UUID.fromString(rs.getString("playerid"));
                    UUID issuerId = UUID.fromString(rs.getString("issuerid"));
                    String message = rs.getString("msg");
                    Timestamp timestamp = rs.getTimestamp("time");
                    logList.add(new LogEntry(type, entryPlayerId, issuerId, message, timestamp.getTime() / 1000));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.SEVERE, "Unknown entry type " + rs.getString("type") + " while loading log entry!");
                    logList.add(new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation(
                            "neobans.error.unknownentrytype",
                            "type", typeStr
                    )));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get the log entries on page " + page + " for player " + playerId + "!", e);
            logList.add(new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation(
                    "neobans.error.database"
            )));
        }
        return logList;
    }

    @Override
    public void disable() {
        plugin.getLogger().info("Closing database connection...");
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
        plugin.getLogger().info("Database connection closed.");
    }

    @Override
    public boolean update(int userId, String column, String value) {

        String query = "UPDATE " + getTablePrefix() + "bans SET " + column + "=? WHERE id=?";

        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            sta.setString(1, value);
            sta.setInt(2, userId);
            sta.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Entry add(PunishmentEntry entry) {
        String query = "INSERT INTO " + getTablePrefix() + "bans (type, bannedid, issuerid, reason, comment, time, endtime) values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            sta.setString(1, entry.getType().toString());
            sta.setString(2, entry.getPunished().toString());
            sta.setString(3, entry.getIssuer().toString());
            sta.setString(4, entry.getReason());
            sta.setString(5, entry.getComment());
            sta.setString(6, Long.toString(entry.getTime()));
            if (entry instanceof TimedPunishmentEntry)
                sta.setLong(7, ((TimedPunishmentEntry) entry).getEndtime());
            else
                sta.setLong(7, 0);
            sta.executeUpdate();
            ResultSet rs = sta.getGeneratedKeys();

            if (rs.next()) {
                entry.setDbId(rs.getInt(1));
                String msg = entry.getReason().isEmpty() ? "" : "Reason: " + entry.getReason();
                if (entry instanceof TimedPunishmentEntry) {
                    msg = "Duration: " + ((TimedPunishmentEntry) entry).getFormattedDuration(plugin.getLanguageConfig(), true) + " " + msg;
                }
                if (!log(entry.getType(), entry.getPunished(), entry.getIssuer(), msg)) {
                    plugin.getLogger().warning("Error while trying to log addition of ban " + rs.getInt(1) + " for player " + plugin.getPlayerName(entry.getPunished()) + "!");
                }
                return entry;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to add ban for player " + plugin.getPlayerName(entry.getPunished()) + " to the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        }
        return entry;
    }

    @Override
    public Entry remove(PunishmentEntry punishment, UUID invokeId, boolean log) {

        if (log) {
            String msg = punishment.getType() + " " + (invokeId.equals(new UUID(0, 0)) ? "Automatic removal. " : "") + "Orig. reason: " + punishment.getReason();
            if (punishment instanceof TimedPunishmentEntry) {
                msg += "Orig. endtime: " + ((TimedPunishmentEntry) punishment).getEndtime(plugin.getLanguageConfig().getTranslation("time.format"));
            }
            log(EntryType.REMOVED, punishment.getPunished(), invokeId, msg);
        }


        String query = "DELETE FROM " + getTablePrefix() + "bans WHERE " + ((punishment.getDbId() > 0) ? "id=?" : "bannedid=? AND type=? ORDER BY time DESC LIMIT 1");
        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            if (punishment.getDbId() > 0) {
                sta.setInt(1, punishment.getDbId());
            } else {
                sta.setString(1, punishment.getPunished().toString());
                sta.setString(2, punishment.getType().toString());
            }
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to delete ban of player " + plugin.getPlayerName(punishment.getPunished()) + " (BanID: " + punishment.getDbId() + ") from the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        }
        return punishment;
    }

    @Override
    public int getCount(EntryType type, UUID playerId) {
        String query = "SELECT count(id) as count FROM " + getTablePrefix() + "log WHERE type=? AND playerid=?";
        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            sta.setString(1, type.toString());
            sta.setString(2, playerId.toString());

            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to to get the count of " + type.toString().toLowerCase() + " entries for player " + plugin.getPlayerName(playerId) + " from the log table!");
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Entry get(UUID id, EntryType... types) {
        String query = "SELECT id, type, issuerid, reason, comment, time, endtime FROM " + getTablePrefix() + "bans WHERE bannedid=?";
        if (types.length == 1) {
            query += " AND type = ?";
        } else if (types.length > 0) {
            query += " AND type IN (" + types[0].toString();
            for (int i = 1; i < types.length; i++) {
                query += "," + types[i].toString();
            }
            query += ")";
        }
        query += " ORDER BY time DESC;";

        try (Connection conn = getConn();
             PreparedStatement sta = conn.prepareStatement(query);
        ) {
            sta.setString(1, id.toString());
            if (types.length == 1) {
                sta.setString(2, types[0].toString());
            }

            ResultSet rs = sta.executeQuery();

            if (rs.next()) {
                UUID issuerId = UUID.fromString(rs.getString("issuerid"));
                String reason = rs.getString("reason");
                String comment = rs.getString("comment");
                long time = rs.getLong("time");
                long endtime = rs.getLong("endtime");
                EntryType type;
                try {
                    type = EntryType.valueOf(rs.getString("type").toUpperCase());
                } catch (IllegalArgumentException e) {
                    type = endtime > 0 ? EntryType.TEMPBAN : EntryType.BAN;
                }

                if (endtime > 0) {
                    return new TimedPunishmentEntry(type, id, issuerId, reason, comment, time, endtime);
                } else {
                    return new PunishmentEntry(type, id, issuerId, reason, comment, time);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to get ban of player " + plugin.getPlayerName(id) + " from the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        }
        return null;
    }

    public Connection getConn() throws SQLException {
        return ds.getConnection();
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

}
