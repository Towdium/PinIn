package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.towdium.pinin.Searcher.Logic.*;

public class CachedSearcher<T> extends SimpleSearcher<T> {
    IntList all = new IntArrayList();
    float scale;
    int len = -1;
    int total = 0;
    PinIn.Ticket ticket;
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
        this.scale = scale;
        ticket = context.ticket(this::reset);
    }

    public void put(String name, T identifier) {
        reset();
        for (int i = 0; i < name.length(); i++)
            context.genChar(name.charAt(i));
        total += name.length();
        all.add(all.size());
        len = -1;
        super.put(name, identifier);
    }

    public List<T> search(String name) {
        ticket.renew();
        if (len == -1) len = (int) Math.ceil(Math.log(max()) / Math.log(8));
        return test(name).stream().map(i -> objs.get(i)).collect(Collectors.toList());
    }

    private IntList filter(String name) {
        IntList ret;
        if (name.isEmpty()) return all;
        else if ((ret = cache.get(name)) == null) {
            Logic filter = logic == EQUAL ? BEGIN : logic;
            if (name.length() > len) throw new RuntimeException("Unnecessary filter");
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
        IntList is = filter(name.substring(0, Math.min(name.length(), len)));
        if (logic == EQUAL || name.length() > len) {
            IntArrayList ret = new IntArrayList();
            acc.search(name);
            for (int i : is) if (logic.test(acc, 0, strs.offsets().getInt(i))) ret.add(i);
            return ret;
        } else return is;
    }

    public void reset() {
        cache.clear();
    }
}
