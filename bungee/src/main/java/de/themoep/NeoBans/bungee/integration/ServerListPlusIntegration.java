package de.themoep.NeoBans.bungee.integration;

/*
 * NeoBans
 * Copyright (C) 2020. Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.themoep.NeoBans.bungee.NeoBans;
import de.themoep.NeoBans.core.Entry;
import de.themoep.NeoBans.core.EntryType;
import de.themoep.NeoBans.core.PunishmentEntry;
import de.themoep.NeoBans.core.TemporaryPunishmentEntry;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.player.PlayerIdentity;
import net.minecrell.serverlistplus.core.player.ban.BanProvider;

import java.util.Date;
import java.util.UUID;

public class ServerListPlusIntegration implements BanProvider {

    private final NeoBans plugin;

    public ServerListPlusIntegration(NeoBans plugin) {
        this.plugin = plugin;
        ServerListPlusCore.getInstance().setBanProvider(this);
    }

    private PunishmentEntry getBan(UUID playerId) {
        Entry entry = plugin.getPunishmentManager().getPunishment(playerId, EntryType.BAN, EntryType.TEMPBAN);
        if (entry.getType() == EntryType.BAN || entry.getType() == EntryType.TEMPBAN) {
            return (PunishmentEntry) entry;
        }
        return null;
    }

    @Override
    public boolean isBanned(PlayerIdentity playerIdentity) {
        return getBan(playerIdentity.getUuid()) != null;
    }

    @Override
    public String getBanReason(PlayerIdentity playerIdentity) {
        PunishmentEntry entry = getBan(playerIdentity.getUuid());
        return entry != null ? entry.getReason() : null;
    }

    @Override
    public String getBanOperator(PlayerIdentity playerIdentity) {
        PunishmentEntry entry = getBan(playerIdentity.getUuid());
        return entry != null ? plugin.getPlayerName(entry.getIssuer()) : null;
    }

    @Override
    public Date getBanExpiration(PlayerIdentity playerIdentity) {
        PunishmentEntry entry = getBan(playerIdentity.getUuid());
        if (entry instanceof TemporaryPunishmentEntry) {
            return new Date(((TemporaryPunishmentEntry) entry).getEndtime() * 1000);
        }
        return null;
    }
}
