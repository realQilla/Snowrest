package net.qilla.snowrest.painter;

import net.qilla.snowrest.PalettedContainer;
import net.qilla.snowrest.bitstorage.BitStorage;
import net.qilla.snowrest.data.DataPersistent;
import java.util.Arrays;
import java.util.SplittableRandom;

public final class PalettePainter {
    private static final int[] AIR_STATE_IDS = new int[]{0, 15090, 15091};

    private final SplittableRandom random;
    private int[] palette;
    private int paletteLength;
    private final int[] unpacked;

    public PalettePainter(PalettedContainer container, long chunkKey) {
        this.random = new SplittableRandom(chunkKey);
        this.palette = container.palette();
        this.paletteLength = container.paletteLength();
        this.unpacked = container.data().unpack(DataPersistent.BLOCKS_PER_SECTION);
    }

    public void replace(int id, int[] replacementId) {
        for(int i = 0; i < paletteLength; i++) {
            if(palette[i] != id) continue;
            int randomId = getRandom(replacementId);
            palette[i] = randomId;
        }
    }

    public void setPositions(int[] positions, Paintries paintries) {
        int[] paletteIndexes = getOrAddPaletteIndexes(paintries.entries(), paintries.size());

        for(int blockIndex = 0; blockIndex < DataPersistent.BLOCKS_PER_SECTION; blockIndex++) {
            for(int pos : positions) {
                if(blockIndex != pos) continue;
                unpacked[blockIndex] = chooseRandom(paintries, paletteIndexes);
                break;
            }
        }
    }

    public int[] replacePositions(int[] positions, Paintries lookups, Paintries replacements) {
        int[] lookupIndexes = getOrAddPaletteIndexes(lookups.entries(), lookups.size());
        int[] replacementIndexes = getOrAddPaletteIndexes(replacements.entries(), replacements.size());

        for(int blockIndex = 0; blockIndex < DataPersistent.BLOCKS_PER_SECTION; blockIndex++) {
            for(int i = 0; i < positions.length; i++) {
                int pos = positions[i];

                if(blockIndex != pos) continue;
                for(int lookupIndex : lookupIndexes) {
                    if(unpacked[blockIndex] != lookupIndex) continue;
                    unpacked[blockIndex] = chooseRandom(replacements, replacementIndexes);
                    positions[i] = -1;
                    break;
                }
            }
        }

        return positions;
    }

    public int[] replaceNonAir(int[] positions, Paintries entries) {
        int[] airPaletteIndexes = new int[0];
        int[] entryIndexes = getOrAddPaletteIndexes(entries.entries(), entries.size());

        for(int airId : AIR_STATE_IDS) {
            int paletteIndex = getPaletteIndex(airId);

            if(paletteIndex != -1) {
                airPaletteIndexes = Arrays.copyOf(airPaletteIndexes, airPaletteIndexes.length + 1);
                airPaletteIndexes[airPaletteIndexes.length - 1] = paletteIndex;
            }
        }

        for(int blockIndex = 0; blockIndex < DataPersistent.BLOCKS_PER_SECTION; blockIndex++) {
            for(int i = 0; i < positions.length; i++) {
                int pos = positions[i];

                if(blockIndex != pos) continue;
                for(int paletteIndex : airPaletteIndexes) {
                    if(unpacked[blockIndex] == paletteIndex) continue;
                    unpacked[blockIndex] = chooseRandom(entries, entryIndexes);
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

    private int addToPalette(int id) {
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

    private int[] addToPalette(int[] ids) {
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
     * Rebuilds the palette container so that encoded data uses the correct bits per entry.
     * @return New PalettedContainer. For changes to apply, must be set for current chunk section.
     */

    public PalettedContainer build() {
        int bpe = BitStorage.calculateBpeForBlockStates(paletteLength);
        BitStorage bitStorage = BitStorage.ofUnpacked(bpe, unpacked.length, unpacked);

        return PalettedContainer.ofIndirect(bpe, this.palette, bitStorage.raw());
    }

    /**
     * Searches for the specified blockstate id, returns -1 if not found
     * @param entry Blockstate id to search for then create, if needbe
     * @return Palette index for the found or -1
     */

    private int getPaletteIndex(int entry) {
        for(int i = 0; i < paletteLength; i++) {
            if(palette[i] == entry) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches for the specified blockstate id, returns -1 if not found
     * @param entries Blockstate id to search for then create, if needbe
     * @param size Paintry array size to avoid repeated length calculations
     * @return Palette index for the found or -1
     */

    private int[] getPaletteIndexes(Paintry[] entries, int size) {
        int[] indexes = new int[size];

        for(int i = 0; i < size; i++) {
            indexes[i] = getPaletteIndex(entries[i].id());
        }

        return indexes;
    }

    /**
     * Searches for the specified blockstate id and creates it if necessary
     * @param entry Blockstate id to search for then create, if needbe
     * @return Palette index for the found or newly created blockstate
     */

    private int getOrAddPaletteIndex(int entry) {
        int index = getPaletteIndex(entry);

        return index == -1 ? addToPalette(entry) : index;
    }

    /**
     * Searches for an array of Paintry objects, which is a simple collection of blockstate ids
     * @param entries Paintry array which holds blockstate ids. Chance property is disregarded
     * @param size Paintry array size to avoid repeated length calculations
     * @return Palette indexes for found or newly created blockstate's
     */


    private int[] getOrAddPaletteIndexes(Paintry[] entries, int size) {
        int[] indexes = new int[size];

        for(int i = 0; i < size; i++) {
            indexes[i] = getPaletteIndex(entries[i].id());
        }

        int missingCount = 0;
        for(int i = 0; i < size; i++) {
            if(indexes[i] == -1) {
                missingCount++;
            }
        }

        if(missingCount > 0) {
            int[] missingIds = new int[missingCount];
            int[] missingIndexes = new int[missingCount];
            int missingIdx = 0;

            for(int i = 0; i < size; i++) {
                if(indexes[i] == -1) {
                    missingIds[missingIdx] = entries[i].id();
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


    /**
     * Returns an array containing index positions that match the specified Paintries collection
     * @param positions Array of positions to test
     * @param entries Paintries object that holds a collection of Paintry's. Chance property is disregarded
     * @return New array containing matched index positions
     */

    public int[] matchConditions(int[] positions, Paintries entries) {
        int[] entryIndexes = getPaletteIndexes(entries.entries(), entries.size());
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
     * @param entries Paintries object that holds a collection of Paintry's. Chance property is disregarded
     * @return New array containing unmatched index positions
     */

    public int[] notMatchConditions(int[] positions, Paintries entries) {
        int[] entryIndexes = getPaletteIndexes(entries.entries(), entries.size());
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

    private int chooseRandom(Paintries paintries, int[] indexes) {
        if(paintries.size() != indexes.length) throw new IllegalArgumentException("Paintries and indexes must be the same length");

        int index = (int)(random.nextFloat() * paintries.size());

        return indexes[index];
    }

    private int getRandom(int[] ids) {
        int idCount = ids.length;
        if(idCount == 1) return ids[0];
        return ids[random.nextInt(idCount)];
    }
}
