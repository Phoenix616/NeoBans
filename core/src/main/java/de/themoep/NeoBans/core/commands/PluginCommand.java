package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.NeoBansPlugin;

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
        if("reload".equalsIgnoreCase(args[0])) {
            plugin.loadConfig();
            sender.sendMessage(plugin.getName() + " config reloaded!");
        } else  if ("version".equalsIgnoreCase(args[0])) {
            sender.sendMessage(plugin.getName() + " " + plugin.getVersion() + "");
        }
    }

    @Override
    public boolean validateInput() {
        return args.length > 0 && "|reload|version|".contains("|" + args[0].toLowerCase() + "|");
    }

}
