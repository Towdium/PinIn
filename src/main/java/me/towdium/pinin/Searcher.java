package me.towdium.pinin;

import java.util.Collection;

public interface Searcher<T> {
    void put(String name, T identifier);

    Collection<T> search(String name);
}
