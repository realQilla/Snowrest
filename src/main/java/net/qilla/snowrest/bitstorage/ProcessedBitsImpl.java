package net.qilla.snowrest.bitstorage;

import net.qilla.snowrest.data.DataPersistent;
import org.jetbrains.annotations.NotNull;

public final class ProcessedBitsImpl implements ProcessedBits {
    private final int bpe; //Encoded as unsinged Byte
    private final int[] unpacked; //Encoded as an array of Long
    private final int mask;
    private final int entriesPerLong;

    ProcessedBitsImpl(int bpe, long[] packed, int unpackedLength ) {
        this.bpe = bpe;
        this.mask = (1 << bpe) - 1;
        this.entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;

        int[] unpacked = new int[unpackedLength];

        int index = 0;
        for(long entry : packed) {
            for(int i = 0; i < entriesPerLong && index < unpackedLength; i++, index++) {
                int value = (int) ((entry >> (i * bpe)) & mask);

                unpacked[index] = value;
            }
        }

        this.unpacked = unpacked;
    }

    ProcessedBitsImpl(int bpe, int[] unpacked) {
        this.bpe = bpe;
        this.unpacked = unpacked;
        this.mask = (1 << bpe) - 1;
        this.entriesPerLong = DataPersistent.BITS_PER_LONG / bpe;
    }

    @Override
    public int bpe() {
        return bpe;
    }

    @Override
    public int size() {
        return unpacked.length;
    }

    @Override
    public int[] unpacked() {
        return unpacked.clone();
    }

    @Override
    public int getValue(int index) {
        if(index < 0 || index >= unpacked.length) {
            return -1;
        }
        return unpacked[index];
    }

    @Override
    public long[] pack() {
        int packedLength = (unpacked.length + entriesPerLong - 1) / entriesPerLong;
        long[] packed = new long[packedLength];

        int idx = 0;
        for(int i = 0; i < packedLength; i++) {
            long entry = 0;

            for(int j = 0; j < entriesPerLong && idx < unpacked.length; j++, idx++) {
                int maskedValue = unpacked[idx] & mask;
                entry |= ((long) maskedValue << (j * bpe));
            }
            packed[i] = entry;
        }

        return packed;
    }

    @Override
    public @NotNull ProcessedBitsImpl copy() {
        return new ProcessedBitsImpl(bpe, unpacked.clone());
    }
}
