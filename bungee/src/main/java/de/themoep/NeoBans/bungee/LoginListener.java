package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.TemporaryPunishmentEntry;
import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
                BaseComponent[] cancelReason = null;
                if (entry.getType() == EntryType.FAILURE) {
                    cancelReason = MineDown.parse(entry.getReason());
                } else if (entry.getType() == EntryType.BAN) {
                    cancelReason = (entry.getReason().isEmpty())
                            ? MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.punished"), "player", event.getConnection().getName())
                            : MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.bannedwithreason"), "player", event.getConnection().getName(), "reason", entry.getReason());
                } else if (entry.getType() == EntryType.TEMPBAN) {
                    TemporaryPunishmentEntry timedPunishment = (TemporaryPunishmentEntry) entry;
                    cancelReason = (entry.getReason().isEmpty())
                            ? MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.tempbanned"), "player", event.getConnection().getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")))
                            : MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.tempbannedwithreason"), "player", event.getConnection().getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")));
                } else if (entry.getType() == EntryType.JAIL && plugin.getConfig().getJailServer().isEmpty()) {
                    TemporaryPunishmentEntry timedPunishment = (TemporaryPunishmentEntry) entry;
                    cancelReason = (entry.getReason().isEmpty())
                            ? MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.jailed"), "player", event.getConnection().getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")))
                            : MineDown.parse(plugin.getLanguageManager().getDefaultConfig().get("neobans.join.jailedwithreason"), "player", event.getConnection().getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageManager().getDefaultConfig()), "endtime", timedPunishment.getEndtime(plugin.getLanguageManager().getDefaultConfig().get("time.format")));
                }

                if (cancelReason != null) {
                    event.setCancelled(true);
                    event.setReason(TextComponent.fromArray(cancelReason));
                }
            }
            event.completeIntent(plugin);
        });
    }
}
