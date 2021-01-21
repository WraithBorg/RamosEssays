## finally的异常会把try的异常覆盖
```java
/**
 * finally的异常会把try的「异常覆盖」
 */
public class Finally_BUG {
    public static void main(String[] args) {
        try {
            throw new RuntimeException("try1111111");
        } finally {
            throw new RuntimeException("finally222222");
            // 输出结果：Exception in thread "main" java.lang.RuntimeException: finally222222
            //	at com.Finally_BUG.main(Finally_BUG.java:9)
            // 发下异常‘try1111111’丢失
        }
    }
}
```
