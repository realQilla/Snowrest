package net.qilla.snowrest.painter;

import org.jetbrains.annotations.NotNull;

public final class Paintries {
    private final Paintry[] entries;
    private final int size;
    private final boolean single;

    private Paintries(Paintry[] entries) {
        this.entries = entries;
        this.size = entries.length;
        this.single = this.size == 1;
    }

    private Paintries(Paintry entry) {
        this.entries = new Paintry[]{entry};
        this.size = 1;
        this.single = true;
    }

    public static Paintries multi(int... ids) {
        Paintry[] entries = new Paintry[ids.length];

        for(int i = 0; i < entries.length; i++) {
            entries[i] = Paintry.of(ids[i]);
        }

        return new Paintries(entries);
    }

    public static Paintries multi(Paintry... entries) {
        return new Paintries(entries);
    }

    public static Paintries single(Paintry entry) {
        return new Paintries(entry);
    }

    public static Paintries single(int id) {
        return new Paintries(Paintry.of(id));
    }

    public @NotNull Paintry[] entries() {
        return this.entries;
    }

    public int size() {
        return this.size;
    }

    public boolean isSingle() {
        return this.single;
    }
}
