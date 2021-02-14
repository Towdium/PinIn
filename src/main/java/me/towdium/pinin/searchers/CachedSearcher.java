package me.towdium.pinin.searchers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.towdium.pinin.PinIn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.towdium.pinin.searchers.Searcher.Logic.*;

public class CachedSearcher<T> extends SimpleSearcher<T> {
    IntList all = new IntArrayList();
    float scale;
    int lenCached = 0;  // longest string with cached result
    int maxCached = 0;  // maximum amount of cached results
    int total = 0;  // total characters of all strings

    LinkedHashMap<String, IntList> cache = new LinkedHashMap<String, IntList>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() >= maxCached;
        }
    };

    public CachedSearcher(Searcher.Logic logic, PinIn context) {
        this(logic, context, 1);
    }

    public CachedSearcher(Searcher.Logic logic, PinIn context, float scale) {
        super(logic, context);
        this.scale = scale;
    }

    @Override
    public void put(String name, T identifier) {
        resetCache();
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

    private IntList filter(String name) {
        IntList ret;
        if (name.isEmpty()) return all;
        else if ((ret = cache.get(name)) == null) {
            // TODO eject cache based on n-gram frequency
            Searcher.Logic filter = logic == EQUAL ? BEGIN : logic;
            if (name.length() > lenCached) throw new RuntimeException("Unnecessary filter");
            IntArrayList tmp = new IntArrayList();
            IntList is = filter(name.substring(0, name.length() - 1));
            acc.search(name);
            for (int i : is) if (filter.test(acc, 0, strs.offsets().getInt(i))) tmp.add(i);
            if (tmp.size() == is.size()) ret = is;
            else {
                tmp.trim();
                ret = tmp;
            }
        }
        cache.remove(name);
        cache.put(name, ret);
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

    public void resetCache() {
        cache.clear();
    }
}
