package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class EditEntryCommand extends AbstractCommand {

    public EditEntryCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    public void execute() {
        plugin.runAsync(() -> {
            String option = args[1];
            String value = Arrays.stream(args).skip(2).collect(Collectors.joining(" ")).trim();
            for (String playerName : args[0].split(",")) {
                if(value.length() < 140) {
                    Entry entry = plugin.getPunishmentManager().updatePunishment(playerName, sender.getUniqueID(), option.toLowerCase(), value);
                    if(entry.getType() != EntryType.FAILURE) {
                        sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.message.entryedited", "player", playerName, "option", option.toLowerCase(), "value", value));
                    } else {
                        sender.sendMessage(entry.getReason());
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", "player", playerName, "reason", value));
                }
            }
        });
    }

    public boolean validateInput() {
        return args.length > 2;
    }
}
