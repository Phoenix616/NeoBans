package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.commands.SenderType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Created by Phoenix616 on 10.02.2015.
 */
public class Sender implements NeoSender {
    /**
     * Command sender
     */
    protected CommandSender sender;

    /**
     * Constructor for the sender
     * @param sender The CommandSender
     */
    public Sender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public SenderType getType() {
        return isPlayer() ? SenderType.PLAYER : SenderType.CONSOLE;
    }

    @Override
    public UUID getUniqueID() {
        return isPlayer() ? ((ProxiedPlayer) sender).getUniqueId() : new UUID(0, 0);
    }

    @Override
    public boolean hasPermission(String perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    @Override
    public void sendMessage(String message, String... replacements) {
        // insert replacements
        if (replacements != null)
            for (int i = 0; i + 1 < replacements.length; i += 2)
                message = message.replaceAll("%" + replacements[i] + "%", replacements[i + 1]);
        
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
}
