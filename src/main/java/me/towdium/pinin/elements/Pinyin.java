package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static me.towdium.pinin.PinIn.MAX;
import static me.towdium.pinin.PinIn.MIN;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class Pinyin implements Element {
    private static String[][] data;
    private static final String[] EMPTY = new String[0];
    boolean duo = false;
    public final int id;

    static {
        data = new String[MAX - MIN][];
        String resourceName = "data.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinIn.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                char ch = line.charAt(0);
                String sounds = line.substring(3);
                data[ch - MIN] = sounds.split(", ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < data.length; i++)
            if (data[i] == null) data[i] = EMPTY;
    }

    private Phoneme[] phonemes;

    public Pinyin(String str, PinIn p, int id) {
        reload(str, p);
        this.id = id;
    }

    public Phoneme[] phonemes() {
        return phonemes;
    }

    public static Pinyin[] get(char ch, PinIn p) {
        String[] ss = data[(int) ch - MIN];
        Pinyin[] ret = new Pinyin[ss.length];
        for (int i = 0; i < ss.length; i++)
            ret[i] = p.genPinyin(ss[i]);
        return ret;
    }

    public IndexSet match(String str, int start, boolean partial) {
        IndexSet ret = new IndexSet(0x1);
        ret = phonemes[0].match(str, ret, start, partial);
        if (duo) ret = phonemes[1].match(str, ret, start, partial);
        else ret.merge(phonemes[1].match(str, ret, start, partial));
        if (phonemes.length == 3) ret.merge(phonemes[2].match(str, ret, start, partial));
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (Phoneme p : phonemes) ret.append(p);
        return ret.toString();
    }

    public void reload(String str, PinIn p) {
        List<Phoneme> l = new ArrayList<>();
        for (String s : p.keyboard().split(str)) {
            Phoneme ph = p.genPhoneme(s);
            if (!ph.isEmpty()) l.add(ph);
        }
        phonemes = l.toArray(new Phoneme[]{});
        duo = p.keyboard().duo;
    }
}
