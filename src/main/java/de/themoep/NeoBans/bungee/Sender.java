package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.commands.SenderType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
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
     * Command sender's id
     */
    private UUID id;

    /**
     * Command sender's name
     */
    protected String name;
    
    /**
     * Type of the sender 
     */
    protected SenderType type;
    
    /**
     * Constructor for the sender
     * @param id The UUID of the sender, if null it gets a 0-UUID (v3 UUID based on CustomSender:[name] is planed)
     * @param name The name of this sender
     */
    public Sender(UUID id, String name) {
        if(id == null)
            id = UUID.fromString("00000000-0000-0000-0000-000000000000");
            //id = UUID.nameUUIDFromBytes(("CustomSender:" + name).getBytes(Charsets.UTF_8));
        
        this.id = id;
        this.sender = NeoBans.getInstance().getProxy().getPlayer(id) != null ? NeoBans.getInstance().getProxy().getPlayer(id) : NeoBans.getInstance().getProxy().getConsole();
        this.name = name;
        this.type = isPlayer() ? SenderType.PLAYER : SenderType.CONSOLE;
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
        return type;
    }

    @Override
    public UUID getUniqueID() {
        return id;
    }

    @Override
    public boolean hasPermission(String perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public void notify(String key) {
        String string = NeoBans.getInstance().getLanguageConfig().getTranslation(key);
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', string)));
    }
    
    @Override
    public void sendMessage(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(String message, String... replacements) {
        // insert replacements
        if (replacements != null)
            for (int i = 0; i + 1 < replacements.length; i += 2)
                message = message.replaceAll("%" + replacements[i] + "%", replacements[i + 2]);
        
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
}
