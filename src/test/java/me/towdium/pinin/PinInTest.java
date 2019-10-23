package me.towdium.pinin;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PinInTest {
    /*
    First char int index memory 42MB, construction 320ms, search 0.5ms
    First char generic index memory 46MB, construction 700ms, search 0.6ms
    Pinyin generic index memory 41MB, construction 2000ms, search 0.3ms
    Pinyin dense tree slice memory 33MB, construction 1700ms, search 0.5ms
    Pinyin dense tree dnode memory 24MB, construction 1900ms, search 0.6ms
    Pinyin dense phoneme cache memory 22MB construction 1700ms, search 0.5ms
     */

    public static void main(String[] args) throws IOException {
        new PinInTest().test();
        // System.out.println(new PinIn().begins("安全安全", "qanq"));
    }

    @Test
    @SuppressWarnings({"UnusedAssignment", "unused"})
    public void test() throws IOException {
        List<String> strs = new ArrayList<>();
        PinyinTree tree = new PinyinTree(true, new PinIn());
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinInTest.class.getResourceAsStream("examples.txt"), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            String str = line.substring(0, line.length() - 1);
            strs.add(str);
        }
        System.out.println("Tree constructing");
        long time = System.currentTimeMillis();
        for (int i = 0; i < strs.size(); i++) tree.put(strs.get(i), i);
        System.out.println("Construction time: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        Set<Integer> is = null;
        for (int i = 0; i < 1000; i++) is = tree.search("hong2");
        System.out.println("Index search time: " + (System.currentTimeMillis() - time) / 1000f);

        //for (Integer i: is) System.out.println(strs.get(i));

        time = System.currentTimeMillis();
        PinIn p = new PinIn();
        for (String s: strs) p.contains(s, "hong2");
        System.out.println("Loop search time: " + (System.currentTimeMillis() - time));
    }
}
