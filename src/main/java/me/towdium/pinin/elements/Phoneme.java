package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.Cache;
import me.towdium.pinin.utils.IndexSet;

import java.util.Collections;
import java.util.HashSet;

import static me.towdium.pinin.utils.Matcher.strCmp;

public class Phoneme {
    private static Cache<String, Phoneme> cache = new Cache<>(Phoneme::new);

    String[] strs;

    @Override
    public String toString() {
        return strs[0];
    }

    public Phoneme(String str) {
        HashSet<String> ret = new HashSet<>();
        ret.add(str);

        if (PinIn.enableFuzzyCh2c && str.startsWith("c")) Collections.addAll(ret, "c", "ch");
        if (PinIn.enableFuzzySh2s && str.startsWith("s")) Collections.addAll(ret, "s", "sh");
        if (PinIn.enableFuzzyZh2z && str.startsWith("z")) Collections.addAll(ret, "z", "zh");
        if (PinIn.enableFuzzyU2v && str.startsWith("v"))
            ret.add("u" + str.substring(1));
        if ((PinIn.enableFuzzyAng2an && str.endsWith("ang"))
                || (PinIn.enableFuzzyEng2en && str.endsWith("eng"))
                || (PinIn.enableFuzzyIng2in && str.endsWith("ing")))
            ret.add(str.substring(0, str.length() - 1));
        if ((PinIn.enableFuzzyAng2an && str.endsWith("an"))
                || (str.endsWith("en") && PinIn.enableFuzzyEng2en)
                || (str.endsWith("in") && PinIn.enableFuzzyIng2in))
            ret.add(str + 'g');
        strs = ret.stream().map(PinIn.getKeyboard()::keys).toArray(String[]::new);
    }

    public static Phoneme get(String str) {
        return cache.get(str);
    }

    public static void refresh() {
        cache.clear();
    }

    IndexSet match(String source, IndexSet idx, int start) {
        if (strs.length == 1 && strs[0].isEmpty()) return new IndexSet(idx);
        else {
            IndexSet ret = new IndexSet();
            idx.foreach(i -> {
                for (String str : strs) {
                    int size = strCmp(source, str, i + start);
                    if (i + start + size == source.length()) ret.set(i + size);  // ending match
                    else if (size == str.length()) ret.set(i + size); // full match
                }
                return true;
            });
            return ret;
        }
    }
}
