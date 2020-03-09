package me.towdium.pinin.elements;


import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Towdium
 * Date: 29/04/19
 */
public class Chinese extends Char {
    public static final int MIN = 0x3000;
    public static final int MAX = 0x9FFF;

    Pinyin[] pinyin;

    public Chinese(char ch, PinIn p) {
        super(ch);
        ArrayList<Pinyin> list = new ArrayList<>();
        if (isChinese(ch)) {
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

    public static boolean isChinese(char i) {
        return MIN <= i && i < MAX;
    }

    public static boolean isChinese(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; i--)
            if (isChinese(s.charAt(i))) return true;
        return false;
    }
}