package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class Pinyin implements Element {
    private static String[][] data;
    private static final String[] EMPTY = new String[0];

    static {
        data = new String[41000][];
        String resourceName = "data.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinIn.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                char ch = line.charAt(0);
                String sounds = line.substring(3);
                data[ch] = sounds.split(", ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 41000; i++)
            if (data[i] == null) data[i] = EMPTY;
    }

    private Phoneme initial;
    private Phoneme finale;
    private Phoneme tone;
    private Phoneme[] phonemes;

    public Pinyin(String str, PinIn p) {
        String[] elements = p.keyboard.separate(str);
        initial = p.genPhoneme(elements[0]);
        finale = p.genPhoneme(elements[1]);
        tone = p.genPhoneme(elements[2]);
        phonemes = new Phoneme[]{initial, finale, tone};
    }

    public Phoneme[] phonemes() {
        return phonemes;
    }

    public static Pinyin[] get(char ch, PinIn p) {
        String[] ss = data[(int) ch];
        Pinyin[] ret = new Pinyin[ss.length];
        for (int i = 0; i < ss.length; i++)
            ret[i] = p.genPinyin(ss[i]);
        return ret;
    }

    public IndexSet match(String str, int start) {
        IndexSet ret = new IndexSet(0x1);
        ret = initial.match(str, ret, start);
        ret.merge(finale.match(str, ret, start));
        ret.merge(tone.match(str, ret, start));
        return ret;
    }

    public char start() {
        String ret = initial.toString();
        if (ret.isEmpty()) ret = finale.toString();
        return ret.charAt(0);
    }

    @Override
    public String toString() {
        return "" + initial + finale + tone;
    }
}
