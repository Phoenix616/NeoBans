package de.themoep.NeoBans.core.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.LogEntry;
import de.themoep.NeoBans.core.NeoBansPlugin;

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

        initializeTables();
    }

    @Override
    public void initializeTables(){
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

            Statement staBans = getConn().createStatement();
            staBans.execute(sqlBans);
            staBans.close();

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

            Statement staLog = getConn().createStatement();
            staLog.execute(sqlLog);
            staLog.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error while initializing the database tables!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean log(EntryType type, UUID playerId, UUID issuerid, String message) {
        try {
            String query = "INSERT INTO " + getTablePrefix() + "log (type, playerid, issuerid, msg) values (?, ?, ?, ?)";
            PreparedStatement sta = getConn().prepareStatement(query);
            sta.setString(1, type.toString());
            sta.setString(2, playerId.toString());
            sta.setString(3, issuerid.toString());
            sta.setString(4, message);
            sta.execute();
            sta.close();
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
        try {
            String query = "SELECT * FROM " + getTablePrefix() + "log WHERE playerid = ? OR issuerid = ? ORDER BY time DESC LIMIT ?,?";
            PreparedStatement sta = getConn().prepareStatement(query);
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

            rs.close();
            sta.close();
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

    public Connection getConn() throws SQLException {
        return ds.getConnection();
    }
    
    public String getTablePrefix() { 
        return tablePrefix;
    }
}
