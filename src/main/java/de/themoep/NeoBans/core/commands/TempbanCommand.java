package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TempbanEntry;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class TempbanCommand extends AbstractCommand {

    public TempbanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, ImmutableMap<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runSync(new Runnable() {
            @Override
            public void run() {
                String toBan = args[0];
                String duration = args[1];
                String reason = "";
                if (args.length > 2)
                    for (int i = 2; i < args.length; i++)
                        reason += args[i] + " ";
                reason = reason.trim();

                if (reason.length() < 140) {
                    try {
                        UUID playerid = plugin.getPlayerId(toBan);
                        if (playerid == null) {
                            sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player", toBan)));
                            return;
                        }
                        TempbanEntry tbe = new TempbanEntry(playerid, sender.getUniqueID(), reason, duration);

                        String banmsg = (reason.isEmpty())
                                ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.tempban", ImmutableMap.of("player", toBan, "sender", sender.getName(), "time", tbe.getFormattedDuration(plugin.getLanguageConfig(), false)))
                                : plugin.getLanguageConfig().getTranslation("neobans.disconnect.tempbanwithreason", ImmutableMap.of("player", toBan, "reason", reason, "sender", sender.getName(), "time", tbe.getFormattedDuration(plugin.getLanguageConfig(), false)));
                        String banbc = (reason.isEmpty())
                                ? plugin.getLanguageConfig().getTranslation("neobans.message.tempban", ImmutableMap.of("player", toBan, "sender", sender.getName(), "time", tbe.getFormattedDuration(plugin.getLanguageConfig(), false)))
                                : plugin.getLanguageConfig().getTranslation("neobans.message.tempbanwithreason", ImmutableMap.of("player", toBan, "reason", reason, "sender", sender.getName(), "time", tbe.getFormattedDuration(plugin.getLanguageConfig(), false)));

                        Entry entry = plugin.getBanManager().addBan(tbe);
                        if (entry.getType() != EntryType.FAILURE) {
                            plugin.kickPlayer(sender, toBan, banmsg);
                            plugin.broadcast(sender, plugin.getConfig().getBroadcastDestination("tempban"), banbc);
                        } else {
                            sender.sendMessage(entry.getReason());
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", ImmutableMap.of("player", toBan, "reason", reason)));
                }
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 1;
    }

}
