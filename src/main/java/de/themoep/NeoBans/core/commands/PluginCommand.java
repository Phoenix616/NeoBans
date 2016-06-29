package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.NeoBansPlugin;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Map;


/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class PluginCommand extends AbstractCommand {

    public PluginCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        if("reload".equals(args[0])) {
            plugin.loadConfig();
            sender.sendMessage(ChatColor.GREEN + plugin.getName() + " config reloaded!");
        }
    }

    @Override
    public boolean validateInput() {
        return args.length > 0 && "reload".equalsIgnoreCase(args[0]);
    }

}
