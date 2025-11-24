package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.EntryData;
import net.qilla.snowrest.holder.PaletteContainer;
import net.qilla.snowrest.data.DataPersistent;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.SplittableRandom;

public final class SingleValueEditor extends AbstractContainerEditor {
    SingleValueEditor(int value, long chunkKey) {
        super(new int[]{value}, new int[0], chunkKey );
    }

    SingleValueEditor(int value, @NotNull SplittableRandom random) {
        super(new int[]{value}, new int[0], random);
    }

    public static ContainerEditor of(@NotNull PaletteContainer container, long chunkKey) {
        return new SingleValueEditor(container.palette()[0], chunkKey);
    }

    @Override
    public @NotNull ContainerEditor set(int[] positions, @NotNull EntryData holder) {
        return this.toIndirect().set(positions, holder);
    }

    @Override
    public @NotNull ContainerEditor set(@NotNull EntryData holder) {
        if(holder.size() > 1) {
            return this.toIndirect().set(holder);
        }

        this.palette[0] = holder.entryIds()[0];

        return this;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] lookup, @NotNull EntryData replacement) {
        if(replacement.size() > 1) {
            return this.toIndirect().replace(lookup, replacement);
        }

        for(int id : lookup) {
            if(this.palette[0] == id) {
                this.palette[0] = replacement.entryIds()[0];
                break;
            }
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] positions, int[] lookup, @NotNull EntryData replacement) {
        return this.toIndirect().replace(positions, lookup, replacement);
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] ignore, @NotNull EntryData replacement) {
        return this.toIndirect().replaceExcept(ignore, replacement);
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] positions, int[] ignore, @NotNull EntryData replacement) {
        return this.toIndirect().replaceExcept(positions, ignore, replacement);
    }

    @NotNull ContainerEditor toSingleValue() {
        return this;
    }

    @NotNull ContainerEditor toIndirect() {
        int[] upgradedIndexes = new int[DataPersistent.BLOCKS_PER_SECTION];
        Arrays.fill(upgradedIndexes, palette[0]);

        return new IndirectEditor(palette, upgradedIndexes, random);
    }
    @NotNull ContainerEditor toDirect() {
        throw new RuntimeException("Cannot convert SingleValueEditor to DirectEditor");
    }
}
