package net.qilla.snowrest;

import net.qilla.snowrest.data.DataPersistent;
import net.qilla.snowrest.data.RegistryEntries;

import java.util.HashSet;
import java.util.Set;

public final class ChunkSection {

    private short blockCount; //Short
    private PaletteContainer blockStates;
    private PaletteContainer biomes;

    public ChunkSection(short blockCount, PaletteContainer blockStates, PaletteContainer biomes) {
        this.blockCount = blockCount;
        this.blockStates = blockStates;
        this.biomes = biomes;
    }

    public short blockCount() {
        return blockCount;
    }

    public PaletteContainer blockStates() {
        return blockStates;
    }

    public PaletteContainer biomes() {
        return biomes;
    }

    public void setBlockCount(short blockCount) {
        this.blockCount = blockCount;
    }

    public void setBlockStates(PaletteContainer blockStates) {
        this.blockStates = blockStates;

        int[] unpacked = blockStates.data().unpacked();
        short blockCount = 0;

        Set<Integer> airIndexes = new HashSet<>();
        for(int paletteIndex = 0; paletteIndex < blockStates.paletteLength(); paletteIndex++) {
            for(int air : RegistryEntries.AIR_IDS) {
                if(blockStates.palette()[paletteIndex] == air) {
                    airIndexes.add(air);
                    break;
                }
            }
        }

        if(airIndexes.isEmpty()) {
            this.blockCount = DataPersistent.BLOCKS_PER_SECTION;
            return;
        }

        for(int index : unpacked) {
            for(int air : airIndexes) {
                if(index == air) continue;
                blockCount++;
                break;
            }
        }

        this.blockCount = blockCount;
    }

    public void setBiomes(PaletteContainer biomes) {
        this.biomes = biomes;
    }
}
