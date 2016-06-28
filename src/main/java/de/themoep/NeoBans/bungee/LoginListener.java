package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.TempbanEntry;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Phoenix616 on 27.02.2015.
 */
public class LoginListener implements Listener {

    NeoBans plugin;

    public LoginListener() {
        this.plugin = NeoBans.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(final LoginEvent event) {
        event.registerIntent(plugin);
        plugin.runAsync(new Runnable() {
            @Override
            public void run() {
                Entry entry = plugin.getBanManager().getBan(event.getConnection().getUniqueId());
                if(entry != null) {
                    plugin.getLogger().info(event.getConnection().getName() + " tried to join. He has the following entry: " + entry.getType().toString() + " - " + entry.getReason());
                    if (entry.getType() == EntryType.FAILURE) {
                        event.setCancelled(true);
                        event.setCancelReason(entry.getReason());
                    } else if (entry.getType() == EntryType.BAN) {
                        String msg = (entry.getReason().isEmpty())
                                ? plugin.getLanguageConfig().getTranslation("neobans.join.banned", "player", event.getConnection().getName())
                                : plugin.getLanguageConfig().getTranslation("neobans.join.bannedwithreason", "player", event.getConnection().getName(), "reason", entry.getReason());

                        event.setCancelled(true);
                        event.setCancelReason(msg);
                    } else if (entry.getType() == EntryType.TEMPBAN && entry instanceof TempbanEntry) {
                        TempbanEntry tbe = (TempbanEntry) entry;
                        String msg = (entry.getReason().isEmpty())
                                ? plugin.getLanguageConfig().getTranslation("neobans.join.tempbanned", "player", event.getConnection().getName(), "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                                : plugin.getLanguageConfig().getTranslation("neobans.join.tempbannedwithreason", "player", event.getConnection().getName(), "reason", entry.getReason(), "duration", tbe.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", tbe.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));

                        event.setCancelled(true);
                        event.setCancelReason(msg);
                    }
                }
                event.completeIntent(plugin);
            }
        });
    }
}
