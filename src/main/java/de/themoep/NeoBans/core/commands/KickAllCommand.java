package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;
import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class KickAllCommand extends AbstractCommand {

    public KickAllCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, ImmutableMap<String, ArrayList<String>> completions) {
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
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notfound", ImmutableMap.of("value", serverName)));
                return;
            }
            reasonStart = 1;
        } else {
            players.addAll(plugin.getOnlinePlayers());
        }
        String reason = "";
        boolean silent = false;
        if(args.length > reasonStart) {
            for (int i = reasonStart; i < args.length; i++) {
                if(i == reasonStart && args[i].equalsIgnoreCase("-silent")) {
                    silent = true;
                } else {
                    reason += args[i] + " ";
                }
            }
        }
        reason = reason.trim();

        for(String toKick : players) {
            String kickmsg = reason.isEmpty()
                    ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.kick", ImmutableMap.of("player", toKick, "sender", sender.getName()))
                    : plugin.getLanguageConfig().getTranslation("neobans.disconnect.kickwithreason", ImmutableMap.of("player", toKick, "reason", reason, "sender", sender.getName()));
            if(plugin.kickPlayer(sender, toKick, kickmsg) == -1) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.kicknotallowed", ImmutableMap.of("player", toKick)));
            }
        }
        String kickbc = serverName.isEmpty()
                ? plugin.getLanguageConfig().getTranslation("neobans.message.kickall", ImmutableMap.of("sender", sender.getName()))
                : plugin.getLanguageConfig().getTranslation("neobans.message.kickallserver", ImmutableMap.of("server", serverName, "sender", sender.getName()));

        if(!reason.isEmpty()) {
            kickbc += plugin.getLanguageConfig().getTranslation("neobans.message.kickallreason", ImmutableMap.of("reason", reason));
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
