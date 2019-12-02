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
public class Chinese extends Char {
    Pinyin[] pinyin;

    public Chinese(char ch, PinIn p) {
        super(ch);
        ArrayList<Pinyin> list = new ArrayList<>();
        if (Matcher.isChinese(ch)) {
            Pinyin[] pinyin = Pinyin.get(ch, p);
            list.addAll(Arrays.asList(pinyin));
        }
        pinyin = list.toArray(new Pinyin[0]);
    }

    @Override
    public IndexSet match(String str, int start, boolean partial) {
        IndexSet ret = super.match(str, start, partial).copy();
        for (Element p : pinyin) ret.merge(p.match(str, start, partial));
        return ret;
    }

    @Override
    public Pinyin[] pinyins() {
        return pinyin;
    }
}