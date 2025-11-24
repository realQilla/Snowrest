package net.qilla.snowrest.holder;

import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.util.PaletteUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;

public final class PaletteContainer {
    private final int[] palette;
    private final ProcessedBits data;

    private PaletteContainer(int[] palette, @NotNull ProcessedBits processed) {
        this.palette = palette;
        this.data = processed;
    }

    public static PaletteContainer of(int[] palette, ProcessedBits processed) {
        return new PaletteContainer(palette, processed);
    }

    public int[] palette() {
        return this.palette;
    }

    public int paletteLength() {
        return this.palette.length;
    }

    public @NotNull ProcessedBits data() {
        return data;
    }

    public boolean isEmpty() {
        return this.paletteLength() == 0 || this.data.size() == 0;
    }

    public int[] match(int[] positions, EntryData holder) {
        int[] unpacked = this.data.unpacked();

        int[] entryIndexes = PaletteUtil.getIndexes(holder.entryIds(), this.palette);
        int[] temp = new int[positions.length];
        int count = 0;

        for(int pos : positions) {
            for(int index : entryIndexes) {
                if(unpacked[pos] != index) continue;
                temp[count++] = pos;
            }
        }

        return Arrays.copyOf(temp, count);
    }

    public int[] notMatch(int[] positions, EntryData holder) {
        int[] unpacked = this.data.unpacked();

        int[] entryIndexes = PaletteUtil.getIndexes(holder.entryIds(), this.palette);
        int[] temp = new int[positions.length];
        int count = 0;

        for(int pos : positions) {
            for(int index : entryIndexes) {
                if(unpacked[pos] == index) continue;
                temp[count++] = pos;
            }
        }

        return Arrays.copyOf(temp, count);
    }
}