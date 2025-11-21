package net.qilla.snowrest.bitstorage;

import org.jetbrains.annotations.NotNull;

public interface ProcessedBits {

    int bpe();

    int size();

    int[] unpacked();

    int getValue(int index);

    long[] pack();

    @NotNull ProcessedBits copy();

    static @NotNull ProcessedBits packed(int bpe, long[] packed, int unpackedLength ) {
        if(packed.length == 0 || bpe == 0) return new EmptyProcessedBitsImpl();

        return new ProcessedBitsImpl(bpe, packed, unpackedLength );
    }

    static @NotNull ProcessedBits unpacked(int bpe, int[] unpacked) {
        if(unpacked.length == 0 || bpe == 0) return new EmptyProcessedBitsImpl();

        return new ProcessedBitsImpl(bpe, unpacked);
    }

    static @NotNull ProcessedBits empty() {
        return new EmptyProcessedBitsImpl();
    }
}
