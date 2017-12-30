package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.PunishmentEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
            boolean silent = args.length > 1 && ("-silent".equalsIgnoreCase(args[1]) || "-s".equalsIgnoreCase(args[1]));
            String reason = Arrays.stream(args).skip(silent ? 2 : 1).collect(Collectors.joining(" "));
            Entry entry = plugin.getPunishmentManager().unjail(sender, playerId, reason, silent);
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
