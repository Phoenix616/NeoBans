package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.commands.CommandMap;
import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.config.NeoLanguageConfig;
import de.themoep.NeoBans.core.config.NeoPluginConfig;
import de.themoep.NeoBans.core.mysql.DatabaseManager;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public interface NeoBansPlugin {

    /**
     *Gets the plugin's language config
     */
    public NeoLanguageConfig getLanguageConfig();

    /**
     * Get the plugin's config
     */
    public NeoPluginConfig getConfig();

    /**
     * Gets the plugin's ban manager
     */
    public BanManager getBanManager();
    
    /**
     * Gets the plugin's command map
     */
    public CommandMap getCommandMap();

    /**
     * Get the plugin's database manager
     * @return The DatabaseManager
     */
    public DatabaseManager getDatabaseManager();

    /**
     * Get a list of the names of all online players
     * @return A list of playernames
     */
    public List<String> getOnlinePlayers();
    
    /**
     * Get the UUID of a player from his username <br />
     * If you input a string of an UUID it will return the UUID object!
     * @param username The name of the player to get the uuid of
     * @return The UUID of the player. Null if not found.
     */
    public UUID getPlayerId(String username);

    /**
     * Get the name of a player
     * @param playerid The uuid of the player
     * @return The playername, should be case correct!
     */
    String getPlayerName(UUID playerid);
    
    /**
     * Kick a player with a reason 
     * @param name The name of the player to kick
     * @param reason The reason to display in chat and to the player
     * @return 1 if the player was online, 0 if not, -1 if the sender is not allowed to kick this player
     */
    int kickPlayer(NeoSender sender, String name, String reason);

    /**
     * Kick a player with a reason
     * @param id The uuid of the player to kick
     * @param reason The reason to display in chat and to the player
     * @return 1 if the player was online, 0 if not, -1 if the sender is not allowed to kick this player
     */
    int kickPlayer(NeoSender sender, UUID id, String reason);

    /**
     * Broadcast a message to certain players
     * @param destination The players to broadcast to
     * @param message The message to broadcast
     */
    public void broadcast(NeoSender sender, BroadcastDestination destination, String message);

    /**
     * Run a synchronous task
     * @param runnable The Runnable to run synchronously
     */
    public void runSync(Runnable runnable);

    /**
     * Run an asynchronous task
     * @param runnable The Runnable to run asynchronously
     */
    public void runAsync(Runnable runnable);
    
    /**
     * Get the plugin's logger
     * @return The Logger of the plugin
     */
    public Logger getLogger();

}
