package me.towdium.pinin;

import me.towdium.pinin.utils.Accelerator;

import java.util.ArrayList;
import java.util.List;

public class SimpleSearcher<T> implements Searcher<T> {
    List<String> strs = new ArrayList<>();
    List<T> objs = new ArrayList<>();
    final Accelerator<String> acc;
    final PinIn context;
    final boolean suffix;

    public SimpleSearcher(boolean suffix, PinIn context) {
        this.context = context;
        this.suffix = suffix;
        acc = new Accelerator<String>(context) {
            @Override
            protected char get(String str, int offset) {
                return str.charAt(offset);
            }

            @Override
            protected boolean end(String str, int offset) {
                return str.length() == offset;
            }
        };
    }

    @Override
    public void put(String name, T identifier) {
        strs.add(name);
        objs.add(identifier);
        acc.initialize(name);
    }

    @Override
    public List<T> search(String name) {
        List<T> ret = new ArrayList<>();
        acc.search(name);
        for (int i = 0; i < strs.size(); i++)
            if (acc.contains(strs.get(i), suffix)) ret.add(objs.get(i));
        return ret;
    }
}
