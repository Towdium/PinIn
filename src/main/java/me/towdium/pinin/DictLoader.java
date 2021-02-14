package me.towdium.pinin;

import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

@FunctionalInterface
interface DictLoader {
    void load(BiConsumer<Character, String[]> feed);

    class Default implements DictLoader {
        @Override
        public void load(BiConsumer<Character, String[]> feed) {
            InputStream is = PinIn.class.getResourceAsStream("data.txt");
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    char ch = line.charAt(0);
                    String[] records = line.substring(3).split(", ");
                    feed.accept(ch, records);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
