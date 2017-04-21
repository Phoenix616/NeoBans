package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.PunishmentEntry;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class UnjailCommand extends AbstractCommand {

    public UnjailCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runAsync(() -> {
            UUID playerId = plugin.getPlayerId(args[0]);
            if (playerId == null) {
                sender.sendMessage(plugin.getLanguageConfig().getTranslation("neobans.error.uuidnotfound", "player", args[0]));
                return;
            }

            Entry entry = plugin.getPunishmentManager().unjail(sender, playerId, args.length > 1 && args[1].equalsIgnoreCase("-silent"));
            if (entry != null && entry.getType() == EntryType.FAILURE) {
                sender.sendMessage(entry.getReason(), "player", args[0]);
            }
        });

    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
