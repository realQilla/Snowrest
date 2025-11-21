package net.qilla.snowrest;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.qilla.snowrest.commands.SnowrestCommand;
import net.qilla.snowrest.config.SnowrestConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class Snowrest extends JavaPlugin {
    private static Logger LOGGER;
    private ProtocolManager protocolManager;
    private SnowrestConfig config;

    @Override
    public void onLoad() {
        LOGGER = getSLF4JLogger();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.config = SnowrestConfig.ofDefault();
        PacketListeners.init(this);
    }

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            new SnowrestCommand(this, commands).register();
        });
    }

    public @NotNull ProtocolManager protocolManager() {
        if(protocolManager == null) throw new IllegalStateException("ProtocolManager not yet initialized");
        return protocolManager;
    }

    public @NotNull SnowrestConfig config() {
        if(config == null) throw new IllegalStateException("Config not yet initialized");
        return config;
    }

    public static @NotNull Logger logger() {
        if(LOGGER == null) throw new IllegalStateException("Logger not yet initialized");
        return LOGGER;
    }
}