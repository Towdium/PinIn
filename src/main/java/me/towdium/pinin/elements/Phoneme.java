package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;

import java.util.Collections;
import java.util.HashSet;

public class Phoneme implements Element {

    String[] strs;

    @Override
    public String toString() {
        return strs[0];
    }

    public Phoneme(String str, PinIn p) {
        reload(str, p);
    }

    public IndexSet match(String source, IndexSet idx, int start, boolean partial) {
        if (strs.length == 1 && strs[0].isEmpty()) return new IndexSet(idx);
        IndexSet ret = new IndexSet();
        idx.foreach(i -> {
            IndexSet is = match(source, start + i, partial);
            is.offset(i);
            ret.merge(is);
        });
        return ret;
    }

    public boolean matchSequence(char c) {
        for (String str : strs) {
            if (str.charAt(0) == c) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return strs.length == 1 && strs[0].isEmpty();
    }

    static int strCmp(String a, String b, int aStart) {
        int len = Math.min(a.length() - aStart, b.length());
        for (int i = 0; i < len; i++)
            if (a.charAt(i + aStart) != b.charAt(i)) return i;
        return len;
    }

    @Override
    public IndexSet match(String source, int start, boolean partial) {
        IndexSet ret = new IndexSet();
        if (strs.length == 1 && strs[0].isEmpty()) return ret;
        for (String str : strs) {
            int size = strCmp(source, str, start);
            if (partial && size != 0 && start + size == source.length()) ret.set(size);  // ending match
            else if (size == str.length()) ret.set(size); // full match
        }
        return ret;
    }

    public void reload(String str, PinIn p) {
        HashSet<String> ret = new HashSet<>();
        ret.add(str);

        if (p.fCh2C() && str.startsWith("c")) Collections.addAll(ret, "c", "ch");
        if (p.fSh2S() && str.startsWith("s")) Collections.addAll(ret, "s", "sh");
        if (p.fZh2Z() && str.startsWith("z")) Collections.addAll(ret, "z", "zh");
        if (p.fU2V() && str.startsWith("v"))
            ret.add("u" + str.substring(1));
        if ((p.fAng2An() && str.endsWith("ang"))
                || (p.fEng2En() && str.endsWith("eng"))
                || (p.fIng2In() && str.endsWith("ing")))
            ret.add(str.substring(0, str.length() - 1));
        if ((p.fAng2An() && str.endsWith("an"))
                || (p.fEng2En() && str.endsWith("en"))
                || (p.fIng2In() && str.endsWith("in")))
            ret.add(str + 'g');
        strs = ret.stream().map(p.keyboard()::keys).toArray(String[]::new);
    }
}
