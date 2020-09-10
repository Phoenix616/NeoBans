package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.TemporaryPunishmentEntry;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Phoenix616 on 27.02.2015.
 */
public class LoginListener implements Listener {

    NeoBans plugin;

    public LoginListener(NeoBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(final LoginEvent event) {
        event.registerIntent(plugin);
        plugin.runAsync(() -> {
            Entry entry = plugin.getPunishmentManager().getPunishment(event.getConnection().getUniqueId());
            if(entry != null) {
                plugin.getLogger().info(event.getConnection().getName() + " tried to join. He has the following entry: " + entry.getType().toString() + " - " + entry.getReason());
                if (entry.getType() == EntryType.FAILURE) {
                    event.setCancelled(true);
                    event.setCancelReason(entry.getReason());
                } else if (entry.getType() == EntryType.BAN) {
                    String msg = (entry.getReason().isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.join.punished", "player", event.getConnection().getName())
                            : plugin.getLanguageConfig().getTranslation("neobans.join.bannedwithreason", "player", event.getConnection().getName(), "reason", entry.getReason());

                    event.setCancelled(true);
                    event.setCancelReason(msg);
                } else if (entry.getType() == EntryType.TEMPBAN) {
                    TemporaryPunishmentEntry timedPunishment = (TemporaryPunishmentEntry) entry;
                    String msg = (entry.getReason().isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.join.tempbanned", "player", event.getConnection().getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                            : plugin.getLanguageConfig().getTranslation("neobans.join.tempbannedwithreason", "player", event.getConnection().getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));

                    event.setCancelled(true);
                    event.setCancelReason(msg);
                } else if (entry.getType() == EntryType.JAIL && plugin.getConfig().getJailServer().isEmpty()) {
                    TemporaryPunishmentEntry timedPunishment = (TemporaryPunishmentEntry) entry;
                    String msg = (entry.getReason().isEmpty())
                            ? plugin.getLanguageConfig().getTranslation("neobans.join.jailed", "player", event.getConnection().getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                            : plugin.getLanguageConfig().getTranslation("neobans.join.jailedwithreason", "player", event.getConnection().getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));

                    event.setCancelled(true);
                    event.setCancelReason(msg);
                }
            }
            event.completeIntent(plugin);
        });
    }
}
