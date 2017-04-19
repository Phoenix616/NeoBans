package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.PunishmentEntry;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class UnjailCommand extends AbstractCommand {

    public UnjailCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(() -> {
            UUID playerId = plugin.getPlayerId(args[0]);
            if (playerId == null) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", args[0]));
                return;
            }

            Entry entry = plugin.getPunishmentManager().getPunishment(playerId);
            if (entry == null || entry.getType().getRemoveType() != EntryType.UNJAIL) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notjailed", "player", args[0]));
                return;
            }

            if (entry instanceof PunishmentEntry) {
                Entry removedEntry = plugin.getPunishmentManager().removePunishment((PunishmentEntry) entry, sender.getUniqueID());
                if (removedEntry != null && removedEntry.getType() == EntryType.FAILURE) {
                    sender.sendMessage(entry.getReason(), "player", args[0]);
                    return;
                }

                boolean silent = (args.length > 1 && args[1].equalsIgnoreCase("-silent"));
                plugin.broadcast(sender,
                        (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("unjail"),
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.unjail",
                                "player", plugin.getPlayerName(((PunishmentEntry) entry).getPunished()),
                                "sender", sender.getName()
                        )
                );
                plugin.movePlayer(playerId, plugin.getConfig().getUnjailTarget());
                plugin.runLater(() -> plugin.sendTitle(playerId, plugin.getLanguageConfig().getTranslation(
                        "neobans.title.unjail",
                        "player", plugin.getPlayerName(((PunishmentEntry) entry).getPunished()),
                        "sender", sender.getName()
                )), 100);
            } else {
                sender.sendMessage(entry.getReason(), "player", args[0]);
            }
        });

    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
