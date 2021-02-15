package me.towdium.pinin.searchers;

import it.unimi.dsi.fastutil.ints.IntList;
import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.Accelerator;
import me.towdium.pinin.utils.Compressor;

import java.util.ArrayList;
import java.util.List;

public class SimpleSearcher<T> implements Searcher<T> {
    List<T> objs = new ArrayList<>();
    final Accelerator acc;
    final Compressor strs = new Compressor();
    final PinIn context;
    final Logic logic;
    final PinIn.Ticket ticket;

    public SimpleSearcher(Logic logic, PinIn context) {
        this.context = context;
        this.logic = logic;
        acc = new Accelerator(context);
        acc.setProvider(strs);
        ticket = context.ticket(this::reset);
    }

    @Override
    public void put(String name, T identifier) {
        strs.put(name);
        for (int i = 0; i < name.length(); i++)
            context.getChar(name.charAt(i));
        objs.add(identifier);
    }

    @Override
    public List<T> search(String name) {
        List<T> ret = new ArrayList<>();
        acc.search(name);
        IntList offsets = strs.offsets();
        for (int i = 0; i < offsets.size(); i++) {
            int s = offsets.getInt(i);
            if (logic.test(acc, 0, s)) ret.add(objs.get(i));
        }
        return ret;
    }

    @Override
    public PinIn context() {
        return context;
    }

    public void reset() {
        acc.reset();
    }
}
