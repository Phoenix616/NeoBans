package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.bungee.Sender;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Phoenix616 on 16.03.2015.
 */
public class CommandMap {

    NeoBansPlugin plugin;
    
    public CommandMap(NeoBansPlugin plugin) {
        this.plugin = plugin;        
    }

    /**
     * Get a command object
     * @param command The name of the command to get
     * @param sender The sender who wants to execute the command
     * @param args The arguments of the command
     * @return A command object you can execute or tabcomplete
     */
    public NeoCommand get(String command, Sender sender, String[] args) {
        if (command.equals("neoban")) {
            return new BanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("%online%"))));
        } else if (command.equals("neounban")) {
            return new UnbanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("%online%"))));
        } else if (command.equals("neotempban")) {
            return new TempbanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("%online%"))));
        } else if (command.equals("neokick")) {
            return new KickCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("%online%"))));
        } else if (command.equals("neoinfo")) {
            return new InfoCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("%online%"))));
        } else if (command.equals("neoeditban")) {
            return new EditBanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>(Arrays.asList("duration", "reason"))));
        } else {
            return new HelpCommand(plugin, sender, args);
        }
    }
}
