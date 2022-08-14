package me.towdium.pinin.utils;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public class IndexSet {
    public static final IndexSet ZERO = new IndexSet(0x1);
    public static final IndexSet ONE = new IndexSet(0x2);
    public static final IndexSet NONE = new IndexSet(0x0);

    int value = 0x0;

    public IndexSet() {
    }

    public IndexSet(IndexSet set) {
        value = set.value;
    }

    public IndexSet(int value) {
        this.value = value;
    }

    public void set(int index) {
        int i = 0x1 << index;
        value |= i;
    }

    public boolean get(int index) {
        int i = 0x1 << index;
        return (value & i) != 0;
    }

    public void merge(IndexSet s) {
        value = value == 0x1 ? s.value : (value | s.value);
    }

    public boolean traverse(IntPredicate p) {
        int v = value;
        for (int i = 0; i < 7; i++) {
            if ((v & 0x1) == 0x1 && p.test(i)) return true;
            else if (v == 0) return false;
            v >>= 1;
        }
        return false;
    }

    public void foreach(IntConsumer c) {
        int v = value;
        for (int i = 0; i < 7; i++) {
            if ((v & 0x1) == 0x1) c.accept(i);
            else if (v == 0) return;
            v >>= 1;
        }
    }

    public void offset(int i) {
        value <<= i;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        foreach(i -> {
            builder.append(i);
            builder.append(", ");
        });
        if (builder.length() != 0) {
            builder.delete(builder.length() - 2, builder.length());
            return builder.toString();
        } else return "0";
    }

    public boolean isEmpty() {
        return value == 0x0;
    }

    public IndexSet copy() {
        return new IndexSet(value);
    }

    static class Immutable extends IndexSet {
        @Override
        public void set(int index) {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void merge(IndexSet s) {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void offset(int i) {
            throw new UnsupportedOperationException("Immutable collection");
        }
    }

    static class Storage {
        IndexSet tmp = new Immutable();
        int[] data = new int[16];

        public void set(IndexSet is, int index) {
            if (index >= data.length) {
                // here we get the smallest power of 2 that is larger than index
                int size = index;
                size |= size >> 1;
                size |= size >> 2;
                size |= size >> 4;
                size |= size >> 8;
                size |= size >> 16;
                int[] replace = new int[size + 1];
                System.arraycopy(data, 0, replace, 0, data.length);
                data = replace;
            }
            data[index] = is.value + 1;
        }

        public IndexSet get(int index) {
            if (index >= data.length) return null;
            int ret = data[index];
            if (ret == 0) return null;
            tmp.value = ret - 1;
            return tmp;
        }
    }
}
