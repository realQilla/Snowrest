package net.qilla.snowrest.util;

import net.minecraft.world.level.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockConverter {
    public static int BlockStateToId(@NotNull BlockState blockState) {
        CraftBlockState craftBlockState = (CraftBlockState) blockState;

        return Block.getId(craftBlockState.getHandle());
    }

    public static @NotNull String BlockStateToString(@NotNull BlockState blockState) {
        CraftBlockState craftBlockState = (CraftBlockState) blockState;

        return craftBlockState.getBlockData().getAsString();
    }

    public static @Nullable BlockState StringToBlockState(@NotNull String stateID) {
        try {
            return Bukkit.getServer().createBlockData(stateID).createBlockState();
        } catch(IllegalArgumentException ignored) {
            return null;
        }
    }
}
