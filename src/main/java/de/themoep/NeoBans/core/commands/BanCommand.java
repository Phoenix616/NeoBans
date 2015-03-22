package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.BanEntry;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class BanCommand extends AbstractCommand {
    
    public BanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, ImmutableMap<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runSync(new Runnable() {
            @Override
            public void run() {
                String toBan = args[0];
                String reason = "";
                if(args.length > 1)
                    for(int i = 1; i < args.length; i++)
                        reason += args[i] + " ";
                reason = reason.trim();
                
                if(reason.length() < 140) {
                    UUID playerid = plugin.getPlayerId(toBan);
                    if(playerid == null) {
                        sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", ImmutableMap.of("player", toBan)));
                        return;
                    }
                    BanEntry be = new BanEntry(playerid, sender.getUniqueID(), reason);

                    String banmsg = (reason.isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.disconnect.ban", ImmutableMap.of("player", toBan, "sender", sender.getName()))
                            : plugin.getLanguageConfig().getTranslation("neobans.disconnect.banwithreason", ImmutableMap.of("player", toBan, "reason", reason, "sender", sender.getName()));
                    String banbc = (reason.isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.message.ban", ImmutableMap.of("player", toBan, "sender", sender.getName()))
                            : plugin.getLanguageConfig().getTranslation("neobans.message.banwithreason", ImmutableMap.of("player", toBan, "reason", reason, "sender", sender.getName()));

                    Entry entry = plugin.getBanManager().addBan(be);
                    if (entry.getType() != EntryType.FAILURE) {
                        plugin.kickPlayer(sender, toBan, banmsg);
                        plugin.broadcast(sender, plugin.getConfig().getBroadcastDestination("ban"), banbc);
                    } else {
                        sender.sendMessage(entry.getReason());
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", ImmutableMap.of("player", toBan, "reason", reason)));
                }
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }
}
