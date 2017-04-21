package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.commands.CommandMap;
import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.config.NeoLanguageConfig;
import de.themoep.NeoBans.core.config.NeoPluginConfig;
import de.themoep.NeoBans.core.mysql.DatabaseManager;

import java.util.List;
import java.util.NoSuchElementException;
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
    public PunishmentManager getPunishmentManager();
    
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
     * Get a list of the names of all players online on a specific server (or world if Minecraft plugin)
     * @param serverName The name of the server (or world depending on implementation)
     * @return A list of playernames
     * @throws NoSuchElementException When there is no server/world with that name
     */
    public List<String> getOnlinePlayers(String serverName) throws NoSuchElementException;
    
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
     * Broadcast a message to certain players
     * @param serverName The name of the server (or world on Minecraft) where this message should be sent to
     * @param message The message to broadcast
     */
    public void broadcast(NeoSender sender, String serverName, String message);

    /**
     * Run a synchronous task
     * @param runnable The Runnable to run synchronously
     * @return The task id; -1 if no new task was created
     */
    public int runSync(Runnable runnable);

    /**
     * Run an asynchronous task
     * @param runnable The Runnable to run asynchronously
     * @return The task id; -1 if no new task was created
     */
    public int runAsync(Runnable runnable);

    /**
     * Run a synchronous task later
     * @param runnable The Runnable to run later
     * @param delay The amount of ticks (one tick = 50ms) to wait
     * @return The task id; -1 if no new task was created
     */
    int runLater(Runnable runnable, long delay);

    /**
     * Run a repeating synchronous task
     * @param runnable The Runnable to run repeatingly
     * @param delay The amount of ticks (one tick = 50ms) to wait
     * @param period The amount of ticks (one tick = 50ms) between each run
     * @return The task id; -1 if no new task was created
     */
    int runRepeating(Runnable runnable, long delay, long period);
    
    /**
     * Get the plugin's logger
     * @return The Logger of the plugin
     */
    public Logger getLogger();

    /**
     * Get the name of the plugin
     * @return The name as a string
     */
    String getName();

    /**
     * Load the plugin's config from file
     */
    void loadConfig();

    /**
     * Check the plugin version
     * @param version The version string to compare to
     * @return -1 if the plugin version is below; 0 if the version is equal; 1 if the version is above
     */
    int compareVersion(String version);

    /**
     * Send a title to a player
     * @param playerId The UUID of the player
     * @param message The message to send (use a new line character to split the message)
     * @return true if the player is online; false if the player is offline
     */
    boolean sendTitle(UUID playerId, String message);

    /**
     * Move the player either to another world or server
     * @param playerId The UUID of the player
     * @param target The name of the target world or server
     * @return true if the player is online; false if the player is offline or the target doesn't exist
     */
    boolean movePlayer(UUID playerId, String target);
}
