package net.qilla.snowrest.painter;

import net.qilla.snowrest.bitstorage.BitStorage;
import net.qilla.snowrest.data.DataPersistent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HeightmapEditor {
    private int[] heightmap;

    private HeightmapEditor(long[] data) {
        BitStorage bitStorage = BitStorage.ofPacked(DataPersistent.HEIGHTMAP_BPE, data.length, data);
        int[] rawHeights = bitStorage.unpack(DataPersistent.BLOCKS_PER_HEIGHTMAP);
        int[] heightmap = new int[DataPersistent.BLOCKS_PER_HEIGHTMAP];

        for(int index = 0; index < DataPersistent.BLOCKS_PER_HEIGHTMAP; index++) {
            int x = index & 15;
            int z = index >> 4;
            int y = rawHeights[index];

            heightmap[index] = ((y << 8) | (z << 4) | x);;
        }

        this.heightmap = heightmap;
    }

    private HeightmapEditor(int[] heightmap) {
        this.heightmap = heightmap;
    }

    public static HeightmapEditor of(long[] data) {
        return new HeightmapEditor(data);
    }

    public HeightmapEditor offsetY(int offset) {
        for(int i = 0; i < heightmap.length; i++) {
            int y = heightmap[i] >> 8;

            heightmap[i] = (y + offset) << 8 | (heightmap[i] & 255);
        }

        return this;
    }

    /**
     * Stacks the heightmap to include the specified amount of layers above the original amount.
     * Calling this method more than once is extremely inefficient and will result in duplicate values
     * @param amount Number of blocks to include in heightmap above original layer
     */

    public HeightmapEditor stack(int amount) {
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
        return this;
    }

    public int[][] build() {
        List<Integer>[] temp = new List[DataPersistent.SECTIONS_PER_CHUNK];

        for(int i = 0; i < DataPersistent.SECTIONS_PER_CHUNK; i++) {
            temp[i] = new ArrayList<>();
        }

        for(int entry : heightmap) {
            int x = entry & 15;
            int z = (entry >> 4) & 15;
            int y = entry >> 8;
            int sectionIndex = y >> 4;

            temp[sectionIndex].add(((y & 15) << 8) | (z << 4) | x);
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

    public HeightmapEditor copy() {
        return new HeightmapEditor(Arrays.copyOf(this.heightmap, this.heightmap.length));
    }
}
