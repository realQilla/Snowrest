package net.qilla.snowrest.outdated;

public final class Heightmap {

    private int type;
    private PrefixedArray<Long> data;

    public Heightmap(int type, PrefixedArray<Long> data) {
        this.type = type;
        this.data = data;
    }

    public Heightmap() {
    }

    public int type() {
        return type;
    }

    public PrefixedArray<Long> data() {
        return data;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setData(PrefixedArray<Long> data) {
        this.data = data;
    }
}
