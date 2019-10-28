# PinIn

一个用于解决各类汉语拼音匹配问题的 Java 库。对于即时匹配，提供基于 NFA 的实现，对于索引匹配，提供类后缀树的实现。

#### 特性

- 极为灵活的简拼组合
- 7 种模糊音选项
- 支持全拼，双拼（自然码，小鹤），注音（大千）
- 提供即时匹配逻辑和基于缓存的匹配逻辑
- 允许实时的配置切换（包括模糊音以及键盘）

> 对于“中国”，可以允许的搜索串包括但不限于“中国”，“中guo”，“zhongguo”，“zhong国”，“zhong1国”，“zh1国”，“zh国”。
  基于模糊音设置，还允许“zong国”，“z国”等。

> 双拼输入尚在测试阶段，并且不（也不会）支持字形码。重码过多时，可以使用声调作为辅助码。

#### 性能

性能测试使用 Enigmatica 整合导出的 [测试样本][1]。样本中含有约 37k 词条，中英混合，约 400k 字符，容量约 900 KB。 性能如下：

 |  匹配逻辑  | 使用索引 | 搜索耗时 | 索引耗时 | 内存占用 |
 |:----------:|:--------:|:--------:|:--------:|:--------:|
 | startsWith |    否    |   15ms   |    N/A   |    N/A   |
 |  contains  |    否    |   60ms   |    N/A   |    N/A   |
 | startsWith |    是    |   0.7ms  |   230ms  |   3.5MB  |
 |  contains  |    是    |   0.9ms  |   500ms  |   12MB   |
 
对于索引模型，一些常量参数可以进一步调整，从而在速度与内存消耗间取得平衡。
 
#### 示例

```java
public static void main(String[] args) {
    PinIn p = new PinIn();  // context
    // direct match
    boolean result1 = p.contains("测试文本", "ceshi");
    // indexed match
    PinyinTree<Integer> tree = new PinyinTree<>(true, p);
    p.put("测试文本", 0);
    boolean result2 = tree.search("ceshi").contains(0);
}
```

更多示例参见 [测试代码][2]。

#### 致谢

内置的拼音数据来自于 [地球拼音][3] 和 [pinyin-data][4]，感谢他们的工作

Hava fun!

[1]: /src/test/resources/me/towdium/pinin/examples.txt
[2]: /src/test/java/me/towdium/pinin/PinInTest.java
[3]: https://github.com/rime/rime-terra-pinyin
[4]: https://github.com/mozillazg/pinyin-data