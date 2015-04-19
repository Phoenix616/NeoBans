package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.core.BanEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.TempbanEntry;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class InfoCommand extends AbstractCommand {
    
    public InfoCommand(NeoBansPlugin plugin, NeoSender sender, String[] args,ImmutableMap<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runSync(new Runnable() {
            @Override
            public void run() {
                Entry entry = plugin.getBanManager().getBan(args[0]);

                int bancurrent = 0;
                int tempbancurrent = 0;

                if (entry != null) {
                    if (entry.getType() == EntryType.FAILURE) {
                        sender.sendMessage(entry.getReason());
                    } else if (entry instanceof BanEntry) {
                        BanEntry be = (BanEntry) entry;
                        bancurrent = 1;

                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.name",
                                        ImmutableMap.of(
                                                "player",
                                                plugin.getPlayerName(be.getBanned())
                                        )
                                )
                        );
                        
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.reason",
                                        ImmutableMap.of(
                                                "reason",
                                                be.getReason()
                                        )
                                )
                        );
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.issuer",
                                        ImmutableMap.of(
                                                "issuer",
                                                plugin.getPlayerName(be.getIssuer())
                                        )
                                )
                        );

                        Date date = new Date(be.getTime() * 1000L);
                        SimpleDateFormat sdf = new SimpleDateFormat(plugin.getLanguageConfig().getTranslation("time.format"));
                        sdf.setTimeZone(Calendar.getInstance().getTimeZone());

                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.message.info.currentban.time",
                                        ImmutableMap.of(
                                                "time",
                                                sdf.format(date)
                                        )
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
                                            ImmutableMap.of(
                                                    "endtime",
                                                    sdf.format(enddate),
                                                    "duration",
                                                    ((TempbanEntry) be).getFormattedDuration(plugin.getLanguageConfig(), true)
                                            )
                                    )
                            );
                        }
                    }
                } else {
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.name",
                                    ImmutableMap.of(
                                            "player",
                                            args[0]
                                    )
                            )
                    );
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.info.currentban.reason",
                                    ImmutableMap.of("reason", "None")
                            )
                    );
                }

                sender.sendMessage(
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.info.previous.bans",
                                ImmutableMap.of(
                                        "count",
                                        Integer.toString(plugin.getBanManager().getCount(EntryType.BAN, args[0]) - bancurrent)
                                )
                        )
                );
                sender.sendMessage(
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.info.previous.tempbans",
                                ImmutableMap.of(
                                        "count",
                                        Integer.toString(plugin.getBanManager().getCount(EntryType.TEMPBAN, args[0]) - tempbancurrent)
                                )
                        )
                );
                sender.sendMessage(
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.info.previous.kicks",
                                ImmutableMap.of(
                                        "count",
                                        Integer.toString(plugin.getBanManager().getCount(EntryType.KICK, args[0]))
                                )
                        )
                );
            }
        });
    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
