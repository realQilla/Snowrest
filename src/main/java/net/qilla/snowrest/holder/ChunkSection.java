package net.qilla.snowrest.holder;

public interface ChunkSection {
    short blockCount();

    PaletteContainer blockStates();

    PaletteContainer biomes();

    void setBlockCount(short blockCount);

    void setBlockStates(PaletteContainer blockStates);

    void setBiomes(PaletteContainer biomes);
}
