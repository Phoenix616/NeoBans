package de.themoep.NeoBans.core;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class PunishmentManager {

    private NeoBansPlugin plugin;

    protected ConcurrentHashMap<UUID, PunishmentEntry> punishments;

    public PunishmentManager(NeoBansPlugin plugin) {
        this.plugin = plugin;
        punishments = new ConcurrentHashMap<>();
    }

    /**
     * Get the ban of a player with the given username
     * <br /><br />
     * <strong>Note:</strong> This method might execute a database query and should not be run on the main thread!
     * @param id The id of the player to get the ban of
     * @return The entry of the player, null if he doesn't have one, entry with type FAILURE if an error occurs (SQLException, etc.)
     */
    public Entry getPunishment(UUID id) {
        if (id == null) {
            return null;
        }

        Entry entry = punishments.get(id);
        if (entry == null) {
            entry = plugin.getDatabaseManager().get(id);
        }

        if (entry instanceof TimedPunishmentEntry) {
            if (checkExpiration((PunishmentEntry) entry) != null) {
                punishments.put(id, (PunishmentEntry) entry);
                return entry;
            }
        } else if (entry instanceof PunishmentEntry) {
            punishments.put(id, (PunishmentEntry) entry);
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
    public Entry getPunishment(String username) {
        UUID playerid = plugin.getPlayerId(username);
        return (playerid == null)
                ? new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", username))
                : getPunishment(playerid);
    }

    ;

    /**
     * Check if a ban is still valid or if it is a temporary ban and expired
     * @param entry The (Temp)PunishmentEntry to check
     * @return The entry if it is still valid, null if it isn't
     */
    private Entry checkExpiration(PunishmentEntry entry) {
        if (entry instanceof TimedPunishmentEntry && ((TimedPunishmentEntry) entry).isExpired()) {
            removePunishment(entry);
            plugin.getLogger().info("Temporary ban of " + plugin.getPlayerName(entry.getPunished()) + " expired.");
            return null;
        } else if (!punishments.containsKey(entry.getPunished())) {
            punishments.put(entry.getPunished(), entry);
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
    public Entry addPunishment(PunishmentEntry entry) {
        Entry existingEntry = getPunishment(entry.getPunished());
        if (existingEntry != null) {
            if (existingEntry.getType() == EntryType.FAILURE)
                return existingEntry;
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.alreadybanned", "player", plugin.getPlayerName(entry.getPunished())));
        }

        punishments.put(entry.getPunished(), entry);

        return plugin.getDatabaseManager().add(entry);
    }

    /**
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param entry    The PunishmentEntry to update
     * @param invokeId The UUID of the player who updates the ban
     * @param option   The option of the ban to change
     * @param value    The value to change to
     * @return The changed PunishmentEntry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updatePunishment(PunishmentEntry entry, UUID invokeId, String option, String value) {

        String dbColumn = null;
        String dbValue = null;
        String oldValue = "-";
        PunishmentEntry changedEntry = null;
        if (option.equalsIgnoreCase("reason")) {
            dbColumn = "reason";
            dbValue = value;
            oldValue = entry.getReason();
            if (entry instanceof TimedPunishmentEntry) {
                changedEntry = new TimedPunishmentEntry(entry.getType(), entry.getPunished(), entry.getIssuer(), value, entry.getComment(), entry.getTime(), ((TimedPunishmentEntry) entry).getEndtime());
            } else {
                changedEntry = new PunishmentEntry(entry.getType(), entry.getPunished(), entry.getIssuer(), value, entry.getComment(), entry.getTime());
            }
        } else if (option.equalsIgnoreCase("endtime") || option.equalsIgnoreCase("end")) {
            dbColumn = "endtime";
            dbValue = "0";
            if (entry instanceof TimedPunishmentEntry) {
                oldValue = Long.toString(((TimedPunishmentEntry) entry).getEndtime());
            }
            if (value.equalsIgnoreCase("never") || value.equalsIgnoreCase("0")) {
                changedEntry = new PunishmentEntry(EntryType.BAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
            } else {
                try {
                    long endTime = Long.parseLong(value);
                    changedEntry = new TimedPunishmentEntry(entry.getType() != EntryType.BAN ? EntryType.JAIL : EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime(), endTime);
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (option.equalsIgnoreCase("duration") || value.equalsIgnoreCase("dur")) {
            dbColumn = "endtime";
            if (entry instanceof TimedPunishmentEntry) {
                oldValue = ((TimedPunishmentEntry) entry).getFormattedDuration(plugin.getLanguageConfig(), true);
            }
            if (value.equalsIgnoreCase("permanent") || value.equalsIgnoreCase("perm")) {
                changedEntry = new PunishmentEntry(EntryType.BAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
                dbValue = "0";
            } else {
                try {
                    if (value.startsWith("~")) {
                        changedEntry = new TimedPunishmentEntry(entry.getType() != EntryType.BAN ? EntryType.JAIL : EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), value.substring(1));
                    } else {
                        changedEntry = new TimedPunishmentEntry(entry.getType() != EntryType.BAN ? EntryType.JAIL : EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getTime(), value);
                    }
                    dbValue = Long.toString(((TimedPunishmentEntry) changedEntry).getEndtime());
                } catch (NumberFormatException e) {
                    changedEntry = null;
                }
            }
        }

        if (dbColumn == null) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongoption", "option", option.toLowerCase()));
        } else if (dbValue == null || changedEntry == null) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.wrongvalue", "option", option.toLowerCase(), "value", value.toLowerCase()));
        }
        changedEntry.setDbId(entry.getDbId());

        if (plugin.getDatabaseManager().update(changedEntry.getDbId(), dbColumn, dbValue)) {
            String msg = "Updated '" + option.toLowerCase() + "' from '" + oldValue + "' to '" + value.toLowerCase() + "'";
            if (!plugin.getDatabaseManager().log(EntryType.EDITBAN, entry.getPunished(), entry.getIssuer(), msg)) {
                plugin.getLogger().warning("Error while trying to log update of ban " + entry.getDbId() + " for player " + plugin.getPlayerName(entry.getPunished()) + "! (" + option + ": " + value + ")");
            }
        } else {
            plugin.getLogger().severe("Encountered SQLException while trying to update ban for player " + plugin.getPlayerName(entry.getPunished()) + "! (" + option + ": " + value + ")");
            new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        }
        punishments.put(entry.getPunished(), changedEntry);
        return changedEntry;
    }

    /**
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param playerId The UUID to update
     * @param invokeId The UUID of the player who updates the ban
     * @param option   The option of the ban to change
     * @param value    The value to change to
     * @return The changed PunishmentEntry, <tt>null</tt> if player wasn't punished, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updatePunishment(UUID playerId, UUID invokeId, String option, String value) {
        Entry entry = getPunishment(playerId);
        if (entry == null)
            return null;
        if (entry instanceof PunishmentEntry)
            return updatePunishment((PunishmentEntry) entry, invokeId, option, value);
        return new Entry(EntryType.FAILURE, "Error: Found entry but it isn't a ban entry? " + entry.getType() + "/" + entry.getClass().getName());
    }

    /**
     * Update a value of a punishment entry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param username The name of the player to update
     * @param invokeId The UUID of the player who updates the punishment
     * @param option   The option of the punishment to change
     * @param value    The value to change to
     * @return The changed PunishmentEntry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updatePunishment(String username, UUID invokeId, String option, String value) {
        UUID playerId = plugin.getPlayerId(username);
        if (playerId == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", username));
        Entry entry = updatePunishment(playerId, invokeId, option, value);
        if (entry == null)
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", "player", username));
        return entry;
    }

    /**
     * Remove a punishment entry from the punishment map and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param punishment The punishment entry to remove
     * @param invokeId   The id of the player who invoked the removal of this punishment.
     * @param log        If we should log this change to the log table or not
     * @return The previous PunishmentEntry , null if the player wasn't punished before
     */
    public Entry removePunishment(PunishmentEntry punishment, UUID invokeId, boolean log) {
        punishments.remove(punishment.getPunished());

        return plugin.getDatabaseManager().remove(punishment, invokeId, log);
    }

    /**
     * Remove a punishment entry from the punishment map and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param punishmentEntry The punishment entry to remove
     * @return The previous PunishmentEntry , null if the player wasn't punished before
     */
    public Entry removePunishment(PunishmentEntry punishmentEntry, UUID invokeId) {
        return removePunishment(punishmentEntry, invokeId, true);
    }

    /**
     * Remove a punishment entry from the punishment map and database
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param punishmentEntry The punishment entry to remove
     * @return The previous PunishmentEntry , null if the player wasn't punished before
     */
    public Entry removePunishment(PunishmentEntry punishmentEntry) {
        return removePunishment(punishmentEntry, new UUID(0, 0), true);
    }

    /**
     * Get the number of entries in the log table of a type
     * @param type     The EntryType
     * @param playerId The UUID of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, UUID playerId) {
        if (playerId != null) {
            return plugin.getDatabaseManager().getCount(type, playerId);
        }
        return 0;
    }

    /**
     * Get the number of entries in the log table of a type
     * @param type     The EntryType
     * @param username The name of the player
     * @return The count of entries in the log table
     */
    public int getCount(EntryType type, String username) {
        return getCount(type, plugin.getPlayerId(username));
    }
}
