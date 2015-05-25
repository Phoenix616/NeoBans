package de.themoep.NeoBans.core;

import com.google.common.collect.ImmutableMap;
import de.themoep.NeoBans.core.mysql.MysqlManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class BanManager {
    
    private NeoBansPlugin plugin;

    protected ConcurrentHashMap<UUID, BanEntry> banMap;
    
    public BanManager(NeoBansPlugin plugin) {
        this.plugin = plugin;
        banMap = new ConcurrentHashMap<UUID, BanEntry>();
    }

    /**
     * Get the ban of a player with the given username
     * <br /><br />
     * <strong>Note:</strong> This method might execute a database query and should not be run on the main thread!
     * @param id The id of the player to get the ban of
     * @return The entry of the player, null if he doesn't have one, entry with type FAILURE if an error occurs (SQLException, etc.)
     */
    public Entry getBan(UUID id){
        if(id == null) {
            return null;
        }
        
        if(banMap.containsKey(id)) {
            return checkExpiration(banMap.get(id));
        }
            
        if(plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());
            
            String sql = "SELECT id, issuerid, reason, comment, time, endtime FROM " + mysql.getTablePrefix() + "bans WHERE bannedid=? ORDER BY time DESC";

            if(!mysql.isConnected()) {
                plugin.getLogger().severe("Could not establish a database connection when we should have one!");
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
            }
            
            try {
                PreparedStatement sta = mysql.getConn().prepareStatement(sql);
                sta.setString(1, id.toString());
                
                ResultSet rs = sta.executeQuery();
                
                if(rs.next()) {
                    String issuerid = rs.getString("issuerid");
                    String reason = rs.getString("reason");
                    String comment = rs.getString("comment");
                    long time = rs.getLong("time");
                    long endtime = rs.getLong("endtime");
                    sta.close();

                    if (endtime > 0) {
                        TempbanEntry tbe = new TempbanEntry(id, UUID.fromString(issuerid), reason, comment, time, endtime);
                        if(checkExpiration(tbe) != null) {
                            banMap.put(id, tbe);
                            return tbe;
                        }
                    } else {
                        BanEntry be = new BanEntry(id, UUID.fromString(issuerid), reason, comment, time);
                        banMap.put(id, be);
                        return be;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to get ban of player " + plugin.getPlayerName(id) + " from the ban table!");
                e.printStackTrace();
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
            }
        }
        return null;
    }

    /**
     * Get the ban of a player with the given username
     * <br /><br />
     * <strong>Note:</strong> This method might execute a database query and should not be run on the main thread!
     * @param username The username of the player to get the ban of
     * @return The banentry of the player, null if he doesn't have one
     */
    public Entry getBan(String username){
        UUID playerid = plugin.getPlayerId(username);
        return (playerid == null) 
                ? new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player",username)))
                : getBan(playerid);
    };

    /**
     * Check if a ban is still valid or if it is a temporary ban and expired
     * @param entry The (Temp)BanEntry to check
     * @return The entry if it is still valid, null if it isn't
     */
    private Entry checkExpiration(BanEntry entry) {
        if(entry instanceof TempbanEntry && ((TempbanEntry) entry).isExpired()) {
            removeBan(entry);
            plugin.getLogger().info("Temporary ban of " + plugin.getPlayerName(entry.getBanned()) + " expired.");
            return null;
        }            
        return entry;
    }

    /**
     * Add a banentry to the banmap and database!
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param entry The banentry to add
     * @return The added entry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry addBan(BanEntry entry) {
        Entry existingentry = getBan(entry.getBanned());
        if(existingentry != null) {
            if(existingentry.getType() == EntryType.FAILURE)
                return existingentry;
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.alreadybanned", ImmutableMap.of("player", plugin.getPlayerName(entry.getBanned()))));
        }
        
        banMap.put(entry.getBanned(), entry);

        if(plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            String query = "INSERT INTO " + mysql.getTablePrefix() + "bans (bannedid, issuerid, reason, comment, time, endtime) values (?, ?, ?, ?, ?, ?)";

            try {
                PreparedStatement sta = mysql.getConn().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                sta.setString(1, entry.getBanned().toString());
                sta.setString(2, entry.getIssuer().toString());
                sta.setString(3, entry.getReason());
                sta.setString(4, entry.getComment());
                sta.setString(5, Long.toString(entry.getTime()));
                if(entry instanceof TempbanEntry)
                    sta.setLong(6, ((TempbanEntry) entry).getEndtime());
                else
                    sta.setLong(6, 0);
                sta.executeUpdate();
                ResultSet rs = sta.getGeneratedKeys();
                
                if(rs.next()) {
                    entry.setDbId(rs.getInt(1));
                    String msg = (entry.getReason().isEmpty()) ? "" : "Reason: " + entry.getReason();
                    if (entry instanceof TempbanEntry) {
                        msg = "Duration: " + ((TempbanEntry) entry).getFormattedDuration(plugin.getLanguageConfig(), true) + " " + msg;
                    }
                    if (!mysql.log(entry.getType(), entry.getBanned(), entry.getIssuer(), msg))
                        plugin.getLogger().warning("Error while trying to log addition of ban " + rs.getInt(1) + " for player " + plugin.getPlayerName(entry.getBanned()) + "!");
                    return entry;
                }
                sta.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to add ban for player " + plugin.getPlayerName(entry.getBanned()) + " to the ban table!");
                e.printStackTrace();
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
            }
        }
        return entry;
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param banentry The banentry to remove
     * @param invokeid The id of the player who invoked the removal of this ban.
     * @param log If we should log this change to the log table or not
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banentry, UUID invokeid, boolean log) {
        banMap.remove(banentry.getBanned());

        if(plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            String msg = (invokeid.compareTo(UUID.fromString("00000000-0000-0000-0000-000000000000")) == 0) ? "Automatic removal. " : "";
            if(banentry instanceof TempbanEntry) {
                msg += "Orig. endtime: " + ((TempbanEntry) banentry).getEndtime(plugin.getLanguageConfig().getTranslation("time.format"));
            }
            if (log && !mysql.log(EntryType.UNBAN, banentry.getBanned(), invokeid, msg))
                plugin.getLogger().warning("Error while trying to log deletion of ban of player " + plugin.getPlayerName(banentry.getBanned()) + "!");

            try {
                String query = "DELETE FROM " + mysql.getTablePrefix() + "bans WHERE " + ((banentry.getDbId() > 0) ? "id=?" : "bannedid=? ORDER BY time DESC LIMIT 1");
                PreparedStatement sta = mysql.getConn().prepareStatement(query);
                sta.setString(1, (banentry.getDbId() > 0) ? Integer.toString(banentry.getDbId()) : banentry.getBanned().toString());
                sta.executeUpdate();
                sta.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to delete ban of player " + plugin.getPlayerName(banentry.getBanned()) + " (BanID: " + banentry.getDbId() + ") from the ban table!");
                e.printStackTrace();
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
            }
        }
        return banentry;
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param banentry The banentry to remove
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banentry, UUID invokeid) {
        return removeBan(banentry, invokeid, true);
    }
    
    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param banentry The banentry to remove
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banentry) {
        return removeBan(banentry, UUID.fromString("00000000-0000-0000-0000-000000000000"), true);
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param playerid The UUID of the player to unban
     * @param invokeid The id of the player who invoked the removal of this ban.
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(UUID playerid, UUID invokeid) {
        Entry entry = getBan(playerid);
        
        if(entry instanceof BanEntry)
            return removeBan((BanEntry) entry, invokeid);
        return entry;
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param username The name of the player to unban
     * @param invokeid The id of the player who invoked the removal of this ban.
     * @return The previous BanEntry or aa Entry with Type FAILURE and the fail as reason
     */
    public Entry removeBan(String username, UUID invokeid) {
        UUID playerid = plugin.getPlayerId(username);
        if(playerid == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player",username)));
        Entry entry = removeBan(playerid, invokeid);
        if(entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", ImmutableMap.of("player",username)));
        return entry;
    }

    /**
     * Get the number of entries in the log table of a type
     * @param type The EntryType
     * @param playerid The UUID of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, UUID playerid) {
        if(playerid != null && plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            try {
                String query = "SELECT count(id) as count FROM " + mysql.getTablePrefix() + "log WHERE type=? AND playerid=?";
                PreparedStatement sta = mysql.getConn().prepareStatement(query);
                sta.setString(1, type.toString());
                sta.setString(2, playerid.toString());

                ResultSet rs = sta.executeQuery();
                if(rs.next()) {
                    return rs.getInt("count");
                }
                sta.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to to get the count of " + type.toString().toLowerCase() + " entries for player " + plugin.getPlayerName(playerid) + " from the log table!");
                e.printStackTrace();
            }
        }
        return 0;
    }
    
    /**
     * Get the number of entries in the log table of a type
     * @param type The EntryType
     * @param username The name of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, String username) {
        return getCount(type, plugin.getPlayerId(username));
    }
}
