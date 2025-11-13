package net.qilla.snowrest.outdated;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.qilla.snowrest.util.PrefixedArray;

import java.util.ArrayList;
import java.util.List;

public final class DissectChunkPacket {

    private final FriendlyByteBuf byteBuf;
    private final int chunkX; //Integer
    private final int chunkZ; //Integer
    private final PrefixedArray<Heightmap> heightmaps; //Prefixed Array of Heightmaps
    private final int size; //Variable Integer
    private byte[] data;

    /**
     * Breaks down a chunk packet's byte buffer into readable pieces.
     * Pieces consist of:
     * - Chunk X
     * - Chunk Z
     * - Heightmap
     * - Data size
     * - Chunk Sections
     * @param byteBuf
     */
    public DissectChunkPacket(ByteBuf byteBuf) {
        this.byteBuf = new FriendlyByteBuf(byteBuf);

        this.chunkX = separateChunkX();
        this.chunkZ = separateChunkZ();
        this.heightmaps = separateHeightMap();
        this.size = separateDataSize();
        this.data = separateChunkSections();
    }

    private int separateChunkX() {
        return byteBuf.readInt();
    }

    private int separateChunkZ() {
        return byteBuf.readInt();
    }

    private PrefixedArray<Heightmap> separateHeightMap() {
        final int heightmapCount = byteBuf.readVarInt();

        if(heightmapCount == 0) return new PrefixedArray<>();

        final List<Heightmap> heightmaps = new ArrayList<>(heightmapCount);

        for(int i = 0; i < heightmapCount; i++) {
            final int type = byteBuf.readVarInt();
            final int length = byteBuf.readVarInt();
            final Long[] data = new Long[length];

            for(int j = 0; j < length; j++) {
                data[j] = byteBuf.readLong();
            }
            Heightmap heightmap = new Heightmap(type, new PrefixedArray<>(data));

            heightmaps.add(heightmap);
        }


        return new PrefixedArray<>(heightmaps);
    }

    private byte[] separateChunkSections() {
        byte[] data = new byte[size];
        for(int i = 0; i < size; i++) {
            data[i] = byteBuf.readByte();
        }

        return data;
    }

    private int separateDataSize() {
        return byteBuf.readVarInt();
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public PrefixedArray<Heightmap> heightmaps() {
        return heightmaps;
    }

    public int size() {
        return size;
    }

    public byte[] data() {
        return data;
    }
}
