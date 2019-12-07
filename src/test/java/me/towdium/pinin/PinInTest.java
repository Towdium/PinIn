package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.towdium.pinin.Searcher.Logic;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static me.towdium.pinin.Keyboard.*;
import static me.towdium.pinin.Searcher.Logic.CONTAIN;
import static me.towdium.pinin.Searcher.Logic.EQUAL;

public class PinInTest {
    @Test
    @SuppressWarnings({"UnusedAssignment", "unused"})
    public void performance() throws IOException {
        List<String> search = new ArrayList<>();
        search.add("boli");
        search.add("yangmao");
        search.add("hongse");
        Logic logic = CONTAIN;
        Supplier<Searcher<Integer>> supplier = () -> new TreeSearcher<>(logic, new PinIn());
        String source = "small";
        System.out.println("Test performance");
        List<String> strs = new ArrayList<>();
        Searcher<Integer> searcher = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinInTest.class.getResourceAsStream(source + ".txt"), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            strs.add(line);
        }
        String full = String.join("", strs);

        int loop = 10;
        long time = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            searcher = supplier.get();
            for (int j = 0; j < strs.size(); j++) {
                searcher.put(strs.get(j), j);
            }
            //searcher.put(full, 0);
        }

        System.out.println("Construction time: " + (System.currentTimeMillis() - time) / (float) loop);

        float warm = 0;
        float acc = 0;
        float traverse = 0;
        float contains = 0;

        for (String s : search) {
            List<Integer> is = null;
            float t;
            //noinspection ConstantConditions
            if (searcher instanceof CachedSearcher) {
                time = System.currentTimeMillis();
                loop = 100;
                for (int i = 0; i < loop; i++) {
                    ((CachedSearcher) searcher).reset();
                    is = searcher.search(s);
                }
                t = (System.currentTimeMillis() - time) / (float) loop;
                warm += t;
                System.out.println("Warm up time: " + t);
                searcher.search("jiqi");
                searcher.search("yangmao");
                searcher.search("yunshanmuban");
                searcher.search("hongshi");
                searcher.search("xianlan");
                searcher.search("kuangjia");
                searcher.search("lvse");
                searcher.search("niantu");
                searcher.search("yangmao");
                searcher.search("hunningtu");
                System.out.println("Test search completed.");
            }

            time = System.currentTimeMillis();
            loop = 10000;
            //noinspection ConstantConditions
            if (searcher instanceof SimpleSearcher) loop /= 100;
            for (int i = 0; i < loop; i++) {
                is = searcher.search(s);
            }
            t = (System.currentTimeMillis() - time) / (float) loop;
            acc += t;
            System.out.println("Accelerated search time: " + t);

            //for (Integer i: is) System.out.println(strs.get(i));

            time = System.currentTimeMillis();
            PinIn p = new PinIn();
            IntSet result = new IntOpenHashSet();
            loop = 3;
            for (int j = 0; j < loop; j++) {
                for (int i = 0; i < strs.size(); i++) {
                    String k = strs.get(i);
                    if (logic.test(p, k, s)) result.add(i);
                }
            }
            t = (System.currentTimeMillis() - time) / (float) loop;
            traverse += t;
            System.out.println("Loop search time: " + t);
            assert is != null && result.containsAll(is) && is.containsAll(result);
            System.out.println("Total matches: " + is.size());

            time = System.currentTimeMillis();
            result = new IntOpenHashSet();
            loop = 10;
            for (int j = 0; j < loop; j++) {
                for (int i = 0; i < strs.size(); i++) {
                    String k = strs.get(i);
                    if (logic.raw(k, s)) result.add(i);
                }
            }
            t = (System.currentTimeMillis() - time) / (float) loop;
            contains += t;
            System.out.println("Contains search time: " + t);
        }
        if (search.size() == 1) return;
        //noinspection ConstantConditions
        if (searcher instanceof CachedSearcher)
            System.out.println("Average warm up time: " + warm / search.size());
        System.out.println("Average accelerated search time: " + acc / search.size());
        System.out.println("Average loop search time: " + traverse / search.size());
        System.out.println("Average contains search time: " + contains / search.size());
    }

    @Test
    public void quanpin() {
        System.out.println("Test quanpin");
        PinIn p = new PinIn();
        assert p.contains("测试文本", "ceshiwenben");
        assert p.contains("测试文本", "ceshiwenbe");
        assert p.contains("测试文本", "ceshiwben");
        assert p.contains("测试文本", "ce4shi4w2ben");
        assert !p.contains("测试文本", "ce2shi4w2ben");
        assert p.contains("合金炉", "hejinlu");
        assert p.contains("洗矿场", "xikuangchang");
        assert p.contains("流体", "liuti");
        assert p.contains("轰20", "hong2");
        assert p.contains("hong2", "hong2");
    }

    @Test
    public void daqian() {
        System.out.println("Test daqian");
        PinIn p = new PinIn().config().keyboard(DAQIAN).commit();
        assert p.contains("测试文本", "hk4g4jp61p3");
        assert p.contains("测试文本", "hkgjp1");
        assert p.contains("錫", "vu6");
        assert p.contains("物質", "j456");
    }

    @Test
    public void xiaohe() {
        System.out.println("Test xiaohe");
        PinIn p = new PinIn().config().keyboard(XIAOHE).commit();
        assert p.contains("测试文本", "ceuiwfbf");
        assert p.contains("测试文本", "ceuiwf2");
        assert !p.contains("测试文本", "ceuiw2");
        assert p.contains("合金炉", "hej");
        assert p.contains("洗矿场", "xikl4");
        assert p.contains("月球", "ytqq");
    }

    @Test
    public void ziranma() {
        System.out.println("Test ziranma");
        PinIn p = new PinIn().config().keyboard(ZIRANMA).commit();
        assert p.contains("测试文本", "ceuiwfbf");
        assert p.contains("测试文本", "ceuiwf2");
        assert !p.contains("测试文本", "ceuiw2");
        assert p.contains("合金炉", "hej");
        assert p.contains("洗矿场", "xikd4");
        assert p.contains("月球", "ytqq");
    }

    @Test
    public void tree() {
        System.out.println("Test tree");
        TreeSearcher<Integer> tree = new TreeSearcher<>(CONTAIN, new PinIn());
        tree.put("测试文本", 1);
        tree.put("测试切分", 5);
        tree.put("测试切分文本", 6);
        tree.put("合金炉", 2);
        tree.put("洗矿场", 3);
        tree.put("流体", 4);
        tree.put("轰20", 7);
        tree.put("hong2", 8);

        Collection<Integer> s;
        s = tree.search("ceshiwenben");
        assert s.size() == 1 && s.contains(1);
        s = tree.search("ceshiwenbe");
        assert s.size() == 1 && s.contains(1);
        s = tree.search("ceshiwben");
        assert s.size() == 1 && s.contains(1);
        s = tree.search("ce4shi4w2ben");
        assert s.size() == 1 && s.contains(1);
        s = tree.search("ce2shi4w2ben");
        assert s.size() == 0;
        s = tree.search("hejinlu");
        assert s.size() == 1 && s.contains(2);
        s = tree.search("xikuangchang");
        assert s.size() == 1 && s.contains(3);
        s = tree.search("liuti");
        assert s.size() == 1 && s.contains(4);
        s = tree.search("ceshi");
        assert s.size() == 3 && s.contains(1) && s.contains(5);
        s = tree.search("ceshiqiefen");
        assert s.size() == 2 && s.contains(5);
        s = tree.search("ceshiqiefenw");
        assert s.size() == 1 && s.contains(6);
        s = tree.search("hong2");
        assert s.contains(7) && s.contains(8);
    }

    @Test
    public void context() {
        PinIn p = new PinIn();
        TreeSearcher<Integer> tree = new TreeSearcher<>(CONTAIN, p);
        tree.put("测试文本", 0);
        tree.put("测试文字", 3);
        Collection<Integer> s;
        s = tree.search("ce4shi4w2ben");
        assert s.size() == 1 && s.contains(0);
        s = tree.search("ce4shw");
        assert s.size() == 2;
        s = tree.search("ce4sw");
        assert s.isEmpty();
        p.config().fSh2S(true).commit();
        s = tree.search("ce4sw");
        assert s.size() == 2;
        p.config().fSh2S(false).keyboard(DAQIAN).commit();
        s = tree.search("hk4g4jp61p3");
        assert s.size() == 1;
        s = tree.search("ce4shi4w2ben");
        assert s.isEmpty();
    }

    @Test
    public void full() {
        List<Searcher<Integer>> ss = new ArrayList<>();
        ss.add(new TreeSearcher<>(EQUAL, new PinIn()));
        ss.add(new SimpleSearcher<>(EQUAL, new PinIn()));
        for (Searcher<Integer> s : ss) {
            s.put("测试文本", 1);
            s.put("测试切分", 5);
            s.put("测试切分文本", 6);
            s.put("合金炉", 2);
            s.put("洗矿场", 3);
            s.put("流体", 4);
            s.put("轰20", 7);
            s.put("hong2", 8);
            s.put("月球", 9);
            s.put("汉化", 10);
            s.put("喊话", 11);
            List<Integer> is;
            is = s.search("hong2");
            assert is.size() == 1 && is.contains(8);
            is = s.search("hong20");
            assert is.size() == 1 && is.contains(7);
            is = s.search("ceshqf");
            assert is.size() == 1 && is.contains(5);
            is = s.search("ceshqfw");
            assert is.isEmpty();
            is = s.search("hh");
            assert is.size() == 2 && is.contains(10) && is.contains(11);
            is = s.search("hhu");
            assert is.isEmpty();
        }
    }

    @Test
    public void format() {
        PinIn p = new PinIn();
        Char c = p.genChar('圆');
        Pinyin py = c.pinyins()[0];
        assert py.format(Pinyin.Format.NUMBER).equals("yuan2");
        assert py.format(Pinyin.Format.RAW).equals("yuan");
        assert py.format(Pinyin.Format.UNICODE).equals("yuán");
        assert py.format(Pinyin.Format.PHONETIC).equals("ㄩㄢˊ");
        assert p.genPinyin("le0").format(Pinyin.Format.PHONETIC).equals("˙ㄌㄜ");
    }

    public static void main(String[] args) throws IOException {
        new PinInTest().performance();
    }
}
