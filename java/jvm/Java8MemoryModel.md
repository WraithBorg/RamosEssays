## Java8 内存模型
VM 内存共分为虚拟机栈、堆、方法区、程序计数器、本地方法栈五个部分。  

1.虚拟机栈：每个线程有一个私有的栈，随着线程的创建而创建。  
栈里面存着的是一种叫“栈帧”的东西，每个方法会创建一个栈帧，
栈帧中存放了局部变量表（基本数据类型和对象引用）、操作数栈、方法出口等信息  

2.本地方法栈
主要与虚拟机用到的 Native 方法相关  

3.PC 寄存器
PC 寄存器，也叫程序计数器。JVM支持多个线程同时运行，每个线程都有自己的程序计数器。  
倘若当前执行的是 JVM 的方法，则该寄存器中保存当前执行指令的地址；倘若执行的是native 方法，则PC寄存器中为空。

4.堆(属于线程共享区域)
堆内存是 JVM 所有线程共享的部分，在虚拟机启动的时候就已经创建。所有的对象和数组都在堆上进行分配。  
这部分空间可通过 GC 进行回收。当申请不到空间时会抛出 OutOfMemoryError

5.方法区(属于线程共享区域)
方法区也是所有线程共享。主要用于存储类的信息、常量池、方法数据、方法代码等。
方法区逻辑上属于堆的一部分，但是为了与堆进行区分，通常又叫“非堆”

```
/**
 * 堆内存溢出代码
 * java.lang.OutOfMemoryError: Java heap space
 */
public class AtTest {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<byte[]>();
        int i = 0;
        boolean flag = true;
        while (flag) {
            try {
                i++;
                list.add(new byte[1024 * 1024]);//每次增加一个1M大小的数组对象
            } catch (Throwable e) {
                e.printStackTrace();
                flag = false;
                System.out.println("count=" + i);//记录运行的次数
            }
        }
    }
}
```

## PermGen（永久代）
方法区和“PermGen space”又有着本质的区别。  
前者是 JVM 的规范，而后者则是 JVM 规范的一种实现，并且只有 HotSpot 才有 “PermGen space  
方法区主要存储类的相关信息，所以对于动态生成类的情况比较容易出现永久代的内存溢出  
```
// 永久代内存溢出

public class Test {
}

public class PermGenOomMock{
    public static void main(String[] args) {
        URL url = null;
        List<ClassLoader> classLoaderList = new ArrayList<ClassLoader>();
        try {
            url = new File("/tmp").toURI().toURL();
            URL[] urls = {url};
            while (true){
                ClassLoader loader = new URLClassLoader(urls);
                classLoaderList.add(loader);
// 通过每次生成不同URLClassLoader对象来加载Test类，从而生成不同的类对象，就会产生 "java.lang.OutOfMemoryError: PermGen space " 异常
                loader.loadClass("com.paddx.test.memory.Test");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
## Metaspace（元空间）
空间的本质和永久代类似，都是对JVM规范中方法区的实现。  
不过元空间与永久代之间最大的区别在于：元空间并不在虚拟机中，而是使用本地内存。  
因此，默认情况下，元空间的大小仅受本地内存限制
```
/**
 * 产生：Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
 * 证明1.8 将字符串常量由永久代转移到堆中，并且 JDK 1.8 中已经不存在永久代
 */
public class AtTest {
    static String base = "string";
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String str = base + base;
            base = str;
            list.add(str.intern());
        }
    }
}
```



















