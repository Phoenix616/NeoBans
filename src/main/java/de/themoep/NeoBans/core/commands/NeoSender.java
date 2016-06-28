package de.themoep.NeoBans.core.commands;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 10.02.2015.
 */
public interface NeoSender {
    /**
     * Get the name of the command sender 
     * @return The name of the command sender as a String
     */
    public String getName();

    /**
     * Get if the command sender is a player or not
     * @return True if sender is player, false if not 
     */
    public boolean isPlayer();

    /**
     * Get the type of the command sender
     * @return PLAYER, CONSOLE or CUSTOM
     */
    public SenderType getType();


    /**
     * Get the uuid of the command sender 
     * @return The UUID of the sender, 0-UUID (00000000-00...) if sender is not a player and has no custom UUID
     */
    public UUID getUniqueID();

    /**
     * Get whether or not the sender has the permission given in the string
     * @param perm The permission to check for
     * @return True if sender has the permission, false if not
     */
    public boolean hasPermission(String perm);

    /**
     * Sends a system notification to a sender
     * @param key The message's key from the language config
     */
    public void notify(String key);

    /**
     * Sends a message to a sender
     * @param message The message string to send
     */
    public void sendMessage(String message);

    /**
     * Sends a message to a sender and replace placeholder
     * @param message The message string to send
     * @param replacements The replacements in a map from placeholder to replacement
     */
    void sendMessage(String message, String... replacements);
}

