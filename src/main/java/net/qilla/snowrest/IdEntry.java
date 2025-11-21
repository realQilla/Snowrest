package net.qilla.snowrest;

public record IdEntry(int id, float chance) {

    public static IdEntry of(int id) {
        return new IdEntry(id, 1.0f);
    }

    public static IdEntry of(int id, float chance) {
        return new IdEntry(id, chance);
    }

    public int id() {
        return this.id;
    }

    public float chance() {
        return this.chance;
    }
}
