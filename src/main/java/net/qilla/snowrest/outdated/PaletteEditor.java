package net.qilla.snowrest.outdated;

import net.qilla.snowrest.PaletteContainer;
import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.EntryHolder;
import net.qilla.snowrest.IdEntry;
import net.qilla.snowrest.util.BPEUtil;
import net.qilla.snowrest.util.PaletteUtil;
import java.util.Arrays;
import java.util.SplittableRandom;

/**
 * Editor for palette containers. Original containers are not mutated and must be set again.
 */

public final class PaletteEditor {
    private final SplittableRandom random;
    private int[] palette;
    private int paletteLength;
    private final int[] unpacked;

    private PaletteEditor(PaletteContainer container, long chunkKey) {
        this.random = new SplittableRandom(chunkKey);
        this.palette = container.palette().clone();
        this.paletteLength = container.paletteLength();
        this.unpacked = container.data().unpacked();
    }

    public static PaletteEditor of(PaletteContainer container, long chunkKey) {
        return new PaletteEditor(container, chunkKey);
    }

    /**
     * Returns an array containing index positions that match the specified Paintries collection
     * @param positions Array of positions to test
     * @param holder Container for entries
     * @return New array containing matched index positions
     */

    public int[] matchConditions(int[] positions, EntryHolder holder) {
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

    /**
     * Returns an array containing index positions that DO NOT match the specified Paintries collection
     * @param positions Array of positions to test
     * @param holder Container for entries
     * @return New array containing unmatched index positions
     */

    public int[] notMatchConditions(int[] positions, EntryHolder holder) {
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

    public void replace(int id, IdEntry entry) {
        for(int i = 0; i < paletteLength; i++) {
            if(palette[i] != id) continue;
            palette[i] = entry.id();
        }
    }

    /**
     * Sets all array positions to the specified container items
     * @param positions Array of chunk section indexes
     * @param holder Container for entries
     */

    public void setPositions(int[] positions, EntryHolder holder) {
        int[] paletteIndexes = getOrAddPaletteIndexes(holder);

        for(int index = 0; index < unpacked.length; index++) {
            for(int pos : positions) {
                if(index != pos) continue;
                unpacked[index] = chooseRandom(holder, paletteIndexes);
                break;
            }
        }
    }

    public void setAll(EntryHolder holder) {
        int[] paletteIndexes = getOrAddPaletteIndexes(holder);

        for(int index = 0; index < unpacked.length; index++) {
            unpacked[index] = chooseRandom(holder, paletteIndexes);
        }
    }

    /**
     * Replaces all entries at the given position array with a new entry holder
     * @param positions Array of chunk section indexes
     * @param lookup Entries to be replaced
     * @param replacement Entry container that will replace old entries
     * @return An array of indexes that were mutated
     */

    public int[] replacePositions(int[] positions, EntryHolder lookup, EntryHolder replacement) {
        int[] lookupIndexes = getOrAddPaletteIndexes(lookup);
        int[] replacementIndexes = getOrAddPaletteIndexes(replacement);

        for(int index = 0; index < unpacked.length; index++) {
            for(int i = 0; i < positions.length; i++) {
                int pos = positions[i];

                if(index != pos) continue;
                for(int lookupIndex : lookupIndexes) {
                    if(unpacked[index] != lookupIndex) continue;
                    unpacked[index] = chooseRandom(replacement, replacementIndexes);
                    positions[i] = -1;
                    break;
                }
            }
        }

        return positions;
    }

    public int[] replacePositionsExcept(int[] positions, EntryHolder exceptions, EntryHolder replacement) {
        int[] lookupIndexes = getOrAddPaletteIndexes(exceptions);
        int[] replacementIndexes = getOrAddPaletteIndexes(replacement);

        for(int index = 0; index < unpacked.length; index++) {
            for(int i = 0; i < positions.length; i++) {
                int pos = positions[i];

                if(index != pos) continue;
                for(int lookupIndex : lookupIndexes) {
                    if(unpacked[index] == lookupIndex) continue;
                    unpacked[index] = chooseRandom(replacement, replacementIndexes);
                    positions[i] = -1;
                    break;
                }
            }
        }

        return positions;
    }

    /**
     * Adds an element to the palette, then returns its index relative to the palette array.
     * @param id Blockstate id to add to the palette
     * @return Index of added palette entry
     */

    public int addToPalette(int id) {
        int[] newPalette = new int[paletteLength + 1];

        System.arraycopy(this.palette, 0, newPalette, 0, this.palette.length);
        newPalette[paletteLength] = id;

        this.palette = newPalette;
        this.paletteLength++;

        return paletteLength - 1;
    }

    /**
     * Adds an array of elements to the palette, then returns an array of index's relative to the palette array
     * @param ids Blockstate id's to add to the palette
     * @return Array of index's for each added palette entry
     */

    public int[] addToPalette(int[] ids) {
        int idCount = ids.length;
        if (idCount == 0) return new int[0];
        int[] newPaletteIndexes = new int[idCount];

        int[] newPalette = new int[paletteLength + idCount];
        System.arraycopy(this.palette, 0, newPalette, 0, paletteLength);

        int insertIndex = paletteLength;
        for (int i = 0; i < idCount; i++) {
            newPalette[insertIndex] = ids[i];
            newPaletteIndexes[i] = insertIndex;
            insertIndex++;
        }

        this.paletteLength = insertIndex;
        this.palette = newPalette;

        return newPaletteIndexes;
    }

    /**
     * Rebuilds the palette container, encoding the data with the biome bpe
     * @return New PalettedContainer. For changes to apply, must be set for current chunk section.
     */

    public PaletteContainer buildBiomes() {
        int bpe = BPEUtil.calculateBpeForBiomes(paletteLength);
        ProcessedBits bitStorage = ProcessedBits.unpacked(bpe, unpacked);

        return PaletteContainer.of(this.palette, bitStorage);
    }

    /**
     * Rebuilds the palette container, encoding the data with the blockstate bpe
     * @return New PalettedContainer. For changes to apply, must be set for current chunk section.
     */

    public PaletteContainer buildBlockStates() {
        int bpe = BPEUtil.calculateBpeForBlockStates(paletteLength);
        ProcessedBits bitStorage = ProcessedBits.unpacked(bpe, unpacked);

        return PaletteContainer.of(this.palette, bitStorage);
    }

    /**
     * Searches for the specified blockstate id and creates it if necessary
     * @param entry Blockstate id to search for then create, if needbe
     * @return Palette index for the found or newly created blockstate
     */

    public int getOrAddPaletteIndex(int entry) {
        int index = PaletteUtil.getIndex(entry, this.palette);

        return index == -1 ? addToPalette(entry) : index;
    }

    /**
     * Searches for an array of Paintry objects, which is a simple collection of blockstate ids
     * @param holder Simple container for storing block ids, along with their chances, if applicable.
     * @return Palette indexes for found or newly created blockstate's
     */


    public int[] getOrAddPaletteIndexes(EntryHolder holder) {
        int[] indexes = new int[holder.size()];

        for(int i = 0; i < holder.size(); i++) {
            indexes[i] = PaletteUtil.getIndex(holder.entryId(i), this.palette);
        }

        int missingCount = 0;
        for(int i = 0; i < holder.size(); i++) {
            if(indexes[i] == -1) {
                missingCount++;
            }
        }

        if(missingCount > 0) {
            int[] missingIds = new int[missingCount];
            int[] missingIndexes = new int[missingCount];
            int missingIdx = 0;

            for(int i = 0; i < holder.size(); i++) {
                if(indexes[i] == -1) {
                    missingIds[missingIdx] = holder.entryId(i);
                    missingIndexes[missingIdx] = i;
                    missingIdx++;
                }
            }

            int[] newPaletteIndexes = addToPalette(missingIds);

            for(int i = 0; i < missingCount; i++) {
                indexes[missingIndexes[i]] = newPaletteIndexes[i];
            }
        }

        return indexes;
    }

    private int chooseRandom(EntryHolder holder, int[] indexes) {
        if(holder.size() == 0) throw new IllegalArgumentException("Holder must contain at least one entry");
        if(holder.size() != indexes.length) throw new IllegalArgumentException("Holder and indexes must be the same length");
        if(holder.size() == 1) return indexes[0];

        int index = (int)(random.nextFloat() * holder.size());

        return indexes[index];
    }
}
