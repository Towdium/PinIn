package me.towdium.pinin.searchers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.towdium.pinin.PinIn;

import java.util.*;
import java.util.stream.Collectors;

import static me.towdium.pinin.searchers.Searcher.Logic.*;

public class CachedSearcher<T> extends SimpleSearcher<T> {
    IntList all = new IntArrayList();
    float scale;
    int lenCached = 0;  // longest string with cached result
    int maxCached = 0;  // maximum amount of cached results
    int total = 0;  // total characters of all strings
    Stats<String> stats = new Stats<>();

    Map<String, IntList> cache = new HashMap<>();

    public CachedSearcher(Searcher.Logic logic, PinIn context) {
        this(logic, context, 1);
    }

    public CachedSearcher(Searcher.Logic logic, PinIn context, float scale) {
        super(logic, context);
        this.scale = scale;
    }

    @Override
    public void put(String name, T identifier) {
        reset();
        for (int i = 0; i < name.length(); i++)
            context.getChar(name.charAt(i));
        total += name.length();
        all.add(all.size());
        lenCached = 0;
        maxCached = 0;
        super.put(name, identifier);
    }

    @Override
    public List<T> search(String name) {
        ticket.renew();
        if (all.isEmpty()) return new ArrayList<>();

        if (maxCached == 0) {
            float totalSearch = logic == CONTAIN ? total : all.size();
            maxCached = (int) (scale * Math.ceil(2 * Math.log(totalSearch) / Math.log(2) + 16));
        }
        if (lenCached == 0) lenCached = (int) Math.ceil(Math.log(maxCached) / Math.log(8));
        return test(name).stream().map(i -> objs.get(i)).collect(Collectors.toList());
    }

    @Override
    public void reset() {
        super.reset();
        stats.reset();
        lenCached = 0;
        maxCached = 0;
    }

    private IntList filter(String name) {
        IntList ret;
        if (name.isEmpty()) return all;

        ret = cache.get(name);
        stats.count(name);

        if (ret == null) {
            IntList base = filter(name.substring(0, name.length() - 1));
            if (cache.size() >= maxCached) {
                String least = stats.least(cache.keySet(), name);
                if (!least.equals(name)) cache.remove(least);
                else return base;
            }

            acc.search(name);
            IntArrayList tmp = new IntArrayList();
            Searcher.Logic filter = logic == EQUAL ? BEGIN : logic;
            for (int i : base) {
                if (filter.test(acc, 0, strs.offsets().getInt(i))) tmp.add(i);
            }

            if (tmp.size() == base.size()) {
                ret = base;
            } else {
                tmp.trim();
                ret = tmp;
            }

            cache.put(name, ret);
        }

        return ret;
    }

    private IntList test(String name) {
        IntList is = filter(name.substring(0, Math.min(name.length(), lenCached)));
        if (logic == EQUAL || name.length() > lenCached) {
            IntArrayList ret = new IntArrayList();
            acc.search(name);
            for (int i : is) if (logic.test(acc, 0, strs.offsets().getInt(i))) ret.add(i);
            return ret;
        } else return is;
    }

     static class Stats<T> {
        Object2IntMap<T> data = new Object2IntOpenHashMap<>();

        public void count(T key) {
            int cnt = data.getInt(key) + 1;
            data.put(key, cnt);
            if (cnt == Integer.MAX_VALUE) {
                data.forEach((k, v) -> data.put(k, v / 2));
            }
        }

        public T least(Collection<T> keys, T extra) {
            T ret = extra;
            int cnt = data.getInt(extra);
            for (T i: keys) {
                int value = data.getInt(i);
                if (value < cnt) {
                    ret = i;
                    cnt = value;
                }
            }
            return ret;
        }

        public void reset() {
            data.clear();
        }
    }
}
