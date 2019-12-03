package me.towdium.pinin.elements;

import me.towdium.pinin.PinIn;
import me.towdium.pinin.utils.IndexSet;
import me.towdium.pinin.utils.Slice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    String raw;

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
        raw = str;
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

    public String format(Format f) {
        return f.format(this);
    }

    public static abstract class Format {
        private static final Set<Slice> OFFSET = Stream.of(new String[]{
                "ui", "iu", "uan", "uang", "ian", "iang", "ua",
                "ie", "uo", "iong", "iao", "ve", "ia"
        }).map(Slice::new).collect(Collectors.toSet());


        private static final Map<Character, Character> NONE = Stream.of(new Character[][]{
                {'a', 'a'}, {'o', 'o'}, {'e', 'e'}, {'i', 'i'}, {'u', 'u'}, {'v', 'ü'}
        }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

        private static final Map<Character, Character> FIRST = Stream.of(new Character[][]{
                {'a', 'ā'}, {'o', 'ō'}, {'e', 'ē'}, {'i', 'ī'}, {'u', 'ū'}, {'v', 'ǖ'}
        }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

        private static final Map<Character, Character> SECOND = Stream.of(new Character[][]{
                {'a', 'á'}, {'o', 'ó'}, {'e', 'é'}, {'i', 'í'}, {'u', 'ú'}, {'v', 'ǘ'}
        }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

        private static final Map<Character, Character> THIRD = Stream.of(new Character[][]{
                {'a', 'ǎ'}, {'o', 'ǒ'}, {'e', 'ě'}, {'i', 'ǐ'}, {'u', 'ǔ'}, {'v', 'ǚ'}
        }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

        private static final Map<Character, Character> FOURTH = Stream.of(new Character[][]{
                {'a', 'à'}, {'o', 'ò'}, {'e', 'è'}, {'i', 'ì'}, {'u', 'ù'}, {'v', 'ǜ'}
        }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

        private static final List<Map<Character, Character>> TONES =
                Stream.of(NONE, FIRST, SECOND, THIRD, FOURTH)
                        .collect(Collectors.toList());

        private static final Map<Slice, String> SYMBOLS = Stream.of(new String[][]{
                {"a", "ㄚ"}, {"o", "ㄛ"}, {"e", "ㄜ"}, {"er", "ㄦ"}, {"ai", "ㄞ"},
                {"ei", "ㄟ"}, {"ao", "ㄠ"}, {"ou", "ㄡ"}, {"an", "ㄢ"}, {"en", "ㄣ"},
                {"ang", "ㄤ"}, {"eng", "ㄥ"}, {"ong", "ㄨㄥ"}, {"i", "ㄧ"}, {"ia", "ㄧㄚ"},
                {"iao", "ㄧㄠ"}, {"ie", "ㄧㄝ"}, {"iu", "ㄧㄡ"}, {"ian", "ㄧㄢ"}, {"in", "ㄧㄣ"},
                {"iang", "ㄧㄤ"}, {"ing", "ㄧㄥ"}, {"iong", "ㄩㄥ"}, {"u", "ㄨ"}, {"ua", "ㄨㄚ"},
                {"uo", "ㄨㄛ"}, {"uai", "ㄨㄞ"}, {"ui", "ㄨㄟ"}, {"uan", "ㄨㄢ"}, {"un", "ㄨㄣ"},
                {"uang", "ㄨㄤ"}, {"ueng", "ㄨㄥ"}, {"uen", "ㄩㄣ"}, {"v", "ㄩ"}, {"ve", "ㄩㄝ"},
                {"van", "ㄩㄢ"}, {"vang", "ㄩㄤ"}, {"vn", "ㄩㄣ"}, {"b", "ㄅ"}, {"p", "ㄆ"},
                {"m", "ㄇ"}, {"f", "ㄈ"}, {"d", "ㄉ"}, {"t", "ㄊ"}, {"n", "ㄋ"},
                {"l", "ㄌ"}, {"g", "ㄍ"}, {"k", "ㄎ"}, {"h", "ㄏ"}, {"j", "ㄐ"},
                {"q", "ㄑ"}, {"x", "ㄒ"}, {"zh", "ㄓ"}, {"ch", "ㄔ"}, {"sh", "ㄕ"},
                {"r", "ㄖ"}, {"z", "ㄗ"}, {"c", "ㄘ"}, {"s", "ㄙ"}, {"w", "ㄨ"},
                {"y", "ㄧ"}, {"1", ""}, {"2", "ˊ"}, {"3", "ˇ"}, {"4", "ˋ"},
                {"0", "˙"}, {"", ""}
        }).collect(Collectors.toMap(d -> new Slice(d[0]), d -> d[1]));

        private static final Map<Slice, String> LOCAL = Stream.of(new String[][]{
                {"yi", "i"}, {"you", "iu"}, {"yin", "in"}, {"ye", "ie"}, {"ying", "ing"},
                {"wu", "u"}, {"wen", "un"}, {"yu", "v"}, {"yue", "ve"}, {"yuan", "van"},
                {"yun", "vn"}, {"ju", "jv"}, {"jue", "jve"}, {"juan", "jvan"}, {"jun", "jvn"},
                {"qu", "qv"}, {"que", "qve"}, {"quan", "qvan"}, {"qun", "qvn"}, {"xu", "xv"},
                {"xue", "xve"}, {"xuan", "xvan"}, {"xun", "xvn"}, {"shi", "sh"}, {"si", "s"},
                {"chi", "ch"}, {"ci", "c"}, {"zhi", "zh"}, {"zi", "z"}, {"ri", "r"}
        }).collect(Collectors.toMap(d -> new Slice(d[0]), d -> d[1]));

        public static final Format RAW = new Format() {
            @Override
            public String format(Pinyin p) {
                return p.raw.substring(0, p.raw.length() - 1);
            }
        };

        public static final Format NUMBER = new Format() {
            @Override
            public String format(Pinyin p) {
                return p.raw;
            }
        };

        public static final Format PHONETIC = new Format() {
            @Override
            public String format(Pinyin p) {
                String s = p.raw;
                String str = LOCAL.get(new Slice(s, 0, -1));
                if (str != null) s = str + s.charAt(s.length() - 1);
                StringBuilder sb = new StringBuilder();

                Slice[] split;
                if (s.startsWith("a") || s.startsWith("e") || s.startsWith("i")
                        || s.startsWith("o") || s.startsWith("u")) {
                    split = new Slice[]{new Slice(""), new Slice(s, 0, -1), new Slice(s, -1)};
                } else {
                    int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
                    split = new Slice[]{new Slice(s, 0, i), new Slice(s, i, -1), new Slice(s, -1)};
                }
                @SuppressWarnings("EqualsBetweenInconvertibleTypes")
                boolean weak = split[2].equals("0");
                if (weak) sb.append(SYMBOLS.get(split[2]));
                sb.append(SYMBOLS.get(split[0]));
                sb.append(SYMBOLS.get(split[1]));
                if (!weak) sb.append(SYMBOLS.get(split[2]));
                return sb.toString();
            }
        };

        public static final Format UNICODE = new Format() {
            @Override
            public String format(Pinyin p) {
                StringBuilder sb = new StringBuilder();
                String s = p.raw;
                Slice vowel;

                if (s.startsWith("a") || s.startsWith("e") || s.startsWith("i")
                        || s.startsWith("o") || s.startsWith("u")) {
                    vowel = new Slice(s, 0, -1);
                } else {
                    int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
                    sb.append(s, 0, i);
                    vowel = new Slice(s, i, -1);
                }

                int offset = OFFSET.contains(vowel) ? 1 : 0;
                if (offset == 1) vowel.append(sb, 0, 1);
                Map<Character, Character> group = TONES.get(s.charAt(s.length() - 1) - '0');
                sb.append(group.get(vowel.charAt(offset)));
                if (vowel.length() > offset + 1) vowel.append(sb, offset + 1, vowel.length());
                return sb.toString();
            }
        };

        public abstract String format(Pinyin p);
    }
}
