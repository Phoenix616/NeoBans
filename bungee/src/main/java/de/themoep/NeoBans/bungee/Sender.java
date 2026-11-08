package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.commands.SenderType;
import de.themoep.minedown.MineDown;
import de.themoep.utils.lang.LanguageConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

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
     * The language config used by this sender
     */
    private LanguageConfig<?> languageConfig;

    /**
     * Constructor for the sender
     * @param sender The CommandSender
     */
    public Sender(CommandSender sender, LanguageConfig<Configuration> languageConfig) {
        this.sender = sender;
	    this.languageConfig = languageConfig;
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
    public LanguageConfig<?> getLanguageConfig() {
        return languageConfig;
    }

    @Override
    public boolean hasPermission(String perm) {
        return sender.hasPermission(perm);
    }

    public void sendMessage(BaseComponent[] components) {
        sender.sendMessage(components);
    }

    @Override
    public void sendMessage(String message, String... replacements) {
        sendMessage(MineDown.parse(message, replacements));
    }

    @Override
    public void sendTranslation(String langKey, String... replacements) {
        sendMessage(MineDown.parse(languageConfig.get(langKey), replacements));
    }
}
