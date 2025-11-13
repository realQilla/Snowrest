package net.qilla.snowrest;

public final class ChunkSection {
    private short blockCount; //Short
    private PalettedContainer blockStates;
    private PalettedContainer biomes;

    public ChunkSection(short blockCount, PalettedContainer blockStates, PalettedContainer biomes) {
        this.blockCount = blockCount;
        this.blockStates = blockStates;
        this.biomes = biomes;
    }

    public short blockCount() {
        return blockCount;
    }

    public PalettedContainer blockStates() {
        return blockStates;
    }

    public PalettedContainer biomes() {
        return biomes;
    }

    public void setBlockCount(short blockCount) {
        this.blockCount = blockCount;
    }

    public void setBlockStates(PalettedContainer blockStates) {
        this.blockStates = blockStates;
    }

    public void setBiomes(PalettedContainer biomes) {
        this.biomes = biomes;
    }
}
