# CPU缓存行的作用
```java
/**
 * 给长度为10000的二维数组赋值
 * 运行结果:97,103,95
 */
public class TestForContent {
    static final int LINE_NUM = 10000;
    static final int COLUM_NUM = 10000;
    public static void main (String[] args) {
        long[][] array = new long[LINE_NUM][COLUM_NUM];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LINE_NUM; ++i) {
            for (int k = 0; k < COLUM_NUM; ++k) {
                array[i][k] = i * 2 + k;//array[0][0],array[0][1],array[0][2],array[0][3],,array[9999][9999]=29997
            }
        }
        long endTime = System.currentTimeMillis();
        long cacheTime = endTime - startTime;
        System.out.println("cache time:" + cacheTime);
    }
}
```
```java
/**
 * 给长度为10000的二维数组赋值
 * 运行结果:2039,2053,2064
 */
public class TestForContent2 {
    static final int LINE_NUM = 10000;
    static final int COLUM_NUM = 10000;
    public static void main (String[] args) {
        long[][] array = new long[LINE_NUM][COLUM_NUM];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < COLUM_NUM; ++i) {
            for (int k = 0; k < LINE_NUM; ++k) {
                array[k][i] = i * 2 + k;//array[0][0],array[1][0],array[2][0],array[3][0],,array[9999][9999]=29997
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("no cache time:" + (endTime - startTime));
    }
}
```

### 运行结果对比
>   TestForContent明显比TestForContent2快很多 
>   `TestForContent`是因为数组内数组元素之间内存地址是连续的,当访问数组第一个元素时候,会把第一个元素后续若干元素一块放入到cache line,   
这样顺序访问数组元素时候会在cache中直接命中,就不会去主内存读取,后续访问也是这样  
    总结下也就是当顺序访问数组里面元素时候,如果当前元素在cache没有命中,  
那么会从主内存一下子读取后续若干个元素到cache,也就是一次访问内存可以让后面多次直接在cache命中  
    `TestForContent2`是跳跃式访问数组元素的,而不是顺序的,这破坏了程序访问的局部性原理,并且cache是有容量控制的,  
cache满了会根据一定淘汰算法替换cache行,会导致从内存置换过来的cache行的元素还没等到读取就被替换掉了.  
    所以单个线程下顺序修改一个cache行中的多个变量,是充分利用了程序运行局部性原理,会加速程序的运行,  
而多线程下并发修改一个cache行中的多个变量而就会进行竞争cache行,降低程序运行性能.  
