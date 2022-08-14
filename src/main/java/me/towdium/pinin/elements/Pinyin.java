package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class Pinyin implements Element {
    boolean duo = false;
    boolean sequence = false;
    public final int id;
    String raw;

    private Phoneme[] phonemes;

    public Pinyin(String str, PinIn p, int id) {
        raw = str;
        this.id = id;
        reload(str, p);
    }

    public Phoneme[] phonemes() {
        return phonemes;
    }

    public IndexSet match(String str, int start, boolean partial) {
        IndexSet ret;
        if (duo) {
            // in shuangpin we require initial and final both present,
            // the phoneme, which is tone here, is optional
            ret = IndexSet.ZERO;
            ret = phonemes[0].match(str, ret, start, partial);
            ret = phonemes[1].match(str, ret, start, partial);
            ret.merge(phonemes[2].match(str, ret, start, partial));
        } else {
            // in other keyboards, match of precedent phoneme
            // is compulsory to match subsequent phonemes
            // for example, zhong1, z+h+ong+1 cannot match zong or zh1
            IndexSet active = IndexSet.ZERO;
            ret = new IndexSet();
            for (Phoneme phoneme : phonemes) {
                active = phoneme.match(str, active, start, partial);
                if (active.isEmpty()) break;
                ret.merge(active);
            }
        }
        if (sequence && phonemes[0].matchSequence(str.charAt(start))) {
            ret.set(1);
        }

        return ret;
    }

    @Override
    public String toString() {
        return raw;
    }

    public void reload(String str, PinIn p) {
        Collection<String> split = p.keyboard().split(str);
        List<Phoneme> l = new ArrayList<>();
        for (String s : split) l.add(p.getPhoneme(s));
        phonemes = l.toArray(new Phoneme[]{});

        duo = p.keyboard().duo;
        sequence = p.keyboard().sequence;
    }

    public static boolean hasInitial(String s) {
        return Stream.of('a', 'e', 'i', 'o', 'u', 'v').noneMatch(i -> s.charAt(0) == i);
    }
}
