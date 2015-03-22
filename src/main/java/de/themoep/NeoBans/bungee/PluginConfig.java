package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.BroadcastDestination;
import de.themoep.NeoBans.core.config.NeoPluginConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class PluginConfig extends YamlConfig implements NeoPluginConfig {
    private HashMap<String, List<String>>[] commandMap;

    /**
     * Load configuration from disk
     * @param path Path the the configuration file
     * @throws IOException
     */
    public PluginConfig(String path) throws IOException {
        super(path);
    }

    @Override
    public void createDefaultConfig() {
        cfg = ymlCfg.load(new InputStreamReader(NeoBans.getInstance().getResourceAsStream("config.yml")));

        save();
    }

    public String getLanguage() {
        return cfg.getString("language");
    }


    public Boolean getLatebind() {
        return cfg.getBoolean("commandlatebind");
    }

    public String[] getCommandAliases(String cmdname) {
        return cfg.getStringList("commandaliases." + cmdname.toLowerCase()).toArray(new String[0]);
    }

    public BroadcastDestination getBroadcastDestination(String type) {
        return BroadcastDestination.valueOf(cfg.getString("broadcast." + type.toLowerCase(), "SENDER").toUpperCase());
    }

}
