package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class TempbanCommand extends AbstractCommand {

    public TempbanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(new Runnable() {
            @Override
            public void run() {
                String toBan = args[0];
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
                        UUID playerid = plugin.getPlayerId(toBan);
                        if (playerid == null) {
                            sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", toBan));
                            return;
                        }
                        TempbanEntry tbe = new TempbanEntry(playerid, sender.getUniqueID(), reason, duration);

                        String banmsg = reason.isEmpty()
                                ? plugin.getLanguageConfig().getTranslation(
                                        "neobans.disconnect.tempban",
                                        "player", plugin.getPlayerName(playerid),
                                        "sender", sender.getName(),
                                        "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false),
                                        "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                                )
                                : plugin.getLanguageConfig().getTranslation(
                                        "neobans.disconnect.tempbanwithreason",
                                        "player", plugin.getPlayerName(playerid),
                                        "reason", reason,
                                        "sender", sender.getName(),
                                        "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false),
                                        "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                                );
                        String banbc = reason.isEmpty()
                                ? plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.tempban",
                                        "player", plugin.getPlayerName(playerid),
                                        "sender", sender.getName(),
                                        "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false),
                                        "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                                )
                                : plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.tempbanwithreason",
                                        "player", plugin.getPlayerName(playerid),
                                        "reason", reason,
                                        "sender", sender.getName(),
                                        "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false),
                                        "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format"))
                                );

                        Entry entry = plugin.getBanManager().addBan(tbe);
                        if (entry.getType() != EntryType.FAILURE) {
                            plugin.kickPlayer(sender, playerid, banmsg);
                            BroadcastDestination bd = (silent) ? BroadcastDestination.SENDER : plugin.getConfig().getBroadcastDestination("tempban");
                            plugin.broadcast(sender, bd, banbc);
                        } else {
                            sender.sendMessage(entry.getReason());
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("&c" + e.getMessage());
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", "player", toBan, "reason", reason));
                }
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 1;
    }

}
