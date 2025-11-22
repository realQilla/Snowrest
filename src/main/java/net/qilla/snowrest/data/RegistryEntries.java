package net.qilla.snowrest.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import net.qilla.snowrest.util.BlockConverter;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftRegistry;
import java.util.List;

public final class RegistryEntries {
    public static final List<BlockState> AIR_STATES = BlockType.AIR.createBlockDataStates().stream().map(BlockData::createBlockState).toList();
    public static final List<BlockState> SNOW_LAYER_STATES = BlockType.SNOW.createBlockDataStates().stream().map(BlockData::createBlockState).toList();
    public static final List<BlockState> WATER_STATES = BlockType.WATER.createBlockDataStates().stream().map(BlockData::createBlockState).toList();
    public static final List<BlockState> ICE_STATES = List.of(
            BlockType.ICE.createBlockData().createBlockState(),
            BlockType.PACKED_ICE.createBlockData().createBlockState(),
            BlockType.BLUE_ICE.createBlockData().createBlockState()
    );
    public static final int SNOW_BIOME = CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.BIOME).getIdOrThrow(
            CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.BIOME).getValue(Biomes.SNOWY_PLAINS));

    public static final int[] AIR_IDS = RegistryEntries.AIR_STATES.stream().map(BlockConverter::BlockStateToId).mapToInt(Integer::intValue).toArray();
    public static final int[] WATER_IDS = RegistryEntries.WATER_STATES.stream().map(BlockConverter::BlockStateToId).mapToInt(Integer::intValue).toArray();
    public static final int[] ICE_IDS = RegistryEntries.ICE_STATES.stream().map(BlockConverter::BlockStateToId).mapToInt(Integer::intValue).toArray();
}