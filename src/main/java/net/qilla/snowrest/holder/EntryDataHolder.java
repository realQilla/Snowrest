package net.qilla.snowrest.holder;

import net.qilla.snowrest.Snowrest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class EntryDataHolder implements EntryData {
    private static final Logger LOGGER = Snowrest.logger();

    private final IdEntry[] entries;
    private final int[] ids;

    private EntryDataHolder(@NotNull IdEntry[] entries) {
        this.entries = entries;
        this.ids = new int[entries.length];

        for(int i = 0; i < entries.length; i++) {
            this.ids[i] = entries[i].id();
        }
    }

    public static EntryData of(int... entries) {
        int size = entries.length;
        IdEntry[] idEntries = new IdEntry[size];

        for(int i = 0; i < size; i++) {
            idEntries[i] = IdEntry.of(entries[i]);
        }

        return new EntryDataHolder(idEntries);
    }

    public static EntryData of(IdEntry... entries) {
        return new EntryDataHolder(entries);
    }

    @Override
    public @NotNull IdEntry[] entries() {
        return entries;
    }

    public @NotNull IdEntry entry(int index) {
        if(index < 0 || index >= entries.length) {
            LOGGER.warn("Index {} out of pounds in EntryContainer", index);
            return IdEntry.of(0);
        }
        return entries[index];
    }

    @Override
    public int entryId(int index) {
        return entry(index).id();
    }

    @Override
    public int[] entryIds() {
        return ids;
    }

    @Override
    public int size() {
        return entries.length;
    }
}
