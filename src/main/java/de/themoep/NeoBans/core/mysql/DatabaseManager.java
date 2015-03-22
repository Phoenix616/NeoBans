package de.themoep.NeoBans.core.mysql;

import de.themoep.NeoBans.core.EntryType;

import java.util.UUID;

/**
 * Created by Phoenix616 on 08.03.2015.
 */
public interface DatabaseManager {

    /**
     * Initialize the tables needed for this plugin.
     */
    public void initializeTables();

    /**
     * Log something to the database
     * @param type The type of this log entry
     * @param playerid The id of the player this entry is for
     * @param issuerid The id of the player because of which this got logged
     * @param message The message to log
     * @return
     */
    public boolean log(EntryType type, UUID playerid, UUID issuerid, String message);

    /**
     * Disable the database manager. This closes all connections.
     */
    void disable();
}
