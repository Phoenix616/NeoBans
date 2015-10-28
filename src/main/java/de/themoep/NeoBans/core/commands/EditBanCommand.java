package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class EditBanCommand extends AbstractCommand {

    public EditBanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, ImmutableMap<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    public void execute() {
        plugin.runSync(new Runnable() {
            public void run() {
                String playerName = args[0];
                String option = args[1];
                String value = "";
                for(int i = 2; i < args.length; i++) {
                    value += args[i] + " ";
                }
                value = value.trim();

                if(value.length() < 140) {
                    Entry entry = plugin.getBanManager().updateBan(playerName, sender.getUniqueID(), option.toLowerCase(), value);
                    if(entry.getType() != EntryType.FAILURE) {
                        sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.message.banedited", ImmutableMap.of("player", playerName, "option", option.toLowerCase(), "value", value)));
                    } else {
                        sender.sendMessage(entry.getReason());
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.reasontoolong", ImmutableMap.of("player", playerName, "reason", value)));
                }
            }
        });
    }

    public boolean validateInput() {
        return args.length > 2;
    }
}
