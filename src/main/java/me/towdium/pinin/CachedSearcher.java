package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return (int) (scale * ((suffix ? total : all.size()) / 2048 + 16));
    }

    public CachedSearcher(boolean suffix, PinIn context) {
        this(suffix, context, 1);
    }

    public CachedSearcher(boolean suffix, PinIn context, float scale) {
        super(suffix, context);
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
            for (int i : is) if (acc.contains(acc.strs().getInt(i), suffix)) ret.add(i);
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
