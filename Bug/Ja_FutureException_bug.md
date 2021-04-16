# 线程池提交过程中,小心异常被吞掉
```java
/**
 * 线程池提交过程中,小心异常被吞掉
 */
public class FutureException_BUG {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.rangeClosed(1, 10).forEach(i -> executorService.submit(()-> {
                    if (i == 5) {
                        System.out.println("发生异常啦");
                        throw new RuntimeException("error 这是异常信息");
                    }
                    System.out.println("当前执行第几:" + Thread.currentThread().getName() );
                }
        ));
        executorService.shutdown();
// 输出结果
    // 当前执行第几:pool-1-thread-1
    // 当前执行第几:pool-1-thread-2
    // 当前执行第几:pool-1-thread-3
    // 当前执行第几:pool-1-thread-4
    // 发生异常啦
    // 当前执行第几:pool-1-thread-6
    }
}

```
### 解决方案
+ 1.在任务代码try/catch捕获异常
+ 2.通过Future对象的get方法接收抛出的异常,再处理
+ 3.为工作者线程设置UncaughtExceptionHandler,在uncaughtException方法中处理异常
+ 4.重写ThreadPoolExecutor的afterExecute方法,处理传递的异常引用
