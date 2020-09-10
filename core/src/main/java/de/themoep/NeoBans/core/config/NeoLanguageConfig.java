package de.themoep.NeoBans.core.config;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public interface NeoLanguageConfig {

    /**
     * Get translation from language config
     * @param key The language key to get from the config
     * @return The translated string
     */
    String getTranslation(String key);

    /**
     * Get translation from language config and insert replacements
     * @param key The language key to get from the config
     * @param replacements The replacements, 2n = key, 2n + 1 = value
     * @return The translated and replaced string
     */
    String getTranslation(String key, String... replacements);
}
