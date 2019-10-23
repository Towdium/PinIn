package me.towdium.pinin.utils;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.elements.Element;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class Matcher {
    public static boolean contains(String s1, CharSequence s2, PinIn p) {
        boolean ret;
        if (Matcher.isChinese(s1)) ret = check(s1, s2.toString(), p);
        else ret = s1.contains(s2);
        return ret;
    }

    public static boolean isChinese(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (isChinese(s.charAt(i))) return true;
        }
        return false;
    }

    public static boolean isChinese(char i) {
        return 0x3007 <= i && i < 0x9FA5;
    }

    public static int strCmp(String a, String b, int aStart) {
        return strCmp(a, b, aStart, 0, Integer.MAX_VALUE);
    }

    public static int strCmp(String a, String b, int aStart, int bStart, int max) {
        int len = Math.min(a.length() - aStart, b.length() - bStart);
        len = Math.min(len, max);
        for (int i = 0; i < len; i++)
            if (a.charAt(i + aStart) != b.charAt(i + bStart)) return i;
        return len;
    }

    private static boolean check(String s1, CharSequence s2, PinIn p) {
        boolean b;
        if (s2 instanceof String) {
            if (s2.toString().isEmpty()) {
                b = true;
            } else {
                b = false;
                for (int i = 0; i < s1.length(); i++) {
                    if (check(s2.toString(), 0, s1, i, p)) {
                        b = true;
                        break;
                    }
                }
            }
        } else b = s1.contains(s2);
        return b;
    }

    public static boolean check(String s1, int start1, String s2, int start2, PinIn p) {
        if (start1 == s1.length()) return true;

        Element r = p.genChar(s2.charAt(start2));
        IndexSet s = r.match(s1, start1);

        if (start2 == s2.length() - 1) {
            int i = s1.length() - start1;
            return s.get(i);
        } else return !s.traverse(i -> !check(s1, start1 + i, s2, start2 + 1, p));
    }
}
