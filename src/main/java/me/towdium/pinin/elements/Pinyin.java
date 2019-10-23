package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    private Phoneme[] phonemes;

    public Pinyin(String str, PinIn p) {
        List<Phoneme> l = new ArrayList<>();
        for (String s: p.keyboard.separate(str)) {
            Phoneme ph = p.genPhoneme(s);
            if (!ph.isEmpty()) l.add(ph);
        }
        phonemes = l.toArray(new Phoneme[]{});
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
        ret = phonemes[0].match(str, ret, start);
        for (int i = 1; i < phonemes.length; i++)
            ret.merge(phonemes[i].match(str, ret, start));
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (Phoneme p : phonemes) ret.append(p);
        return ret.toString();
    }
}
