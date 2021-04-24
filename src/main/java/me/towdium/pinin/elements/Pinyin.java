package me.towdium.pinin.elements;

import me.towdium.pinin.Keyboard;
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
        if (duo) {
            // in shuangpin we require initial and final both present,
            // the phoneme, which is tone here, is optional
            IndexSet ret = IndexSet.ZERO;
            ret = phonemes[0].match(str, ret, start, partial);
            ret = phonemes[1].match(str, ret, start, partial);
            ret.merge(phonemes[2].match(str, ret, start, partial));
            return ret;
        } else {
            // in other keyboards, match of precedent phoneme
            // is compulsory to match subsequent phonemes
            // for example, zhong1, z+h+ong+1 cannot match zong or zh1
            IndexSet active = IndexSet.ZERO;
            IndexSet ret = new IndexSet();
            for (Phoneme phoneme : phonemes) {
                active = phoneme.match(str, active, start, partial);
                if (active.isEmpty()) break;
                ret.merge(active);
            }
            return ret;
        }
    }

    @Override
    public String toString() {
        return raw;
    }

    public void reload(String str, PinIn p) {
        Collection<String> split = p.keyboard().split(str);
        List<Phoneme> l = new ArrayList<>();
        for (String s : split) l.add(p.getPhoneme(s));
        if (str.charAt(1) == 'h' && p.keyboard() == Keyboard.QUANPIN) {
            // here we implement sequence matching in quanpin, with a dirty trick
            // if initial is one of 'zh' 'sh' 'ch', and fuzzy is not on, we slice it
            // the first is one if 'z' 's' 'c', and the second is 'h'
            boolean slice;
            char sequence = str.charAt(0);
            switch (sequence) {
                case 'z':
                    slice = !p.fZh2Z();
                    break;
                case 'c':
                    slice = !p.fCh2C();
                    break;
                case 's':
                    slice = !p.fSh2S();
                    break;
                default:
                    slice = false;
            }
            if (slice) {
                l.set(0, p.getPhoneme(Character.toString(sequence)));
                l.add(1, p.getPhoneme("h"));
            }
        }
        phonemes = l.toArray(new Phoneme[]{});

        duo = p.keyboard().duo;
    }

    public static boolean hasInitial(String s) {
        return Stream.of('a', 'e', 'i', 'o', 'u', 'v').noneMatch(i -> s.charAt(0) == i);
    }
}
