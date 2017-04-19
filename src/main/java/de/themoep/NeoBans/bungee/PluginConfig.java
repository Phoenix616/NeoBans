package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.config.NeoPluginConfig;
import de.themoep.bungeeplugin.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class PluginConfig extends FileConfiguration implements NeoPluginConfig {
    private final NeoBans plugin;

    /**
     * Load configuration from disk
     * @param path Path the the configuration file
     * @throws IOException
     */
    public PluginConfig(NeoBans plugin, String path) throws IOException {
        super(plugin, new File(plugin.getDataFolder(), path));
        this.plugin = plugin;
    }

    @Override
    public String getLanguage() {
        return getString("language");
    }

    @Override
    public Boolean getLatebind() {
        return getBoolean("commandlatebind");
    }

    @Override
    public String[] getCommandAliases(String cmdname) {
        List<String> stringList = getStringList("commandaliases." + cmdname.toLowerCase());
        return stringList.toArray(new String[stringList.size()]);
    }

    @Override
    public BroadcastDestination getBroadcastDestination(String type) {
        return BroadcastDestination.valueOf(getString("broadcast." + type.toLowerCase(), "SENDER").toUpperCase());
    }

    @Override
    public String getJailTarget() {
        return getString("target.jail");
    }

    @Override
    public String getUnjailTarget() {
        if (isSet("target.unjail") && !getString("target.unjail").isEmpty()) {
            return getString("target.unjail");
        }
        return plugin.getProxy().getConfig().getListeners().iterator().next().getServerPriority().get(0);
    }

    @Override
    public String getBackend() {
        return getString("backend");
    }
}
