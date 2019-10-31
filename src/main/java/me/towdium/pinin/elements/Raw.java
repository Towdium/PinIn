package me.towdium.pinin.elements;

import me.towdium.pinin.utils.IndexSet;

public class Raw extends Char {
    private char ch;

    public Raw(char ch) {
        this.ch = ch;
        patterns = new Element[]{this};
    }

    @Override
    public IndexSet match(String str, int start) {
        return str.charAt(start) == ch ? IndexSet.ONE : IndexSet.NONE;
    }
}
