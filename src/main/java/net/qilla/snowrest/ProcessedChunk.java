package net.qilla.snowrest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.data.DataMasks;
import net.qilla.snowrest.data.DataPersistent;
import org.jetbrains.annotations.NotNull;

public final class ProcessedChunk {
    private final FriendlyByteBuf byteBuf;
    private final ChunkSection[] sections;

    private ProcessedChunk(@NotNull FriendlyByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.sections = separateSections();
    }

    public static ProcessedChunk of(ByteBuf byteBuf) {
        return new ProcessedChunk(new FriendlyByteBuf(byteBuf));
    }

    public static ProcessedChunk of(FriendlyByteBuf byteBuf) {
        return new ProcessedChunk(byteBuf);
    }

    private ChunkSection[] separateSections() {
        ChunkSection[] chunkSections = new ChunkSection[DataPersistent.SECTIONS_PER_CHUNK];

        for(int i = 0; i < DataPersistent.SECTIONS_PER_CHUNK; i++) {
            short blockCount = byteBuf.readShort();
            PaletteContainer blockStates = this.separateSectionBlockStates();
            PaletteContainer biomes = this.separateSectionBiomes();

            chunkSections[i] = new ChunkSection(blockCount, blockStates, biomes);
        }

        return chunkSections;
    }

    private PaletteContainer separateSectionBlockStates() {
        int bpe = byteBuf.readUnsignedByte();
        PaletteContainer container;

        if(bpe == 0) {
            int value = byteBuf.readVarInt();

            container = PaletteContainer.of(new int[]{value}, ProcessedBits.empty());
        } else {
            int entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
            int packedLength = (DataPersistent.BLOCKS_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
            long [] packed = new long[packedLength];

            if(bpe <= 8) {
                bpe = Math.max(bpe, 4);

                int paletteLength = byteBuf.readVarInt();
                int[] palette = new int[paletteLength];

                //Read each blockstate part of this palette.
                if(paletteLength > 0) {
                    for(int i = 0; i < paletteLength; i++) {
                        palette[i] = byteBuf.readVarInt();
                    }
                }

                //Read block data for this chunk section. Type is indirect, so block data must refer it's to palette.
                for(int i = 0; i < packedLength; i++) {
                    packed[i] = byteBuf.readLong();
                }

                container = PaletteContainer.of(palette, ProcessedBits.packed(bpe, packed, DataPersistent.BLOCKS_PER_SECTION));
            } else {
                //Read block data for this chunk section. Type is direct, so blockstate id's are directly encoded right into the data.
                for(int i = 0; i < packedLength; i++) {
                    packed[i] = byteBuf.readLong();
                }

                container = PaletteContainer.of(new int[0], ProcessedBits.packed(bpe, packed, DataPersistent.BLOCKS_PER_SECTION));
            }
        }

        return container;
    }

    private PaletteContainer separateSectionBiomes() {
        int bpe = byteBuf.readUnsignedByte();
        PaletteContainer container;

        if(bpe == 0) {
            int value = byteBuf.readVarInt();

            container = PaletteContainer.of(new int[]{value}, ProcessedBits.empty());
        } else {
            int entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
            int packedLength = (DataPersistent.BIOMES_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
            long[] packed = new long[packedLength];

            if(bpe <= 3) {
                int paletteLength = byteBuf.readVarInt();
                int[] palette = new int[paletteLength];

                //Read each biome part of this palette.
                if(paletteLength > 0) {
                    for(int i = 0; i < paletteLength; i++) {
                        palette[i] = byteBuf.readVarInt();
                    }
                }

                //Read biome data for this chunk section. Type is indirect, so biome data must refer it's to palette.
                for(int i = 0; i < packedLength; i++) {
                    packed[i] = byteBuf.readLong();
                }

                container = PaletteContainer.of(palette, ProcessedBits.packed(bpe, packed, DataPersistent.BIOMES_PER_SECTION));
            } else {
                //Read biome data for this chunk section. Type is direct, so biome id's are directly encoded right into the data.
                for(int i = 0; i < packedLength; i++) {
                    packed[i] = byteBuf.readLong();
                }

                container = PaletteContainer.of(new int[0], ProcessedBits.packed(bpe, packed, DataPersistent.BIOMES_PER_SECTION));
            }
        }

        return container;
    }

    public byte[] buildBuffer() {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

        try {
            for(ChunkSection section : sections) {
                byteBuf.writeShort(section.blockCount());

                writeBlockStates(byteBuf, section.blockStates());
                writeBiomes(byteBuf, section.biomes());
            }
            byte[] buffer = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(buffer);

            return buffer;
        } finally {
            byteBuf.release();
        }
    }

    private void writeBlockStates(FriendlyByteBuf byteBuf, PaletteContainer blockStates) {
        ProcessedBits data = blockStates.data();

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
                for(long entry : data.pack()) {
                    byteBuf.writeLong(entry);
                }
            }
        }
    }

    private void writeBiomes(FriendlyByteBuf byteBuf, PaletteContainer blockStates) {
        ProcessedBits data = blockStates.data();

        byteBuf.writeByte(data.bpe() & DataMasks.UNSIGNED_BYTE);

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
                for(long entry : data.pack()) {
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
