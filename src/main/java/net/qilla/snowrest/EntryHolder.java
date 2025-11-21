package net.qilla.snowrest;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Set;

public final class EntryHolder {
    private static final Logger LOGGER = Snowrest.logger();

    private final IdEntry[] entries;
    private final int[] ids;

    private EntryHolder(@NotNull IdEntry[] entries) {
        this.entries = entries;
        this.ids = new int[entries.length];

        for(int i = 0; i < entries.length; i++) {
            this.ids[i] = entries[i].id();
        }
    }

    public static EntryHolder of(int... entries) {
        int size = entries.length;
        IdEntry[] idEntries = new IdEntry[size];

        for(int i = 0; i < size; i++) {
            idEntries[i] = IdEntry.of(entries[i]);
        }

        return new EntryHolder(idEntries);
    }

    public static EntryHolder of(IdEntry... entries) {
        return new EntryHolder(entries);
    }

    public IdEntry[] entries() {
        return entries;
    }

    public IdEntry entry(int index) {
        if(index < 0 || index >= entries.length) {
            LOGGER.warn("Index {} out of pounds in EntryContainer", index);
            return IdEntry.of(0);
        }
        return entries[index];
    }

    public int entryId(int index) {
        return entry(index).id();
    }

    public int[] entryIds() {
        return ids;
    }

    public int size() {
        return entries.length;
    }
}
