package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BanEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TempbanEntry;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class InfoCommand extends AbstractCommand {
    
    public InfoCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runSync(new Runnable() {
            @Override
            public void run() {
                
                UUID playerid = plugin.getPlayerId(args[0]);
                
                if(playerid != null) {
                    Entry entry = plugin.getBanManager().getBan(playerid);

                    int bancurrent = 0;
                    int tempbancurrent = 0;

                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.name",
                                    "player", plugin.getPlayerName(playerid)
                            )
                    );

                    if (entry != null) {
                        if (entry.getType() == EntryType.FAILURE) {
                            sender.sendMessage(entry.getReason());
                        } else if (entry instanceof BanEntry) {
                            BanEntry be = (BanEntry) entry;
                            bancurrent = 1;

                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.reason",
                                            "reason", be.getReason()
                                    )
                            );
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.issuer",
                                            "issuer", plugin.getPlayerName(be.getIssuer())
                                    )
                            );

                            Date date = new Date(be.getTime() * 1000L);
                            SimpleDateFormat sdf = new SimpleDateFormat(plugin.getLanguageConfig().getTranslation("time.format"));
                            sdf.setTimeZone(Calendar.getInstance().getTimeZone());

                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.time",
                                            "time", sdf.format(date)
                                    )
                            );

                            String endtime = "";
                            if (entry instanceof TempbanEntry) {
                                bancurrent = 0;
                                tempbancurrent = 1;
                                Date enddate = new Date(((TempbanEntry) be).getEndtime() * 1000L);
                                sender.sendMessage(
                                        plugin.getLanguageConfig().getTranslation(
                                                "neobans.message.info.currentban.temporary",
                                                "endtime", sdf.format(enddate),
                                                "duration",((TempbanEntry) be).getFormattedDuration(plugin.getLanguageConfig(), true)
                                        )
                                );
                            }
                        }
                    } else {
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.reason",
                                        "reason", "None"
                                )
                        );
                    }

                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.bans",
                                    "count", Integer.toString(plugin.getBanManager().getCount(EntryType.BAN, playerid) - bancurrent)
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.tempbans",
                                    "count", Integer.toString(plugin.getBanManager().getCount(EntryType.TEMPBAN, playerid) - tempbancurrent)
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.kicks",
                                    "count", Integer.toString(plugin.getBanManager().getCount(EntryType.KICK, playerid))
                            )
                    );
                } else {
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.error.uuidnotfound",
                                    "player", args[0]
                            )
                    );
                }
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
