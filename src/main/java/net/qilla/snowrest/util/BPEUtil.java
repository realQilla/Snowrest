package net.qilla.snowrest.util;

import net.minecraft.util.Mth;

public final class BPEUtil {
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
}
