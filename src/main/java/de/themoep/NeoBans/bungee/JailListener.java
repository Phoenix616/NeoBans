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
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Phoenix616 on 19.04.2017.
 */
public class JailListener implements Listener {

    private final NeoBans plugin;
    private ScheduledTask noticeTask = null;
    private Set<TimedPunishmentEntry> jails = new HashSet<>();


    public JailListener(NeoBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        if (event.getPlayer().hasPermission("neobans.exempt.jail")) {
            return;
        }

        if (event.getPlayer().getServer() == null) { // Is join
            Entry entry = plugin.getPunishmentManager().getPunishment(event.getPlayer().getUniqueId(), EntryType.JAIL);
            if (entry == null || entry.getType() != EntryType.JAIL) {
                return;
            }

            ServerInfo server = plugin.getProxy().getServerInfo(plugin.getConfig().getJailTarget());
            if (server != null) {
                event.setTarget(server);
                plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.sendTitle(event.getPlayer().getUniqueId(), getMessage(event.getPlayer(), (TimedPunishmentEntry) entry)), 5, TimeUnit.SECONDS);
                startNoticeTask((TimedPunishmentEntry) entry);
            } else {
                event.setCancelled(true);
                event.getPlayer().disconnect(TextComponent.fromLegacyText(getMessage(event.getPlayer(), (TimedPunishmentEntry) entry)));
            }

        } else if (event.getPlayer().getServer().getInfo().getName().equals(plugin.getConfig().getJailTarget())) { // Player is on jail server
            Entry entry = plugin.getPunishmentManager().getPunishment(event.getPlayer().getUniqueId(), EntryType.JAIL);
            if (entry != null && entry.getType() == EntryType.JAIL) {
                event.setTarget(event.getPlayer().getServer().getInfo());
                event.setCancelled(true);
                plugin.sendTitle(event.getPlayer().getUniqueId(), getMessage(event.getPlayer(), (TimedPunishmentEntry) entry));
            }
        }
    }

    private void startNoticeTask(TimedPunishmentEntry entry) {
        jails.add(entry);
        if (noticeTask == null) {
            noticeTask = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                for (Iterator<TimedPunishmentEntry> it = jails.iterator(); it.hasNext();) {
                    TimedPunishmentEntry e = it.next();
                    ProxiedPlayer player = plugin.getProxy().getPlayer(e.getPunished());
                    if (player == null) {
                        // player is not online anymore
                        it.remove();
                        continue;
                    }

                    Entry currentEntry = plugin.getPunishmentManager().getPunishment(e.getPunished(), EntryType.JAIL);
                    if (currentEntry == null || currentEntry.getType() == EntryType.JAIL) {
                        if (currentEntry != null && ((TimedPunishmentEntry) currentEntry).isExpired() || currentEntry == null && e.isExpired()) {
                            it.remove();
                            plugin.getPunishmentManager().unjail(new Sender(plugin.getProxy().getConsole()), player.getUniqueId(), true);
                        } else if (currentEntry != null) {
                            plugin.sendTitle(e.getPunished(), getMessage(player, e));
                        } else {
                            it.remove();
                        }
                    }
                }
                if (jails.isEmpty()) {
                    noticeTask.cancel();
                    noticeTask = null;
                }
            }, 5 + 60, 60, TimeUnit.SECONDS);
        }
    }

    @EventHandler
    public void onPlayerChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || ((ProxiedPlayer) event.getSender()).hasPermission("neobans.exempt.jail")) {
            return;
        }

        if (!((ProxiedPlayer) event.getSender()).getServer().getInfo().getName().equals(plugin.getConfig().getJailTarget())) { // Player is not on jail server
            return;
        }

        Entry entry = plugin.getPunishmentManager().getPunishment(((ProxiedPlayer) event.getSender()).getUniqueId(), EntryType.JAIL);
        if (entry != null && entry.getType() == EntryType.JAIL) {
            event.setCancelled(true);

            plugin.sendTitle(((ProxiedPlayer) event.getSender()).getUniqueId(), getMessage((ProxiedPlayer) event.getSender(), (TimedPunishmentEntry) entry));
        }
    }

    private String getMessage(ProxiedPlayer player, TimedPunishmentEntry timedPunishment) {
        return (timedPunishment.getReason().isEmpty())
                ? plugin.getLanguageConfig().getTranslation("neobans.title.jailed", "player", player.getName(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")))
                : plugin.getLanguageConfig().getTranslation("neobans.title.jailedwithreason", "player", player.getName(), "reason", timedPunishment.getReason(), "duration", timedPunishment.getFormattedDuration(plugin.getLanguageConfig(), false), "endtime", timedPunishment.getEndtime(plugin.getLanguageConfig().getTranslation("time.format")));
    }
}
