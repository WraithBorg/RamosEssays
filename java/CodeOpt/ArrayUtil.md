## 将数组根据MAX_SEND分割成多个小数组
```java
package com.fx.share.aaaa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将数组根据MAX_SEND分割成多个小数组
 */
public class Test {
    /**
     * 测试数组分割
     */
    public static void main(String[] args) {
        List<String> toBeSplitArray = new ArrayList<>();//需要分割的数组
        for (int i = 0; i < 20; i++) toBeSplitArray.add(i + "");
        List<List<String>> splitList = splitList(toBeSplitArray);
        for (List<String> strings : splitList) {
            StringBuilder sb = new StringBuilder();
            strings.forEach(m -> sb.append(m).append(","));
            System.out.println(sb.toString());
        }
    }
    private final static Integer MAX_SEND = 6;
    /**
     * 将数组根据MAX_SEND分割成多个小数组
     */
    private static List<List<String>> splitList(List<String> list) {
        List<List<String>> mgList = new ArrayList<>();
        int limit = (list.size() + MAX_SEND - 1) / MAX_SEND;
        Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
            mgList.add(list.stream().skip(i * MAX_SEND).limit(MAX_SEND).collect(Collectors.toList()));
        });
        return mgList;
    }
}

```

#### 输出结果  
```
0,1,2,3,4,5,
6,7,8,9,10,11,
12,13,14,15,16,17,
18,19,
```
