package me.towdium.pinin;

import me.towdium.pinin.utils.Matcher;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PinInTest {
    @Test
    public void test() throws IOException {
        PinIn.initialize();
        List<String> strs = new ArrayList<>();
        PinyinTree<Integer> tree = new PinyinTree<>(new PinIn());
        BufferedReader br = new BufferedReader(new InputStreamReader(
                PinInTest.class.getResourceAsStream("examples.txt"), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            String str = line.substring(0, line.length() - 1);
            strs.add(str);
        }
        for (int i = 0; i < strs.size(); i++) tree.put(strs.get(i), i);
        System.out.print("Tree constructed");
        for (Integer i: tree.search("hong2")) {
            System.out.println(strs.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        new PinInTest().test();
    }
}
