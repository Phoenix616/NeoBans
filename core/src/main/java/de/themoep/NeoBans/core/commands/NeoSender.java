package de.themoep.NeoBans.core.commands;

import java.util.UUID;

/**
 * Created by Phoenix616 on 10.02.2015.
 */
public interface NeoSender {
    /**
     * Get the name of the command sender 
     * @return The name of the command sender as a String
     */
    String getName();

    /**
     * Get if the command sender is a player or not
     * @return True if sender is player, false if not 
     */
    boolean isPlayer();

    /**
     * Get the type of the command sender
     * @return PLAYER, CONSOLE or CUSTOM
     */
    SenderType getType();


    /**
     * Get the uuid of the command sender 
     * @return The UUID of the sender, 0-UUID (00000000-00...) if sender is not a player and has no custom UUID
     */
    UUID getUniqueID();

    /**
     * Get whether or not the sender has the permission given in the string
     * @param perm The permission to check for
     * @return True if sender has the permission, false if not
     */
    boolean hasPermission(String perm);

    /**
     * Sends a message to a sender
     * @param message The message string to send
     */
    void sendMessage(String message);

    /**
     * Sends a message to a sender and replace placeholder
     * @param message The message string to send
     * @param replacements The replacements in a map from placeholder to replacement
     */
    void sendMessage(String message, String... replacements);
}

