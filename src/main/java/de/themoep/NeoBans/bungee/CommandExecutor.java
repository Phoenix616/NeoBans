package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.commands.NeoCommand;

import de.themoep.bungeeplugin.PluginCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;

/**
 * Created by Phoenix616 on 09.02.2015.
 * Based on zaiyers ChannelsCommandExecutor:
 * https://github.com/zaiyers/Channels/blob/master/src/main/java/net/zaiyers/Channels/name/ChannelsCommandExecutor.java
 */
public class CommandExecutor extends PluginCommand implements Listener {

    private final NeoBans plugin;

    public CommandExecutor(NeoBans plugin, String name) {
        super(plugin, name.toLowerCase());
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public boolean run(CommandSender commandSender, String[] args) {
        Sender sender = new Sender(commandSender);

        NeoCommand cmd = plugin.getCommandMap().get(this.getName(), sender, args);

        // execute command
        if (sender.hasPermission(this.getPermission())) {
            if (cmd.validateInput()) {
                cmd.execute();
            } else {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.usage", "usage", getUsage().replace("<command>", getName())));
            }
        } else {
            sender.sendMessage(plugin.getLanguageConfig().getTranslation(
                    "neobans.error.nopermission",
                    "permission", this.getPermission()
            ));
        }
        return true;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String cmd = event.getCursor().replaceFirst("/", "").split(" ")[0].toLowerCase();

        String[] args = new String[]{};
        int index = event.getCursor().indexOf(" ");
        if (index >= 0)
            args = event.getCursor().substring(index).trim().split(" ");
        // For whatever reason the array still has an argument even if it is empty?
        if (args.length == 1 && args[0].isEmpty())
            args = new String[]{};

        if (cmd.equals(this.getName()) || Arrays.asList(getAliases()).contains(cmd)) {
            Sender sender = new Sender((CommandSender) event.getSender());
            event.getSuggestions().clear();
            event.getSuggestions().addAll(plugin.getCommandMap().get(this.getName(), sender, args).getTabSuggestions(event.getCursor()));
        } else if (args.length == 0)
            if (this.getName().startsWith(cmd))
                event.getSuggestions().add("/" + this.getName());
            else
                for (String a : getAliases())
                    if (a.startsWith(cmd))
                        event.getSuggestions().add("/" + a);
    }
}
