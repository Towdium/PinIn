package me.towdium.pinin.utils;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Element;
import me.towdium.pinin.elements.Pinyin;

import java.util.ArrayList;
import java.util.List;

public abstract class Accelerator<T> {
    final PinIn context;
    List<IndexSet[]> cache;
    String search;

    protected abstract char get(T str, int offset);

    protected abstract boolean end(T str, int offset);

    public Accelerator(PinIn context) {
        this.context = context;
    }

    public void search(String s) {
        search = s;
        cache = new ArrayList<>();
    }

    public IndexSet get(Char c, int offset) {
        IndexSet ret = new IndexSet();
        for (Element p : c.patterns()) {
            IndexSet is;
            if (p instanceof Pinyin) is = get((Pinyin) p, offset);
            else is = p.match(search, offset);
            ret.merge(is);
        }
        return ret;
    }

    public IndexSet get(Pinyin p, int offset) {
        for (int i = cache.size(); i <= offset; i++)
            cache.add(new IndexSet[context.total()]);
        IndexSet[] data = cache.get(offset);
        IndexSet ret = data[p.id];
        if (ret == null) {
            ret = p.match(search, offset);
            data[p.id] = ret;
        }
        return ret;
    }

    public boolean check(int offset, T s2, int start2) {
        if (offset == search.length()) return true;

        Char r = context.genChar(get(s2, start2));
        IndexSet s = get(r, offset);

        if (end(s2, start2 + 1)) {
            int i = search.length() - offset;
            return s.get(i);
        } else return !s.traverse(i -> !check(offset + i, s2, start2 + 1));
    }

    public boolean contains(T s1, boolean full) {
        for (int i = 0; full ? !end(s1, i) : i == 0; i++) {
            if (check(0, s1, i)) return true;
        }
        return false;
    }

    public String search() {
        return search;
    }

    public void initialize(String s) {
        for (char c : s.toCharArray()) context.genChar(c);
    }
}