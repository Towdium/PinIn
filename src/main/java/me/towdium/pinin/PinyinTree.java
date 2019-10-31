package me.towdium.pinin;

import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Element;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.IndexSet;
import me.towdium.pinin.utils.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class PinyinTree<T> {
    Node root = new NDense();
    PinIn context;
    CharList chars = new CharArrayList();
    List<T> objects = new ObjectArrayList<>();
    Accelerator acc = new Accelerator();
    final boolean suffix;

    private int charsEnd(int start) {
        for (int i = start; ; i++) {
            if (chars.getChar(i) == '\0') return i;
        }
    }

    private String charsStr(int start) {
        return charsStr(start, charsEnd(start));
    }

    private String charsStr(int start, int end) {
        return new String(chars.subList(start, end).toCharArray());
    }

    public PinyinTree(boolean suffix, PinIn context) {
        this.suffix = suffix;
        this.context = context;
    }

    public void put(String name, T identifier) {
        int pos = chars.size();
        for (char c : name.toCharArray()) {
            chars.add(c);
            context.genChar(c);
        }
        chars.add('\0');
        for (int i = 0; i < (suffix ? name.length() : 1); i++)
            root = root.put(this, pos + i, objects.size());
        objects.add(identifier);
    }

    public Set<T> search(String s) {
        acc.input(s);
        IntSet ret = new IntOpenHashSet();
        root.get(this, ret, 0);
        return ret.stream().map(i -> objects.get(i))
                .collect(Collectors.toSet());
    }

    public PinIn context() {
        return context;
    }

    interface Node {
        void get(PinyinTree p, IntSet ret, int offset);

        void get(PinyinTree p, IntSet ret);

        Node put(PinyinTree p, int name, int identifier);
    }

    public static class NSlice implements Node {
        Node exit = new NMap();
        int start, end;

        public NSlice(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void get(PinyinTree p, IntSet ret, int offset) {
            get(p, ret, offset, 0);
        }

        @Override
        public void get(PinyinTree p, IntSet ret) {
            exit.get(p, ret);
        }

        @Override
        public Node put(PinyinTree p, int name, int identifier) {
            int length = end - start;
            int match = Matcher.strCmp(p.charsStr(start, end), p.charsStr(name), 0, 0, length);
            if (match >= length) exit = exit.put(p, name + length, identifier);
            else {
                cut(p, start + match);
                exit = exit.put(p, name + match, identifier);
            }
            return start == end ? exit : this;
        }

        private void cut(PinyinTree p, int offset) {
            NMap insert = new NMap();
            if (offset + 1 == end) insert.put(p, p.chars.getChar(offset), exit);
            else {
                NSlice half = new NSlice(offset + 1, end);
                half.exit = exit;
                insert.put(p, p.chars.getChar(offset), half);
            }
            exit = insert;
            end = offset;
        }

        private void get(PinyinTree p, IntSet ret, int offset, int start) {
            if (this.start + start == end)
                exit.get(p, ret, offset);
            else if (offset == p.acc.input.length()) exit.get(p, ret);
            else {
                char ch = p.chars.getChar(this.start + start);
                p.acc.get(p.context.genChar(ch), offset).foreach(i ->
                        get(p, ret, offset + i, start + 1));
            }
        }
    }

    public static class NDense implements Node {
        // offset, object, offset, object
        IntList data = new IntArrayList();

        @Override
        public void get(PinyinTree p, IntSet ret, int offset) {
            if (p.acc.input.length() == offset) get(p, ret);
            else {
                for (int i = 0; i < data.size() / 2; i++) {
                    int ch = data.getInt(i * 2);
                    int obj = data.getInt(i * 2 + 1);
                    if (p.chars.getChar(ch) == '\0') continue;
                    if (p.acc.check(offset, ch, 0, p.context)) ret.add(obj);
                }
            }
        }

        @Override
        public void get(PinyinTree p, IntSet ret) {
            for (int i = 0; i < data.size() / 2; i++)
                ret.add(data.getInt(i * 2 + 1));
        }

        @Override
        public Node put(PinyinTree p, int name, int identifier) {
            if (data.size() >= 128) {
                int pattern = data.getInt(0);
                int common = Integer.MAX_VALUE;
                for (int i = 0; i < data.size() / 2; i++) {
                    int offset = data.getInt(i * 2);
                    common = Math.min(common, Matcher.strCmp(p.chars, pattern, offset));
                    if (common == 0) break;
                }
                Node ret = new NSlice(pattern, pattern + common);
                for (int j = 0; j < data.size() / 2; j++)
                    ret.put(p, data.getInt(j * 2), data.getInt(j * 2 + 1));
                ret.put(p, name, identifier);
                return ret;
            } else {
                data.add(name);
                data.add(identifier);
                return this;
            }
        }
    }

    public static class NMap implements Node {
        Char2ObjectMap<Node> children;
        Glue glue;
        IntSet leaves = new IntArraySet(1);

        @Override
        public void get(PinyinTree p, IntSet ret, int offset) {
            if (p.acc.input.length() == offset) get(p, ret);
            else if (children != null && glue != null) {
                Node n = children.get(p.acc.input.charAt(offset));
                if (n != null) n.get(p, ret, offset + 1);
                glue.get(p, offset).forEach((c, is) -> is.foreach(i ->
                        children.get(c.charValue()).get(p, ret, offset + i)));
            }
        }

        @Override
        public void get(PinyinTree p, IntSet ret) {
            ret.addAll(leaves);
            if (children != null) children.forEach((c, n) -> n.get(p, ret));
        }

        @Override
        public NMap put(PinyinTree p, int name, int identifier) {
            if (p.chars.getChar(name) == '\0') {
                if (leaves.size() >= 32 && leaves instanceof IntArraySet)
                    leaves = new IntOpenHashSet(leaves);
                leaves.add(identifier);
            } else {
                init();
                char ch = p.chars.getChar(name);
                Node sub = children.get(ch);
                if (sub == null) put(p, ch, sub = new NDense());
                sub = sub.put(p, name + 1, identifier);
                children.put(ch, sub);
            }
            return this;
        }

        private void put(PinyinTree p, char ch, Node n) {
            init();
            if (children.size() >= 32 && children instanceof Char2ObjectArrayMap)
                children = new Char2ObjectOpenHashMap<>(children);
            children.put(ch, n);
            glue.put(ch, p);
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

        public Char2ObjectMap<IndexSet> get(PinyinTree t, int offset) {
            Char2ObjectMap<IndexSet> ret = new Char2ObjectArrayMap<>();
            BiConsumer<Pinyin, CharSet> add = (p, cs) -> t.acc.get(p, offset).foreach(i -> {
                for (char c : cs) ret.computeIfAbsent(c, k -> new IndexSet()).set(i);
            });

            if (index != null) {
                index.forEach((ph, ps) -> {
                    if (!ph.match(t.acc.input, offset).isEmpty())
                        ps.forEach(p -> add.accept(p, map.get(p)));
                });
            } else map.forEach(add);
            return ret;
        }

        public void put(char ch, PinyinTree pi) {
            if (!Matcher.isChinese(ch)) return;
            for (Pinyin p : Pinyin.get(ch, pi.context)) {
                map.compute(p, (py, cs) -> {
                    if (cs == null) {
                        cs = new CharArraySet(1);
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

    class Accelerator {
        List<IndexSet[]> cache;
        String input;

        public void input(String s) {
            input = s;
            cache = new ArrayList<>();
        }

        public IndexSet get(Char c, int offset) {
            IndexSet ret = new IndexSet();
            for (Element p : c.patterns()) {
                IndexSet is;
                if (p instanceof Pinyin) is = get((Pinyin) p, offset);
                else is = p.match(input, offset);
                ret.merge(is);
            }
            return ret;
        }

        public IndexSet get(Pinyin p, int offset) {
            for (int i = cache.size(); i <= offset; i++)
                cache.add(new IndexSet[context.total()]);
            IndexSet[] data = cache.get(offset);
            IndexSet ret = data[p.id];
            if (ret == null) {
                ret = p.match(input, offset);
                data[p.id] = ret;
            }
            return ret;
        }

        public boolean check(int offset, int s2, int start2, PinIn p) {
            if (offset == input.length()) return true;

            Char r = p.genChar(chars.getChar(s2 + start2));
            IndexSet s = get(r, offset);

            if (chars.getChar(s2 + start2 + 1) == '\0') {
                int i = input.length() - offset;
                return s.get(i);
            } else return !s.traverse(i -> !check(offset + i, s2, start2 + 1, p));
        }
    }
}
