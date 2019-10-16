package me.towdium.pinin;

import me.towdium.pinin.elements.Pinyin;

public class PinIn {
    static PinyinSpell keyboard = PinyinSpell.QUANPIN;
    public static boolean enableFuzzyZh2z = false;
    public static boolean enableFuzzySh2s = false;
    public static boolean enableFuzzyCh2c = false;
    public static boolean enableFuzzyAng2an = false;
    public static boolean enableFuzzyIng2in = false;
    public static boolean enableFuzzyEng2en = false;
    public static boolean enableFuzzyU2v = false;

    public static PinyinSpell getKeyboard() {
        return keyboard;
    }

    public static void setKeyboard(PinyinSpell keyboard) {
        Pinyin.refresh();
        PinyinTree.refresh();
        PinIn.keyboard = keyboard;
    }
}
