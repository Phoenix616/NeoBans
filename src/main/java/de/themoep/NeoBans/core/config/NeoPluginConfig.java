package de.themoep.NeoBans.core.config;

import de.themoep.NeoBans.core.BroadcastDestination;

/**
 * Created by Phoenix616 on 27.02.2015.
 */
public interface NeoPluginConfig {

    String getString(String key, String def);

    /**
     * Get language name
     * @return The language string
     */
    String getLanguage();

    /**
     * Get wether or not we should latebind the commands and their aliases
     * @return True if we should wait a second after start to bind the commands
     */
    Boolean getLatebind();

    /**
     * Get a commands alias list
     * @param cmdname The name of the command
     * @return A list with the aliases of a command
     */
    String[] getCommandAliases(String cmdname);

    /**
     * Get the configured destination for a broadcast type (ban/kick/... etc.)
     * param type The broadcast type
     * @return The BroadcastDesination
     */
    BroadcastDestination getBroadcastDestination(String type);

    /**
     * Get the Name of the jail server as configured,  if the name is empty the jail functionality is disabled
     * @return The name of the jail server
     */
    String getJailTarget();

    /**
     * Get the Name of the server where player should be moved to when unjailed
     * @return The name of the unjail server
     */
    String getUnjailTarget();

    /**
     * Get the name of the storage backend
     * @return The name of the storage backend
     */
    String getBackend();
}
