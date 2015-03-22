package de.themoep.NeoBans.bungee;

import com.sun.deploy.util.StringUtils;
import de.themoep.NeoBans.core.config.NeoLanguageConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
                new InputStreamReader(NeoBans.getInstance().getResourceAsStream("lang.en.yml"))
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
            for (String variable: replacements.keySet())
                string = string.replaceAll("%"+variable+"%", replacements.get(variable));
        return string;
    }

}