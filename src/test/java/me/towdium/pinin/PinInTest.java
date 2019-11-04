package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.towdium.pinin.Keyboard.*;

public class PinInTest {
    @Test
    @SuppressWarnings({"UnusedAssignment", "unused"})
    public void performance() throws IOException {
        System.out.println("Test performance");
        String search = "hong2";
        List<String> strs = new ArrayList<>();
        Searcher<Integer> searcher = new CachedSearcher<>(true, new PinIn());
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinInTest.class.getResourceAsStream("large.txt"), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            strs.add(line);
        }

        long time = System.currentTimeMillis();
        for (int i = 0; i < strs.size(); i++) {
            searcher.put(strs.get(i), i);
        }
        System.out.println("Construction time: " + (System.currentTimeMillis() - time));

        Collection<Integer> is = null;
        //noinspection ConstantConditions
        if (searcher instanceof CachedSearcher) {
            time = System.currentTimeMillis();
            int loop = 100;
            for (int i = 0; i < loop; i++) {
                ((CachedSearcher) searcher).reset();
                is = searcher.search(search);
            }
            System.out.println("Warm up time: " + (System.currentTimeMillis() - time) / (float) loop);
        }

        time = System.currentTimeMillis();
        int loop = 10000;
        for (int i = 0; i < loop; i++) {
            is = searcher.search(search);
        }
        System.out.println("Accelerated search time: " + (System.currentTimeMillis() - time) / (float) loop);

        //for (Integer i: is) System.out.println(strs.get(i));

        time = System.currentTimeMillis();
        PinIn p = new PinIn();
        IntSet result = new IntOpenHashSet();
        loop = 3;
        for (int j = 0; j < loop; j++) {
            for (int i = 0; i < strs.size(); i++) {
                if (p.contains(strs.get(i), search)) result.add(i);
            }
        }
        System.out.println("Loop search time: " + (System.currentTimeMillis() - time) / (float) loop);
        assert result.containsAll(is) && is.containsAll(result);

        time = System.currentTimeMillis();
        result = new IntOpenHashSet();
        loop = 10;
        for (int j = 0; j < loop; j++) {
            for (int i = 0; i < strs.size(); i++) {
                if (strs.get(i).contains("hong2")) result.add(i);
            }
        }
        System.out.println("Contains search time: " + (System.currentTimeMillis() - time) / (float) loop);
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
        TreeSearcher<Integer> tree = new TreeSearcher<>(true, new PinIn());
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
        TreeSearcher<Integer> tree = new TreeSearcher<>(true, p);
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

    public static void main(String[] args) throws IOException {
        new PinInTest().performance();
    }
}
