package net.qilla.snowrest.outdated;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.bitstorage.ProcessedBitsImpl;
import net.qilla.snowrest.data.DataPersistent;

import java.util.ArrayList;
import java.util.List;

public final class SectionUtil {
    public static int[][] getSectionPositionsFromHeightmap(long[] heightMapData, int yOffset) {
        ProcessedBits heightmapData = ProcessedBits.packed(DataPersistent.HEIGHTMAP_BPE, heightMapData, DataPersistent.BLOCKS_PER_HEIGHTMAP);
        int[] heights = heightmapData.unpacked();
        List<List<Integer>> sectionPositions = new ArrayList<>(24);

        while(sectionPositions.size() < 24) {
            sectionPositions.add(new ArrayList<>());
        }

        for(int index = 0; index < DataPersistent.BLOCKS_PER_HEIGHTMAP; index++) {
            int x = index & 15;
            int z = index >> 4;
            int y = heights[index] + yOffset;

            int sectionIndex = y >> 4;

            sectionPositions.get(sectionIndex).add(((y & 15) << 8) | (z << 4) | x);
        }

        int[][] sectionPositionsArray = new int[sectionPositions.size()][];

        for(int i = 0; i < sectionPositions.size(); i++) {
            sectionPositionsArray[i] = sectionPositions.get(i).stream().mapToInt(Integer::intValue).toArray();
        }

        return sectionPositionsArray;
    }

    public static BlockPosition[] heightmapPositionsToSectionRelative(int[] positions, int yOffset) {
        BlockPosition[] relativePositions = new BlockPosition[DataPersistent.BLOCKS_PER_HEIGHTMAP];

        for(int index = 0; index < DataPersistent.BLOCKS_PER_HEIGHTMAP; index++) {
            int x = index & 15;
            int z = index >> 4;
            int y = positions[index] + yOffset;

            relativePositions[index] = Position.block(x, y, z);
        }

        return relativePositions;
    }
}
