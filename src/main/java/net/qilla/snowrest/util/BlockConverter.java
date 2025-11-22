package net.qilla.snowrest.util;

import net.minecraft.world.level.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public final class BlockConverter {
    public static int BlockStateToId(@NotNull BlockState blockState) {
        CraftBlockState craftBlockState = (CraftBlockState) blockState;

        return Block.getId(craftBlockState.getHandle());
    }

    public static @NotNull List<String> BlockStatesToString(@NotNull List<BlockState> blockStates) {
        List<String> stateStrings = new ArrayList<>();

        for(BlockState state : blockStates) {
            stateStrings.add(state.getBlockData().getAsString());
        }

        return stateStrings;
    }

    public static @NotNull List<BlockState> StringToBlockState(@NotNull List<String> stateIDs) {
        List<BlockState> blockStates = new ArrayList<>();

        for(String id : stateIDs) {
            try {
                blockStates.add(Bukkit.getServer().createBlockData(id).createBlockState());
            } catch(IllegalArgumentException ignored) {}
        }

        return blockStates;
    }
}
