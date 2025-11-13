package net.qilla.snowrest.bitstorage;

import org.jetbrains.annotations.NotNull;

public final class EmptyBitStorage implements BaseBitStorage {
    public static final long[] RAW_EMPTY = new long[0];
    public static final int[] UNPACK_EMPTY = new int[0];

    @Override
    public int bpe() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public long[] raw() {
        return RAW_EMPTY;
    }

    @Override
    public int[] unpack(int unpackSize) {
        return UNPACK_EMPTY;
    }

    @Override
    public @NotNull BaseBitStorage copy() {
        return this;
    }
}
