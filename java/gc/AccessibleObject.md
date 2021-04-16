# Gc可达对象分析
### finalize
当对象变成(GC Roots)不可达时,GC会判断该对象是否覆盖了finalize方法,若未覆盖,则直接将其回收.
否则,若对象未执行过finalize方法,将其放入F-Queue队列,由一低优先级线程执行该队列中对象的finalize方法.
执行finalize方法完毕后,GC会再次判断该对象是否可达,若不可达,则进行回收,否则,对象“复活


### 可达对象(reachable object)
```
/**
 * 可达对象(reachable object)
 * 是可以从任何活动线程的任何潜在的持续访问中的任何对象；
 * java 编译器或代码生成器可能会对不再访问的对象提前置为 null,使得对象可以被提前回收
 * 也就是说,在 jvm 的优化下,可能会出现对象不可达之后被提前置空并回收的情况
 */
public class PermGenOomMock {
    @Override
    protected void finalize() throws Throwable {
        System.out.println(this + " was finalized!");
    }
    public static void main(String[] args) {
        PermGenOomMock pg = new PermGenOomMock();
        System.out.println("Created pg");
        for (int i = 0; i < 1000_000_000; i++) {
            if (i % 10_000 == 0) {
                System.gc();
            }
        }
        System.out.println("Done!");// finalized出现在Done之前,证明虽然从对象作用域来说,方法没有执行完,栈帧并没有出栈,但是还是会被提前回收
    }
}
```

```
public class GcTest {
    @Override
    protected void finalize() throws Throwable {
        System.out.println(this + " was finalized!");
    }
    public static void main(String[] args) {
        GcTest pg = new GcTest();
        System.out.println("Created pg");
        for (int i = 0; i < 1_000_000; i++) {
            if (i % 10_000 == 0) {
                System.gc();
            }
        }
        System.out.println("Done!");
        System.out.println(pg);
        /**
         * 输出结果 表示对象并没有被提前回收
         * Created pg
         * Done!
         * com.ctp.athread.PermGenOomMock@13221655
         */
    }
}
```
```
public class GcTest {
    @Override
    protected void finalize() throws Throwable {
        System.out.println(this + " was finalized!");
    }
    public static void main(String[] args) {
        GcTest pg = new GcTest();
        pg = null;
        System.out.println("Created pg");
        for (int i = 0; i < 1_000_000; i++) {
            if (i % 10_000 == 0) {
                System.gc();
            }
        }
        System.out.println("Done!");
        System.out.println(pg);
        /**
         * 输出结果,表示如果将对象置为null,即便后续有对该对象的引用,也仍然会被回收
         * Created pg
         * com.ctp.athread.GcTest@32a48caa was finalized!
         * Done!
         * null
         */
    }
}
```
