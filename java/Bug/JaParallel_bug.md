# 并行流处理引起的BUG
```java
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Tests {
    public static void main(String[] args) {
        System.out.println(getMapSizeParal());// 99052 结果一直小于100000
        System.out.println(getMapSize()); // 100000
        System.out.println(getConcurrentMapSize()); // 100000
    }

    private static int getConcurrentMapSize() {
        Stream<BigInteger> bigIntStream = Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.ONE)).limit(100000);
        ConcurrentHashMap<String, Object> mm = new ConcurrentHashMap<>();
        bigIntStream.parallel().forEach(m -> {
            mm.put("a" + m, "");
        });
        return mm.size();
    }

    private static int getMapSizeParal() {
        Stream<BigInteger> bigIntStream = Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.ONE)).limit(100000);
        Map<String, Object> mm = new HashMap<>();
        bigIntStream.parallel().forEach(m -> {
            mm.put("a" + m, null);
        });
        return mm.size();
    }

    private static int getMapSize() {
        Stream<BigInteger> bigIntStream = Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.ONE)).limit(100000);
        Map<String, Object> mm = new HashMap<>();
        bigIntStream.forEach(m -> {
            mm.put("a" + m, null);
        });
        return mm.size();
    }

}

```
