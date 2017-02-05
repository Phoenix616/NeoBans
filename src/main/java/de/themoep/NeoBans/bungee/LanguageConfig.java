package de.themoep.NeoBans.bungee;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;
import de.themoep.bungeeplugin.FileConfiguration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

/**
 * By zaiyers
 * https://github.com/zaiyers/Channels/blob/master/src/main/java/net/zaiyers/Channels/config/LanguageConfig.java
 */
public class LanguageConfig extends FileConfiguration implements NeoLanguageConfig {

    public LanguageConfig(Plugin plugin, String configFilePath) throws IOException {
        super(plugin, configFilePath);
    }

    public String getTranslation(String key) {
        if (getString(key, "").isEmpty()) {
            return ChatColor.RED + "Unknown language key: " + ChatColor.YELLOW + key;
        } else {
            return ChatColor.translateAlternateColorCodes('&', getString(key));
        }
    }
    
    public String getTranslation(String key, String... replacements) {
        String string = getTranslation(key);

        // insert replacements
        if (replacements != null)
            for (int i = 0; i + 1 < replacements.length; i += 2)
                string = string.replace("%" + replacements[i] + "%", replacements[i + 1]);
        return string;
    }

}