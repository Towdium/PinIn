package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.towdium.pinin.Searcher.Logic.CONTAIN;
import static me.towdium.pinin.Searcher.Logic.MATCH;

public class CachedSearcher<T> extends SimpleSearcher<T> {
    IntList all = new IntArrayList();
    float scale;
    int total = 0;
    LinkedHashMap<String, IntList> cache = new LinkedHashMap<String, IntList>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() >= max();
        }
    };

    private int max() {
        return (int) (scale * ((logic == CONTAIN ? 16f * total / all.size() : 16) + 16));
    }

    public CachedSearcher(Logic logic, PinIn context) {
        this(logic, context, 1);
    }

    public CachedSearcher(Logic logic, PinIn context, float scale) {
        super(logic, context);
        if (logic == MATCH) throw new IllegalArgumentException("Cached searcher do not support match logic");
        this.scale = scale;
        context.listen(this, this::reset);
    }

    public void put(String name, T identifier) {
        reset();
        total += name.length();
        all.add(all.size());
        super.put(name, identifier);
    }

    public List<T> search(String name) {
        return generate(name).stream().map(i -> objs.get(i)).collect(Collectors.toList());
    }

    private IntList generate(String name) {
        IntList ret;
        if (name.isEmpty()) return all;
        else if ((ret = cache.get(name)) == null) {
            int len = (int) Math.ceil(Math.log(max()) / Math.log(8));
            ret = new IntArrayList();
            IntList is = generate(name.substring(0, Math.min(name.length() - 1, len)));
            acc.search(name);
            for (int i : is) if (logic.test(acc, 0, acc.strs().getInt(i))) ret.add(i);
            if (ret.size() == is.size()) ret = is;
            if (name.length() > len) return ret;
        }
        cache.remove(name);
        cache.put(name, ret);
        return ret;
    }

    public void reset() {
        cache.clear();
    }
}
