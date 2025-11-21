package net.qilla.snowrest.bitstorage;

import org.jetbrains.annotations.NotNull;

public final class EmptyProcessedBitsImpl implements ProcessedBits {
    public static final int[] UNPACKED_EMPTY = new int[0];
    public static final long[] PACKED_EMPTY = new long[0];

    EmptyProcessedBitsImpl() {}

    @Override
    public int bpe() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int[] unpacked() {
        return UNPACKED_EMPTY;
    }

    @Override
    public int getValue(int index) {
        return -1;
    }

    @Override
    public long[] pack() {
        return PACKED_EMPTY;
    }

    @Override
    public @NotNull ProcessedBits copy() {
        return this;
    }
}
