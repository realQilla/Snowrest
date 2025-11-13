package net.qilla.snowrest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.qilla.snowrest.bitstorage.BaseBitStorage;
import net.qilla.snowrest.data.DataMasks;
import net.qilla.snowrest.data.DataPersistent;

public final class DissectChunkData {
    private final FriendlyByteBuf byteBuf;
    private final ChunkSection[] sections;

    public DissectChunkData(ByteBuf byteBuf) {
        this.byteBuf = new FriendlyByteBuf(byteBuf);
        this.sections = separateSections();
    }

    private ChunkSection[] separateSections() {
        ChunkSection[] chunkSections = new ChunkSection[DataPersistent.SECTIONS_PER_CHUNK];

        for(int i = 0; i < DataPersistent.SECTIONS_PER_CHUNK; i ++) {
            short blockCount = byteBuf.readShort();
            PalettedContainer blockStates =  separateSectionBlockStates();
            PalettedContainer biomes = separateSectionBiomes();

            chunkSections[i] = new ChunkSection(blockCount, blockStates, biomes);
        }

        return chunkSections;
    }

    private PalettedContainer separateSectionBlockStates() {
        int bpe = byteBuf.readUnsignedByte();
        PalettedContainer container;

        if(bpe == 0) {
            int value = byteBuf.readVarInt();

            container = PalettedContainer.ofSingle(value);
        } else {
            int entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
            int dataArrayLength = (DataPersistent.BLOCKS_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
            long [] data = new long[dataArrayLength];

            if(bpe <= 8) {
                bpe = Math.max(bpe, 4);

                int paletteLength = byteBuf.readVarInt();
                int[] palette = new int[paletteLength];

                //Read each blockstate apart of this palette.
                if(paletteLength > 0) {
                    for(int i = 0; i < paletteLength; i++) {
                        palette[i] = byteBuf.readVarInt();
                    }
                }

                //Read block data for this chunk section. Type is indirect, so block data must refer it's to palette.
                for(int i = 0; i < dataArrayLength; i++) {
                    data[i] = byteBuf.readLong();
                }

                container = PalettedContainer.ofIndirect(bpe, palette, data);
            } else {
                //Read block data for this chunk section. Type is direct, so blockstate id's are directly encoded right into the data.
                for(int i = 0; i < dataArrayLength; i++) {
                    data[i] = byteBuf.readLong();
                }

                container = PalettedContainer.ofDirect(bpe, data);
            }
        }

        return container;
    }

    private PalettedContainer separateSectionBiomes() {
        int bpe = byteBuf.readUnsignedByte();
        PalettedContainer container;

        if(bpe == 0) {
            int value = byteBuf.readVarInt();

            container = PalettedContainer.ofSingle(value);
        } else {
            int entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
            int dataArrayLength = (DataPersistent.BIOMES_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
            long [] data = new long[dataArrayLength];

            if(bpe <= 3) {
                int paletteLength = byteBuf.readVarInt();
                int[] palette = new int[paletteLength];

                //Read each biome apart of this palette.
                if(paletteLength > 0) {
                    for(int i = 0; i < paletteLength; i++) {
                        palette[i] = byteBuf.readVarInt();
                    }
                }

                //Read biome data for this chunk section. Type is indirect, so biome data must refer it's to palette.
                for(int i = 0; i < dataArrayLength; i++) {
                    data[i] = byteBuf.readLong();
                }

                container = PalettedContainer.ofIndirect(bpe, palette, data);
            } else {
                //Read biome data for this chunk section. Type is direct, so biome id's are directly encoded right into the data.
                for(int i = 0; i < dataArrayLength; i++) {
                    data[i] = byteBuf.readLong();
                }

                container = PalettedContainer.ofDirect(bpe, data);
            }
        }

        return container;
    }

    public byte[] buildBuffer() {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

        for(ChunkSection section : sections) {
            byteBuf.writeShort(section.blockCount());

            writeBlockStates(byteBuf, section.blockStates());
            writeBiomes(byteBuf, section.biomes());
        }
        return byteBuf.array();
    }

    private void writeBlockStates(FriendlyByteBuf byteBuf, PalettedContainer blockStates) {
        BaseBitStorage data = blockStates.data();

        byteBuf.writeByte(data.bpe() & DataMasks.UNSIGNED_BYTE);

        switch(data.bpe()) {
            case 0: {
                byteBuf.writeVarInt(blockStates.palette()[0]);
                break;
            }
            case 1, 2, 3, 4, 5, 6, 7, 8: {
                byteBuf.writeVarInt(blockStates.palette().length);

                for(int i = 0; i < blockStates.palette().length; i++) {
                    byteBuf.writeVarInt(blockStates.palette()[i]);
                }
            }
            default: {
                for(long entry : data.raw()) {
                    byteBuf.writeLong(entry);
                }
            }
        }
    }

    private void writeBiomes(FriendlyByteBuf byteBuf, PalettedContainer blockStates) {
        BaseBitStorage data = blockStates.data();

        byteBuf.writeByte(data.bpe() & 0xFF);

        switch(data.bpe()) {
            case 0: {
                byteBuf.writeVarInt(blockStates.palette()[0]);
                break;
            }
            case 1, 2, 3: {
                byteBuf.writeVarInt(blockStates.palette().length);

                for(int i = 0; i < blockStates.palette().length; i++) {
                    byteBuf.writeVarInt(blockStates.palette()[i]);
                }
            }
            default: {
                for(long entry : data.raw()) {
                    byteBuf.writeLong(entry);
                }
            }
        }
    }

    public ChunkSection[] sections() {
        return this.sections;
    }

    public int dataSize() {
        return this.byteBuf.readableBytes();
    }
}
