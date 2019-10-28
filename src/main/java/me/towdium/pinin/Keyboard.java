package me.towdium.pinin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Keyboard {
    public static Keyboard QUANPIN = new Keyboard(null, null, Keyboard::standard, false);
    private static HashMap<String, String> DAQIAN_KEYS = new HashMap<String, String>() {{
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
    private static HashMap<String, String> XIAOHE_KEYS = new HashMap<String, String>() {{
        put("ai", "d");
        put("an", "j");
        put("ang", "h");
        put("ao", "c");
        put("ch", "i");
        put("ei", "w");
        put("en", "f");
        put("eng", "g");
        put("ia", "x");
        put("ian", "m");
        put("iang", "l");
        put("iao", "n");
        put("ie", "p");
        put("in", "b");
        put("ing", "k");
        put("iong", "s");
        put("iu", "q");
        put("ong", "s");
        put("ou", "z");
        put("sh", "u");
        put("ua", "x");
        put("uai", "k");
        put("uan", "r");
        put("uang", "l");
        put("ui", "v");
        put("un", "y");
        put("uo", "o");
        put("ve", "t");
        put("ue", "t");
        put("vn", "y");
        put("zh", "v");
    }};
    public static Keyboard XIAOHE = new Keyboard(null, XIAOHE_KEYS, Keyboard::zero, true);
    private static HashMap<String, String> ZIRANMA_KEYS = new HashMap<String, String>() {{
        put("ai", "l");
        put("an", "j");
        put("ang", "h");
        put("ao", "k");
        put("ch", "i");
        put("ei", "z");
        put("en", "f");
        put("eng", "g");
        put("ia", "w");
        put("ian", "m");
        put("iang", "d");
        put("iao", "c");
        put("ie", "x");
        put("in", "n");
        put("ing", "y");
        put("iong", "s");
        put("iu", "q");
        put("ong", "s");
        put("ou", "b");
        put("sh", "u");
        put("ua", "w");
        put("uai", "y");
        put("uan", "r");
        put("uang", "d");
        put("ui", "v");
        put("un", "p");
        put("uo", "o");
        put("ve", "t");
        put("ue", "t");
        put("vn", "p");
        put("zh", "v");
    }};
    public static Keyboard ZIRANMA = new Keyboard(null, ZIRANMA_KEYS, Keyboard::zero, true);
    private static HashMap<String, String> PHONETIC_LOCAL = new HashMap<String, String>() {{
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
    public static Keyboard DAQIAN = new Keyboard(PHONETIC_LOCAL, DAQIAN_KEYS, Keyboard::standard, false);
    public final boolean duo;
    final Map<String, String> local;
    final Map<String, String> keys;
    final Function<String, List<String>> cutter;

    public Keyboard(Map<String, String> local, Map<String, String> keys,
                    Function<String, List<String>> cutter, boolean duo) {
        this.local = local;
        this.keys = keys;
        this.cutter = cutter;
        this.duo = duo;
    }

    public static List<String> standard(String s) {
        if (s.startsWith("a") || s.startsWith("e") || s.startsWith("i")
                || s.startsWith("o") || s.startsWith("u")) {
            return Arrays.asList(s.substring(0, s.length() - 1), s.substring(s.length() - 1));
        } else {
            int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
            return Arrays.asList(s.substring(0, i), s.substring(i, s.length() - 1), s.substring(s.length() - 1));
        }
    }

    public static List<String> zero(String s) {
        List<String> ss = standard(s);
        if (ss.size() == 3) return ss;
        else {
            String vowel = ss.get(0);
            if (vowel.length() == 1) ss.add(0, vowel);
            else {
                ss.add(0, Character.toString(vowel.charAt(0)));
                if (vowel.length() == 2) ss.set(1, Character.toString(vowel.charAt(1)));
            }
            return ss;
        }
    }

    public String[] split(String s) {
        return cutter.apply(local == null ? s : local.getOrDefault(s, s)).toArray(new String[]{});
    }

    public String keys(String s) {
        return keys == null ? s : keys.getOrDefault(s, s);
    }
}
