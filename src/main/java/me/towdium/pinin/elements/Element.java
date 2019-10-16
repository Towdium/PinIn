package me.towdium.pinin.elements;

import me.towdium.pinin.utils.IndexSet;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public interface Element {
    IndexSet match(String str, int start);
}
