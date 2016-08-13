package de.themoep.NeoBans.core;

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
        banMap = new ConcurrentHashMap<>();
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

        Entry entry = banMap.get(id);
        if(entry == null) {
            entry = plugin.getDatabaseManager().get(id);
        }

        if (entry instanceof TempbanEntry) {
            if(checkExpiration((BanEntry) entry) != null) {
                banMap.put(id, (BanEntry) entry);
                return entry;
            }
        } else if (entry instanceof BanEntry){
            banMap.put(id, (BanEntry) entry);
            return entry;
        } else {
            return entry;
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
                ? new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player",username))
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
        } else if (!banMap.containsKey(entry.getBanned())){
            banMap.put(entry.getBanned(), entry);
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
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.alreadybanned", "player", plugin.getPlayerName(entry.getBanned())));
        }
        
        banMap.put(entry.getBanned(), entry);

        return plugin.getDatabaseManager().add(entry);
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
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongoption", "option",option.toLowerCase()));
        } else if(dbValue == null || changedEntry == null) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongvalue", "option",option.toLowerCase(),"value",value.toLowerCase()));
        }
        changedEntry.setDbId(entry.getDbId());

        if(plugin.getDatabaseManager().update(changedEntry.getDbId(), dbColumn, dbValue)) {
            String msg = "Updated '" + option.toLowerCase() + "' from '" + oldValue + "' to '" + value.toLowerCase() + "'";
            if (!plugin.getDatabaseManager().log(EntryType.EDITBAN, entry.getBanned(), entry.getIssuer(), msg)) {
                plugin.getLogger().warning("Error while trying to log update of ban " + entry.getDbId() + " for player " + plugin.getPlayerName(entry.getBanned()) + "! (" + option + ": " + value + ")");
            }
        } else {
            plugin.getLogger().severe("Encountered SQLException while trying to update ban for player " + plugin.getPlayerName(entry.getBanned()) + "! (" + option + ": " + value + ")");
            new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
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
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player",username));
        Entry entry = updateBan(playerId, invokeId, option, value);
        if(entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", "player",username));
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

        return plugin.getDatabaseManager().remove(banentry, invokeId, log);
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
        return removeBan(banEntry, new UUID(0, 0), true);
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
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player",username));
        Entry entry = removeBan(playerid, invokeId);
        if(entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", "player",username));
        return entry;
    }

    /**
     * Get the number of entries in the log table of a type
     * @param type The EntryType
     * @param playerId The UUID of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, UUID playerId) {
        if(playerId != null) {
            return plugin.getDatabaseManager().getCount(type, playerId);
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
