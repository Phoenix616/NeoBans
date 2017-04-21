package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TimedPunishmentEntry;

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
        plugin.runAsync(new Runnable() {
            @Override
            public void run() {
                
                UUID playerid = plugin.getPlayerId(args[0]);
                
                if(playerid != null) {
                    Entry banEntry = plugin.getPunishmentManager().getPunishment(playerid, EntryType.BAN, EntryType.TEMPBAN);

                    int bancurrent = 0;
                    int tempbancurrent = 0;

                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.name",
                                    "player", plugin.getPlayerName(playerid)
                            )
                    );

                    if (banEntry != null) {
                        if (banEntry.getType() == EntryType.FAILURE) {
                            sender.sendMessage(banEntry.getReason());
                        } else if (banEntry instanceof PunishmentEntry) {
                            PunishmentEntry be = (PunishmentEntry) banEntry;
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

                            if (banEntry instanceof TimedPunishmentEntry) {
                                bancurrent = 0;
                                tempbancurrent = 1;
                                Date enddate = new Date(((TimedPunishmentEntry) be).getEndtime() * 1000L);
                                sender.sendMessage(
                                        plugin.getLanguageConfig().getTranslation(
                                                "neobans.message.info.currentban.temporary",
                                                "endtime", sdf.format(enddate),
                                                "duration",((TimedPunishmentEntry) be).getFormattedDuration(plugin.getLanguageConfig(), true)
                                        )
                                );
                            }

                            if (!be.getComment().isEmpty()) {
                                sender.sendMessage(
                                        plugin.getLanguageConfig().getTranslation(
                                                "neobans.message.info.currentban.comment",
                                                "comment", be.getComment()
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

                    Entry jailEntry = plugin.getPunishmentManager().getPunishment(playerid, EntryType.JAIL);

                    int jailcurrent = 0;

                    if (jailEntry != null) {
                        if (jailEntry.getType() == EntryType.FAILURE) {
                            sender.sendMessage(jailEntry.getReason());
                        } else if (jailEntry instanceof TimedPunishmentEntry) {
                            PunishmentEntry je = (PunishmentEntry) jailEntry;
                            jailcurrent = 1;

                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentjail.reason",
                                            "reason", je.getReason()
                                    )
                            );
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentjail.issuer",
                                            "issuer", plugin.getPlayerName(je.getIssuer())
                                    )
                            );

                            Date date = new Date(je.getTime() * 1000L);
                            SimpleDateFormat sdf = new SimpleDateFormat(plugin.getLanguageConfig().getTranslation("time.format"));
                            sdf.setTimeZone(Calendar.getInstance().getTimeZone());

                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentjail.time",
                                            "time", sdf.format(date)
                                    )
                            );

                            Date enddate = new Date(((TimedPunishmentEntry) je).getEndtime() * 1000L);
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentjail.temporary",
                                            "endtime", sdf.format(enddate),
                                            "duration",((TimedPunishmentEntry) je).getFormattedDuration(plugin.getLanguageConfig(), true)
                                    )
                            );

                            if (!je.getComment().isEmpty()) {
                                sender.sendMessage(
                                        plugin.getLanguageConfig().getTranslation(
                                                "neobans.message.info.currentban.comment",
                                                "comment", je.getComment()
                                        )
                                );
                            }
                        }
                    } else {
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentjail.reason",
                                        "reason", "None"
                                )
                        );
                    }

                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.bans",
                                    "count", Integer.toString(plugin.getPunishmentManager().getCount(EntryType.BAN, playerid) - bancurrent)
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.tempbans",
                                    "count", Integer.toString(plugin.getPunishmentManager().getCount(EntryType.TEMPBAN, playerid) - tempbancurrent)
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.jails",
                                    "count", Integer.toString(plugin.getPunishmentManager().getCount(EntryType.JAIL, playerid) - jailcurrent)
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.previous.kicks",
                                    "count", Integer.toString(plugin.getPunishmentManager().getCount(EntryType.KICK, playerid))
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
