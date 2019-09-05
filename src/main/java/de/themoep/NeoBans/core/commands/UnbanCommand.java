package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.PunishmentEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
            List<UUID> playerIds = new ArrayList<>();
            for (String nameString : args[0].split(",")) {
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

            boolean silent = args.length > 1 && ("-silent".equalsIgnoreCase(args[1]) || "-s".equalsIgnoreCase(args[1]));
            for (UUID playerId : playerIds) {
                Entry entry = plugin.getPunishmentManager().getPunishment(playerId, EntryType.BAN, EntryType.TEMPBAN);
                if (entry == null) {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.notbanned", "player", plugin.getPlayerName(playerId)));
                    continue;
                }

                if (entry instanceof PunishmentEntry) {
                    String reason = Arrays.stream(args).skip(silent ? 2 : 1).collect(Collectors.joining(" "));
                    Entry removedEntry = plugin.getPunishmentManager().removePunishment((PunishmentEntry) entry, sender.getUniqueID(), reason);
                    if (removedEntry != null && removedEntry.getType() == EntryType.FAILURE) {
                        sender.sendMessage(entry.getReason(), "player", plugin.getPlayerName(playerId));
                        continue;
                    }

                    plugin.broadcast(sender,
                            (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("unban"),
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.unban",
                                    "player", plugin.getPlayerName(((PunishmentEntry) entry).getPunished()),
                                    "sender", sender.getName()
                            )
                    );
                } else {
                    sender.sendMessage(entry.getReason(), "player", plugin.getPlayerName(playerId));
                }
            }
        });

    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
