package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class BanCommand extends AbstractCommand {
    
    public BanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(() -> {
            String toBan = args[0];
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

            if(reason.length() < 140) {
                UUID playerid = plugin.getPlayerId(toBan);
                if(playerid == null) {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", toBan));
                    return;
                }
                PunishmentEntry be = new PunishmentEntry(EntryType.BAN, playerid, sender.getUniqueID(), reason);

                String banmsg = (reason.isEmpty())
                        ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.ban", "player", plugin.getPlayerName(playerid), "sender", sender.getName())
                        : plugin.getLanguageConfig().getTranslation("neobans.disconnect.banwithreason", "player", plugin.getPlayerName(playerid), "reason", reason, "sender", sender.getName());
                String banbc = (reason.isEmpty())
                        ? plugin.getLanguageConfig().getTranslation("neobans.message.ban", "player", plugin.getPlayerName(playerid), "sender", sender.getName())
                        : plugin.getLanguageConfig().getTranslation("neobans.message.banwithreason", "player", plugin.getPlayerName(playerid), "reason", reason, "sender", sender.getName());

                Entry entry = plugin.getPunishmentManager().addPunishment(be);
                if (entry.getType() != EntryType.FAILURE) {
                    plugin.kickPlayer(sender, playerid, banmsg);
                    BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("ban");
                    plugin.broadcast(sender, bd, banbc);
                } else {
                    sender.sendMessage(entry.getReason());
                }
            } else {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", "player", toBan, "reason", reason));
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }
}
