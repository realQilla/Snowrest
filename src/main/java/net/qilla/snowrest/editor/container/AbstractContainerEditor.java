package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.EntryData;
import net.qilla.snowrest.holder.PaletteContainer;
import net.qilla.snowrest.bitstorage.ProcessedBits;
import net.qilla.snowrest.util.BPEUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public abstract class AbstractContainerEditor implements ContainerEditor {
    protected final SplittableRandom random;
    protected int[] palette;
    protected int[] indexes;

    protected AbstractContainerEditor(int[] palette, int[] indexes, long chunkKey) {
        this.random = new SplittableRandom(chunkKey);
        this.palette = palette;
        this.indexes = indexes;
    }

    protected AbstractContainerEditor(int[] palette, int[] indexes, SplittableRandom random) {
        this.random = random;
        this.palette = palette;
        this.indexes = indexes;
    }

    /**
     * Attempts to set the given palette index to the specified id.
     * WARNING: In cases where this palette id already exists, it'll return the existing position
     * @param index Palette index to attempt to modify
     * @param id Incoming id
     * @return The pre-existing/newly added index that was modified
     */
    int setPalette(int index, int id) {
        int paletteIndex = getPaletteIndex(id);

        if(paletteIndex != -1) return paletteIndex;
        if(index >= palette.length) {
            this.palette = Arrays.copyOf(this.palette, this.palette.length + 1);
            this.palette[this.palette.length - 1] = id;
        } else {
            this.palette[index] = id;
        }

        return index;
    }

    /**
     * Replaces the existing palette with the incoming one
     * @param palette New palette
     */
    void setPalette(int[] palette) {
        this.palette = palette.clone();
    }

    /**
     * Attempts to add an id to the palette
     * WARNING: In cases where this palette id already exists, it'll return the existing position
     * @param id Incoming ID
     * @return The pre-existing/newly added index that was modified
     */
    int addPalette(int id) {
        int paletteIndex = getPaletteIndex(id);

        if(paletteIndex != -1) return paletteIndex;
        this.palette = Arrays.copyOf(this.palette, this.palette.length + 1);
        this.palette[this.palette.length - 1] = id;
        return this.palette.length - 1;
    }

    /**
     * Attempts to add the given ids to the palette
     * WARNING: In cases where a palette id already exist, it'll return the existing position
     * @param ids Incoming IDs
     * @return A combination of pre-existing/newly added indexes
     */
    int[] addPalette(int[] ids) {
        int[] indexes = new int[ids.length];
        int count = 0;

        for(int id : ids) {
            indexes[count++] = addPalette(id);
        }

        return indexes;
    }

    /**
     * Attempts to find a given id, returns -1 if not found
     * @param id ID to search for
     * @return The found id, otherwise -1
     */
    protected int getPaletteIndex(int id) {
        for(int index = 0; index < palette.length; index++) {
            if(this.palette[index] == id) return index;
        }

        return -1;
    }

    /**
     * Attempts to find given ids, returns -1 if not found
     * @param ids IDs to search for
     * @return An array found index positions
     */
    protected int[] getPaletteIndexes(int[] ids) {
        int[] indexes = new int[ids.length];
        int count = 0;

        for(int id : ids) {
            for(int index = 0; index < palette.length; index++) {
                if(palette[index] == id) {
                    indexes[count++] = index;
                    break;
                }
            }
        }

        return Arrays.copyOf(indexes, count);
    }

    /**
     * Attempts to find a palette index, otherwise is added
     * @param id ID to search for, otherwise, create
     * @return The existing or newly added palette index
     */
    protected int getOrAddPaletteIndex(int id) {
        for(int index = 0; index < palette.length; index++) {
            if(this.palette[index] == id) return index;
        }

        return addPalette(id);
    }

    /**
     * Attempts to find palette indexes, missing items are added
     * @param ids IDs to search for, otherwise, create
     * @return Return a combination of pre-existing and newly-added indexes
     */
    protected int[] getOrAddPaletteIndexes(int[] ids) {
        int[] indexes = new int[ids.length];
        int count = 0;

        for(int id : ids) {
            indexes[count++] = getOrAddPaletteIndex(id);
        }

        return Arrays.copyOf(indexes, count);
    }

    /**
     * Sets the given id to 0
     * @param id ID to remove if it exists
     */
    public void removePalette(int id) {
        for(int i = 0; i < this.palette.length; i++) {
            if(this.palette[i] == id) {
                this.palette[i] = 0;
                break;
            }
        }
    }

    /**
     * Sets the given IDs to 0
     * @param ids IDs to remove if they exists
     */
    public void removePalette(int[] ids) {
        for(int id : ids) {
            this.removePalette(id);
        }
    }

    /**
     * Checks if a given palette exists
     * @param id IDs to search for
     */
    protected boolean hasPalette(int id) {
        for(int p : palette) {
            if(p == id) return true;
        }

        return false;
    }

    /**
     * Clears the existing palette and sets all indexes to be empty.
     * @return Dynamic current, promoted, or demoted container editor
     */
    public ContainerEditor clearPalette() {
        this.palette = new int[1];
        this.indexes = new int[0];
        return this.toSingleValue();
    }

    /**
     * Returns a randomly selected entry from holder
     * @param holder Entry container, each entry consists of a value and a chance value
     * @return Returns the id of the selected entry
     */
    protected int randomEntry(@NotNull EntryData holder) {
        if(holder.size() == 1) return holder.entryIds()[0];
        return holder.entryIds()[random.nextInt(holder.size())];
    }

    @Override
    public @NotNull PaletteContainer buildBiomes() {
        int bpe = BPEUtil.calculateBpeForBiomes(palette.length);
        ProcessedBits processed = bpe == 1 ? ProcessedBits.empty() : ProcessedBits.unpacked(bpe, indexes);

        return PaletteContainer.of(palette, processed);
    }

    @Override
    public @NotNull PaletteContainer buildBlockStates() {
        int bpe = BPEUtil.calculateBpeForBlockStates(palette.length);
        ProcessedBits processed = bpe == 1 ? ProcessedBits.empty() : ProcessedBits.unpacked(bpe, indexes);

        return PaletteContainer.of(palette, processed);
    }


    /**
     * Converts the current palette to the SingleValued format
     * @return Dynamic current, promoted, or demoted container editor
     */
    abstract @NotNull ContainerEditor toSingleValue();

    /**
     * Converts the current palette to the Indirect format
     * @return Dynamic current, promoted, or demoted container editor
     */
    abstract @NotNull ContainerEditor toIndirect();

    /**
     * Converts the current palette to the Direct format
     * @return Dynamic current, promoted, or demoted container editor
     */
    abstract @NotNull ContainerEditor toDirect();
}
