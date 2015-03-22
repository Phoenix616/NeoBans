package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;
import de.themoep.NeoBans.core.BanEntry;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class UnbanCommand extends AbstractCommand {
    
    public UnbanCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        super(plugin, sender, args, completions);
    }

    @Override
    public void execute() {
        plugin.runSync(new Runnable() {
            @Override
            public void run() {
                Entry entry = plugin.getBanManager().removeBan(args[0], sender.getUniqueID());
                if (entry instanceof BanEntry)
                    plugin.broadcast(sender,
                            plugin.getConfig().getBroadcastDestination("unban"),
                            plugin.getLanguageConfig().getTranslation("neobans.message.unban", ImmutableMap.of("player", args[0], "sender", sender.getName()))
                    );
                else
                    sender.sendMessage(entry.getReason(), ImmutableMap.of("player", args[0]));
            }
        });

    }

    @Override
    public boolean validateInput() {
        return args.length > 0;
    }

}
