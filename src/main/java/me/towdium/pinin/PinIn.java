package me.towdium.pinin;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import me.towdium.pinin.elements.*;
import me.towdium.pinin.utils.Accelerator;
import me.towdium.pinin.utils.Cache;
import me.towdium.pinin.utils.IndexSet;

@SuppressWarnings("unused")
public class PinIn {
    private int total = 0;

    private final Cache<String, Phoneme> phonemes = new Cache<>(s -> new Phoneme(s, this));
    private final Cache<String, Pinyin> pinyins = new Cache<>(s -> new Pinyin(s, this, total++));
    private final Char[] chars = new Char[Character.MAX_VALUE];
    private final Char.Dummy temp = new Char.Dummy();
    private final Accelerator acc;

    private Keyboard keyboard = Keyboard.QUANPIN;
    private int modification = 0;
    private boolean fZh2Z = false;
    private boolean fSh2S = false;
    private boolean fCh2C = false;
    private boolean fAng2An = false;
    private boolean fIng2In = false;
    private boolean fEng2En = false;
    private boolean fU2V = false;
    private boolean accelerate = false;

    /**
     * Use PinIn object to manage the context
     * To configure it, use {@link #config()}
     */
    public PinIn() {
        this(new DictLoader.Default());
    }

    public PinIn(DictLoader loader) {
        acc = new Accelerator(this);
        loader.load((c, ss) -> {
            if (ss == null) {
                chars[c] = null;
            } else {
                Pinyin[] pinyins = new Pinyin[ss.length];
                for (int i = 0; i < ss.length; i++) {
                    pinyins[i] = getPinyin(ss[i]);
                }
                chars[c] = new Char(c, pinyins);
            }
        });
    }

    public boolean contains(String s1, String s2) {
        if (accelerate) {
            acc.setProvider(s1);
            acc.search(s2);
            return acc.contains(0, 0);
        } else return Matcher.contains(s1, s2, this);
    }

    public boolean begins(String s1, String s2) {
        if (accelerate) {
            acc.setProvider(s1);
            acc.search(s2);
            return acc.begins(0, 0);
        } else return Matcher.begins(s1, s2, this);
    }

    public boolean matches(String s1, String s2) {
        if (accelerate) {
            acc.setProvider(s1);
            acc.search(s2);
            return acc.matches(0, 0);
        } else return Matcher.matches(s1, s2, this);
    }

    public Phoneme getPhoneme(String s) {
        return phonemes.get(s);
    }

    public Pinyin getPinyin(String s) {
        return pinyins.get(s);
    }

    public Char getChar(char c) {
        Char ret = chars[c];
        if (ret != null) {
            return ret;
        } else {
            temp.set(c);
            return temp;
        }
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

    /**
     * Set values in returned {@link Config} object,
     * then use {@link Config#commit()} to apply
     */
    public Config config() {
        return new Config();
    }

    public Ticket ticket(Runnable r) {
        return new Ticket(r);
    }

    private void config(Config c) {
        if (fAng2An == c.fAng2An && fEng2En == c.fEng2En && fIng2In == c.fIng2In
                && fZh2Z == c.fZh2Z && fSh2S == c.fSh2S && fCh2C == c.fCh2C
                && keyboard == c.keyboard && fU2V == c.fU2V && accelerate == c.accelerate) return;

        keyboard = c.keyboard;
        fZh2Z = c.fZh2Z;
        fSh2S = c.fSh2S;
        fCh2C = c.fCh2C;
        fAng2An = c.fAng2An;
        fIng2In = c.fIng2In;
        fEng2En = c.fEng2En;
        fU2V = c.fU2V;
        accelerate = c.accelerate;
        phonemes.foreach((s, p) -> p.reload(s, this));
        pinyins.foreach((s, p) -> p.reload(s, this));
        modification++;
    }

    public static class Matcher {
        public static boolean begins(String s1, String s2, PinIn p) {
            if (s1.isEmpty()) return s1.startsWith(s2);
            else return check(s1, 0, s2, 0, p, true);
        }

        public static boolean contains(String s1, String s2, PinIn p) {
            if (s1.isEmpty()) return s1.contains(s2);
            else {
                for (int i = 0; i < s1.length(); i++)
                    if (check(s1, i, s2, 0, p, true)) return true;
                return false;
            }
        }

        public static boolean matches(String s1, String s2, PinIn p) {
            if (s1.isEmpty()) return s1.equals(s2);
            else return check(s1, 0, s2, 0, p, false);
        }

        private static boolean check(String s1, int start1, String s2, int start2, PinIn p, boolean partial) {
            if (start2 == s2.length()) return partial || start1 == s1.length();

            Element r = p.getChar(s1.charAt(start1));
            IndexSet s = r.match(s2, start2, partial);

            if (start1 == s1.length() - 1) {
                int i = s2.length() - start2;
                return s.get(i);
            } else return !s.traverse(i -> !check(s1, start1 + 1, s2, start2 + i, p, partial));
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

    public class Config {
        public Keyboard keyboard;
        public boolean fZh2Z;
        public boolean fSh2S;
        public boolean fCh2C;
        public boolean fAng2An;
        public boolean fIng2In;
        public boolean fEng2En;
        public boolean fU2V;
        public boolean accelerate;

        private Config() {
            keyboard = PinIn.this.keyboard;
            fZh2Z = PinIn.this.fZh2Z;
            fSh2S = PinIn.this.fSh2S;
            fCh2C = PinIn.this.fCh2C;
            fAng2An = PinIn.this.fAng2An;
            fIng2In = PinIn.this.fIng2In;
            fEng2En = PinIn.this.fEng2En;
            fU2V = PinIn.this.fU2V;
            accelerate = PinIn.this.accelerate;
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

        /**
         * Set accelerate mode of immediate matching.
         * When working in accelerate mode, accelerator will be used.
         * <p>
         * When calling immediate matching functions continuously with
         * different {@code s1} but same {@code s2}, for, say, 100 times,
         * they are considered stable calls.
         * If the scenario uses mainly stable calls and most of {@code s1}
         * contains Chinese characters, using accelerate mode provides
         * significant speed up. Otherwise, overhead of cache management
         * in accelerator will slow down the matching process.
         * Accelerate mode is disabled by default for consistency in
         * different scenarios.
         */
        public Config accelerate(boolean accelerate) {
            this.accelerate = accelerate;
            return this;
        }

        public PinIn commit() {
            PinIn.this.config(this);
            return PinIn.this;
        }
    }
}
