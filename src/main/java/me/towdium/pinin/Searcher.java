package me.towdium.pinin;

import me.towdium.pinin.utils.Accelerator;

import java.util.List;

public interface Searcher<T> {
    void put(String name, T identifier);

    List<T> search(String name);

    PinIn context();

    abstract class Logic {
        public static final Logic BEGIN = new Logic() {
            @Override
            public boolean test(Accelerator a, int offset, int start) {
                return a.begins(offset, start);
            }

            @Override
            public boolean test(PinIn p, String s1, String s2) {
                return p.begins(s1, s2);
            }

            @Override
            public boolean raw(String s1, String s2) {
                return s1.startsWith(s2);
            }
        };

        public static final Logic CONTAIN = new Logic() {
            @Override
            public boolean test(Accelerator a, int offset, int start) {
                return a.contains(offset, start);
            }

            @Override
            public boolean test(PinIn p, String s1, String s2) {
                return p.contains(s1, s2);
            }

            @Override
            public boolean raw(String s1, String s2) {
                return s1.contains(s2);
            }
        };

        public static final Logic MATCH = new Logic() {
            @Override
            public boolean test(Accelerator a, int offset, int start) {
                return a.matches(offset, start);
            }

            @Override
            public boolean test(PinIn p, String s1, String s2) {
                return p.matches(s1, s2);
            }

            @Override
            public boolean raw(String s1, String s2) {
                return s1.equals(s2);
            }
        };

        public abstract boolean test(Accelerator a, int offset, int start);

        public abstract boolean test(PinIn p, String s1, String s2);

        public abstract boolean raw(String s1, String s2);
    }
}
