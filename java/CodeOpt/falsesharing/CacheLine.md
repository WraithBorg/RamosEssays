# CPU缓存行的作用
```java
/**
 * 给长度为10000的二维数组赋值
 * 运行结果:97,103,95
 */
public class Test4CpuCache {
    public static void main (String[] args) {
        long[][] array = new long[10000][10000];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            for (int k = 0; k < 10000; ++k) {
                array[i][k] = i * 2 + k;
                // 依次给array[0][0],array[0][1],array[0][2],array[0][3],,array[9999][9999]=29997赋值
                // 注:array[0][0],array[1][0],array[2][0]内存地址是不连续的
            }
        }
        long endTime = System.currentTimeMillis();
        long cacheTime = endTime - startTime;
        System.out.println("array[9999][9999]:" + array[9999][9999]);
        System.out.println("cache time:" + cacheTime);
    }
}
```
```java
/**
 * 给长度为10000的二维数组赋值
 * 运行结果:2039,2053,2064
 */
public class Test4CpuCache2 {
    public static void main (String[] args) {
        long[][] array = new long[10000][10000];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            for (int k = 0; k < 10000; ++k) {
                array[k][i] = i * 2 + k;
                //分别给array[0][0],array[1][0],array[2][0],array[3][0],,array[9999][9999]=29997赋值
                //注:array[0][0],array[1][0],array[2][0]内存地址是不连续的
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("array[9999][9999]:" + array[9999][9999]);
        System.out.println("no cache time:" + (endTime - startTime));
    }
}
```

### 运行结果对比
>   Test4CpuCache明显比Test4CpuCache2快很多 
>   `Test4CpuCache`是因为数组内数组元素之间内存地址是连续的,当访问数组第一个元素时候,会把第一个元素后续若干元素一块放入到cache line,   
这样顺序访问数组元素时候会在cache中直接命中,就不会去主内存读取,后续访问也是这样  
    总结下也就是当顺序访问数组里面元素时候,如果当前元素在cache没有命中,  
那么会从主内存一下子读取后续若干个元素到cache,也就是一次访问内存可以让后面的数组元素多次直接在cache里命中  
    `Test4CpuCache2`是跳跃式访问数组元素的,而不是顺序的,这破坏了程序访问的局部性原理,并且cache是有容量控制的,  
cache满了会根据一定淘汰算法替换cache行,会导致从内存置换过来的cache行的元素还没等到读取就被替换掉了.  
    所以单个线程下顺序修改一个cache行中的多个变量,是充分利用了程序运行局部性原理,会加速程序的运行,  
但多线程下并发修改一个cache行中的多个变量而就会进行竞争cache行,降低程序运行性能.  
