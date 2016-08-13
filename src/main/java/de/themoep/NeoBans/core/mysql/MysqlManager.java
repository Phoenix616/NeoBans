package de.themoep.NeoBans.core.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.NeoBans.core.BanEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.LogEntry;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TempbanEntry;

import java.sql.*;
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
    }

    @Override
    public void initializeTables() {
        Connection conn = null;
        Statement staBans = null;
        Statement staLog = null;
        try {
            String sqlBans = "CREATE TABLE IF NOT EXISTS " + getTablePrefix() + "bans ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "bannedid VARCHAR(36), INDEX (bannedid),"
                    + "issuerid VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000',"
                    + "reason TINYTEXT,"
                    + "comment TINYTEXT,"
                    + "time BIGINT(11) NOT NULL,"
                    + "endtime BIGINT(11)"
                    + ")  DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;";

            conn = getConn();
            staBans = conn.createStatement();
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
                    + "type VARCHAR(20),"
                    + "playerid VARCHAR(36),"
                    + "issuerid VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000',"
                    + "msg TEXT,"
                    + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "INDEX (type, playerid)"
                    + ")  DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;";

            staLog = conn.createStatement();
            staLog.execute(sqlLog);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error while initializing the database tables!");
            e.printStackTrace();
        } finally {
            close(staBans);
            close(staLog);
            close(conn);
        }
    }

    @Override
    public boolean log(EntryType type, UUID playerId, UUID issuerid, String message) {
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            String query = "INSERT INTO " + getTablePrefix() + "log (type, playerid, issuerid, msg) values (?, ?, ?, ?)";
            conn = getConn();
            sta = conn.prepareStatement(query);
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
        } finally {
            close(sta);
            close(conn);
        }
    }

    public List<Entry> getLogEntries(UUID playerId, int page, int amount) {
        int start = page * amount;
        List<Entry> logList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            String query = "SELECT * FROM " + getTablePrefix() + "log WHERE playerid = ? OR issuerid = ? ORDER BY time DESC LIMIT ?,?";
            conn = getConn();
            sta = conn.prepareStatement(query);
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
        } finally {
            close(sta);
            close(conn);
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

        Connection conn = null;
        PreparedStatement sta = null;
        try {
            conn = getConn();
            sta = conn.prepareStatement(query);
            sta.setString(1, value);
            sta.setInt(2, userId);
            sta.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(sta);
            close(conn);
        }
        return true;
    }

    @Override
    public Entry add(BanEntry entry) {
        String query = "INSERT INTO " + getTablePrefix() + "bans (bannedid, issuerid, reason, comment, time, endtime) values (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            conn = getConn();
            sta = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            sta.setString(1, entry.getBanned().toString());
            sta.setString(2, entry.getIssuer().toString());
            sta.setString(3, entry.getReason());
            sta.setString(4, entry.getComment());
            sta.setString(5, Long.toString(entry.getTime()));
            if (entry instanceof TempbanEntry)
                sta.setLong(6, ((TempbanEntry) entry).getEndtime());
            else
                sta.setLong(6, 0);
            sta.executeUpdate();
            ResultSet rs = sta.getGeneratedKeys();

            if (rs.next()) {
                entry.setDbId(rs.getInt(1));
                String msg = entry.getReason().isEmpty() ? "" : "Reason: " + entry.getReason();
                if (entry instanceof TempbanEntry) {
                    msg = "Duration: " + ((TempbanEntry) entry).getFormattedDuration(plugin.getLanguageConfig(), true) + " " + msg;
                }
                if (!log(entry.getType(), entry.getBanned(), entry.getIssuer(), msg)) {
                    plugin.getLogger().warning("Error while trying to log addition of ban " + rs.getInt(1) + " for player " + plugin.getPlayerName(entry.getBanned()) + "!");
                }
                return entry;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to add ban for player " + plugin.getPlayerName(entry.getBanned()) + " to the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        } finally {
            close(sta);
            close(conn);
        }
        return entry;
    }

    @Override
    public Entry remove(BanEntry banentry, UUID invokeId, boolean log) {

        String msg = invokeId.equals(new UUID(0, 0)) ? "Automatic removal. " : "Orig. reason: " + banentry.getReason();
        if (banentry instanceof TempbanEntry) {
            msg += "Orig. endtime: " + ((TempbanEntry) banentry).getEndtime(plugin.getLanguageConfig().getTranslation("time.format"));
        }
        if (log && !log(EntryType.UNBAN, banentry.getBanned(), invokeId, msg))
            plugin.getLogger().warning("Error while trying to log deletion of ban of player " + plugin.getPlayerName(banentry.getBanned()) + "!");

        Connection conn = null;
        PreparedStatement sta = null;
        try {
            String query = "DELETE FROM " + getTablePrefix() + "bans WHERE " + ((banentry.getDbId() > 0) ? "id=?" : "bannedid=? ORDER BY time DESC LIMIT 1");
            conn = getConn();
            sta = conn.prepareStatement(query);
            sta.setString(1, (banentry.getDbId() > 0) ? Integer.toString(banentry.getDbId()) : banentry.getBanned().toString());
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to delete ban of player " + plugin.getPlayerName(banentry.getBanned()) + " (BanID: " + banentry.getDbId() + ") from the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        } finally {
            close(sta);
            close(conn);
        }
        return banentry;
    }

    @Override
    public int getCount(EntryType type, UUID playerId) {
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            String query = "SELECT count(id) as count FROM " + getTablePrefix() + "log WHERE type=? AND playerid=?";
            conn = getConn();
            sta = conn.prepareStatement(query);
            sta.setString(1, type.toString());
            sta.setString(2, playerId.toString());

            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to to get the count of " + type.toString().toLowerCase() + " entries for player " + plugin.getPlayerName(playerId) + " from the log table!");
            e.printStackTrace();
        } finally {
            close(sta);
            close(conn);
        }
        return 0;
    }

    @Override
    public Entry get(UUID id) {
        String sql = "SELECT id, issuerid, reason, comment, time, endtime FROM " + getTablePrefix() + "bans WHERE bannedid=? ORDER BY time DESC";

        Connection conn = null;
        PreparedStatement sta = null;
        try {
            conn = getConn();
            sta = conn.prepareStatement(sql);
            sta.setString(1, id.toString());

            ResultSet rs = sta.executeQuery();

            if (rs.next()) {
                String issuerid = rs.getString("issuerid");
                String reason = rs.getString("reason");
                String comment = rs.getString("comment");
                long time = rs.getLong("time");
                long endtime = rs.getLong("endtime");

                if (endtime > 0) {
                    return new TempbanEntry(id, UUID.fromString(issuerid), reason, comment, time, endtime);
                } else {
                    return new BanEntry(id, UUID.fromString(issuerid), reason, comment, time);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Encountered SQLException while trying to get ban of player " + plugin.getPlayerName(id) + " from the ban table!");
            e.printStackTrace();
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        } finally {
            close(sta);
            close(conn);
        }
        return null;
    }

    public Connection getConn() throws SQLException {
        return ds.getConnection();
    }
    
    public String getTablePrefix() { 
        return tablePrefix;
    }

    private static void close(Object o) {
        if (o != null) {
            try {
                if (o instanceof Connection) {
                    ((Connection) o).close();
                } else if (o instanceof Statement) {
                    ((Statement) o).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
