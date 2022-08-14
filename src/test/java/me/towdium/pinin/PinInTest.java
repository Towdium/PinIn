package me.towdium.pinin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;
import me.towdium.pinin.searchers.CachedSearcher;
import me.towdium.pinin.searchers.Searcher;
import me.towdium.pinin.searchers.Searcher.Logic;
import me.towdium.pinin.searchers.SimpleSearcher;
import me.towdium.pinin.searchers.TreeSearcher;
import me.towdium.pinin.utils.PinyinFormat;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.towdium.pinin.Keyboard.*;
import static me.towdium.pinin.searchers.Searcher.Logic.CONTAIN;
import static me.towdium.pinin.searchers.Searcher.Logic.EQUAL;

public class PinInTest {
    @Test
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    public void performance() throws IOException {
        List<String> search = new ArrayList<>();
        search.add("boli");
        search.add("yangmao");
        search.add("hongse");
        List<Function<Logic, Searcher<Integer>>> funcs = new ArrayList<>();
        funcs.add(l -> new TreeSearcher<>(l, new PinIn()));
        funcs.add(l -> new CachedSearcher<>(l, new PinIn()));
        funcs.add(l -> new SimpleSearcher<>(l, new PinIn()));
        String[] sources = new String[]{"small", "large"};

        for (Logic j : Logic.values()) {
            for (String k : sources) {
                List<String> data = loadTestData(k);
                boolean reduced = data.size() > 100000;
                System.out.print("Logic: " + j.toString().toLowerCase() + ", source: " + k);

                float contains = time(reduced ? 10 : 100, search, s -> {
                    IntSet result = new IntOpenHashSet();
                    for (int i = 0; i < data.size(); i++) {
                        String in = data.get(i);
                        if (j.raw(in, s)) result.add(i);
                    }
                });
                System.out.print(", contains search: " + String.format("%.2f", contains));

                PinIn p = new PinIn();
                float traverse = time(reduced ? 10 : 100, search, s -> {
                    IntSet result = new IntOpenHashSet();
                    for (int i = 0; i < data.size(); i++) {
                        String in = data.get(i);
                        if (j.test(p, in, s)) result.add(i);
                    }
                });
                System.out.println(", loop search: " + String.format("%.1f", traverse));

                for (Function<Logic, Searcher<Integer>> i : funcs) {
                    performance(j, data, search, i);
                }
            }
        }
    }

