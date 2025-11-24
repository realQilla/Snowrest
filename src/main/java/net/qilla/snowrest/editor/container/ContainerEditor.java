package net.qilla.snowrest.editor.container;

import net.qilla.snowrest.holder.EntryData;
import net.qilla.snowrest.holder.PaletteContainer;
import org.jetbrains.annotations.NotNull;

/**
 * PaletteContainer modification class for both blockstate and biome containers.
 * When built, the palette container is dynamically adjusted to correctly suit applied changes
 */
public interface ContainerEditor {

    /**
     * Set all given positions to values in the holder
     * @param positions Array of section positions
     * @param holder Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor set(int[] positions, @NotNull EntryData holder);

    /**
     * Set all positions to values in the holder
     * @param holder Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor set(@NotNull EntryData holder);

    /**
     * Replace all positions that use the specified palette with values in the holder
     * @param lookup Palette values to replace
     * @param replacement Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor replace(int[] lookup, @NotNull EntryData replacement);

    /**
     * Replace all given positions that use the specified palette with values in the holder
     * @param lookup Palette values to replace
     * @param replacement Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor replace(int[] positions, int[] lookup, @NotNull EntryData replacement);

    /**
     * Replace all positions that DO NOT use the specified palette with values in the holder
     * @param ignore Palette values to ignore
     * @param replacement Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor replaceExcept(int[] ignore, @NotNull EntryData replacement);

    /**
     * Replace all given positions that DO NOT use the specified palette with values in the holder
     * @param positions Array of section positions
     * @param ignore Palette values to ignore
     * @param replacement Entry container, each entry consists of a value and a chance value
     * @return Dynamic current, promoted, or demoted container editor
     */
    @NotNull ContainerEditor replaceExcept(int[] positions, int[] ignore, @NotNull EntryData replacement);


    /**
     * Build's the proper container relative to the changes applied to section biomes
     */
    @NotNull PaletteContainer buildBiomes();

    /**
     * Build's the proper container relative to the changes applied to section blockstates
     */
    @NotNull PaletteContainer buildBlockStates();

    /**
     * Creates a new blockstate editor for the given container
     * @param container Pre-existing palette container to modify
     * @param chunkKey Chunkkey to use for randomization
     */
    static @NotNull ContainerEditor ofBlockState(@NotNull PaletteContainer container, long chunkKey) {
        return switch(container.data().bpe()) {
            case 0 -> SingleValueEditor.of(container, chunkKey);
            case 1, 2, 3, 4, 5, 6, 7, 8 -> IndirectEditor.of(container, chunkKey);
            default -> DirectEditor.of(container, chunkKey);
        };
    }

    /**
     * Creates a new biome editor for the given container
     * @param container Pre-existing palette container to modify
     * @param chunkKey Chunkkey to use for randomization
     */
    static @NotNull ContainerEditor ofBiome(@NotNull PaletteContainer container, long chunkKey) {
        return switch(container.data().bpe()) {
            case 0 -> SingleValueEditor.of(container, chunkKey);
            case 1, 2, 3 -> IndirectEditor.of(container, chunkKey);
            default -> DirectEditor.of(container, chunkKey);
        };
    }
}
