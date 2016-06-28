package de.themoep.NeoBans.core.config;

/*
 * By zaiyers
 * https://github.com/zaiyers/Channels/blob/master/src/main/java/net/zaiyers/Channels/config/Config.java
 */

public interface NeoConfig {
    /**
     * creates a default configuration
     */
    void createDefaultConfig();

    /**
     * save configuration
     */
    void save();

    /**
     * remove configuration
     */
    void removeConfig();

    /**
     * Get a string from the config
     * @param path - Path to the string
     * @return String - The string set in the config
     */
    String getString(String path);
    
    /**
     * Get a string from the config
     * @param path - Path to the string
     * @param def - Default value
     * @return String - The string set in the config
     */
    String getString(String path, String def);
}