    private float time(int repeat, Runnable exec) {
        long time = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            exec.run();
        }
        return (System.currentTimeMillis() - time) / (float) repeat;
    }

    private <T> float time(int repeat, Collection<T> objs, Consumer<T> consumer) {
        long time = System.currentTimeMillis();
        for (int j = 0; j < repeat; j++) {
            for (T i: objs) {
                consumer.accept(i);
            }
        }
        return (System.currentTimeMillis() - time) / (float) repeat / objs.size();
    }

    private List<String> loadTestData(String source) throws IOException {
        String line;
        List<String> data = new ArrayList<>();
        InputStream is = PinInTest.class.getResourceAsStream(source + ".txt");
        assert is != null;
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            data.add(line);
        }
        return data;
    }

    private void performance(Logic logic, List<String> texts, List<String> tokens,
                             Function<Logic, Searcher<Integer>> func) {
        long start = System.currentTimeMillis();
        final Searcher<Integer> searcher = func.apply(logic);
        for (int j = 0; j < texts.size(); j++) searcher.put(texts.get(j), j);
        boolean reduced = texts.size() > 100000;
        System.out.print("  " + searcher.getClass().getSimpleName());

        int loop = reduced ? 10 : 100;
        if (searcher instanceof TreeSearcher) loop /= 5;
        float construct = time(loop, () -> {
            Searcher<Integer> temp = func.apply(logic);
            for (int j = 0; j < texts.size(); j++) temp.put(texts.get(j), j);
        });
        System.out.print(": construction: " + String.format("%.1f", construct));

        if (searcher instanceof CachedSearcher) {
            float warm = time(reduced ? 10 : 100, tokens, s -> {
                List<Integer> is = searcher.search(s);
                ((CachedSearcher<Integer>) searcher).reset();
            });
            System.out.print(", warmup: " + String.format("%.1f", warm));
        }

        loop = reduced ? 1000 : 10000;
        if (searcher instanceof SimpleSearcher) loop /= 100;
        float search = time(loop, tokens, (s) -> {
            List<Integer> is = searcher.search(s);
        });
        System.out.print(", accelerated: " + String.format("%.3f", search));
        System.out.println(", total: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void quanpin() {
        PinIn p = new PinIn();
        assert p.contains("测试文本", "ceshiwenben");
        assert p.contains("测试文本", "ceshiwenbe");
        assert p.contains("测试文本", "ceshiwben");
        assert p.contains("测试文本", "ce4shi4wb");
        assert !p.contains("测试文本", "ce2shi4wb");
        assert p.contains("合金炉", "hejinlu");
        assert p.contains("洗矿场", "xikuangchang");
        assert p.contains("流体", "liuti");
        assert p.contains("轰20", "hong2");
        assert p.contains("hong2", "hong2");
        assert !p.begins("测", "ce4a");
        assert !p.begins("", "a");
        assert p.contains("石头", "stou");
        assert p.contains("安全", "aquan");
        assert p.contains("昂扬", "ayang");
        assert !p.contains("昂扬", "anyang");
        assert p.contains("昂扬", "angyang");
    }

    @Test
    public void daqian() {
        PinIn p = new PinIn().config().keyboard(DAQIAN).commit();
        assert p.contains("测试文本", "hk4g4jp61p3");
        assert p.contains("测试文本", "hkgjp1");
        assert p.contains("錫", "vu6");
        assert p.contains("鑽石", "yj0");
        assert p.contains("物質", "j456");
        assert p.contains("腳手架", "rul3g.3ru84");
        assert p.contains("鵝", "k6");
        assert p.contains("葉", "u,4");
        assert p.contains("共同", "ej/wj/");
    }

    @Test
    public void xiaohe() {
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
        PinIn p = new PinIn().config().keyboard(ZIRANMA).commit();
        assert p.contains("测试文本", "ceuiwfbf");
        assert p.contains("测试文本", "ceuiwf2");
        assert !p.contains("测试文本", "ceuiw2");
        assert p.contains("合金炉", "hej");
        assert p.contains("洗矿场", "xikd4");
        assert p.contains("月球", "ytqq");
        assert p.contains("安全", "anqr");
    }

    @Test
    public void tree() {
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
        s = tree.search("ce4shi4wb");
        assert s.size() == 1 && s.contains(1);
        s = tree.search("ce2shi4wb");
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
        s = tree.search("ce4shi4wb");
        assert s.size() == 1 && s.contains(0);
        s = tree.search("ce4shw");
        assert s.size() == 2;
        s = tree.search("ce4sw");
        assert s.size() == 2;
        s = tree.search("ce4siw");
        assert s.isEmpty();
        p.config().fSh2S(true).commit();
        s = tree.search("ce4siw");
        assert s.size() == 2;
        p.config().fSh2S(false).keyboard(DAQIAN).commit();
        s = tree.search("hk4g4jp61p3");
        assert s.size() == 1;
        s = tree.search("ce4shi4wb");
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
        PinIn pi = new PinIn();
        Char ch = pi.getChar('圆');
        Pinyin py = ch.pinyins()[0];
        assert PinyinFormat.NUMBER.format(py).equals("yuan2");
        assert PinyinFormat.RAW.format(py).equals("yuan");
        assert PinyinFormat.UNICODE.format(py).equals("yuán");
        assert PinyinFormat.PHONETIC.format(py).equals("ㄩㄢˊ");
        pi.config().format(PinyinFormat.PHONETIC).commit();
        assert pi.format(pi.getPinyin("le0")).equals("˙ㄌㄜ");
    }

    @Test
    public void dict() {
        PinIn p = new PinIn();
        TreeSearcher<Integer> searcher = new TreeSearcher<>(CONTAIN, p);
        searcher.put("\uE900锭", 0);
        assert !searcher.search("lu2d").contains(0);
        assert !p.contains("\uE900", "lu2");

        p = new PinIn(new DictLoader.Default(){
            @Override
            public void load(BiConsumer<Character, String[]> feed) {
                super.load(feed);
                feed.accept('\uE900', new String[]{"lu2"});
            }
        });
        searcher = new TreeSearcher<>(CONTAIN, p);
        searcher.put("\uE900锭", 0);
        assert searcher.search("lu2d").contains(0);
        assert p.contains("\uE900", "lu2");
    }
}
