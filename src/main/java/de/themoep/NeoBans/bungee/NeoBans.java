package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.NeoBansPlugin;
import de.themoep.NeoBans.core.BanManager;
import de.themoep.NeoBans.core.commands.CommandMap;
import de.themoep.NeoBans.core.commands.NeoSender;
import de.themoep.NeoBans.core.mysql.DatabaseManager;
import de.themoep.NeoBans.core.mysql.MysqlManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.zaiyers.UUIDDB.bungee.UUIDDB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class NeoBans extends Plugin implements NeoBansPlugin, Listener {

    /**
     * NeoBans config 
     */
    private static PluginConfig config;

    /**
     * Language config
     */
    private static LanguageConfig lang;

    /**
     * BanManager 
     */
    private static BanManager bm;
    
    private static DatabaseManager dbm;

    private static CommandMap cm;
    
    /**
     * If the server runs uuiddb or not
     */
    private static Boolean uuiddb = false;

    public void onEnable() {
        
        if(getProxy().getPluginManager().getPlugin("UUIDDB") != null)
            uuiddb = true;
        
        try {
            config = new PluginConfig(getDataFolder() + File.separator + "config.yml");
        } catch (IOException e) {
            getLogger().severe("Unable to load configuration! NeoBans will not be enabled.");
            e.printStackTrace();
            return;
        }

        // load language
        try {
            lang = new LanguageConfig(getDataFolder()+ File.separator + "lang." + config.getLanguage() + ".yml");
        } catch (IOException e) {
            getLogger().severe("Unable to load language! NeoBans will not be enabled.");
            e.printStackTrace();

            return;
        }

        bm = new BanManager(NeoBans.getInstance());
        
        cm = new CommandMap(NeoBans.getInstance());

        if(getConfig().getString("backend").equalsIgnoreCase("mysql"))
            dbm = new MysqlManager(NeoBans.getInstance());

        setupCommands(config.getLatebind());
        
        getLogger().info("Registering Listeners...");
        getProxy().getPluginManager().registerListener(NeoBans.getInstance(), new LoginListener());

    }

    public void onDisable() {
        getDatabaseManager().disable();
        getLogger().info("Plugin disabled!");
    }

    /**
     * Initialize and register all commands
     * @param latebind If we should wait a second or not after initialization to registering commands, useful to overwrite bungee's or other plugins commands
     */
    private void setupCommands(Boolean latebind) {
        getLogger().info("Initializing Commands...");

        if(latebind) {
            getLogger().info("Scheduling the Registering of the Commands...");
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    NeoBans.getInstance().getLogger().info("Late-binding Commands...");
                    getProxy().getPluginManager().registerCommand(NeoBans.getInstance(), new CommandExecutor("neoban"));
                    getProxy().getPluginManager().registerCommand(NeoBans.getInstance(), new CommandExecutor("neounban"));
                    getProxy().getPluginManager().registerCommand(NeoBans.getInstance(), new CommandExecutor("neotempban"));
                    getProxy().getPluginManager().registerCommand(NeoBans.getInstance(), new CommandExecutor("neokick"));
                    getProxy().getPluginManager().registerCommand(NeoBans.getInstance(), new CommandExecutor("neoinfo"));
                }
            }, 1, TimeUnit.SECONDS);
        } else {
            getLogger().info("Registering Commands...");
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor("neoban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor("neounban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor("neotempban"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor("neokick"));
            getProxy().getPluginManager().registerCommand(this, new CommandExecutor("neoinfo"));
        }

    }

    public PluginConfig getConfig() {
        return config;
    }

    public LanguageConfig getLanguageConfig() {
        return lang;
    }

    @Override
    public BanManager getBanManager() {
        return bm;
    }

    @Override
    public CommandMap getCommandMap() {
        return cm;
    }

    @Override
    public DatabaseManager getDatabaseManager() {
        return dbm;
    }
    
    @Override
    public List<String> getOnlinePlayers() {
        List<String> names = new ArrayList<String>();
        for(ProxiedPlayer p : getProxy().getPlayers())
            names.add(p.getName());
        return names;
    }
    
    @Override
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
    
    @Override
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

    @Override
    public int kickPlayer(NeoSender sender, String name, String reason) {
        return kickPlayer(sender, getProxy().getPlayer(name), reason);
    }

    @Override
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
    
    @Override
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
                    for(ProxiedPlayer p : getProxy().getPlayer(sender.getUniqueID()).getServer().getInfo().getPlayers())
                        p.sendMessage(msg);                            
                } else
                    sender.sendMessage(message);
                break;
            case SENDER:
                sender.sendMessage("[" + ChatColor.RED + "Silent" + ChatColor.RESET + "] " + message);
                break;
        }
        this.getLogger().info(message);
    }

    @Override
    public void runSync(Runnable runnable) {
        getProxy().getScheduler().schedule(this, runnable, 0, TimeUnit.SECONDS);
    }

    @Override
    public void runAsync(Runnable runnable) {
        getProxy().getScheduler().runAsync(this, runnable);
    }

    /**
     * Return the plugin's instance
     */
    public static NeoBans getInstance() {
        return (NeoBans) ProxyServer.getInstance().getPluginManager().getPlugin("NeoBans");
    }
}
