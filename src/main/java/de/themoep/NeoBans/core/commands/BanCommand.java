package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.*;

import java.util.ArrayList;
import java.util.List;
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
                List<UUID> playerIds = new ArrayList<>();
                for (String nameString : toBan.split(",")) {
                    UUID playerId = plugin.getPlayerId(nameString);
                    if (playerId == null) {
                        sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", nameString));
                    } else {
                        playerIds.add(playerId);
                    }
                }
                if (playerIds.isEmpty()) {
                    return;
                }
                for (UUID playerId : playerIds) {
                    PunishmentEntry be = new PunishmentEntry(EntryType.BAN, playerId, sender.getUniqueID(), reason);

                    String banmsg = (reason.isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.ban", "player", plugin.getPlayerName(playerId), "sender", sender.getName())
                            : plugin.getLanguageConfig().getTranslation("neobans.disconnect.banwithreason", "player", plugin.getPlayerName(playerId), "reason", reason, "sender", sender.getName());
                    String banbc = (reason.isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.message.ban", "player", plugin.getPlayerName(playerId), "sender", sender.getName())
                            : plugin.getLanguageConfig().getTranslation("neobans.message.banwithreason", "player", plugin.getPlayerName(playerId), "reason", reason, "sender", sender.getName());

                    Entry entry = plugin.getPunishmentManager().addPunishment(be);
                    if (entry.getType() != EntryType.FAILURE) {
                        plugin.kickPlayer(sender, playerId, banmsg);
                        BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("ban");
                        plugin.broadcast(sender, bd, banbc);
                    } else {
                        sender.sendMessage(entry.getReason());
                    }
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
