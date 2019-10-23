package me.towdium.pinin;

import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.Matcher;
import me.towdium.pinin.utils.StringSlice;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static me.towdium.pinin.utils.Matcher.check;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class PinyinTree {
    Node root = new NSlice();
    PinIn context;

    public PinyinTree(PinIn context) {
        this.context = context;
    }

    public void put(String name, int identifier) {
        for (int i = 0; i < name.length(); i++) {
            root = root.put(name, identifier, i);
        }
    }

    public IntSet search(String s) {
        IntSet ret = new IntOpenHashSet();
        root.get(ret, s, 0);
        return ret;
    }

    interface Node {
        void get(IntSet ret, String name, int offset);

        void get(IntSet ret);

        Node put(String name, int identifier, int offset);
    }

    public class NSlice implements Node {
        Node exit = new NMap();
        String name;
        int start, end;

        @Override
        public void get(IntSet ret, String name, int offset) {
            get(ret, name, offset, 0);
        }

        @Override
        public void get(IntSet ret) {
            exit.get(ret);
        }

        @Override
        public Node put(String name, int identifier, int offset) {
            if (this.name == null) {
                this.name = name;
                start = offset;
                end = name.length();
                exit = exit.put(name, identifier, end);
            } else {
                int length = end - start;
                int match = Matcher.strCmp(this.name, name, start, offset, length);
                if (match >= length) exit = exit.put(name, identifier, offset + length);
                else {
                    cut(start + match);
                    exit = exit.put(name, identifier, offset + match);
                }
            }
            return start == end ? exit : this;
        }

        private void cut(int offset) {
            if (end == name.length()) {
                NDense insert = new NDense();
                IntSet is = new IntOpenHashSet();
                exit.get(is);
                insert.set(name, is, offset);
                exit = insert;
            } else {
                NMap insert = new NMap();
                if (offset + 1 == end) insert.put(name.charAt(offset), exit);
                else {
                    NSlice half = new NSlice();
                    half.name = this.name;
                    half.start = offset + 1;
                    half.end = end;
                    half.exit = exit;
                    insert.put(name.charAt(offset), half);
                }
                exit = insert;
            }
            end = offset;
        }

        private void get(IntSet ret, String name, int offset, int start) {
            if (this.start + start == end)
                exit.get(ret, name, offset);
            else if (offset == name.length()) exit.get(ret);
            else {
                char ch = this.name.charAt(this.start + start);
                context.genChar(ch).match(name, offset).foreach(i ->
                        get(ret, name, offset + i, start + 1));
            }
        }
    }

    public class NDense implements Node {
        Map<StringSlice, IntSet> children = new Object2ObjectArrayMap<>();

        @Override
        public void get(IntSet ret, String name, int offset) {
            if (name.length() == offset) get(ret);
            else {
                children.forEach((ss, is) -> {
                    if (ss.isEmpty()) return;
                    if (check(name, offset, ss.str, ss.start, context)) ret.addAll(is);
                });
            }
        }

        @Override
        public void get(IntSet ret) {
            children.values().forEach(ret::addAll);
        }

        private void set(String name, IntSet identifier, int offset) {
            StringSlice ss = new StringSlice(name, offset);
            children.put(ss, identifier);
        }

        @Override
        public Node put(String name, int identifier, int offset) {
            StringSlice ss = new StringSlice(name, offset);
            IntSet is = children.computeIfPresent(ss, (s, l) -> {
                if (l.size() >= 32 && l instanceof IntArraySet) return new IntOpenHashSet(l);
                else return l;
            });
            if (is == null) {
                if (children.size() >= 32 && children instanceof Object2ObjectArrayMap)
                    children = new Object2ObjectOpenHashMap<>(children);
                else if (children.size() >= 256) {
                    Node ret = new NMap();
                    for (Map.Entry<StringSlice, IntSet> entry : children.entrySet()) {
                        StringSlice s = entry.getKey();
                        IntSet l = entry.getValue();
                        for (int i : l) ret = ret.put(s.str, i, s.start);
                    }
                    return ret;
                }
                children.put(ss, is = new IntArraySet(1));
            }
            is.add(identifier);
            return this;
        }
    }

    public class NMap implements Node {
        Char2ObjectMap<Node> children;
        Glue glue;
        IntSet leaves = new IntArraySet();

        @Override
        public void get(IntSet ret, String name, int offset) {
            if (name.length() == offset) get(ret);
            else if (children != null && glue != null) {
                Node n = children.get(name.charAt(offset));
                if (n != null) n.get(ret, name, offset + 1);
                glue.get(name, offset).forEach((c, is) -> {
                    for (int i : is) children.get(c.charValue()).get(ret, name, offset + i);
                });
            }
        }

        @Override
        public void get(IntSet ret) {
            ret.addAll(leaves);
            if (children != null) children.forEach((p, n) -> n.get(ret));
        }

        @Override
        public NMap put(String name, int identifier, int offset) {
            if (offset == name.length()) {
                if (leaves.size() >= 32 && leaves instanceof IntArraySet)
                    leaves = new IntOpenHashSet(leaves);
                leaves.add(identifier);
            } else {
                init();
                char ch = name.charAt(offset);
                Node sub = children.get(ch);
                if (sub == null) put(ch, sub = new NSlice());
                sub = sub.put(name, identifier, offset + 1);
                children.put(ch, sub);
            }
            return this;
        }

        private void put(char ch, Node n) {
            init();
            if (children.size() >= 32 && children instanceof Char2ObjectArrayMap)
                children = new Char2ObjectOpenHashMap<>(children);
            children.put(ch, n);
            glue.put(ch, context);
        }

        private void init() {
            if (children == null || glue == null) {
                children = new Char2ObjectArrayMap<>();
                glue = new Glue();
            }
        }
    }

    static class Glue {
        Map<Pinyin, CharSet> map = new Object2ObjectArrayMap<>();
        Map<Phoneme, Set<Pinyin>> index;

        public Char2ObjectMap<IntSet> get(String name, int offset) {
            Char2ObjectMap<IntSet> ret = new Char2ObjectArrayMap<>();
            BiConsumer<Pinyin, CharSet> add = (p, cs) -> p.match(name, offset).foreach(i -> {
                for (char c : cs) ret.computeIfAbsent(c, k -> new IntArraySet()).add(i.intValue());
            });

            if (index != null) {
                index.forEach((ph, ps) -> {
                    if (!ph.match(name, offset).isEmpty())
                        ps.forEach(p -> add.accept(p, map.get(p)));
                });
            } else map.forEach(add);
            return ret;
        }

        public void put(char ch, PinIn pi) {
            if (!Matcher.isChinese(ch)) return;
            for (Pinyin p : Pinyin.get(ch, pi)) {
                map.compute(p, (py, cs) -> {
                    if (cs == null) {
                        cs = new CharArraySet();
                        if (index != null) index.computeIfAbsent(py.phonemes()[0],
                                c -> new ObjectOpenHashSet<>()).add(py);
                    } else if (cs.size() >= 32 && cs instanceof CharArraySet)
                        cs = new CharOpenHashSet(cs);
                    cs.add(ch);
                    return cs;
                });
            }
            if (map.size() >= 32 && index == null) {
                map = new Object2ObjectOpenHashMap<>(map);
                index();
            }
        }

        public void index() {
            index = new Object2ObjectArrayMap<>();
            map.forEach((p, cs) -> index.computeIfAbsent(p.phonemes()[0],
                    c -> new ObjectOpenHashSet<>()).add(p));
        }
    }
}
