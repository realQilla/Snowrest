package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.EntryData;
import net.qilla.snowrest.holder.PaletteContainer;
import org.jetbrains.annotations.NotNull;
import java.util.SplittableRandom;

public final class DirectEditor extends AbstractContainerEditor {
    DirectEditor(int[] values, long chunkKey) {
        super(new int[0], values, chunkKey);

        throw new RuntimeException("DirectEditor has not been completed yet.");
    }

    DirectEditor(int[] palette, int[] indexes, @NotNull SplittableRandom random) {
        super(palette, indexes, random);

        throw new RuntimeException("DirectEditor has not been completed yet.");
    }

    public static @NotNull ContainerEditor of(PaletteContainer container, long chunkKey) {
        return new DirectEditor(container.data().unpacked().clone(), chunkKey);
    }

    @Override
    public @NotNull ContainerEditor set(int[] positions, @NotNull EntryData holder) {
        return null;
    }

    @Override
    public @NotNull ContainerEditor set(@NotNull EntryData holder) {
        return null;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] lookup, @NotNull EntryData replacement) {
        return null;
    }

    @Override
    public @NotNull ContainerEditor replace(int[] positions, int[] lookup, @NotNull EntryData replacement) {
        return null;
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] ignore, @NotNull EntryData replacement) {
        return null;
    }

    @Override
    public @NotNull ContainerEditor replaceExcept(int[] positions, int[] ignore, @NotNull EntryData replacement) {
        return null;
    }

    @Override
    @NotNull ContainerEditor toSingleValue() {
        throw new RuntimeException("Cannot convert DirectEditor to SingleValueEditor");
    }

    @Override
    @NotNull ContainerEditor toIndirect() {
        throw new RuntimeException("Cannot convert DirectEditor to IndirectEditor");
    }

    @Override
    @NotNull ContainerEditor toDirect() {
        return this;
    }
}
