package me.towdium.pinin;

import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.*;
import me.towdium.pinin.elements.Phoneme;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.utils.IndexSet;
import me.towdium.pinin.utils.Matcher;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Author: Towdium
 * Date: 21/04/19
 */
public class PinyinTree<T> {
    final PinIn config;
    Node root = new NSlice();

    public PinyinTree(PinIn p) {
        config = p;
    }

    public void put(String name, T identifier) {
        for (int i = 0; i < name.length(); i++) {
            root = root.put(name, identifier, i);
        }
    }

    public Set<T> search(String s) {
        Set<T> ret = new ObjectOpenHashSet<>();
        root.get(ret, s, 0);
        return ret;
    }

    public int countSlice() {
        return root.countSlice();
    }

    public int countMap() {
        return root.countMap();
    }

    abstract class Node {
        abstract void get(Set<T> ret, String name, int offset);

        abstract void get(Set<T> ret);

        abstract Node put(String name, T identifier, int offset);

        abstract int countSlice();

        abstract int countMap();
    }

    public class NSlice extends Node {
        Node exit = new NMap();
        String name;
        int start, end;

        @Override
        public void get(Set<T> ret, String name, int offset) {
            get(ret, name, offset, 0);
        }

        @Override
        public void get(Set<T> ret) {
            exit.get(ret);
        }

        @Override
        public Node put(String name, T identifier, int offset) {
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
                    exit.put(name, identifier, offset + match);
                }
            }
            return start == end ? exit : this;
        }

        @Override
        public int countSlice() {
            return 1 + exit.countSlice();
        }

        @Override
        public int countMap() {
            return exit.countMap();
        }

        private void cut(int offset) {
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
            end = offset;
        }

        private void get(Set<T> ret, String name, int offset, int start) {
            if (this.start + start == end) exit.get(ret, name, offset);
            else if (offset == name.length()) exit.get(ret);
            else {
                char ch = this.name.charAt(this.start + start);
                config.genChar(ch).match(name, offset).traverse(i -> {
                    get(ret, name, offset + i, start + 1);
                    return true;
                });
            }
        }
    }

    public class NMap extends Node {
        Char2ObjectMap<Node> children; // = new Char2ObjectOpenHashMap<>();
        Glue glue;
        Set<T> leaves;

        @Override
        public void get(Set<T> ret, String name, int offset) {
            if (name.length() == offset) get(ret);
            else if (children != null && glue != null) {
                Node n = children.get(name.charAt(offset));
                if (n != null) n.get(ret, name, offset + 1);
                glue.get(name, offset).forEach((c, is) -> is.foreach(i ->
                        c.get(ret, name, offset + i)));
            }
        }

        @Override
        public void get(Set<T> ret) {
            if (leaves != null) ret.addAll(leaves);
            if (children != null) children.forEach((p, n) -> n.get(ret));
        }

        @Override
        public NMap put(String name, T identifier, int offset) {
            if (offset == name.length()) {
                if (leaves == null) leaves = new ObjectArraySet<>();
                else if (leaves.size() >= 16 && leaves instanceof IntArraySet)
                    leaves = new ObjectOpenHashSet<>(leaves);
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

        @Override
        public int countSlice() {
            int ret = 0;
            if (children != null) for (Node n : children.values()) ret += n.countSlice();
            return ret;
        }

        @Override
        public int countMap() {
            int ret = 1;
            if (children != null) for (Node n : children.values()) ret += n.countMap();
            return ret;
        }

        private void put(char ch, Node n) {
            init();
            if (children.size() >= 16 && children instanceof Char2ObjectArrayMap)
                children = new Char2ObjectOpenHashMap<>(children);
            children.put(ch, n);
            glue.put(ch);
        }

        private void init() {
            if (children == null || glue == null) {
                children = new Char2ObjectArrayMap<>();
                glue = new Glue();
            }
        }

        class Glue {
            Map<Pinyin, Set<Node>> map = new Object2ObjectArrayMap<>();
            GNode<T> root;

            public Map<Node, IndexSet> get(String name, int offset) {
                Map<Node, IndexSet> ret = new Object2ObjectOpenHashMap<>();
                if (root == null) { map.forEach((p, ns) -> p.match(name, offset).foreach(i ->
                        ns.forEach(n -> ret.computeIfAbsent(n, k -> new IndexSet()).set(i))));
                } else root.get(ret, name, offset, map, offset);
                return ret;
            }

            public void put(char ch) {
                if (!Matcher.isChinese(ch)) return;
                for (Pinyin p : Pinyin.get(ch, config)) {
                    map.compute(p, (py, cs) -> {
                        if (cs == null) cs = new ObjectArraySet<>();
                        else if (cs.size() >= 16 && cs instanceof ObjectArraySet)
                            cs = new ObjectOpenHashSet<>(cs);
                        cs.add(children.get(ch));
                        return cs;
                    });
                    if (root != null) index(p);
                }
                if (map.size() >= 16) {
                    map = new Object2ObjectOpenHashMap<>(map);
                    index();
                }
            }

            private void index() {
                root = new GNode<>();
                map.forEach((p, ns) -> index(p));
            }

            private void index(Pinyin p) {
                Phoneme[] ps = p.phonemes();
                GNode<T> second = root.children().computeIfAbsent(ps[0], i -> new GNode<>());
                GNode<T> shortcut = second.children().computeIfAbsent(ps[2], i -> new GNode<>());
                shortcut.leaves().add(p);
                GNode<T> third = second.children().computeIfAbsent(ps[1], i -> new GNode<>());
                GNode<T> fourth = third.children().computeIfAbsent(ps[2], i -> new GNode<>());
                fourth.leaves().add(p);
            }
        }
    }

    static class GNode<T> {
        Map<Phoneme, GNode<T>> children;
        Set<Pinyin> leaves;

        public void get(Map<PinyinTree<T>.Node, IndexSet> ret, String name, int offset, Map<Pinyin, Set<PinyinTree<T>.Node>> map, int base) {
            collect(ret, offset, map, base);
            if (children != null) children.forEach((i, j) -> {
                IndexSet is = i.match(name, offset);
                is.foreach(k -> j.get(ret, name, offset + k, map, base));
            });
        }

        public void collect(Map<PinyinTree<T>.Node, IndexSet> ret, int offset, Map<Pinyin, Set<PinyinTree<T>.Node>> map, int base) {
            if (offset == base) return;
            if (leaves != null) leaves.forEach(i -> map.get(i).forEach(j ->
                    ret.computeIfAbsent(j, k -> new IndexSet()).set(offset - base)));
            if (children != null) children.forEach((i, j) -> j.collect(ret, offset, map, base));
        }

        public Map<Phoneme, GNode<T>> children() {
            if (children == null) children = new Object2ObjectArrayMap<>();
            else if (children.size() >= 16 && children instanceof Object2ObjectArrayMap)
                children = new Object2ObjectOpenHashMap<>(children);
            return children;
        }

        public Set<Pinyin> leaves() {
            if (leaves == null) leaves = new ObjectArraySet<>();
            else if (leaves.size() >= 16 && leaves instanceof ObjectArraySet)
                leaves = new ObjectOpenHashSet<>(leaves);
            return leaves;
        }
    }
}
