package net.qilla.snowrest.util;

import java.util.Arrays;

public final class PaletteUtil {

    /**
     * Searches for the specified blockstate id, returns -1 if not found
     * @param entry Blockstate id to search for then create, if needbe
     * @return Palette index for the found or -1
     */

    public static int getIndex(int entry, int[] palette) {
        for(int i = 0; i < palette.length; i++) {
            if(palette[i] == entry) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches for the specified blockstate id, returns -1 if not found
     * @param ids Simple container for storing block ids, along with their chances, if applicable.
     * @return Palette index for the found or -1
     */

    public static int[] getIndexes(int[] ids, int[] palette) {
        int[] indexes = new int[ids.length];
        int count = 0;

        for(int id : ids) {
            for(int j = 0; j < palette.length; j++) {
                if(palette[j] == id) {
                    indexes[count++] = j;
                    break;
                }
            }
        }

        return Arrays.copyOf(indexes, count);
    }
}
