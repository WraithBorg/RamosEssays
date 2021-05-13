# 伪共享缓存和解决办法
### 伪共享缓存
    当CPU访问某一个变量时候,首先会去看CPU Cache内是否有该变量,如果有则直接获取,否则就去主内存里面获取该变量,  
    然后把该变量所在内存区域的一个Cache行大小的内存拷贝到Cache  
    由于存放到Cache行的的是内存块而不是单个变量,所以可能会把多个变量存放到了一个cache行  
    伪共享的产生是因为多个变量被放入了一个缓存行,并且多个线程同时去写入缓存行中不同变量.  
    多线程下并发修改一个cache行中的多个变量而就会进行竞争cache行,降低程序运行性能  
> Thread,ConcurrentHashMap,Striped64 ,内部均使用过注解:@sun.misc.Contended("x")
```java
/**
 * 利用四个线程分别给数组testLongs的四个元素(testLongs[0],testLongs[1],testLongs[2],testLongs[3])分别赋值
 *      一般情况下,testLongs[0],testLongs[1],testLongs[2],testLongs[3]会加载到同一个CPU Cache Line
 * 每当线程修改其中一个元素,都会该行CPU Cache Line失效,从而导致其他线程重新从主存中获取该数组,
 * 而且多个线程同时操作同一缓存行,会发生资源竞争,从而降低读写效率
 *      如果testLongs[0],testLongs[1],testLongs[2],testLongs[3]分别独占一行CPU Cache Line,
 * 当线程修改其中一个元素的时候,不会导致其他元素所在的缓存行失效,这样修改数组内任一元素,都不会影响其他数组元素的缓存,
 * 使得CPU能充分利用缓存进行计算
 */
public class FalseSharing implements Runnable {
    public final static int NUM_THREADS = 4; // 线程数量4
    public final static long ITERATIONS = 500L * 1000L * 1000L; //运行次数 5亿
//    private static LongUtils.TestLong1[] testLongs = LongUtils.testLong1();// 执行时间,25236343700,39646190900,51355173400,52297004500
//    private static LongUtils.TestLong2[] testLongs = LongUtils.testLong2();// 执行时间  ,4861626900, 4701989500, 4740426500, 4790690100
    private static LongUtils.TestLong3[] testLongs = LongUtils.testLong3();//   执行时间 未加参数:46033748900,26533029600,加参数`-XX:-RestrictContended`后,4681942400,4725286700   
    private final int arrayIndex;// 数组下标:取值0,1,2,3
    public FalseSharing (final int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }
    /**
     * 运行5亿次,给数组赋值
     */
    @Override
    public void run () {
        long i = ITERATIONS + 1;
        while (0 != --i) {
            testLongs[arrayIndex].value = i;
        }
    }
}
```
```java
public class FalseSharingTest {
    public static void main (final String[] args) throws Exception {
        long start = System.nanoTime();
        runTest();
        System.out.println("duration = " + (System.nanoTime() - start));
    }
    /**
     * 创建四个线程并启动
     */
    private static void runTest () throws InterruptedException {
        Thread[] threads = new Thread[FalseSharing.NUM_THREADS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new FalseSharing(i));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }
}
```
```java
public class LongUtils {
    /**
     * 存在伪共享缓存(false sharing)的样例
     * 执行结果:25849725100,53126311800,32788566200
     */
    public final static class TestLong1 {
        public volatile long value = 0L;
    }
    public static TestLong1[] testLong1 () {
        TestLong1[] testLongs = new TestLong1[NUM_THREADS];
        for (int i = 0; i < testLongs.length; i++) {
            testLongs[i] = new TestLong1();
        }
        return testLongs;
    }
    /**
     * 利用long padding避免false sharing
     * 执行结果:4849755000,4741913400,4802031700,5008210300
     */
    public final static class TestLong2 {
        volatile long p0, p1, p2, p3, p4, p5, p6;
        public volatile long value = 0L;
        volatile long q0, q1, q2, q3, q4, q5, q6;
    }
    public static TestLong2[] testLong2 () {
        TestLong2[] testLongs = new TestLong2[NUM_THREADS];
        for (int i = 0; i < testLongs.length; i++) {
            testLongs[i] = new TestLong2();
        }
        return testLongs;
    }
    /**
     * jdk8新特性,利用Contended注解避免false sharing
     * -XX:-RestrictContended
     * 执行结果:
     * 未加参数 -XX:-RestrictContended:24924735700
     * 填加参数 -XX:-RestrictContended:4682705900,4645343800,4694760000
     */
    @sun.misc.Contended
    public final static class TestLong3 {
        public volatile long value = 0L;
    }
    public static TestLong3[] testLong3 () {
        TestLong3[] testLongs = new TestLong3[NUM_THREADS];
        for (int i = 0; i < testLongs.length; i++) {
            testLongs[i] = new TestLong3();
        }
        return testLongs;
    }
}
```

### 总结
