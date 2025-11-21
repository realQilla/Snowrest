package net.qilla.snowrest.config;

import net.qilla.snowrest.data.RegistryIDs;
import net.qilla.snowrest.file.SnowrestConfigFile;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public final class SnowrestConfig {

    private final List<ConfigSubscription> subscriptions = new ArrayList<>();

    private boolean isEnabled;
    private boolean isSnowing;
    private boolean isSurfaceCovered;
    private boolean isWaterReplaced;
    private Set<Integer> waterReplacement;
    private Set<Integer> surfaceCover;

    public SnowrestConfig(SnowrestConfigFile configFile) {

    }

    private SnowrestConfig() {
        this.isEnabled = false;
        this.isSnowing = true;
        this.isSurfaceCovered = true;
        this.isWaterReplaced = true;
        this.waterReplacement = Arrays.stream(RegistryIDs.ICE_BLOCKS).boxed().collect(Collectors.toSet());
        this.surfaceCover = Arrays.stream(RegistryIDs.SNOW_BLOCKS).boxed().collect(Collectors.toSet());
    }

    public static SnowrestConfig ofDefault() {
        return new SnowrestConfig();
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

    public @NotNull Set<Integer> waterReplacement() {
        return Collections.unmodifiableSet(waterReplacement);
    }

    public @NotNull Set<Integer> surfaceReplacement() {
        return Collections.unmodifiableSet(surfaceCover);
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        notifySubscribers();
    }

    public void setIsSnowing(boolean snowing) {
        this.isSnowing = snowing;
        notifySubscribers();
    }

    public void setIsSurfaceCovered(boolean covered) {
        this.isSurfaceCovered = covered;
        notifySubscribers();
    }

    public void setIsWaterReplaced(boolean replaced) {
        this.isWaterReplaced = replaced;
        notifySubscribers();
    }

    public void setWaterReplacement(@NotNull Set<Integer> waterBlocks) {
        this.waterReplacement = waterBlocks;
        notifySubscribers();
    }

    public void setSurfaceCover(@NotNull Set<Integer> snowBlocks) {
        this.surfaceCover = snowBlocks;
        notifySubscribers();
    }

    private void notifySubscribers() {
        subscriptions.forEach(subscription -> subscription.onChange(this));
    }
}
