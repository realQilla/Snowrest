package net.qilla.snowrest.bitstorage;

import net.minecraft.util.Mth;
import net.qilla.snowrest.data.DataPersistent;
import org.jetbrains.annotations.NotNull;

public final class BitStorage implements BaseBitStorage {
    private final int bpe; //Encoded as unsinged Byte
    private final int size;
    private final long[] data; //Encoded as an array of Long
    private final int mask;
    private final int entriesPerLong;

    private BitStorage(int bpe, int dataSize, long[] data) {
        this.bpe = bpe;
        this.size = dataSize;
        this.data = data;
        this.mask = (1 << bpe) - 1;
        this.entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
    }

    private BitStorage(int bpe, int dataSize, int[] data) {
        this.bpe = bpe;
        this.mask = (1 << bpe) - 1;
        this.entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;

        int finalLength = (dataSize + entriesPerLong - 1) / entriesPerLong;
        long[] processedData = new long[finalLength];

        int idx = 0;
        for(int i = 0; i < processedData.length; i++) {
            long entry = 0;

            for(int j = 0; j < entriesPerLong && idx < data.length; j++, idx++) {
                entry |= ((long) (data[idx]) << (j * bpe));
            }
            processedData[i] = entry;
        }

        this.size = finalLength;
        this.data = processedData;
    }

    private BitStorage() {
        this.bpe = 0;
        this.size = 0;
        this.data = new long[0];
        this.mask = 0;
        this.entriesPerLong = 0;
    }

    public static @NotNull BitStorage ofPacked(int bpe, int size, long[] data) {
        if(size == 0 || bpe == 0 || data.length == 0) return new BitStorage();

        if(size != data.length) throw new IllegalArgumentException("Data size must match size parameter.");

        return new BitStorage(bpe, size, data);
    }

    public static @NotNull BitStorage ofUnpacked(int bpe, int size, int[] data) {
        if(size == 0 || bpe == 0 || data.length == 0) return new BitStorage();

        if(size != data.length) throw new IllegalArgumentException("Data size must match size parameter.");

        return new BitStorage(bpe, size, data);
    }

    public static int normalizeBlockStateBpe(int bpe) {
        if(bpe == 0) return 0;
        if(bpe <= 8) {
            bpe = Math.max(bpe, 4);
        } else {
            bpe = Math.max(Math.min(bpe, 31), 15);
        }
        return bpe;
    }

    public static int normalizeBiomeBpe(int bpe) {
        if(bpe == 0) return 0;
        if(bpe > 3) {
            bpe = Math.max(Math.min(bpe, 31), 7);
        }
        return bpe;
    }

    public static int calculateBpeForBlockStates(int paletteSize) {
        return normalizeBlockStateBpe(calculateBpe(paletteSize));
    }

    public static int calculateBpeForBiomes(int paletteSize) {
        return normalizeBiomeBpe(calculateBpe(paletteSize));
    }

    public static int calculateBpe(int size) {
        return Mth.ceillog2(size);
    }

    public int bpe() {
        return this.bpe;
    }

    public int size() {
        return this.size;
    }

    public long[] raw() {
        return this.data;
    }

    public int[] unpack(int unpackSize) {
        if(this.size == 0 || this.bpe == 0) return new int[0];

        int[] processedData = new int[unpackSize];

        int idx = 0;
        for(long entry : data) {
            for(int i = 0; i < entriesPerLong && idx < unpackSize; i++, idx++) {
                int value = (int) ((entry >> (i * bpe)) & mask);

                processedData[idx] = value;
            }
        }

        return processedData;
    }

    @Override
    public @NotNull BitStorage copy() {
        return new BitStorage(this.bpe, this.size, this.data.clone());
    }
}
