package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TemporaryPunishmentEntry;
import de.themoep.NeoBans.core.TimedPunishmentEntry;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

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
            sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.nojaildefined", "player", args[0]));
            return;
        }
        plugin.runAsync(() -> {
            String toJail = args[0];
            String duration = args[1];
            String reason = "";
            boolean silent = false;
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    if (i == 2 && args[i].equalsIgnoreCase("-silent")) {
                        silent = true;
                    } else {
                        reason += args[i] + " ";
                    }
                }
            }
            reason = reason.trim();

            if (reason.length() < 140) {
                try {
                    UUID playerId = plugin.getPlayerId(toJail);
                    if (playerId == null) {
                        sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", toJail));
                        return;
                    }
                    TimedPunishmentEntry jailEntry = new TimedPunishmentEntry(EntryType.JAIL, playerId, sender.getUniqueID(), reason, duration);

                    String jailMsg = reason.isEmpty()
                            ? plugin.getLanguageConfig().getTranslation(
                                    "neobans.title.tempban",
                                    "player", plugin.getPlayerName(playerId),
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                            )
                            : plugin.getLanguageConfig().getTranslation(
                                    "neobans.title.jailwithreason",
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                            );
                    String jailBc = reason.isEmpty()
                            ? plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.jail",
                                    "player", plugin.getPlayerName(playerId),
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                            )
                            : plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.jailwithreason",
                                    "player", plugin.getPlayerName(playerId),
                                    "reason", reason,
                                    "sender", sender.getName(),
                                    "duration", jailEntry.getFormattedDuration(plugin.getLanguageConfig()),
                                    "endtime", jailEntry.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                            );

                    Entry entry = plugin.getPunishmentManager().addPunishment(jailEntry);
                    if (entry.getType() != EntryType.FAILURE) {
                        plugin.movePlayer(playerId, plugin.getConfig().getJailServer());
                        plugin.runLater(() -> plugin.sendTitle(playerId, jailMsg), 100);
                        BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("jail");
                        plugin.broadcast(sender, bd, jailBc);
                    } else {
                        sender.sendMessage(entry.getReason());
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("&c" + e.getMessage());
                }
            } else {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", "player", toJail, "reason", reason));
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 1;
    }

}
