package me.towdium.pinin;

import java.util.List;

public interface Searcher<T> {
    void put(String name, T identifier);

    List<T> search(String name);
}
