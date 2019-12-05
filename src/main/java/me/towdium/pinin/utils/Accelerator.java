package me.towdium.pinin.utils;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.towdium.pinin.PinIn;
import me.towdium.pinin.Searcher;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;

import java.util.ArrayList;
import java.util.List;

public class Accelerator {
    final PinIn context;
    List<IndexSet[]> cache;
    String search;
    CharList chars = new CharArrayList();
    IntList strs = new IntArrayList();
    final boolean partial;

    public char get(int offset) {
        return chars.getChar(offset);
    }

    public boolean end(int offset) {
        return chars.getChar(offset) == '\0';
    }

    public Accelerator(PinIn context, Searcher.Logic logic) {
        this.context = context;
        partial = logic != Searcher.Logic.MATCH;
    }

    public void search(String s) {
        search = s;
        cache = new ArrayList<>();
    }

    public IndexSet get(Char c, int offset) {
        IndexSet ret = (search.charAt(offset) == c.ch ? IndexSet.ONE : IndexSet.NONE).copy();
        for (Pinyin p : c.pinyins()) ret.merge(get(p, offset));
        return ret;
    }

    public IndexSet get(Pinyin p, int offset) {
        for (int i = cache.size(); i <= offset; i++)
            cache.add(new IndexSet[context.total()]);
        IndexSet[] data = cache.get(offset);
        IndexSet ret = data[p.id];
        if (ret == null) {
            ret = p.match(search, offset, partial);
            data[p.id] = ret;
        }
        return ret;
    }

    // offset - offset in search string
    // start - start point in raw text
    public boolean check(int offset, int start, boolean partial) {
        if (offset == search.length()) return partial;

        Char r = context.genChar(get(start));
        IndexSet s = get(r, offset);

        if (end(start + 1)) {
            int i = search.length() - offset;
            return s.get(i);
        } else return !s.traverse(i -> !check(offset + i, start + 1, partial));
    }

    public boolean matches(int offset, int start) {
        return check(offset, start, partial);
    }

    public boolean begins(int offset, int start) {
        return check(offset, start, partial);
    }

    public boolean contains(int offset, int start) {
        for (int i = start; !end(i); i++) {
            if (check(offset, i, partial)) return true;
        }
        return false;
    }

    public String search() {
        return search;
    }

    public int put(String s) {
        strs.add(chars.size());
        for (char c : s.toCharArray()) {
            chars.add(c);
            context.genChar(c);
        }
        chars.add('\0');
        return strs.getInt(strs.size() - 1);
    }

    public int common(int s1, int s2, int max) {
        for (int i = 0; ; i++) {
            if (i >= max) return max;
            char a = get(s1 + i);
            char b = get(s2 + i);
            if (a != b || a == '\0') return i;
        }
    }

    public IntList strs() {
        return strs;
    }
}