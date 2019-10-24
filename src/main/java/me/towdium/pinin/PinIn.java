package me.towdium.pinin;

import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.Cache;
import me.towdium.pinin.utils.Matcher;

@SuppressWarnings("unused")
public class PinIn {
    private Cache<String, Phoneme> cPhoneme = new Cache<>(s -> new Phoneme(s, this));
    private Cache<String, Pinyin> cPinyin = new Cache<>(s -> new Pinyin(s, this));
    private Cache<Character, Char> cChar = new Cache<>(s -> new Char(s, this));

    private PinyinKeyboard keyboard;
    private boolean fZh2Z;
    private boolean fSh2S;
    private boolean fCh2C;
    private boolean fAng2An;
    private boolean fIng2In;
    private boolean fEng2En;
    private boolean fU2V;

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

    public PinyinKeyboard keyboard() {
        return keyboard;
    }

    public boolean fZh2Z() {
        return fZh2Z;
    }

    public boolean fSh2S() {
        return fSh2S;
    }

    public boolean fCh2C() {
        return fCh2C;
    }

    public boolean fAng2An() {
        return fAng2An;
    }

    public boolean fIng2In() {
        return fIng2In;
    }

    public boolean fEng2En() {
        return fEng2En;
    }

    public boolean fU2V() {
        return fU2V;
    }

    public Config config() {
        return new Config(this);
    }

    private void config(Config c) {
        this.keyboard = c.keyboard;
        this.fZh2Z = c.fZh2Z;
        this.fSh2S = c.fSh2S;
        this.fCh2C = c.fCh2C;
        this.fAng2An = c.fAng2An;
        this.fIng2In = c.fIng2In;
        this.fEng2En = c.fEng2En;
        this.fU2V = c.fU2V;
        cPhoneme.foreach((s, p) -> p.reload(s, this));
        cPinyin.foreach((s, p) -> p.reload(s, this));
    }

    public static class Config {
        public PinyinKeyboard keyboard;
        public boolean fZh2Z;
        public boolean fSh2S;
        public boolean fCh2C;
        public boolean fAng2An;
        public boolean fIng2In;
        public boolean fEng2En;
        public boolean fU2V;
        private PinIn context;

        public Config(PinIn p) {
            keyboard = p.keyboard;
            fZh2Z = p.fZh2Z;
            fSh2S = p.fSh2S;
            fCh2C = p.fCh2C;
            fAng2An = p.fAng2An;
            fIng2In = p.fIng2In;
            fEng2En = p.fEng2En;
            fU2V = p.fU2V;
            context = p;
        }

        public Config keyboard(PinyinKeyboard keyboard) {
            this.keyboard = keyboard;
            return this;
        }

        public Config fZh2Z(boolean fZh2Z) {
            this.fZh2Z = fZh2Z;
            return this;
        }

        public Config fSh2S(boolean fSh2S) {
            this.fSh2S = fSh2S;
            return this;
        }

        public Config fCh2C(boolean fCh2C) {
            this.fCh2C = fCh2C;
            return this;
        }

        public Config fAng2An(boolean fAng2An) {
            this.fAng2An = fAng2An;
            return this;
        }

        public Config setfIng2In(boolean fIng2In) {
            this.fIng2In = fIng2In;
            return this;
        }

        public Config fEng2En(boolean fEng2En) {
            this.fEng2En = fEng2En;
            return this;
        }

        public Config fU2V(boolean fU2V) {
            this.fU2V = fU2V;
            return this;
        }

        public PinIn commit() {
            context.config(this);
            return context;
        }
    }
}
