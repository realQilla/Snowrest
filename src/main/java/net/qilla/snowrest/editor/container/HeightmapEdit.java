package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.ChunkSection;
import net.qilla.snowrest.holder.EntryData;
import org.jetbrains.annotations.NotNull;

public interface HeightmapEdit {

    /**
     * Returns an array of two heightmap entries, the first being a heightmap consisting of ONLY
     * matched values specified in the holder; the second is the inverse
     * @param sections The sections to match this heightmap to
     * @param holder A list of entries that will be used to separate out this heightmap
     * @return An array containing TWO heightmap entries
     */
    @NotNull HeightmapEdit[] separate(@NotNull ChunkSection[] sections, @NotNull EntryData holder);

    /**
     * Offsets all heightmap data by the set amount.
     * @param offset
     */
    void offsetY(int offset);

    /**
     * Stacks the heightmap veritically to include positions above
     * Calling this method more than once is extremely inefficient and will result in duplicate values
     * @param amount Number of blocks to include in heightmap above original layer
     */
    void stack(int amount);

    /**
     * Returns a 2d array where the external array is sized based on the number of sections the
     * current chunk can hold. Each section contains the height values that correspond to its
     * OWN section
     * @return
     */
    int[][] build();

    /**
     * Returns the 256 integer values that each correspond to each blocks height relative to the chunk it's in
     * @return An array of 256 integers
     */
    int[] heightmap();

    /**
     * Returns a deep copy of the current heightmap
     * @return A new independent heightmap
     */
    @NotNull HeightmapEdit copy();
}
