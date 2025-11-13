package net.qilla.snowrest;

import net.qilla.snowrest.bitstorage.BaseBitStorage;
import net.qilla.snowrest.bitstorage.BitStorage;
import net.qilla.snowrest.bitstorage.EmptyBitStorage;
import net.qilla.snowrest.painter.PalettePainter;
import org.jetbrains.annotations.NotNull;

public final class PalettedContainer {
    private final int[] palette; //Variable Integer Array
    private final int paletteLength; //Variable Integer
    private final BaseBitStorage data;

    private PalettedContainer(int value) {
        this.palette = new int[1];
        this.palette[0] = value;
        this.paletteLength = 1;
        this.data = new EmptyBitStorage();
    }

    private PalettedContainer(int[] palette, @NotNull BitStorage data) {
        this.palette = palette;
        this.paletteLength = palette.length;
        this.data = data;
    }

    private PalettedContainer(@NotNull BitStorage data) {
        this.palette = null;
        this.paletteLength = 0;
        this.data = data;
    }

    public static PalettedContainer ofSingle(int singleValue) {
        return new PalettedContainer(singleValue);
    }

    public static PalettedContainer ofIndirect(int bpe, int[] palette, long[] data) {
        return new PalettedContainer(palette, BitStorage.ofPacked(bpe, data.length, data));
    }

    public static PalettedContainer ofDirect(int bpe, long[] data) {
        return new PalettedContainer(BitStorage.ofPacked(bpe, data.length, data));
    }

    public int[] palette() {
        return palette;
    }

    public int paletteLength() {
        return paletteLength;
    }

    public @NotNull BaseBitStorage data() {
        return data;
    }

    public boolean isEmpty() {
        return paletteLength == 0 || this.data.size() == 0;
    }

    public @NotNull PalettePainter painter(long chunkKey) {
        return new PalettePainter(this, chunkKey);
    }
}
