package me.towdium.pinin.elements;

import me.towdium.pinin.utils.IndexSet;

public class Char implements Element {
    public final char ch;
    public static final Pinyin[] NONE = new Pinyin[0];

    public Char(char ch) {
        this.ch = ch;
    }

    @Override
    public IndexSet match(String str, int start, boolean partial) {
        return str.charAt(start) == ch ? IndexSet.ONE : IndexSet.NONE;
    }

    public Pinyin[] pinyins() {
        return NONE;
    }
}
