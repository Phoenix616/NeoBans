package de.themoep.NeoBans.core.mysql;

import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;

import java.util.List;
import java.util.UUID;

/**
 * Created by Phoenix616 on 08.03.2015.
 */
public interface DatabaseManager {

    /**
     * Initialize the tables needed for this plugin.
     */
    void initializeTables();

    /**
     * Update the tables from a previous version
     */
    void updateTables();

    /**
     * Log something to the database
     * @param type The type of this log entry
     * @param playerId The id of the player this entry is for
     * @param issuerId The id of the player because of which this got logged
     * @param message The message to log
     * @return
     */
    boolean log(EntryType type, UUID playerId, UUID issuerId, String message);
    /**
     * Get log entries by a player
     * @param playerId The UUID of the player to get the entries for
     * @param page The page to get, starts at 0
     * @param amount Amount of entries to get
     * @return A List of Entries
     */
    List<Entry> getLogEntries(UUID playerId, int page, int amount);

    /**
     * Disable the database manager. This closes all connections.
     */
    void disable();

    boolean update(int entryId, String column, Object value);

    Entry add(PunishmentEntry entry);

    Entry remove(PunishmentEntry banentry, UUID invokeId, boolean log);

    int getCount(EntryType type, UUID playerId);

    /**
     * Get the last added entry of the given type
     * @param id The UUID of the player
     * @param types An array of types to get; if empty it will get entries of all types
     * @return The latest entry
     */
    Entry get(UUID id, EntryType... types);
}
