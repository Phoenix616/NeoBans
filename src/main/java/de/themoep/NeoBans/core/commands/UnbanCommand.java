package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class UnbanCommand extends AbstractCommand {
    
    public UnbanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
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

            Entry entry = plugin.getPunishmentManager().getPunishment(playerId, EntryType.BAN, EntryType.TEMPBAN);
            if (entry == null) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", "player", args[0]));
                return;
            }

            if (entry instanceof PunishmentEntry) {
                Entry removedEntry = plugin.getPunishmentManager().removePunishment((PunishmentEntry) entry, sender.getUniqueID());
                if (removedEntry != null && removedEntry.getType() == EntryType.FAILURE) {
                    sender.sendMessage(entry.getReason(), "player", args[0]);
                    return;
                }

                boolean silent = (args.length > 1 && (args[1].equalsIgnoreCase("-silent") || args[1].equalsIgnoreCase("-s")));
                plugin.broadcast(sender,
                        (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("unban"),
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.unban",
                                "player", plugin.getPlayerName(((PunishmentEntry) entry).getPunished()),
                                "sender", sender.getName()
                        )
                );
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
