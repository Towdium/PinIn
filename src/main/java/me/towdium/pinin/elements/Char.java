package me.towdium.pinin.elements;


import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;
import me.towdium.pinin.utils.Matcher;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Towdium
 * Date: 29/04/19
 */
public class Char implements Element {
    Element[] patterns;

    Char() {
    }

    public Char(char ch, PinIn p) {
        ArrayList<Element> list = new ArrayList<>();
        list.add(new Raw(ch));
        if (Matcher.isChinese(ch)) {
            Pinyin[] pinyin = Pinyin.get(ch, p);
            list.addAll(Arrays.asList(pinyin));
        }
        patterns = list.toArray(new Element[0]);
    }

    @Override
    public IndexSet match(String str, int start) {
        IndexSet ret = new IndexSet();
        for (Element p : patterns)
            ret.merge(p.match(str, start));
        return ret;
    }

    public Element[] patterns() {
        return patterns;
    }
}