package net.qilla.snowrest.bitstorage;

import org.jetbrains.annotations.NotNull;

public interface BaseBitStorage {

    int bpe();

    int size();

    long[] raw();

    int[] unpack(int unpackSize);

    @NotNull BaseBitStorage copy();
}
