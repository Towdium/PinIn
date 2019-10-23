package me.towdium.pinin;

import java.util.HashMap;
import java.util.Objects;

public enum PinyinKeyboard {

    QUANPIN, DAQIAN;

    private static HashMap<String, String> PHONETIC_PHONEME = new java.util.HashMap<String, String>() {{
        put("", "");
        put("0", "");
        put("1", " ");
        put("2", "6");
        put("3", "3");
        put("4", "4");
        put("a", "8");
        put("ai", "9");
        put("an", "0");
        put("ang", ";");
        put("ao", "l");
        put("b", "1");
        put("c", "h");
        put("ch", "t");
        put("d", "2");
        put("e", "k");
        put("ei", "o");
        put("en", "p");
        put("eng", "/");
        put("er", "-");
        put("f", "z");
        put("g", "e");
        put("h", "c");
        put("i", "u");
        put("ia", "u8");
        put("ian", "u0");
        put("iang", "u;");
        put("iao", "ul");
        put("ie", "u,");
        put("in", "up");
        put("ing", "u/");
        put("iong", "m/");
        put("iu", "u.");
        put("j", "r");
        put("k", "d");
        put("l", "x");
        put("m", "a");
        put("n", "s");
        put("o", "i");
        put("ong", "j/");
        put("ou", ".");
        put("p", "q");
        put("q", "f");
        put("r", "b");
        put("s", "n");
        put("sh", "g");
        put("t", "w");
        put("u", "j");
        put("ua", "j8");
        put("uai", "j9");
        put("uan", "j0");
        put("uang", "j;");
        put("uen", "mp");
        put("ueng", "j/");
        put("ui", "jo");
        put("un", "jp");
        put("uo", "ji");
        put("v", "m");
        put("van", "m0");
        put("vang", "m;");
        put("ve", "m,");
        put("vn", "mp");
        put("w", "j");
        put("x", "v");
        put("y", "u");
        put("z", "y");
        put("zh", "5");
    }};

    private static HashMap<String, String> PHONETIC_SPELL = new HashMap<String, String>() {{
        put("yi", "i");
        put("you", "iu");
        put("yin", "in");
        put("ye", "ie");
        put("ying", "ing");
        put("wu", "u");
        put("wen", "un");
        put("yu", "v");
        put("yue", "ve");
        put("yuan", "van");
        put("yun", "vn");
        put("ju", "jv");
        put("jue", "jve");
        put("juan", "jvan");
        put("jun", "jvn");
        put("qu", "qv");
        put("que", "qve");
        put("quan", "qvan");
        put("qun", "qvn");
        put("xu", "xv");
        put("xue", "xve");
        put("xuan", "xvan");
        put("xun", "xvn");
        put("shi", "sh");
        put("si", "s");
        put("chi", "ch");
        put("ci", "c");
        put("zhi", "zh");
        put("zi", "z");
        put("ri", "r");
    }};

    public String[] separate(String s) {
        if (this == DAQIAN) {
            String str = PHONETIC_SPELL.get(s.substring(0, s.length() - 1));
            if (str != null) s = str + s.charAt(s.length() - 1);
        }

        if (s.startsWith("a") || s.startsWith("e") || s.startsWith("i")
                || s.startsWith("o") || s.startsWith("u")) {
            return new String[]{"", s.substring(0, s.length() - 1), s.substring(s.length() - 1)};
        } else {
            int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
            return new String[]{s.substring(0, i), s.substring(i, s.length() - 1), s.substring(s.length() - 1)};
        }
    }

    public String keys(String s) {
        if (this == QUANPIN) return s;
        else return Objects.requireNonNull(PHONETIC_PHONEME.get(s),
                "Unrecognized elements: " + s);
    }
}
