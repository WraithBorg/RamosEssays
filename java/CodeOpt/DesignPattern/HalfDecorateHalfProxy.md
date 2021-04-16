# 半装饰器半代理模式

```java
/**
 * 半装饰器半代理模式
 */
public class TestAction {
    public static void main (String[] args) {
        raw();
        improve();
    }
    private static void improve () {
        SprOutPut outPut = new SprOutPut();
        SprProExecutor executor = new SprProExecutor(outPut::output);
        executor.run();
// 输出结果:
//    This is before run
//    这是要执行的业务
//    This is after run
    }
    private static void raw () {
        SprOutPut outPut = new SprOutPut();
        outPut.output();
// 输出结果:这是要执行的业务
    }
}
```
```java
@FunctionalInterface
public interface SprAction {
    public void doTask ();
}
```
```java
public class SprOutPut {
    public void output () {
        System.out.println("这是要执行的业务");
    }
}
```
```java
public class SprProExecutor {
    private SprAction action;
    public SprProExecutor (SprAction action) {
        this.action = action;
    }
    public void run () {
        System.out.println("This is before run");
        action.doTask();
        System.out.println("This is after run");
    }
}
```
