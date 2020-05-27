package de.themoep.NeoBans.core.commands;


import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class KickCommand extends AbstractCommand {
    
    public KickCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        boolean silent = args.length > 1 && sender.hasPermission("neobans.silent") && ("-silent".equalsIgnoreCase(args[1]) || "-s".equalsIgnoreCase(args[1]));
        String reason = Arrays.stream(args).skip(silent ? 2 : 1).collect(Collectors.joining(" "));

        for (String toKick : args[0].split(",")) {
            String kickmsg = (reason.isEmpty())
                    ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.kick", "player", toKick, "sender", sender.getName())
                    : plugin.getLanguageConfig().getTranslation("neobans.disconnect.kickwithreason", "player", toKick, "reason", reason, "sender", sender.getName());
            String kickbc = (reason.isEmpty())
                    ? plugin.getLanguageConfig().getTranslation("neobans.message.kick", "player", toKick, "sender", sender.getName())
                    : plugin.getLanguageConfig().getTranslation("neobans.message.kickwithreason", "player", toKick, "reason", reason, "sender", sender.getName());

            int success = plugin.kickPlayer(sender, toKick, kickmsg);
            if(success == 1) {
                BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("kick");
                plugin.broadcast(sender, bd, kickbc);
                final String finalReason = reason;
                plugin.runAsync(() -> plugin.getDatabaseManager().log(EntryType.KICK, plugin.getPlayerId(toKick), sender.getUniqueID(), "Reason: " + finalReason));
            } else if(success == -1)
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.kicknotallowed", "player", toKick));
            else
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notonline", "player", toKick));

        }
        
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
