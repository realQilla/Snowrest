package net.qilla.snowrest.config;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SnowrestConfig {
    void resetDefaults();

    void subscribe(ConfigSubscription subscription);

    boolean isEnabled();

    boolean isSnowing();

    boolean isSurfaceCovered();

    boolean isWaterFrozen();

    @NotNull List<BlockState> waterReplacement();

    @NotNull List<BlockState> surfaceReplacement();

    void setEnabled(boolean enabled);

    void setIsSnowing(boolean snowing);

    void setIsSurfaceCovered(boolean covered);

    void setIsWaterReplaced(boolean replaced);

    void setWaterReplacement(@NotNull List<BlockState> waterBlocks);

    void setSurfaceCover(@NotNull List<BlockState> snowBlocks);

    void addWaterReplacement(@NotNull BlockState block);

    void addSurfaceCover(@NotNull BlockState block);

    void removeWaterReplacement(@NotNull BlockState block);

    void removeSurfaceCover(@NotNull BlockState block);
}
