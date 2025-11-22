package net.qilla.snowrest.file;

import net.qilla.snowrest.Snowrest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import java.io.*;
import java.nio.file.Path;

public final class SnowrestConfigFile {
    private static final Path DEFAULT_RESOURCE = Path.of("snowrest.config");
    private static final Path FILE_DIRECTORY = Path.of("snowrest.config");
    private static final Logger LOGGER = Snowrest.logger();

    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public SnowrestConfigFile(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_DIRECTORY.toString());

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(DEFAULT_RESOURCE.toString(), false);
        }
        this.config = this.getConfig();
    }

    public boolean save() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
        return false;
    }

    private @NotNull YamlConfiguration getConfig() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource(DEFAULT_RESOURCE.toString());

        if(defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));

            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
        }

        return config;
    }

    public @NotNull YamlConfiguration config() {
        return config;
    }
}
