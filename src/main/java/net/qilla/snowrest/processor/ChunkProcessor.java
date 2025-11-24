package net.qilla.snowrest.processor;

import net.qilla.snowrest.holder.ChunkSection;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for breaking down a chunk's section data
 */
public interface ChunkProcessor {
    /**
     * Rebuilds the given section data for a chunk with any changes that may have been applied.
     * @return A new byte array
     */
    byte[] buildBuffer();

    /**
     * Returns an array of chunk section objects that have been built by the given buffer.
     * @return
     */
    @NotNull ChunkSection[] sections();

    /**
     * Returns the size of the given buffer.
     * @return
     */
    int dataSize();
}
