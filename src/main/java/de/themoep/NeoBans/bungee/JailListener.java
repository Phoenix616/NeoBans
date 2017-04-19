package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.TimedPunishmentEntry;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class JailListener implements Listener {

    NeoBans plugin;

    public JailListener(NeoBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        if (event.getPlayer().hasPermission("neobans.exempt.jail")) {
            return;
        }

        if (event.getPlayer().getServer() == null) { // Is join
            Entry entry = plugin.getPunishmentManager().getPunishment(event.getPlayer().getUniqueId());
            if (entry == null || entry.getType() != EntryType.JAIL) {
                return;
            }
            TimedPunishmentEntry timedPunishment = (TimedPunishmentEntry) entry;
            String msg = (entry.getReason().isEmpty())
                    ? plugin.getLanguageConfig().getTranslation("neobans.join.jailed", "player", event.getPlayer().getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                    : plugin.getLanguageConfig().getTranslation("neobans.join.jailedwithreason", "player", event.getPlayer().getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));

            ServerInfo server = plugin.getProxy().getServerInfo(plugin.getConfig().getJailTarget());
            if (server != null) {
                event.setTarget(server);
                plugin.runLater(() -> plugin.sendTitle(event.getPlayer().getUniqueId(), msg), 100);
            } else {
                event.setCancelled(true);
                event.getPlayer().disconnect(TextComponent.fromLegacyText(msg));
            }

        } else if (event.getPlayer().getServer().getInfo().getName().equals(plugin.getConfig().getJailTarget())) { // Player is on jail server
            if (handleAction(event.getPlayer())) {
                event.setTarget(event.getPlayer().getServer().getInfo());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || ((ProxiedPlayer) event.getSender()).hasPermission("neobans.exempt.jail")) {
            return;
        }

        if (handleAction((ProxiedPlayer) event.getSender())) {
            event.setCancelled(true);
        }
    }

    private boolean handleAction(ProxiedPlayer player) {
        Entry entry = plugin.getPunishmentManager().getPunishment(player.getUniqueId());
        if (entry == null || entry.getType() != EntryType.JAIL) {
            return false;
        }
        TimedPunishmentEntry timedPunishment = (TimedPunishmentEntry) entry;
        String msg = (entry.getReason().isEmpty())
                ? plugin.getLanguageConfig().getTranslation("neobans.join.jailed", "player", player.getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                : plugin.getLanguageConfig().getTranslation("neobans.join.jailedwithreason", "player", player.getName(), "reason", entry.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));

        plugin.sendTitle(player.getUniqueId(), msg);
        return true;
    }
}
