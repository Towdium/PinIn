package me.towdium.pinin.utils;

import me.towdium.pinin.elements.Pinyin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PinyinFormat {
    // finals with tones on the second character
    private static final Set<String> OFFSET = Stream.of(new String[]{
            "ui", "iu", "uan", "uang", "ian", "iang", "ua",
            "ie", "uo", "iong", "iao", "ve", "ia"
    }).collect(Collectors.toSet());

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

    private static final Map<String, String> SYMBOLS = Stream.of(new String[][]{
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
    }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

    private static final Map<String, String> LOCAL = Stream.of(new String[][]{
            {"yi", "i"}, {"you", "iu"}, {"yin", "in"}, {"ye", "ie"}, {"ying", "ing"},
            {"wu", "u"}, {"wen", "un"}, {"yu", "v"}, {"yue", "ve"}, {"yuan", "van"},
            {"yun", "vn"}, {"ju", "jv"}, {"jue", "jve"}, {"juan", "jvan"}, {"jun", "jvn"},
            {"qu", "qv"}, {"que", "qve"}, {"quan", "qvan"}, {"qun", "qvn"}, {"xu", "xv"},
            {"xue", "xve"}, {"xuan", "xvan"}, {"xun", "xvn"}, {"shi", "sh"}, {"si", "s"},
            {"chi", "ch"}, {"ci", "c"}, {"zhi", "zh"}, {"zi", "z"}, {"ri", "r"}
    }).collect(Collectors.toMap(d -> d[0], d -> d[1]));

    public static final PinyinFormat RAW = new PinyinFormat() {
        @Override
        public String format(Pinyin p) {
            return p.toString().substring(0, p.toString().length() - 1);
        }
    };

    public static final PinyinFormat NUMBER = new PinyinFormat() {
        @Override
        public String format(Pinyin p) {
            return p.toString();
        }
    };

    public static final PinyinFormat PHONETIC = new PinyinFormat() {
        @Override
        public String format(Pinyin p) {
            String s = p.toString();
            String str = LOCAL.get(s.substring(0, s.length() - 1));
            if (str != null) s = str + s.charAt(s.length() - 1);
            StringBuilder sb = new StringBuilder();

            String[] split;
            int len = s.length();
            if (!Pinyin.hasInitial(s)) {
                split = new String[]{"", s.substring(0, len - 1), s.substring(len - 1)};
            } else {
                int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
                split = new String[]{s.substring(0, i), s.substring(i, len - 1), s.substring(len - 1)};
            }
            boolean weak = split[2].equals("0");
            if (weak) sb.append(SYMBOLS.get(split[2]));
            sb.append(SYMBOLS.get(split[0]));
            sb.append(SYMBOLS.get(split[1]));
            if (!weak) sb.append(SYMBOLS.get(split[2]));
            return sb.toString();
        }
    };

    public static final PinyinFormat UNICODE = new PinyinFormat() {
        @Override
        public String format(Pinyin p) {
            StringBuilder sb = new StringBuilder();
            String s = p.toString();
            String finale;
            int len = s.length();

            if (!Pinyin.hasInitial(s)) {
                finale = s.substring(0, len - 1);
            } else {
                int i = s.length() > 2 && s.charAt(1) == 'h' ? 2 : 1;
                sb.append(s, 0, i);
                finale = s.substring(i, len - 1);
            }

            int offset = OFFSET.contains(finale) ? 1 : 0;
            if (offset == 1) sb.append(finale, 0, 1);
            Map<Character, Character> group = TONES.get(s.charAt(s.length() - 1) - '0');
            sb.append(group.get(finale.charAt(offset)));
            if (finale.length() > offset + 1) sb.append(finale, offset + 1, finale.length());
            return sb.toString();
        }
    };

    public abstract String format(Pinyin p);
}