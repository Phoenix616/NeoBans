package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    public NeoCommand get(String command, NeoSender sender, String[] args) {
        if ("neoban".equals(command)) {
            return new BanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else if ("neounban".equals(command)) {
            return new UnbanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else if ("neotempban".equals(command)) {
            return new TempbanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else if ("neokick".equals(command)) {
            return new KickCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else if ("neokickall".equals(command)) {
            return new KickAllCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>()));
        } else if ("neoinfo".equals(command)) {
            return new InfoCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else if ("neoeditban".equals(command)) {
            return new EditBanCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Arrays.asList("duration", "reason"))));
        } else if ("neolog".equals(command)) {
            return new LogCommand(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<>(Collections.singletonList("%online%"))));
        } else {
            return new HelpCommand(plugin, sender, args);
        }
    }
}
