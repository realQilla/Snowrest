package net.qilla.snowrest.editor;

import net.qilla.snowrest.ChunkSection;
import net.qilla.snowrest.PaletteContainer;
import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.data.DataPersistent;
import net.qilla.snowrest.EntryHolder;
import net.qilla.snowrest.util.PaletteUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HeightmapEditor {
    private int[] heightmap;

    private HeightmapEditor(long[] data, int yOffset) {
        ProcessedBits processed = ProcessedBits.packed(DataPersistent.HEIGHTMAP_BPE, data, DataPersistent.BLOCKS_PER_HEIGHTMAP);
        int[] rawHeights = processed.unpacked();
        int[] heightmap = new int[DataPersistent.BLOCKS_PER_HEIGHTMAP];

        for(int index = 0; index < DataPersistent.BLOCKS_PER_HEIGHTMAP; index++) {
            int x = index & 15;
            int z = index >> 4;
            int height = rawHeights[index] + yOffset;

            heightmap[index] = (height << 8) | (z << 4) | x;
        }

        this.heightmap = heightmap;
    }

    private HeightmapEditor(int[] heightmap) {
        this.heightmap = heightmap;
    }

    public static HeightmapEditor of(long[] packedHeightmap) {
        return new HeightmapEditor(packedHeightmap, 0);
    }

    public static HeightmapEditor of(long[] packedHeightmap, int yOffset) {
        return new HeightmapEditor(packedHeightmap, yOffset);
    }

    public static HeightmapEditor of(int[] heightmap) {
        return new HeightmapEditor(heightmap);
    }

    public HeightmapEditor[] separate(ChunkSection[] sections, EntryHolder holder) {
        int[] firstHeightmap = new int[DataPersistent.BLOCKS_PER_HEIGHTMAP];
        int[] secondHeightmap = new int[DataPersistent.BLOCKS_PER_HEIGHTMAP];
        int firstCount = 0;
        int secondCount = 0;

        for(int i = 0; i < DataPersistent.BLOCKS_PER_HEIGHTMAP; i++) {
            boolean match = false;
            int heightmapValue = heightmap[i];
            int x = heightmapValue & 15;
            int z = (heightmapValue >> 4) & 15;
            int height = heightmapValue >> 8;
            int y = height & 15;
            int sectionIndex = height >> 4;
            int blockIndex = (y << 8) | (z << 4) | x;

            ChunkSection section = sections[sectionIndex];
            PaletteContainer container = section.blockStates();
            int[] paletteIndexes = PaletteUtil.getIndexes(holder.entryIds(), container.palette());
            int[] positions = container.data().unpacked();
            int blockPaletteIndex = positions[blockIndex];

            for(int paletteIndex : paletteIndexes) {
                if(blockPaletteIndex == paletteIndex) {
                    match = true;
                    break;
                }
            }

            if(match) firstHeightmap[firstCount++] = heightmapValue;
            else secondHeightmap[secondCount++] = heightmapValue;
        }

        return new HeightmapEditor[]{
                new HeightmapEditor(Arrays.copyOf(firstHeightmap, firstCount)),
                new HeightmapEditor(Arrays.copyOf(secondHeightmap, secondCount))
        };
    }

    public void offsetY(int offset) {
        for(int i = 0; i < heightmap.length; i++) {
            int y = heightmap[i] >> 8;
            int newY = y + offset;

            if(newY < -64 || newY > 320) {
                throw new IllegalArgumentException("Y offset results in invalid height: " + newY);
            }

            heightmap[i] = (newY << 8) | (heightmap[i] & 255);
        }
    }


    /**
     * Stacks the heightmap veritically to include positions above
     * Calling this method more than once is extremely inefficient and will result in duplicate values
     * @param amount Number of blocks to include in heightmap above original layer
     */

    public void stack(int amount) {
        int[] stackedHeightmap = new int[heightmap.length * (amount + 1)];
        int index = 0;

        for(int entry: heightmap) {
            int x = entry & 15;
            int z = (entry >> 4) & 15;
            int y = entry >> 8;

            for(int offset = 0; offset <= amount; offset++) {
                int newY = y + offset;
                stackedHeightmap[index++] = (newY << 8) | (z << 4) | x;
            }
        }

        this.heightmap = stackedHeightmap;
    }

    public int[][] build() {
        List<Integer>[] temp = new List[DataPersistent.SECTIONS_PER_CHUNK];

        for(int i = 0; i < DataPersistent.SECTIONS_PER_CHUNK; i++) {
            temp[i] = new ArrayList<>();
        }

        for(int entry : heightmap) {
            int x = entry & 15;
            int z = (entry >> 4) & 15;
            int height = entry >> 8;
            int y = height & 15;
            int sectionIndex = height >> 4;

            temp[sectionIndex].add((y << 8) | (z << 4) | x);
        }
        int[][] built = new int[DataPersistent.SECTIONS_PER_CHUNK][];

        for (int i = 0; i < DataPersistent.SECTIONS_PER_CHUNK; i++) {
            List<Integer> list = temp[i];
            int size = list.size();
            int[] arr = new int[size];
            for (int j = 0; j < size; j++) {
                arr[j] = list.get(j);
            }
            built[i] = arr;
        }
        return built;
    }

    public int[] heightmap() {
        return heightmap;
    }

    public HeightmapEditor copy() {
        return new HeightmapEditor(this.heightmap.clone());
    }
}
