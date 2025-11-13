package net.qilla.snowrest.util;

import java.util.List;

public final class PrefixedArray<T> {
    private int length;
    private List<T> data;

    public PrefixedArray(List<T> data) {
        this.length = data.size();
        this.data = data;
    }

    public PrefixedArray(T[] data) {
        this.length = data.length;
        this.data = List.of(data);
    }

    public PrefixedArray() {}

    public int length() {
        return length;
    }

    public List<T> data() {
        return data;
    }

    public void setData(T[] data) {
        this.length = data.length;
        this.data = List.of(data);
    }
}
