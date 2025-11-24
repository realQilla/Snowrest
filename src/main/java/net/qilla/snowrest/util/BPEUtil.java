package net.qilla.snowrest.util;

import net.minecraft.util.Mth;

public final class BPEUtil {
    /**
     * Normalizes the given Blockstate BPE(bits per entry) to be only in the range that
     * Minecraft expects. 0 being the minimum, values between 0 and 4 are set
     * to 4, values between 4 and 15 are untouched, and values over 15 are ceiled
     * to 15
     * @param bpe The original un-normalized BPE.
     * @return A normalized BPE integer
     */
    public static int normalizeBlockStateBpe(int bpe) {
        if(bpe == 0) return 0;
        if(bpe <= 8) {
            bpe = Math.max(bpe, 4);
        } else {
            bpe = Math.max(Math.min(bpe, 31), 15);
        }
        return bpe;
    }

    /**
     * Normalizes the given Biome BPE(bits per entry) to be only in the range that
     * Minecraft expects. 0 being the minimum, values between 0 and 4 are set
     * to 4, values between 4 and 15 are untouched, and values over 15 are ceiled
     * to 15
     * @param bpe The original un-normalized BPE.
     * @return A normalized BPE integer
     */
    public static int normalizeBiomeBpe(int bpe) {
        if(bpe == 0) return 0;
        if(bpe > 3) {
            bpe = Math.max(Math.min(bpe, 31), 7);
        }
        return bpe;
    }

    /**
     * Calculates the Blockstate BPE for a palette then normalizes it.
     * @param paletteSize The length of the current palette array
     * @return A calculated and normalized BPE
     */
    public static int calculateBpeForBlockStates(int paletteSize) {
        return normalizeBlockStateBpe(calculateBpe(paletteSize));
    }

    /**
     * Calculates the Biome BPE for a palette then normalizes it.
     * @param paletteSize The length of the current palette array
     * @return A calculated and normalized BPE
     */
    public static int calculateBpeForBiomes(int paletteSize) {
        return normalizeBiomeBpe(calculateBpe(paletteSize));
    }

    /**
     * Returns the BPE(bits per entry) for the current palette size
     * @param size The length of the current palette array
     * @return A calculated BPE
     */
    public static int calculateBpe(int size) {
        return Mth.ceillog2(size);
    }
}
