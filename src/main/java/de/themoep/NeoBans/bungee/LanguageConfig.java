package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * By zaiyers
 * https://github.com/zaiyers/Channels/blob/master/src/main/java/net/zaiyers/Channels/config/LanguageConfig.java
 */
public class LanguageConfig extends YamlConfig implements NeoLanguageConfig {
    public LanguageConfig(String configFilePath) throws IOException {
        super(configFilePath);
    }

    public void createDefaultConfig() {
        // default is english
        cfg = ymlCfg.load(
                new InputStreamReader(NeoBans.getInstance().getResourceAsStream(configFile.getName()))
        );

        save();
    }
    
    public String getTranslation(String key) {
        if (cfg.getString(key, "").isEmpty()) {
            return ChatColor.RED + "Unknown language key: " + ChatColor.YELLOW + key;
        } else {
            return ChatColor.translateAlternateColorCodes('&', cfg.getString(key));
        }
    }
    
    public String getTranslation(String key, Map<String, String> replacements) {
        String string = getTranslation(key);

        // insert replacements
        if (replacements != null)
            for (Map.Entry<String, String> entry: replacements.entrySet())
                string = string.replace("%" + entry.getKey() + "%", entry.getValue());
        return string;
    }

}