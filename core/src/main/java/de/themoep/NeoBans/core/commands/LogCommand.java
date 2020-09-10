package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.LogEntry;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class LogCommand extends AbstractCommand {

    public LogCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(() -> {

            UUID playerId = "console".equalsIgnoreCase(args[0]) ?
                    new UUID(0, 0) : plugin.getPlayerId(args[0]);

            if (playerId != null) {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(
                                plugin.getLanguageConfig().getTranslation(
                                        "neobans.error.notanumber",
                                        "value", args[1]
                                )
                        );
                        return;
                    }
                }

                if (page <= 0) {
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.error.numbertoosmall",
                                    "value", Integer.toString(page)
                            )
                    );
                }

                List<Entry> entries = plugin.getDatabaseManager().getLogEntries(playerId, page - 1, 5);

                sender.sendMessage(
                        plugin.getLanguageConfig().getTranslation(
                                "neobans.message.log.head",
                                "player", args[0],
                                "page", Integer.toString(page)
                        )
                );

                if (entries.size() > 0) {
                    for (Entry entry : entries) {
                        if (entry.getType() == EntryType.FAILURE) {
                            sender.sendMessage("&6" + entry.getReason());
                        } else if (entry instanceof LogEntry) {
                            LogEntry logEntry = (LogEntry) entry;
                            String player = logEntry.getPlayerId().equals(new UUID(0, 0)) ?
                                    "Console" : plugin.getPlayerName(logEntry.getPlayerId());
                            String issuer = logEntry.getIssuerId().equals(new UUID(0, 0)) ?
                                    "Console" : plugin.getPlayerName(logEntry.getIssuerId());
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.message.log.entry",
                                            "type", logEntry.getType().toString(),
                                            "reason", logEntry.getReason(),
                                            "time", logEntry.getTime(plugin.getLanguageConfig().getTranslation("time.format")),
                                            "player", player != null ? player : logEntry.getPlayerId().toString(),
                                            "issuer", issuer != null ? issuer : logEntry.getIssuerId().toString()
                                    )
                            );
                        } else {
                            sender.sendMessage(
                                    plugin.getLanguageConfig().getTranslation(
                                            "neobans.error.unknownentrytype",
                                            "type", entry.getType().toString()
                                    )
                            );
                        }
                    }
                } else {
                    sender.sendMessage(
                            plugin.getLanguageConfig().getTranslation(
                                    "neobans.message.log.noentries",
                                    "player", args[0]
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
        return args.length > 0;
    }

}
