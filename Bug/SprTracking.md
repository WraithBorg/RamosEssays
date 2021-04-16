# 打印方法调用栈 方便排查问题
```java
/**
 * 打印方法调用栈
 */
public class SprTracking {
    private static Logger LOGGER = LoggerFactory.getLogger(SprTracking.class);
    public static void trace() {
        try {
            Set<String> class_startsWith_rules = new HashSet<>();
            class_startsWith_rules.add("org.");
            class_startsWith_rules.add("sun.");
            class_startsWith_rules.add("javax.");
            class_startsWith_rules.add("java.");

            Set<String> class_contains_rules = new HashSet<>();
            class_contains_rules.add("BySpringCGLIB");
            class_contains_rules.add("SprTracking");
            class_contains_rules.add("ParamsFilter");
            class_contains_rules.add("InjectFilter");
            class_contains_rules.add("MultitenancyFilter");
            class_contains_rules.add("FreemarkerVariableFilter");

            StringBuilder sb = new StringBuilder("打印方法调用栈\n");
            StackTraceElement[] element = Thread.currentThread().getStackTrace();
            out:
            for (StackTraceElement stackTraceElement : element) {
                for (String k : class_startsWith_rules) {//校验规则
                    if (stackTraceElement.getClassName().startsWith(k)) {
                        continue out;
                    }
                }
                for (String k : class_contains_rules) {
                    if (stackTraceElement.getClassName().contains(k)) {
                        continue out;
                    }
                }
                sb.append(stackTraceElement).append("\n");
            }
            LOGGER.warn(sb.toString());
        } catch (Exception e) {
            LOGGER.error("打印方法调用栈日志失败：", e);
        }
    }
}
```
