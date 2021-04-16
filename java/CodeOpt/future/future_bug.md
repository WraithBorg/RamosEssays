```java
public class CompletableFutureTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableFutureTask.class);
    private String name;
    public CompletableFutureTask (String name) {
        this.name = name;
    }
    @Override
    public void run () {
        try {
            for (int i = 0; i < 6; i++) {
                Thread.sleep(1000);
                LOGGER.error("Do Something "+i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
/**
 * 测试中断CompletableFuture
 */
public class CompletableFutureTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableFutureTest.class);
    public static void main (String[] args) throws InterruptedException {
        LOGGER.error("开始测试");
        CompletableFutureTask thread = new CompletableFutureTask("");
        CompletableFuture<Void> future = CompletableFuture.runAsync((thread));
        future.whenCompleteAsync((x, y) -> {
            LOGGER.error("异步任务完成");
        });
        Thread.sleep(4000);
        LOGGER.error("结束任务");
        future.cancel(true);
        Thread.sleep(4000);
    }
    /* 输出结果
    - 开始测试
    - Do Something 0
    - Do Something 1
    - 结束任务
    - 异步任务完成
    - Do Something 2
    - Do Something 3
    cancel方法并没有中断线程
    * */
}
```

```java
public class FutureTask implements Callable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureTask.class);
    private String name;
    public FutureTask (String name) {
        this.name = name;
    }
    @Override
    public Object call () {
        try {
            for (int i = 0; i < 6; i++) {
                Thread.sleep(1000);
                LOGGER.error("Do Something "+i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
/**
 * 测试中断FutureTest
 */
public class FutureTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureTest.class);
    public static void main (String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        LOGGER.error("开始测试");
        FutureTask thread = new FutureTask("线程001");
        Future future = pool.submit(thread);
        Thread.sleep(4000);
        LOGGER.error("结束任务");
        future.cancel(true);
        Thread.sleep(4000);
    }
/* 输出结果
 * 开始测试
 * Do Something 0
 * Do Something 1
 * Do Something 2
 * 结束任务
 */
}
```
### 结论
Future不仅会终止正在等待的get()，还会试图去中断底层的线程   
CompletableFuture.cancel()不会中断线程，尽管Future看起来被取消了，但后台线程仍然在执行
