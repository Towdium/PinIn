package me.towdium.pinin.utils;

public class Slice {
    String str;
    int start, end;

    public Slice(String s) {
        this(s, 0, s.length());
    }

    public Slice(String s, int start) {
        this(s, start, s.length());
    }

    public Slice(String s, int start, int end) {
        str = s;
        if (start < 0) start += s.length();
        this.start = start;
        if (end < 0) end += s.length();
        this.end = end;
    }

    public char charAt(int i) {
        return str.charAt(start + i);
    }

    public int length() {
        return end - start;
    }

    public void append(StringBuilder sb, int start, int end) {
        sb.append(str, this.start + start, this.start + end);
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (int i = start; i < end; i++) ret = 31 * ret + str.charAt(i);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        String os;
        int o0, o1;
        if (obj instanceof Slice) {
            Slice s = (Slice) obj;
            os = s.str;
            o0 = s.start;
            o1 = s.end;
        } else if (obj instanceof String) {
            os = (String) obj;
            o0 = 0;
            o1 = os.length();
        } else return false;

        if (end - start != o1 - o0) return false;
        for (int i = 0; i < end - start; i++)
            if (str.charAt(i + start) != os.charAt(i + o0)) return false;
        return true;
    }
}
