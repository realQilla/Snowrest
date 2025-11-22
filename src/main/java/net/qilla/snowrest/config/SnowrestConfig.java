package net.qilla.snowrest.config;

import net.qilla.snowrest.data.RegistryEntries;
import net.qilla.snowrest.file.SnowrestConfigFile;
import net.qilla.snowrest.util.BlockConverter;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class SnowrestConfig {
    private static final List<BlockState> DEFAULT_WATER_REPLACEMENT = new ArrayList<>(RegistryEntries.ICE_STATES);
    private static final List<BlockState> DEFAULT_SURFACE_REPLACEMENT = new ArrayList<>(RegistryEntries.SNOW_LAYER_STATES.subList(0, 3));

    private final List<ConfigSubscription> subscriptions = new ArrayList<>();

    private final SnowrestConfigFile configFile;
    private boolean isEnabled;
    private boolean isSnowing;
    private boolean isSurfaceCovered ;
    private boolean isWaterReplaced;
    private List<BlockState> waterReplacement;
    private List<BlockState> surfaceCover;

    public SnowrestConfig(@NotNull SnowrestConfigFile configFile) {
        this.configFile = configFile;
        this.isEnabled = configFile.config().getBoolean("global.enabled", false);
        this.isSnowing = configFile.config().getBoolean("settings.snowing", true);
        this.isSurfaceCovered = configFile.config().getBoolean("settings.surface.enabled", true);
        this.surfaceCover = getBlockState("settings.surface.blocks", DEFAULT_SURFACE_REPLACEMENT);
        this.isWaterReplaced = configFile.config().getBoolean("settings.water.enabled", true);
        this.waterReplacement = getBlockState("settings.water.blocks", DEFAULT_WATER_REPLACEMENT);
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<BlockState> getBlockState(@NotNull String path, @NotNull List<BlockState> defaultBlocks) {
        return BlockConverter.StringToBlockState((List<String>) configFile.config().getList(path, defaultBlocks));
    }

    public void resetDefaults() {
        configFile.config().set("settings.surface.blocks", BlockConverter.BlockStatesToString(DEFAULT_SURFACE_REPLACEMENT));
        configFile.config().set("settings.water.blocks", BlockConverter.BlockStatesToString(DEFAULT_WATER_REPLACEMENT));
        this.waterReplacement = new ArrayList<>(DEFAULT_WATER_REPLACEMENT);
        this.surfaceCover = new ArrayList<>(DEFAULT_SURFACE_REPLACEMENT);
        notifySubscribers();
        configFile.save();
    }

    public void subscribe(ConfigSubscription subscription) {
        subscriptions.add(subscription);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isSnowing() {
        return isSnowing;
    }

    public boolean isSurfaceCovered() {
        return isSurfaceCovered;
    }

    public boolean isWaterFrozen() {
        return isWaterReplaced;
    }

    public @NotNull List<BlockState> waterReplacement() {
        return Collections.unmodifiableList(waterReplacement);
    }

    public @NotNull List<BlockState> surfaceReplacement() {
        return Collections.unmodifiableList(surfaceCover);
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        notifySubscribers();
        configFile.config().set("global.enabled", enabled);
        configFile.save();
    }

    public void setIsSnowing(boolean snowing) {
        this.isSnowing = snowing;
        notifySubscribers();
        configFile.config().set("settings.snowing", snowing);
        configFile.save();
    }

    public void setIsSurfaceCovered(boolean covered) {
        this.isSurfaceCovered = covered;
        notifySubscribers();
        configFile.config().set("settings.surface.enabled", covered);
        configFile.save();
    }

    public void setIsWaterReplaced(boolean replaced) {
        this.isWaterReplaced = replaced;
        notifySubscribers();
        configFile.config().set("settings.water.enabled", replaced);
        configFile.save();
    }

    public void setWaterReplacement(@NotNull List<BlockState> waterBlocks) {
        this.waterReplacement = waterBlocks;
        notifySubscribers();
        configFile.config().set("settings.water.blocks", BlockConverter.BlockStatesToString(waterReplacement));
        configFile.save();
    }

    public void setSurfaceCover(@NotNull List<BlockState> snowBlocks) {
        this.surfaceCover = snowBlocks;
        notifySubscribers();
        configFile.config().set("settings.surface.blocks", BlockConverter.BlockStatesToString(surfaceCover));
        configFile.save();
    }

    public void addWaterReplacement(@NotNull BlockState block) {
        this.waterReplacement.add(block);
        notifySubscribers();
        configFile.config().set("settings.water.blocks", BlockConverter.BlockStatesToString(waterReplacement));
        configFile.save();
    }

    public void addSurfaceCover(@NotNull BlockState block) {
        this.surfaceCover.add(block);
        notifySubscribers();
        configFile.config().set("settings.surface.blocks", BlockConverter.BlockStatesToString(surfaceCover));
        configFile.save();
    }

    public void removeWaterReplacement(@NotNull BlockState block) {
        this.waterReplacement.remove(block);
        notifySubscribers();
        configFile.config().set("settings.water.blocks", BlockConverter.BlockStatesToString(waterReplacement));
        configFile.save();
    }

    public void removeSurfaceCover(@NotNull BlockState block) {
        this.surfaceCover.remove(block);
        notifySubscribers();
        configFile.config().set("settings.surface.blocks", BlockConverter.BlockStatesToString(surfaceCover));
        configFile.save();
    }

    private void notifySubscribers() {
        subscriptions.forEach(subscription -> subscription.onChange(this));
    }
}
