package me.towdium.pinin;

import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Chinese;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.Accelerator;
import me.towdium.pinin.utils.Cache;

import static me.towdium.pinin.elements.Chinese.MAX;
import static me.towdium.pinin.elements.Chinese.MIN;

@SuppressWarnings("unused")
public class PinIn {
    private int total = 0;

    private Cache<String, Phoneme> cPhoneme = new Cache<>(s -> new Phoneme(s, this));
    private Cache<String, Pinyin> cPinyin = new Cache<>(s -> new Pinyin(s, this, total++));
    private Chinese[] cChar = new Chinese[MAX - MIN];
    private Accelerator acc;

    private Keyboard keyboard;
    private int modification = 0;
    private boolean fZh2Z;
    private boolean fSh2S;
    private boolean fCh2C;
    private boolean fAng2An;
    private boolean fIng2In;
    private boolean fEng2En;
    private boolean fU2V;

    public PinIn() {
        this(Keyboard.QUANPIN, false, false, false,
                false, false, false, false);
    }

    public PinIn(Keyboard keyboard, boolean fZh2Z, boolean fSh2S, boolean fCh2C,
                 boolean fAng2An, boolean fIng2In, boolean fEng2En, boolean fU2V) {
        this.keyboard = keyboard;
        this.fZh2Z = fZh2Z;
        this.fSh2S = fSh2S;
        this.fCh2C = fCh2C;
        this.fAng2An = fAng2An;
        this.fIng2In = fIng2In;
        this.fEng2En = fEng2En;
        this.fU2V = fU2V;
        acc = new Accelerator(this);
    }

    public boolean contains(String s1, String s2) {
        acc.setProvider(s1);
        acc.search(s2);
        return acc.contains(0, 0);
    }

    public boolean begins(String s1, String s2) {
        acc.setProvider(s1);
        acc.search(s2);
        return acc.begins(0, 0);
    }

    public boolean matches(String s1, String s2) {
        acc.setProvider(s1);
        acc.search(s2);
        return acc.matches(0, 0);
    }

    public Phoneme genPhoneme(String s) {
        return cPhoneme.get(s);
    }

    public Pinyin genPinyin(String s) {
        return cPinyin.get(s);
    }

    public Char genChar(char c) {
        if (Chinese.isChinese(c)) {
            Chinese ret = cChar[c - MIN];
            if (ret == null) {
                ret = new Chinese(c, this);
                cChar[c - MIN] = ret;
            }
            return ret;
        } else return new Char(c);
    }

    public static void initialize() {
        //noinspection ResultOfMethodCallIgnored
        Pinyin.class.getClass();
    }

    public Keyboard keyboard() {
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
        return new Config();
    }

    public Ticket ticket(Runnable r) {
        return new Ticket(r);
    }

    private void config(Config c) {
        if (fAng2An == c.fAng2An && fEng2En == c.fEng2En && fIng2In == c.fIng2In
                && fZh2Z == c.fZh2Z && fSh2S == c.fSh2S && fCh2C == c.fCh2C
                && keyboard == c.keyboard && fU2V == c.fU2V) return;

        keyboard = c.keyboard;
        fZh2Z = c.fZh2Z;
        fSh2S = c.fSh2S;
        fCh2C = c.fCh2C;
        fAng2An = c.fAng2An;
        fIng2In = c.fIng2In;
        fEng2En = c.fEng2En;
        fU2V = c.fU2V;
        cPhoneme.foreach((s, p) -> p.reload(s, this));
        cPinyin.foreach((s, p) -> p.reload(s));
        modification++;
    }

    public class Config {
        public Keyboard keyboard;
        public boolean fZh2Z;
        public boolean fSh2S;
        public boolean fCh2C;
        public boolean fAng2An;
        public boolean fIng2In;
        public boolean fEng2En;
        public boolean fU2V;

        private Config() {
            keyboard = PinIn.this.keyboard;
            fZh2Z = PinIn.this.fZh2Z;
            fSh2S = PinIn.this.fSh2S;
            fCh2C = PinIn.this.fCh2C;
            fAng2An = PinIn.this.fAng2An;
            fIng2In = PinIn.this.fIng2In;
            fEng2En = PinIn.this.fEng2En;
            fU2V = PinIn.this.fU2V;
        }

        public Config keyboard(Keyboard keyboard) {
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

        public Config fIng2In(boolean fIng2In) {
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
            PinIn.this.config(this);
            return PinIn.this;
        }
    }

    public class Ticket {
        int modification;
        Runnable runnable;

        private Ticket(Runnable r) {
            runnable = r;
            modification = PinIn.this.modification;
        }

        public void renew() {
            int i = PinIn.this.modification;
            if (modification != i) {
                modification = i;
                runnable.run();
            }
        }
    }
}
