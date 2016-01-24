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
                    String msg = entry.getReason().isEmpty() ? "" : "Reason: " + entry.getReason();
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
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     *
     * @param entry The BanEntry to update
     * @param invokeId The UUID of the player who updates the ban
     * @param option The option of the ban to change
     * @param value The value to change to
     * @return The changed BanEntry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updateBan(BanEntry entry, UUID invokeId, String option, String value) {

        String dbColumn = null;
        String dbValue = null;
        String oldValue = "-";
        BanEntry changedEntry = null;
        if(option.equalsIgnoreCase("reason")) {
            dbColumn = "reason";
            dbValue = value;
            oldValue = entry.getReason();
            if(entry instanceof TempbanEntry) {
                changedEntry = new TempbanEntry(entry.getBanned(), entry.getIssuer(), value, entry.getComment(), entry.getTime(), ((TempbanEntry) entry).getEndtime());
            } else {
                changedEntry = new BanEntry(entry.getBanned(), entry.getIssuer(), value, entry.getComment(), entry.getTime());
            }
        } else if(option.equalsIgnoreCase("endtime") || option.equalsIgnoreCase("end")) {
            dbColumn = "endtime";
            dbValue = "0";
            if(entry instanceof TempbanEntry) {
                oldValue = Long.toString(((TempbanEntry) entry).getEndtime());
            }
            if(value.equalsIgnoreCase("never") || value.equalsIgnoreCase("0")) {
                changedEntry = new BanEntry(entry.getBanned(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
            } else {
                try {
                    long endTime = Long.parseLong(value);
                    changedEntry = new TempbanEntry(entry.getBanned(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime(), endTime);
                } catch (NumberFormatException ignored) {}
            }
        } else if(option.equalsIgnoreCase("duration") || value.equalsIgnoreCase("dur")) {
            dbColumn = "endtime";
            if(entry instanceof TempbanEntry) {
                oldValue = ((TempbanEntry) entry).getFormattedDuration(plugin.getLanguageConfig(), true);
            }
            if(value.equalsIgnoreCase("permanent") || value.equalsIgnoreCase("perm")) {
                changedEntry = new BanEntry(entry.getBanned(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
                dbValue = "0";
            } else {
                try {
                    if(value.startsWith("~")) {
                        changedEntry = new TempbanEntry(entry.getBanned(), entry.getIssuer(), entry.getReason(), value.substring(1));
                    } else {
                        changedEntry = new TempbanEntry(entry.getBanned(), entry.getIssuer(), entry.getReason(), entry.getTime(), value);
                    }
                    dbValue = Long.toString(((TempbanEntry) changedEntry).getEndtime());
                } catch(NumberFormatException e) {
                    changedEntry = null;
                }
            }
        }

        if(dbColumn == null) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongoption", ImmutableMap.of("option",option.toLowerCase())));
        } else if(dbValue == null || changedEntry == null) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongvalue", ImmutableMap.of("option",option.toLowerCase(),"value",value.toLowerCase())));
        }
        changedEntry.setDbId(entry.getDbId());

        if(plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            String query = "UPDATE " + mysql.getTablePrefix() + "bans SET " + dbColumn + "=?, type=? WHERE id=?";

            try {
                PreparedStatement sta = mysql.getConn().prepareStatement(query);
                sta.setString(1, dbValue);
                sta.setString(2, changedEntry.getType().toString());
                sta.setInt(3, changedEntry.getDbId());
                sta.executeUpdate();
                sta.close();
                String msg = "Updated '" + option.toLowerCase() + "' from '" + oldValue + "' to '" + value.toLowerCase() + "'";
                if(!mysql.log(EntryType.EDITBAN, entry.getBanned(), entry.getIssuer(), msg))
                    plugin.getLogger().warning("Error while trying to log update of ban " + entry.getDbId() + " for player " + plugin.getPlayerName(entry.getBanned()) + "! (" + option + ": " + value + ")");
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to update ban for player " + plugin.getPlayerName(entry.getBanned()) + "! (" + option + ": " + value + ")");
                e.printStackTrace();
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
            }
        }
        banMap.put(entry.getBanned(), changedEntry);
        return changedEntry;
    }

    /**
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     *
     * @param playerId The UUID to update
     * @param invokeId The UUID of the player who updates the ban
     * @param option The option of the ban to change
     * @param value The value to change to
     * @return The changed BanEntry, <tt>null</tt> if player wasn't banned, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updateBan(UUID playerId, UUID invokeId, String option, String value) {
        Entry entry = getBan(playerId);
        if(entry == null)
            return null;
        if(entry instanceof BanEntry)
            return updateBan((BanEntry) entry, invokeId, option, value);
        return new Entry(EntryType.FAILURE, "Error: Found entry but it isn't a ban entry? " + entry.getType() + "/" + entry.getClass().getName());
    }

    /**
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     *
     * @param username The name of the player to update
     * @param invokeId The UUID of the player who updates the ban
     * @param option The option of the ban to change
     * @param value The value to change to
     * @return The changed BanEntry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updateBan(String username, UUID invokeId, String option, String value) {
        UUID playerId = plugin.getPlayerId(username);
        if(playerId == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player",username)));
        Entry entry = updateBan(playerId, invokeId, option, value);
        if(entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", ImmutableMap.of("player",username)));
        return entry;
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param banentry The banentry to remove
     * @param invokeId The id of the player who invoked the removal of this ban.
     * @param log If we should log this change to the log table or not
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banentry, UUID invokeId, boolean log) {
        banMap.remove(banentry.getBanned());

        if(plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            String msg = (invokeId.compareTo(UUID.fromString("00000000-0000-0000-0000-000000000000")) == 0) ? "Automatic removal. " : "Orig. reason: " + banentry.getReason();
            if(banentry instanceof TempbanEntry) {
                msg += "Orig. endtime: " + ((TempbanEntry) banentry).getEndtime(plugin.getLanguageConfig().getTranslation("time.format"));
            }
            if (log && !mysql.log(EntryType.UNBAN, banentry.getBanned(), invokeId, msg))
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
     * @param banEntry The banentry to remove
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banEntry, UUID invokeId) {
        return removeBan(banEntry, invokeId, true);
    }
    
    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param banEntry The banentry to remove
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(BanEntry banEntry) {
        return removeBan(banEntry, UUID.fromString("00000000-0000-0000-0000-000000000000"), true);
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param playerId The UUID of the player to unban
     * @param invokeId The id of the player who invoked the removal of this ban.
     * @return The previous BanEntry , null if the player wasn't banned before
     */
    public Entry removeBan(UUID playerId, UUID invokeId) {
        Entry entry = getBan(playerId);

        if(entry instanceof BanEntry)
            return removeBan((BanEntry) entry, invokeId);
        return entry;
    }

    /**
     * Remove a banentry from the banmap and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param username The name of the player to unban
     * @param invokeId The id of the player who invoked the removal of this ban.
     * @return The previous BanEntry or aa Entry with Type FAILURE and the fail as reason
     */
    public Entry removeBan(String username, UUID invokeId) {
        UUID playerid = plugin.getPlayerId(username);
        if(playerid == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player",username)));
        Entry entry = removeBan(playerid, invokeId);
        if(entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", ImmutableMap.of("player",username)));
        return entry;
    }

    /**
     * Get the number of entries in the log table of a type
     * @param type The EntryType
     * @param playerId The UUID of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, UUID playerId) {
        if(playerId != null && plugin.getDatabaseManager() instanceof MysqlManager) {
            MysqlManager mysql = ((MysqlManager) plugin.getDatabaseManager());

            try {
                String query = "SELECT count(id) as count FROM " + mysql.getTablePrefix() + "log WHERE type=? AND playerid=?";
                PreparedStatement sta = mysql.getConn().prepareStatement(query);
                sta.setString(1, type.toString());
                sta.setString(2, playerId.toString());

                ResultSet rs = sta.executeQuery();
                if(rs.next()) {
                    return rs.getInt("count");
                }
                sta.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Encountered SQLException while trying to to get the count of " + type.toString().toLowerCase() + " entries for player " + plugin.getPlayerName(playerId) + " from the log table!");
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
