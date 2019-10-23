package me.towdium.pinin.utils;

public class StringSlice {
    public final String str;
    public final int start;

    public StringSlice(String str, int start) {
        this.str = str;
        this.start = start;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (int i = start; i < str.length(); i++)
            ret = 31 * ret + str.charAt(i);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StringSlice)) return false;
        StringSlice ss = (StringSlice) obj;
        return str.substring(start).equals(ss.str.substring(ss.start));
    }

    public boolean isEmpty() {
        return str.length() == start;
    }
}
