package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class KickAllCommand extends AbstractCommand {

    public KickAllCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        List<String> players = new ArrayList<>();
        String serverName = "";
        int reasonStart = 0;
        if(args.length > 0 && (args[0].toLowerCase().startsWith("server:") || args[0].toLowerCase().startsWith("world:"))) {
            serverName = args[0].substring(args[0].indexOf(':') + 1);
            try {
                players.addAll(plugin.getOnlinePlayers(serverName));
            } catch(NoSuchElementException e) {
                sender.sendTranslation("neobans.error.notfound", "value", serverName);
                return;
            }
            reasonStart = 1;
        } else {
            players.addAll(plugin.getOnlinePlayers());
        }
        boolean silent = args.length > 1 && sender.hasPermission("neobans.silent") && ("-silent".equalsIgnoreCase(args[reasonStart]) || "-s".equalsIgnoreCase(args[reasonStart]));
        String reason = Arrays.stream(args).skip(silent ? reasonStart + 1 : reasonStart).collect(Collectors.joining(" "));

        for(String toKick : players) {
            if (plugin.kickPlayer(sender, toKick, "neobans.disconnect.kick" + (reason.isEmpty() ? "" : "withreason"),
                    "player", toKick, "reason", reason, "sender", sender.getName()) == -1) {
                sender.sendMessage("neobans.error.kicknotallowed", "player", toKick);
            }
        }
        String kickBroadcastLang = serverName.isEmpty()
                ? ("neobans.message.kickall" + (reason.isEmpty() ? "" : "withreason"))
                : ("neobans.message.kickallserver" + (reason.isEmpty() ? "" : "withreason"));

        BroadcastDestination bd = serverName.isEmpty()
                ? plugin.getConfig().getBroadcastDestination("kickall")
                : plugin.getConfig().getBroadcastDestination("kickallserver");

        if(silent) {
            bd = BroadcastDestination.SENDER;
        }
        if(bd != BroadcastDestination.SERVER || serverName.isEmpty()) {
            plugin.broadcast(sender, bd, kickBroadcastLang, "sender", sender.getName(), "server", serverName, "reason", reason);
        } else {
            plugin.broadcast(sender, serverName, kickBroadcastLang, "sender", sender.getName(), "server", serverName, "reason", reason);
        }
        
    }

    @Override
    public boolean validateInput() {
        return true;
    }

}
