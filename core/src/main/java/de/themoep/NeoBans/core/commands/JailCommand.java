package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TimedPunishmentEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class JailCommand extends AbstractCommand {

    public JailCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        if (plugin.getConfig().getJailServer().isEmpty()) {
            sender.sendTranslation("neobans.error.nojaildefined", "player", args[0]);
            return;
        }
        plugin.runAsync(() -> {
            String toJail = args[0];
            String duration = args[1];
            boolean silent = args.length > 2 && sender.hasPermission("neobans.silent") && ("-silent".equalsIgnoreCase(args[2]) || "-s".equalsIgnoreCase(args[2]));
            String reason = Arrays.stream(args).skip(silent ? 3 : 2).collect(Collectors.joining(" "));

            if (reason.length() < 140) {
                try {
                    List<UUID> playerIds = new ArrayList<>();
                    for (String nameString : toJail.split(",")) {
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
                        TimedPunishmentEntry jailEntry = new TimedPunishmentEntry(EntryType.JAIL, playerId, sender.getUniqueID(), reason, duration);

                        Entry entry = plugin.getPunishmentManager().addPunishment(jailEntry);
                        if (entry.getType() != EntryType.FAILURE) {
                            plugin.movePlayer(playerId, plugin.getConfig().getJailServer());
                            plugin.runLater(() -> plugin.sendTitle(playerId, reason.isEmpty() ? "neobans.title.jailed" : "neobans.title.jailedwithreason",
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format"))),
                                    100);
                            BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("jail");
                            plugin.broadcast(sender, bd, reason.isEmpty() ? "neobans.message.jail" : "neobans.message.jailwithreason",
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format"))
                            );
                        } else {
                            sender.sendMessage(entry.getReason());
                        }
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("&c" + e.getMessage());
                }
            } else {
                sender.sendTranslation("neobans.error.reasontoolong", "player", toJail, "reason", reason);
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 1;
    }

}
