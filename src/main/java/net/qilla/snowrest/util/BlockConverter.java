package net.qilla.snowrest.util;

import net.minecraft.world.level.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public final class BlockConverter {
    /**
     * Converts the given blockstate to use Minecraft's registry ID for that blockstate.
     * Note: Blockstate IDs are unstable between versions and should be recalculated.
     * @param blockState A single blockstate
     * @return Registry ID
     */
    public static int BlockStateToId(@NotNull BlockState blockState) {
        CraftBlockState craftBlockState = (CraftBlockState) blockState;

        return Block.getId(craftBlockState.getHandle());
    }

    /**
     * Converts the given blockstate list to use Minecraft's registry ID for the given blockstates.
     * Note: Blockstate IDs are unstable between versions and should be recalculated.
     * @param blockStates A single blockstate
     * @return Registry ID
     */
    public static int[] BlockStateToId(@NotNull List<BlockState> blockStates) {
        int[] ids = new int[blockStates.size()];

        for(int i = 0; i < blockStates.size(); i++) {
            ids[i] = BlockStateToId(blockStates.get(i));
        }

        return ids;
    }

    /**
     * Converts a blockstate array to an array of strings that can more easily be used for storage. This string
     * can be decrypted using {@link Bukkit#getServer()#createBlockData(int)}
     * @param blockStates An array of blockstates
     * @return An array of encodable strings
     */
    public static @NotNull List<String> BlockStatesToString(@NotNull List<BlockState> blockStates) {
        List<String> stateStrings = new ArrayList<>();

        for(BlockState state : blockStates) {
            stateStrings.add(state.getBlockData().getAsString());
        }

        return stateStrings;
    }

    /**
     * Converts a BlockState id array to a BlockState array(if possible). This string
     * @param stateIDs An array of blockstate IDs
     * @return An array of found blockstates
     */
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
