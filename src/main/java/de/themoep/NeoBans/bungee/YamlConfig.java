package de.themoep.NeoBans.bungee;

/*
 * By zaiyers
 * https://github.com/zaiyers/Channels/blob/master/src/main/java/net/zaiyers/Channels/config/YamlConfig.java
 */

import java.io.File;
import java.io.IOException;

import de.themoep.NeoBans.core.config.NeoConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public abstract class YamlConfig implements NeoConfig {
    protected Configuration cfg;
    protected final static ConfigurationProvider ymlCfg = ConfigurationProvider.getProvider( YamlConfiguration.class );

    protected File configFile;

    /**
     * read configuration into memory
     * @param configFilePath
     * @throws IOException
     */
    public YamlConfig(String configFilePath) throws IOException {
        configFile = new File(configFilePath);

        if (!configFile.exists()) {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            configFile.createNewFile();
            cfg = ymlCfg.load(configFile);

            createDefaultConfig();
        } else {
            cfg = ymlCfg.load(configFile);
        }
    }

    /**
     * save configuration to disk
     */
    public void save() {
        try {
            ymlCfg.save(cfg, configFile);
        } catch (IOException e) {
            NeoBans.getInstance().getLogger().severe("Unable to save configuration at " + configFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
    
    /**
     * deletes configuration file
     */
    public void removeConfig() {
        configFile.delete();
    }

    public String getString(String path) {
        return cfg.getString(path);
    }

    public String getString(String path, String def) {
        return cfg.getString(path, def);
    }
}