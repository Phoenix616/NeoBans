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
        List<String> players = new ArrayList<String>();
        String serverName = "";
        int reasonStart = 0;
        if(args.length > 0 && (args[0].toLowerCase().startsWith("server:") || args[0].toLowerCase().startsWith("world:"))) {
            serverName = args[0].substring(args[0].indexOf(':') + 1);
            try {
                players.addAll(plugin.getOnlinePlayers(serverName));
            } catch(NoSuchElementException e) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notfound", "value", serverName));
                return;
            }
            reasonStart = 1;
        } else {
            players.addAll(plugin.getOnlinePlayers());
        }
        boolean silent = args.length > 1 && ("-silent".equalsIgnoreCase(args[reasonStart]) || "-s".equalsIgnoreCase(args[reasonStart]));
        String reason = Arrays.stream(args).skip(silent ? reasonStart + 1 : reasonStart).collect(Collectors.joining(" "));

        for(String toKick : players) {
            String kickmsg = reason.isEmpty()
                    ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.kick", "player", toKick, "sender", sender.getName())
                    : plugin.getLanguageConfig().getTranslation("neobans.disconnect.kickwithreason", "player", toKick, "reason", reason, "sender", sender.getName());
            if(plugin.kickPlayer(sender, toKick, kickmsg) == -1) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.kicknotallowed", "player", toKick));
            }
        }
        String kickbc = serverName.isEmpty()
                ? plugin.getLanguageConfig().getTranslation("neobans.message.kickall", "sender", sender.getName())
                : plugin.getLanguageConfig().getTranslation("neobans.message.kickallserver", "server", serverName, "sender", sender.getName());

        if(!reason.isEmpty()) {
            kickbc += plugin.getLanguageConfig().getTranslation("neobans.message.kickallreason", "reason", reason);
        }

        BroadcastDestination bd = serverName.isEmpty()
                ? plugin.getConfig().getBroadcastDestination("kickall")
                : plugin.getConfig().getBroadcastDestination("kickallserver");

        if(silent) {
            bd = BroadcastDestination.SENDER;
        }
        if(bd != BroadcastDestination.SERVER || serverName.isEmpty()) {
            plugin.broadcast(sender, bd, kickbc);
        } else {
            plugin.broadcast(sender, serverName, kickbc);
        }
        
    }

    @Override
    public boolean validateInput() {
        return true;
    }

}
