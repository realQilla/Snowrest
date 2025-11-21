package net.qilla.snowrest.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.CraftRegistry;

public final class RegistryIDs {
    public static final int[] AIR_STATE_IDS = new int[]{
            Block.getId(Blocks.AIR.defaultBlockState()),
            Block.getId(Blocks.CAVE_AIR.defaultBlockState()),
            Block.getId(Blocks.VOID_AIR.defaultBlockState())
    };
    public static final int[] SNOW_BLOCKS = {6718, 6719, 6720};
    public static final int[] WATER_BLOCKS = new int[]{
            86, 87, 88, 89, 90, 91, 92, 93
    };
    public static final int[] ICE_BLOCKS = new int[]{
            Block.getId(Blocks.ICE.defaultBlockState()),
            Block.getId(Blocks.BLUE_ICE.defaultBlockState()),
            //Block.getId(Blocks.PACKED_ICE.defaultBlockState())
    };
    public static final int SNOW_BIOME = CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.BIOME).getIdOrThrow(
            CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.BIOME).getValue(Biomes.SNOWY_PLAINS));
}