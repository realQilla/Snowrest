package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.EntryData;
import net.qilla.snowrest.holder.PaletteContainer;
import org.jetbrains.annotations.NotNull;
import java.util.SplittableRandom;

public final class IndirectEditor extends AbstractContainerEditor {
    IndirectEditor(int[] palette, int[] indexes, long chunkKey) {
        super(palette, indexes, chunkKey);
    }

    IndirectEditor(int[] palette, int[] indexes, @NotNull SplittableRandom random) {
        super(palette, indexes, random);
    }

    public static @NotNull ContainerEditor of(@NotNull PaletteContainer container, long chunkKey) {
        return new IndirectEditor(container.palette().clone(), container.data().unpacked().clone(), chunkKey);
    }

    @Override
    public @NotNull ContainerEditor set(int[] positions, @NotNull EntryData holder) {
        if(positions.length == 0) return this;
        if(holder.size() == 0) return this;

        int[] paletteIndexes = getOrAddPaletteIndexes(holder.entryIds());

        for(int index : positions) {
            super.indexes[index] = paletteIndexes[random.nextInt(paletteIndexes.length)];
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor set(@NotNull EntryData holder) {
        if(holder.size() > 1) {
            palette = holder.entryIds();

            for(int index : super.indexes) {
                int randomIndex = getPaletteIndex(randomEntry(holder));

                super.indexes[index] = randomIndex;
            }
        } else {
            return this.toSingleValue().set(holder);
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] lookup, @NotNull EntryData replacement) {
        if(lookup.length == 0) return this;
        if(replacement.size() == 0) return this;

        for(int index : super.indexes) {
            int[] lookupIndexes = getOrAddPaletteIndexes(lookup);

            for(int lookupIndex : lookupIndexes) {
                if(index != lookupIndex) continue;
                int replacementIndex = getPaletteIndex(randomEntry(replacement));

                this.indexes[index] = replacementIndex;
                break;
            }
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] positions, int[] lookup, @NotNull EntryData replacement) {
        if(replacement.size() == 0) return this;

        for(int index : positions) {
            int[] lookupIndexes = getOrAddPaletteIndexes(lookup);

            for(int lookupIndex : lookupIndexes) {
                if(index != lookupIndex) continue;
                int replacementIndex = getPaletteIndex(randomEntry(replacement));

                this.indexes[index] = replacementIndex;
                break;
            }
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] ignore, @NotNull EntryData replacement) {
        if(replacement.size() == 0) return this;

        for(int index : super.indexes) {
            int[] ignoreIndexes = getPaletteIndexes(ignore);

            for(int ignoreIndex : ignoreIndexes) {
                if(index == ignoreIndex) continue;
                int replacementIndex = getOrAddPaletteIndex(randomEntry(replacement));

                this.indexes[index] = replacementIndex;
                break;
            }
        }

        return this;
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] positions, int[] ignore, @NotNull EntryData replacement) {
        if(replacement.size() == 0) return this;

        for(int index : positions) {
            int[] ignoreIndexes = getPaletteIndexes(ignore);

            for(int ignoreIndex : ignoreIndexes) {
                if(index == ignoreIndex) continue;
                int replacementIndex = getOrAddPaletteIndex(randomEntry(replacement));

                this.indexes[index] = replacementIndex;
                break;
            }
        }

        return this;
    }

    @Override
    @NotNull ContainerEditor toSingleValue() {
        return new SingleValueEditor(palette[0], random);
    }

    @Override
    @NotNull ContainerEditor toIndirect() {
        return this;
    }

    @Override
    @NotNull ContainerEditor toDirect() {
        throw new RuntimeException("Cannot convert IndirectEditor to DirectEditor");
    }
}
