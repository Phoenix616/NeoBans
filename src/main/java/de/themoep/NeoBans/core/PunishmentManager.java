package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.commands.NeoSender;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class PunishmentManager {

    private NeoBansPlugin plugin;

    private Map<EntryType, Map<UUID, PunishmentEntry>> punishments = new EnumMap<>(EntryType.class);

    public PunishmentManager(NeoBansPlugin plugin) {
        this.plugin = plugin;
        punishments.put(EntryType.BAN, new ConcurrentHashMap<>());
        punishments.put(EntryType.TEMPBAN, new ConcurrentHashMap<>());
        punishments.put(EntryType.JAIL, new ConcurrentHashMap<>());
    }

    /**
     * Get the ban of a player with the given username
     * <br /><br />
     * <strong>Note:</strong> This method might execute a database query and should not be run on the main thread!
     * @param id The id of the player to get the ban of
     * @param types The types of the punishment to get
     * @return The entry of the player, null if he doesn't have one, entry with type FAILURE if an error occurs (SQLException, etc.)
     */
    public Entry getPunishment(UUID id, EntryType... types) {
        if (id == null) {
            return null;
        }

        List<Entry> entries = new ArrayList<>();
        for (EntryType type : types) {
            if (punishments.get(type).containsKey(id)) {
                entries.add(punishments.get(type).get(id));
            }
        }
        Entry entry;
        if (!entries.isEmpty()) {
            entry = entries.get(0);
            for (int i = 1; i < entries.size(); i++) {
                if (entries.get(i).getTime() > entry.getTime()) {
                    entry = entries.get(i);
                }
            }
        } else {
            entry = plugin.getDatabaseManager().get(id, types);
        }

        if (entry instanceof TemporaryPunishmentEntry) {
            if (checkExpiration((PunishmentEntry) entry) != null) {
                punishments.get(entry.getType()).put(id, (PunishmentEntry) entry);
                return entry;
            }
        } else if (entry instanceof PunishmentEntry) {
            punishments.get(entry.getType()).put(id, (PunishmentEntry) entry);
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
     * @param types The types of the punishment to get
     * @return The banentry of the player, null if he doesn't have one
     */
    public Entry getPunishment(String username, EntryType... types) {
        UUID playerid = plugin.getPlayerId(username);
        return (playerid == null)
                ? new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", username))
                : getPunishment(playerid, types);
    }

    /**
     * Check if a ban is still valid or if it is a temporary ban and expired
     * @param entry The (Temp)PunishmentEntry to check
     * @return The entry if it is still valid, null if it isn't
     */
    private Entry checkExpiration(PunishmentEntry entry) {
        if (!(entry instanceof TimedPunishmentEntry) && entry instanceof TemporaryPunishmentEntry && ((TemporaryPunishmentEntry) entry).isExpired()) {
            removePunishment(entry);
            plugin.getLogger().info(entry.getType() + " of " + plugin.getPlayerName(entry.getPunished()) + " expired.");
            return null;
        } else if (!punishments.get(entry.getType()).containsKey(entry.getPunished())) {
            punishments.get(entry.getType()).put(entry.getPunished(), entry);
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
        Entry existingEntry = getPunishment(entry.getPunished(), entry.getType() == EntryType.JAIL ? new EntryType[] {EntryType.JAIL} : new EntryType[]{EntryType.BAN, EntryType.TEMPBAN});
        if (existingEntry != null) {
            if (existingEntry.getType() == EntryType.FAILURE)
                return existingEntry;
            if (entry.getType() == EntryType.JAIL) {
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.alreadyjailed", "player", plugin.getPlayerName(entry.getPunished())));
            } else {
                return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.alreadybanned", "player", plugin.getPlayerName(entry.getPunished())));
            }
        }

        punishments.get(entry.getType()).put(entry.getPunished(), entry);

        return plugin.getDatabaseManager().add(entry);
    }

    /**
     * Update a value of a banentry
     * <br /><br />
     * <strong>Note:</strong> This method will execute a database query and should not be run on the main thread!
     * @param entry     The PunishmentEntry to update
     * @param invokeId  The UUID of the player who updates the ban
     * @param option    The option of the ban to change
     * @param value     The value to change to
     * @param log       Whether or not this update should be logged to the console and the database
     * @return The changed PunishmentEntry, an Entry with the EntryType FAILURE on failure with the reason as the reason
     */
    public Entry updatePunishment(PunishmentEntry entry, UUID invokeId, String option, String value, boolean log) {

        String dbColumn = null;
        Object dbValue = null;
        String oldValue = "-";
        PunishmentEntry changedEntry = null;
        if (option.equalsIgnoreCase("reason")) {
            dbColumn = "reason";
            dbValue = value;
            oldValue = entry.getReason();
            if (entry instanceof TimedPunishmentEntry) {
                changedEntry = new TimedPunishmentEntry(entry.getType(), entry.getPunished(), entry.getIssuer(), value, entry.getComment(), entry.getTime(), ((TimedPunishmentEntry) entry).getDuration());
            } else if (entry instanceof TemporaryPunishmentEntry) {
                changedEntry = new TemporaryPunishmentEntry(entry.getType(), entry.getPunished(), entry.getIssuer(), value, entry.getComment(), entry.getTime(), ((TemporaryPunishmentEntry) entry).getEndtime());
            } else {
                changedEntry = new PunishmentEntry(entry.getType(), entry.getPunished(), entry.getIssuer(), value, entry.getComment(), entry.getTime());
            }
        } else if (option.equalsIgnoreCase("endtime") || option.equalsIgnoreCase("end")) {
            dbColumn = "endtime";
            dbValue = 0;
            if (entry instanceof TimedPunishmentEntry) {
                oldValue = String.valueOf(((TimedPunishmentEntry) entry).getDuration());
            } else if (entry instanceof TemporaryPunishmentEntry) {
                oldValue = String.valueOf(((TemporaryPunishmentEntry) entry).getEndtime());
            }
            if (value.equalsIgnoreCase("never") || value.equalsIgnoreCase("0")) {
                changedEntry = new PunishmentEntry(EntryType.BAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
            } else {
                try {
                    long endTime = Long.parseLong(value);
                    changedEntry = new TemporaryPunishmentEntry(entry.getType() == EntryType.JAIL ? EntryType.JAIL : EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime(), endTime);
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (option.equalsIgnoreCase("duration") || option.equalsIgnoreCase("dur")) {
            dbColumn = "endtime";
            if (entry instanceof TemporaryPunishmentEntry) {
                oldValue = ((TemporaryPunishmentEntry) entry).getFormattedDuration();
            }
            if (value.equalsIgnoreCase("permanent") || value.equalsIgnoreCase("perm")) {
                changedEntry = new PunishmentEntry(EntryType.BAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getComment(), entry.getTime());
                dbValue = 0;
            } else {
                try {
                    if (entry instanceof TimedPunishmentEntry) {
                        if (value.startsWith("~")) {
                            changedEntry = new TimedPunishmentEntry(EntryType.JAIL, entry.getPunished(), entry.getIssuer(), entry.getReason(), value.substring(1));
                        } else {
                            long duration = NeoUtils.getDurationFromString(value) - (NeoUtils.getDurationFromString(entry.getComment()) - ((TimedPunishmentEntry) entry).getDuration());
                            changedEntry = new TimedPunishmentEntry(EntryType.JAIL, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getTime(), duration);
                        }
                        dbValue = ((TimedPunishmentEntry) changedEntry).getDuration();
                    } else {
                        if (value.startsWith("~")) {
                            changedEntry = new TemporaryPunishmentEntry(EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), value.substring(1));
                        } else {
                            changedEntry = new TemporaryPunishmentEntry(EntryType.TEMPBAN, entry.getPunished(), entry.getIssuer(), entry.getReason(), entry.getTime(), value);
                        }
                        dbValue = ((TemporaryPunishmentEntry) changedEntry).getEndtime();
                    }
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
            if (log) {
                String msg = "Updated " + entry.getType() + "'" + option.toLowerCase() + "' from '" + oldValue + "' to '" + value.toLowerCase() + "'";
                plugin.getLogger().log(Level.INFO, invokeId + ": " + msg + " for " + entry.getPunished());
                if (!plugin.getDatabaseManager().log(EntryType.EDIT, entry.getPunished(), invokeId, msg)) {
                    plugin.getLogger().warning("Error while trying to log update of " + entry.getType() + " " + entry.getDbId() + " for player " + plugin.getPlayerName(entry.getPunished()) + "! (" + option + ": " + value + ")");
                }
            }
        } else {
            plugin.getLogger().severe("Encountered SQLException while trying to update punishment for player " + plugin.getPlayerName(entry.getPunished()) + "! (" + option + ": " + value + ")");
            new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.database"));
        }
        punishments.get(entry.getType()).remove(entry.getPunished());
        punishments.get(changedEntry.getType()).put(entry.getPunished(), changedEntry);
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
            return updatePunishment((PunishmentEntry) entry, invokeId, option, value, true);
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
        punishments.get(punishment.getType()).remove(punishment.getPunished());

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

    /**
     * Unjail a player. This sends titles and moves the player to the unjail target
     * @param sender The sender that initiated the unjail. Should be the console for automatic ones!
     * @param playerId The UUID of the player
     * @param silent Whether or not to broadcast this unjail if the target is set to anything else but SENDER
     * @return The old jail entry or an error entry
     */
    public Entry unjail(NeoSender sender, UUID playerId, boolean silent) {
        String playerName = plugin.getPlayerName(playerId);
        Entry entry = getPunishment(playerId, EntryType.JAIL);
        if (entry != null && entry.getType() == EntryType.FAILURE) {
            return entry;
        } else if (entry == null || entry.getType() != EntryType.JAIL) {
            return new Entry(EntryType.FAILURE, plugin.getLanguageConfig().getTranslation("neobans.error.notjailed", "player", playerName));
        }

        Entry removedEntry = removePunishment((PunishmentEntry) entry, sender.getUniqueID());
        if (removedEntry != null && removedEntry.getType() == EntryType.FAILURE) {
            return removedEntry;
        }

        plugin.sendTitle(playerId, plugin.getLanguageConfig().getTranslation("neobans.title.unjailed", "player", playerName, "sender", sender.getName()));
        plugin.broadcast(sender,
                (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("unjail"),
                plugin.getLanguageConfig().getTranslation(
                        "neobans.message.unjail",
                        "player", playerName,
                        "sender", sender.getName()
                )
        );
        plugin.movePlayer(playerId, plugin.getConfig().getUnjailServer());
        plugin.runLater(() -> plugin.sendTitle(playerId, plugin.getLanguageConfig().getTranslation(
                "neobans.title.unjail",
                "player", playerName,
                "sender", sender.getName()
        )), 100);

        return entry;
    }
}
