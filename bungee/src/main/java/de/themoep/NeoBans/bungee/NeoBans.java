package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.bungee.integration.ServerListPlusIntegration;
import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.NeoUtils;
import de.themoep.NeoBans.core.PunishmentManager;
import de.themoep.NeoBans.core.commands.CommandMap;
import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.storage.DatabaseManager;
import de.themoep.NeoBans.core.storage.MysqlManager;

import de.themoep.bungeeplugin.BungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import net.zaiyers.UUIDDB.bungee.UUIDDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class NeoBans extends BungeePlugin implements NeoBansPlugin, Listener {

    /**
     * NeoBans config 
     */
    private PluginConfig config;

    /**
     * Language config
     */
    private LanguageConfig lang;

    /**
     * PunishmentManager
     */
    private PunishmentManager pm;
    
    private DatabaseManager dbm;

    private CommandMap cm;
    
    /**
     * If the server runs uuiddb or not
     */
    private boolean uuiddb = false;

    public void onEnable() {
        
        if(getProxy().getPluginManager().getPlugin("UUIDDB") != null)
            uuiddb = true;

        loadConfig();

        if (getConfig().getBoolean("serverlistplus") && getProxy().getPluginManager().getPlugin("ServerListPlus") != null) {
            new ServerListPlusIntegration(this);
        }

        pm = new PunishmentManager(this);
        
        cm = new CommandMap(this);
        
        getLogger().info("Registering Listeners...");
        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
        getProxy().getPluginManager().registerListener(this, new JailListener(this));

    }

    public void onDisable() {
        if (getDatabaseManager() != null) {
            getDatabaseManager().disable();
        }
        getLogger().info("Plugin disabled!");
    }

    public void loadConfig() {
        if (getDatabaseManager() != null) {
           getDatabaseManager().disable();
        }
        getProxy().getPluginManager().unregisterCommands(this);

        try {
            config = new PluginConfig(this, "config.yml");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to load configuration! NeoBans will not be enabled.", e);
            return;
        }

        if (getConfig().getJailServer().isEmpty()) {
            getLogger().log(Level.WARNING, "No jail server defined!");
        }

        if (getProxy().getServerInfo(getConfig().getJailServer()) == null) {
            getLogger().log(Level.WARNING, "The server configured for the jail (" + getConfig().getJailServer() + ") does not exist?");
        }

        // load language
        try {
            lang = new LanguageConfig(this, "lang." + config.getLanguage() + ".yml");
        } catch (IOException e) {
            getLogger().severe("Unable to load language! NeoBans will not be enabled.");
            e.printStackTrace();

            return;
        }

        if(getConfig().getBackend().equalsIgnoreCase("mysql"))
            dbm = new MysqlManager(this);

        setupCommands(config.getLatebind());
    }

    /**
     * Initialize and register all commands
     * @param latebind If we should wait a second or not after initialization to registering commands, useful to overwrite bungee's or other plugins commands
     */
    private void setupCommands(boolean latebind) {
        getLogger().info("Initializing Commands...");

        if(latebind) {
            getLogger().info("Scheduling the Registering of the Commands...");
            final NeoBans plugin = this;
            getProxy().getScheduler().schedule(this, () -> {
                plugin.getLogger().info("Late-binding Commands...");
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, plugin.getName().toLowerCase()));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neoban"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neounban"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neotempban"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neojail"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neounjail"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neokick"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neokickall"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neoinfo"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neoeditentry"));
                getProxy().getPluginManager().registerCommand(plugin, new CommandExecutor(plugin, "neolog"));
            }, 1, TimeUnit.SECONDS);
        } else {
            getLogger().info("Registering Commands...");
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, getName().toLowerCase()));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neoban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neounban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neotempban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neojail"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neounjail"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neokick"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neokickall"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neoinfo"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neoeditentry"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor(this, "neolog"));
        }

    }

    public PluginConfig getConfig() {
        return config;
    }

    public LanguageConfig getLanguageConfig() {
        return lang;
    }

    public PunishmentManager getPunishmentManager() {
        return pm;
    }

    public CommandMap getCommandMap() {
        return cm;
    }

    public DatabaseManager getDatabaseManager() {
        return dbm;
    }

    public List<String> getOnlinePlayers(String serverName) {
        List<String> names = new ArrayList<String>();
        ServerInfo server = getProxy().getServerInfo(serverName);
        if(server == null) {
            throw new NoSuchElementException("There is no server with the name " + serverName);
        }
        for(ProxiedPlayer p : server.getPlayers())
            names.add(p.getName());
        return names;
    }

    public List<String> getOnlinePlayers() {
        List<String> names = new ArrayList<String>();
        for(ProxiedPlayer p : getProxy().getPlayers())
            names.add(p.getName());
        return names;
    }
    
    public UUID getPlayerId(String username) {
        try {
            // Did someone input an uuid as a name?
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            ProxiedPlayer p = this.getProxy().getPlayer(username);
            if (p != null)
                return p.getUniqueId();

            if (uuiddb) {
                String id = UUIDDB.getInstance().getStorage().getUUIDByName(username, false);
                if (id != null)
                    return UUID.fromString(id);
            }
        }
        return null;
    }
    
    public String getPlayerName(UUID playerid){
        ProxiedPlayer p = this.getProxy().getPlayer(playerid);
        if(p != null)
            return p.getName();
        if(uuiddb) {
            String name = UUIDDB.getInstance().getStorage().getNameByUUID(playerid);
            if (name != null)
                return name;
        }
        return "ID:" + playerid.toString();
    };

    public int kickPlayer(NeoSender sender, String name, String reason) {
        return kickPlayer(sender, getProxy().getPlayer(name), reason);
    }

    public int kickPlayer(NeoSender sender, UUID id, String reason) {
        return kickPlayer(sender, getProxy().getPlayer(id), reason);
    }

    public int kickPlayer(NeoSender sender, ProxiedPlayer player, String reason) {
        if(player != null) {
            if(player.hasPermission("neobans.command.kick.exempt") && !sender.hasPermission("neobans.command.kick.exempt"))
                return -1;
            player.disconnect(TextComponent.fromLegacyText(reason));
            return 1;
        }
        return 0;
    }
    
    public void broadcast(NeoSender sender, BroadcastDestination destination, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        BaseComponent[] msg = TextComponent.fromLegacyText(message);
        switch(destination){
            case GLOBAL:
                for(ProxiedPlayer p : getProxy().getPlayers())
                    p.sendMessage(msg);
                break;
            case SERVER:
                if(sender.isPlayer()) {
                    broadcast(sender, getProxy().getPlayer(sender.getUniqueID()).getServer().getInfo().getName(), message);
                } else
                    sender.sendMessage(message);
                break;
            case SENDER:
                sender.sendMessage("[" + ChatColor.RED + "Silent" + ChatColor.RESET + "] " + message);
                for(ProxiedPlayer p : getProxy().getPlayers()) {
                    if (!p.getUniqueId().equals(sender.getUniqueID()) && p.hasPermission("neobans.seesilent")) {
                        p.sendMessage("[" + ChatColor.RED + "Silent" + ChatColor.RESET + "] " + message);
                    }
                }
                break;
        }
        if (sender.isPlayer()) {
            getLogger().info(message);
        }
    }

    public void broadcast(NeoSender sender, String serverName, String message) {
        ServerInfo server = getProxy().getServerInfo(serverName);
        if(server != null) {
            BaseComponent[] msg = TextComponent.fromLegacyText(message);
            for(ProxiedPlayer p : server.getPlayers()) {
                p.sendMessage(msg);
            }

            if(sender.isPlayer() && !getProxy().getPlayer(sender.getUniqueID()).getServer().getInfo().equals(server)) {
                sender.sendMessage("[Sent to " + serverName + "] " + message);
            }
        } else {
            sender.sendMessage(getLanguageConfig().getTranslation("neobans.error.notfound", "value", serverName));
        }
    }

    @Override
    public int runSync(Runnable runnable) {
        runnable.run(); // Theoretically everything is always async with bungee
        return -1;
    }

    @Override
    public int runAsync(Runnable runnable) {
        return getProxy().getScheduler().runAsync(this, runnable).getId();
    }

    @Override
    public int runLater(Runnable runnable, long delay) {
        return getProxy().getScheduler().schedule(this, runnable, delay * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runRepeating(Runnable runnable, long delay, long period) {
        return getProxy().getScheduler().schedule(this, runnable, delay * 50, period * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public String getName() {
        return getDescription().getName();
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public int compareVersion(String version) {
        return NeoUtils.compareVersions(getDescription().getVersion(), version);
    }

    @Override
    public boolean sendTitle(UUID playerId, String message) {
        ProxiedPlayer player = getProxy().getPlayer(playerId);
        if (player != null) {
            String[] parts = message.split("\n");
            Title title = getProxy().createTitle();
            if (parts.length > 0) {
                title.title(TextComponent.fromLegacyText(parts[0]));
            }
            if (parts.length > 1) {
                title.subTitle(TextComponent.fromLegacyText(parts[1]));
            }
            title.send(player);
            if (parts.length > 2) {
                player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(parts[2]));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean movePlayer(UUID playerId, String target) {
        ProxiedPlayer player = getProxy().getPlayer(playerId);
        if (player != null) {
            ServerInfo server = getProxy().getServerInfo(target);
            if (server != null) {
                player.connect(server);
                return true;
            } else {
                getLogger().log(Level.WARNING, "Tried to move " + player.getName() + " to the server " + target + " but it does not exist?");
            }
        }
        return false;
    }

}
