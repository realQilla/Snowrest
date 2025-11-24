package net.qilla.snowrest.holder;

public interface EntryData {
    IdEntry[] entries();

    int entryId(int index);

    int[] entryIds();

    int size();
}
