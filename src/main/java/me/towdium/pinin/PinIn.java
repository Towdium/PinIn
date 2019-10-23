package me.towdium.pinin;

import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.Cache;
import me.towdium.pinin.utils.Matcher;

public class PinIn {
    private Cache<String, Phoneme> cPhoneme = new Cache<>(s -> new Phoneme(s, this));
    private Cache<String, Pinyin> cPinyin = new Cache<>(s -> new Pinyin(s, this));
    private Cache<Character, Char> cChar = new Cache<>(s -> new Char(s, this));

    public final PinyinKeyboard keyboard;
    public final boolean fZh2Z;
    public final boolean fSh2S;
    public final boolean fCh2C;
    public final boolean fAng2An;
    public final boolean fIng2In;
    public final boolean fEng2En;
    public final boolean fU2V;

    public PinIn() {
        this(PinyinKeyboard.QUANPIN, false, false, false,
                false, false, false, false);
    }

    public PinIn(PinyinKeyboard keyboard, boolean fZh2Z, boolean fSh2S, boolean fCh2C,
                 boolean fAng2An, boolean fIng2In, boolean fEng2En, boolean fU2V) {
        this.keyboard = keyboard;
        this.fZh2Z = fZh2Z;
        this.fSh2S = fSh2S;
        this.fCh2C = fCh2C;
        this.fAng2An = fAng2An;
        this.fIng2In = fIng2In;
        this.fEng2En = fEng2En;
        this.fU2V = fU2V;
    }

    public boolean contains(String s1, CharSequence s2) {
        return Matcher.check(s1, s2, this, true);
    }

    public boolean begins(String s1, CharSequence s2) {
        return Matcher.check(s1, s2, this, false);
    }

    public Phoneme genPhoneme(String s) {
        return cPhoneme.get(s);
    }

    public Pinyin genPinyin(String s) {
        return cPinyin.get(s);
    }

    public Char genChar(char c) {
        return cChar.get(c);
    }

    public static void initialize() {
        //noinspection ResultOfMethodCallIgnored
        Pinyin.class.getClass();
    }

    public static class Builder {
        public PinyinKeyboard keyboard;
        public boolean fZh2Z;
        public boolean fSh2S;
        public boolean fCh2C;
        public boolean fAng2An;
        public boolean fIng2In;
        public boolean fEng2En;
        public boolean fU2V;

        public Builder setKeyboard(PinyinKeyboard keyboard) {
            this.keyboard = keyboard;
            return this;
        }

        public Builder setfZh2Z(boolean fZh2Z) {
            this.fZh2Z = fZh2Z;
            return this;
        }

        public Builder setfSh2S(boolean fSh2S) {
            this.fSh2S = fSh2S;
            return this;
        }

        public Builder setfCh2C(boolean fCh2C) {
            this.fCh2C = fCh2C;
            return this;
        }

        public Builder setfAng2An(boolean fAng2An) {
            this.fAng2An = fAng2An;
            return this;
        }

        public Builder setfIng2In(boolean fIng2In) {
            this.fIng2In = fIng2In;
            return this;
        }

        public Builder setfEng2En(boolean fEng2En) {
            this.fEng2En = fEng2En;
            return this;
        }

        public Builder setfU2V(boolean fU2V) {
            this.fU2V = fU2V;
            return this;
        }

        public PinIn build() {
            return new PinIn(keyboard, fZh2Z, fSh2S, fCh2C,
                    fAng2An, fIng2In, fEng2En, fU2V);
        }
    }
}
