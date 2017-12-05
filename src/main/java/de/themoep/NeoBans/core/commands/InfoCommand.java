package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TemporaryPunishmentEntry;
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
        plugin.runAsync(() -> {

            UUID playerid = sender.getUniqueID();
            if (args.length > 0) {
                if (sender.hasPermission("neobans.command.info.others")) {
                    playerid = plugin.getPlayerId(args[0]);
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation(
                            "neobans.error.nopermission",
                            "permission", "neobans.command.info.others"
                    ));
                    return;
                }
            }

            if(playerid != null) {
                int bancurrent = 0;
                int tempbancurrent = 0;
                int jailcurrent = 0;

                if (sender.hasPermission("neobans.command.info.ban")) {
                    Entry banEntry = plugin.getPunishmentManager().getPunishment(playerid, EntryType.BAN, EntryType.TEMPBAN);


                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.name",
                                    "player", plugin.getPlayerName(playerid)
                            )
                    );

                    if (banEntry != null && banEntry.getType() == EntryType.FAILURE) {
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
                                        "issuer", be.getIssuer().equals(new UUID(0, 0)) ?
                                                "Console" : plugin.getPlayerName(be.getIssuer())
                                )
                        );


                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.time",
                                        "time", be.getTime(plugin.getLanguageConfig().getTranslation("time.format"))
                                )
                        );

                        if (banEntry instanceof TemporaryPunishmentEntry) {
                            bancurrent = 0;
                            tempbancurrent = 1;
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.temporary",
                                            "endtime", ((TemporaryPunishmentEntry) be).getEndtime(plugin.getLanguageConfig().getTranslation("time.format")),
                                            "duration", ((TemporaryPunishmentEntry) be).getFormattedDuration()
                                    )
                            );
                        }

                        if (!be.getComment().isEmpty() && sender.hasPermission("neobans.command.info.comments")) {
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.comment",
                                            "comment", be.getComment()
                                    )
                            );
                        }
                    } else {
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.reason",
                                        "reason", "None"
                                )
                        );
                    }
                }

                if (sender.hasPermission("neobans.command.info.jail")) {
                    Entry jailEntry = plugin.getPunishmentManager().getPunishment(playerid, EntryType.JAIL);
                    
                    if (jailEntry != null && jailEntry.getType() == EntryType.FAILURE) {
                        sender.sendMessage(jailEntry.getReason());
                    } else if (jailEntry instanceof TimedPunishmentEntry) {
                        TimedPunishmentEntry je = (TimedPunishmentEntry) jailEntry;
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
                        
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentjail.time",
                                        "time", je.getTime(plugin.getLanguageConfig().getTranslation("time.format"))
                                )
                        );
                        
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentjail.temporary",
                                        "duration", je.getFormattedDuration()
                                )
                        );
                        
                        if (!je.getComment().isEmpty() && sender.hasPermission("neobans.command.info.comments")) {
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.info.currentban.comment",
                                            "comment", je.getComment()
                                    )
                            );
                        }
                    } else {
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentjail.reason",
                                        "reason", "None"
                                )
                        );
                    }
                }

                if (sender.hasPermission("neobans.command.info.previous")) {
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
                }
            } else {
                sender.sendMessage(
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.error.uuidnotfound",
                                "player", args[0]
                        )
                );
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 0 || sender.isPlayer();
    }

}
