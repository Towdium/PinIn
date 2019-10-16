package me.towdium.pinin;

import me.towdium.pinin.utils.Matcher;
import org.junit.jupiter.api.Test;

public class PinInTest {
    @Test
    public void test() {
        assert Matcher.contains("中国", "zhg");
    }
}
