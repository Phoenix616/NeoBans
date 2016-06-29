package de.themoep.NeoBans.core.mysql;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.LogEntry;

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
}
