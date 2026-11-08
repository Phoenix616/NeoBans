package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class TempbanCommand extends AbstractCommand {

    public TempbanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(() -> {
            String toBan = args[0];
            String duration = args[1];
            boolean silent = args.length > 2 && sender.hasPermission("neobans.silent") && ("-silent".equalsIgnoreCase(args[2]) || "-s".equalsIgnoreCase(args[2]));
            String reason = Arrays.stream(args).skip(silent ? 3 : 2).collect(Collectors.joining(" "));

            if (reason.length() < 140) {
                try {
                    List<UUID> playerIds = new ArrayList<>();
                    for (String nameString : toBan.split(",")) {
                        UUID playerId = plugin.getPlayerId(nameString);
                        if (playerId == null) {
                            sender.sendTranslation("neobans.error.uuidnotfound", "player", nameString);
                        } else {
                            playerIds.add(playerId);
                        }
                    }
                    if (playerIds.isEmpty()) {
                        return;
                    }
                    for (UUID playerId : playerIds) {
                        TemporaryPunishmentEntry tbe = new TemporaryPunishmentEntry(EntryType.TEMPBAN, playerId, sender.getUniqueID(), reason, duration);
                        Entry entry = plugin.getPunishmentManager().addPunishment(tbe);
                        if (entry.getType() != EntryType.FAILURE) {
                            plugin.kickPlayer(sender, playerId, "neobans.disconnect.tempban" + (reason.isEmpty() ? "" : "withreason"),
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", tbe.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()),
                                    "endtime", tbe.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")));
                            BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("tempban");
                            plugin.broadcast(sender, bd, "neobans.message.tempban" + (reason.isEmpty() ? "" : "withreason"),
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", tbe.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()),
                                    "endtime", tbe.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")));
                        } else {
                            sender.sendMessage(entry.getReason());
                        }
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("&c" + e.getMessage());
                }
            } else {
                sender.sendTranslation("neobans.error.reasontoolong", "player", toBan, "reason", reason);
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 1;
    }

}
