package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.util.Collections;
import java.util.HashSet;

import static me.towdium.pinin.utils.Matcher.strCmp;

public class Phoneme implements Element {
    String[] strs;

    @Override
    public String toString() {
        return strs[0];
    }

    public Phoneme(String str, PinIn p) {
        HashSet<String> ret = new HashSet<>();
        ret.add(str);

        if (p.fCh2C && str.startsWith("c")) Collections.addAll(ret, "c", "ch");
        if (p.fSh2S && str.startsWith("s")) Collections.addAll(ret, "s", "sh");
        if (p.fZh2Z && str.startsWith("z")) Collections.addAll(ret, "z", "zh");
        if (p.fU2V && str.startsWith("v"))
            ret.add("u" + str.substring(1));
        if ((p.fAng2An && str.endsWith("ang"))
                || (p.fEng2En && str.endsWith("eng"))
                || (p.fIng2In && str.endsWith("ing")))
            ret.add(str.substring(0, str.length() - 1));
        if ((p.fAng2An && str.endsWith("an"))
                || (p.fEng2En && str.endsWith("en"))
                || (p.fIng2In && str.endsWith("in")))
            ret.add(str + 'g');
        strs = ret.stream().map(p.keyboard::keys).toArray(String[]::new);
    }

    public IndexSet match(String source, IndexSet idx, int start) {
        if (strs.length == 1 && strs[0].isEmpty()) return new IndexSet(idx);
        IndexSet ret = new IndexSet();
        idx.traverse(i -> {
            IndexSet is = match(source, start + i);
            is.offset(i);
            ret.merge(is);
            return true;
        });
        return ret;
    }

    public boolean isEmpty() {
        return strs.length == 1 && strs[0].isEmpty();
    }

    @Override
    public IndexSet match(String source, int start) {
        IndexSet ret = new IndexSet();
        if (strs.length == 1 && strs[0].isEmpty()) return ret;
        for (String str : strs) {
            int size = strCmp(source, str, start);
            if (start + size == source.length()) ret.set(size);  // ending match
            else if (size == str.length()) ret.set(size); // full match
        }
        return ret;
    }
}
