package de.themoep.NeoBans.core.commands;


import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class KickCommand extends AbstractCommand {
    
    public KickCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        final String toKick = args[0];
        String reason = "";
        boolean silent = false;
        if(args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                if(i == 1 && ("-silent".equalsIgnoreCase(args[i]) || "-s".equalsIgnoreCase(args[i]))) {
                    silent = true;
                } else {
                    reason += args[i] + " ";
                }
            }
        }
        reason = reason.trim();
        
        String kickmsg = (reason.isEmpty()) 
                ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.kick", "player", toKick, "sender", sender.getName())
                : plugin.getLanguageConfig().getTranslation("neobans.disconnect.kickwithreason", "player", toKick, "reason", reason, "sender", sender.getName());
        String kickbc = (reason.isEmpty()) 
                ? plugin.getLanguageConfig().getTranslation("neobans.message.kick", "player", toKick, "sender", sender.getName())
                : plugin.getLanguageConfig().getTranslation("neobans.message.kickwithreason", "player", toKick, "reason", reason, "sender", sender.getName());

        final UUID playerid = plugin.getPlayerId(toKick);
        
        int success = plugin.kickPlayer(sender, toKick, kickmsg);
        if(success == 1) {
            BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("kick");
            plugin.broadcast(sender, bd, kickbc);
            final String finalReason = reason;
            plugin.runAsync(new Runnable() {
                @Override
                public void run() {
                    plugin.getDatabaseManager().log(EntryType.KICK, playerid, sender.getUniqueID(), finalReason);
                }
            });
        } else if(success == -1)
            sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.kicknotallowed", "player", toKick));
        else
            sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notonline", "player", toKick));
        
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
