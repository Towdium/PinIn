package me.towdium.pinin.utils;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.elements.Element;

import static me.towdium.pinin.PinIn.MAX;
import static me.towdium.pinin.PinIn.MIN;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class Matcher {
    public static boolean isChinese(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; i--)
            if (isChinese(s.charAt(i))) return true;
        return false;
    }

    public static boolean isChinese(char i) {
        return MIN <= i && i < MAX;
    }

    public static int strCmp(String a, String b, int aStart, int bStart, int max) {
        int len = Math.min(a.length() - aStart, b.length() - bStart);
        len = Math.min(len, max);
        for (int i = 0; i < len; i++)
            if (a.charAt(i + aStart) != b.charAt(i + bStart)) return i;
        return len;
    }

    public static boolean begins(String s1, CharSequence s2, PinIn p) {
        String ss2 = s2.toString();
        if (isChinese(s1)) return check(s1, 0, ss2, 0, p, true);
        else return s1.startsWith(ss2);
    }

    public static boolean contains(String s1, CharSequence s2, PinIn p) {
        String ss2 = s2.toString();
        if (isChinese(s1)) {
            for (int i = 0; i < s1.length(); i++)
                if (check(s1, i, ss2, 0, p, true)) return true;
            return false;
        } else return s1.contains(s2);
    }

    public static boolean matches(String s1, String s2, PinIn p) {
        if (isChinese(s1)) return check(s1, 0, s2, 0, p, false);
        else return s1.equals(s2);
    }

    private static boolean check(String s1, int start1, String s2, int start2, PinIn p, boolean partial) {
        if (start2 == s2.length()) return partial || start1 == s1.length();

        Element r = p.genChar(s1.charAt(start1));
        IndexSet s = r.match(s2, start2, partial);

        if (start1 == s1.length() - 1) {
            int i = s2.length() - start2;
            return s.get(i);
        } else return !s.traverse(i -> !check(s1, start1 + 1, s2, start2 + i, p, partial));
    }
}
