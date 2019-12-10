# PinIn

一个用于解决各类汉语拼音匹配问题的 Java 库。对即时匹配提供基于 NFA 的实现，对索引匹配提供类后缀树的实现。除此之外，它还可以将汉字转换为拼音字符串，包括 ASCII，Unicode 和注音符号。

## 特性

- 极为灵活的简拼组合
- 7 种模糊音选项
- 支持全拼，双拼（自然码，小鹤），注音（大千）
- 提供即时匹配逻辑和基于缓存的匹配逻辑
- 允许实时的配置切换（包括模糊音以及键盘）

> 对于“中国”，可以允许的搜索串包括但不限于“中国”，“中guo”，“zhongguo”，“zhong国”，“zhong1国”，“zh1国”，“zh国”。
  基于模糊音设置，还允许“zong国”，“z国”等。

> 双拼输入尚在测试阶段，并且不（也不会）支持字形码。重码过多时，可以使用声调作为辅助码。

对于原理和思路，参见 [再谈拼音搜索][5] 系列。

## 性能

性能测试使用 Enigmatica 整合导出的 [测试样本][1]。共约 37k 词条，中英混合，约 400k 字符，容量约 900 KB。性能如下：

__部分匹配__

| 匹配逻辑 | 构建耗时 | 预热耗时 | 搜索耗时 | 内存使用 |
|:------:|:------:|:--------:|:-------:|-------|
| TreeSearcher | 210ms | N/A | 0.19ms | 9.50MB |
| SimpleSearcher | 27ms | N/A | 9.1ms | 1.84MB |
| CachedSearcher | 28ms | 16ms | 0.55ms | 见备注 |
| 遍历拼音匹配 | N/A | N/A | 23ms | N/A |
| 遍历 contains | N/A | N/A | 0.53ms | N/A |

__前缀匹配__

| 匹配逻辑 | 构建耗时 | 预热耗时 | 搜索耗时 | 内存使用 |
|:------:|:------:|:--------:|:-------:|-------|
| TreeSearcher | 62.5ms | N/A | 0.083ms | 2.80MB |
| SimpleSearcher | 30ms | N/A | 2.4ms | 1.84MB |
| CachedSearcher | 28ms | 2.8ms | 0.10ms | 见备注 |
| 遍历拼音匹配 | N/A | N/A | 8.8ms | N/A |
| 遍历 startsWith | N/A | N/A | 0.53ms | N/A |

> `CachedSearcher` 的内存使用和搜索速度在不同场景下可能会有明显波动，一般介于 `TreeSearcher` 和 `SimpleSearcher` 之间。
 
对于 `TreeSearcher` 和 `CachedSearcher`，一些常量参数可以进一步调整，从而在速度与内存消耗间取得平衡。
 
## 示例

```java
public static void main(String[] args) {
    PinIn p = new PinIn();  // context
    // direct match
    boolean result1 = p.contains("测试文本", "ceshi");
    // indexed match
    PinyinTree<Integer> tree = new PinyinTree<>(true, p);
    p.put("测试文本", 0);
    boolean result2 = tree.search("ceshi").contains(0);
    // pinyin format
    Char c = p.genChar('圆');
    Pinyin y = c.pinyins()[0];
    String s1 = y.format(UNICODE)  // yuán
    String s2 = y.format(PHONETIC)  // ㄩㄢˊ
}
```

更多示例参见 [测试代码][2]。

## 致谢

本项目依赖于 [Fastutil][6]。在 shadow 版 Jar 文件中内置了一个裁剪过的实现，使用纯净版 Jar 文件时则需要用户自行配置。

内置的拼音数据来自于 [地球拼音][3] 和 [pinyin-data][4]。

Hava fun!

[1]: /src/test/resources/me/towdium/pinin/small.txt
[2]: /src/test/java/me/towdium/pinin/PinInTest.java
[3]: https://github.com/rime/rime-terra-pinyin
[4]: https://github.com/mozillazg/pinyin-data
[5]: https://www.towdium.me/2019/11/05/pinyin-search-again-1/
[6]: http://fastutil.di.unimi.it/
