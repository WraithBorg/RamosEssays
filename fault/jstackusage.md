# 排查CPU使用率过高
`Author:zxu`
### 准备测试环境
#### 测试代码
```java

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class JstackCase {
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static Object lock = new Object();
    public static void main(String[] args) {
        MyTask myTask1 = new MyTask();
        MyTask myTask2 = new MyTask();
        executorService.execute(myTask1);
        executorService.execute(myTask2);
    }
    static class MyTask implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                long sum = 0L;
                while (true) {
                    sum += 1;
                }
            }
        }
    }
}
```
#### 模拟线上环境
```
cd /test
mkdir -p mydemo/bin/classes
mkdir -p mydemo/src/com/test
-- 将java文件放到test文件夹下,略
javac mydemo/src/com/test/JstackCase.java -d mydemo/bin/classes/
java -cp mydemo/bin/classes com.test.JstackCase &
```
#### 排查问题,执行TOP命令
发现进程19161占用最多的CPU资源
```
[root@test test]# top
top - 20:21:54 up 9 days,  9:24,  1 user,  load average: 1.21, 0.90, 0.78
Tasks: 160 total,   1 running,  88 sleeping,   0 stopped,   0 zombie
%Cpu(s): 26.5 us,  1.6 sy,  0.0 ni, 71.9 id,  0.0 wa,  0.0 hi,  0.1 si,  0.0 st
KiB Mem :  8047020 total,   911344 free,  2589760 used,  4545916 buff/cache
KiB Swap:  8126460 total,  8125424 free,     1036 used.  4999284 avail Mem 

  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND                                             
19161 root      20   0 4622036  27576  15756 S 100.0  0.3   5:14.46 java                                                
 1482 root      20   0  121116   8548   2804 S   2.0  0.1 253:59.94 phdaemon                                            
   10 root      20   0       0      0      0 I   0.3  0.0   6:22.63 rcu_sched   
```
通过top -Hp 19161查看该进程下各个线程的CPU使用情况，发现pid为19176的线程占用最多的CPU资源
```
[root@test test]# top -Hp 19161
top - 20:23:33 up 9 days,  9:25,  1 user,  load average: 1.04, 0.94, 0.80
Threads:  17 total,   1 running,  16 sleeping,   0 stopped,   0 zombie
%Cpu(s): 24.6 us,  1.6 sy,  0.0 ni, 73.8 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
KiB Mem :  8047020 total,   911996 free,  2589080 used,  4545944 buff/cache
KiB Swap:  8126460 total,  8125424 free,     1036 used.  4999964 avail Mem 

  PID USER      PR  NI    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND                                              
19176 root      20   0 4622036  27576  15756 R 99.9  0.3   6:52.83 java                                                 
19161 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.00 java                                                 
19162 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.04 java                                                 
19163 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.00 java                                                 
19164 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.00 java                                                 
19165 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.00 java                                                 
19166 root      20   0 4622036  27576  15756 S  0.0  0.3   0:00.00 java    
```
#### 使用jstack pid查看19176进程的堆栈状态
```
[root@test test]# jstack 19176
2020-12-03 20:26:11
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.181-b13 mixed mode):

"DestroyJavaVM" #11 prio=5 os_prio=0 tid=0x00007f5d80009000 nid=0x4ada waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"pool-1-thread-2" #10 prio=5 os_prio=0 tid=0x00007f5d800f9800 nid=0x4ae9 waiting for monitor entry [0x00007f5d706f5000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at com.test.JstackCase$MyTask.run(JstackCase.java:19)
	- waiting to lock <0x00000000d715c208> (a java.lang.Object)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

"pool-1-thread-1" #9 prio=5 os_prio=0 tid=0x00007f5d800f8000 nid=0x4ae8 runnable [0x00007f5d707f6000]
   java.lang.Thread.State: RUNNABLE
	at com.test.JstackCase$MyTask.run(JstackCase.java:21)
	- locked <0x00000000d715c208> (a java.lang.Object)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

"Service Thread" #8 daemon prio=9 os_prio=0 tid=0x00007f5d800d0800 nid=0x4ae6 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C1 CompilerThread2" #7 daemon prio=9 os_prio=0 tid=0x00007f5d800bd000 nid=0x4ae5 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread1" #6 daemon prio=9 os_prio=0 tid=0x00007f5d800bb800 nid=0x4ae4 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread0" #5 daemon prio=9 os_prio=0 tid=0x00007f5d800b8800 nid=0x4ae3 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Signal Dispatcher" #4 daemon prio=9 os_prio=0 tid=0x00007f5d800b6800 nid=0x4ae2 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Finalizer" #3 daemon prio=8 os_prio=0 tid=0x00007f5d80083800 nid=0x4ae1 in Object.wait() [0x00007f5d70efd000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000000d7108ed0> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
	- locked <0x00000000d7108ed0> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
	at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)

"Reference Handler" #2 daemon prio=10 os_prio=0 tid=0x00007f5d80081000 nid=0x4ae0 in Object.wait() [0x00007f5d70ffe000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000000d7106bf8> (a java.lang.ref.Reference$Lock)
	at java.lang.Object.wait(Object.java:502)
	at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
	- locked <0x00000000d7106bf8> (a java.lang.ref.Reference$Lock)
	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)

"VM Thread" os_prio=0 tid=0x00007f5d80077800 nid=0x4adf runnable 

"GC task thread#0 (ParallelGC)" os_prio=0 tid=0x00007f5d8001e800 nid=0x4adb runnable 

"GC task thread#1 (ParallelGC)" os_prio=0 tid=0x00007f5d80020000 nid=0x4adc runnable 

"GC task thread#2 (ParallelGC)" os_prio=0 tid=0x00007f5d80022000 nid=0x4add runnable 

"GC task thread#3 (ParallelGC)" os_prio=0 tid=0x00007f5d80024000 nid=0x4ade runnable 

"VM Periodic Task Thread" os_prio=0 tid=0x00007f5d800d5000 nid=0x4ae7 waiting on condition 

JNI global references: 5

Heap
 PSYoungGen      total 36864K, used 1905K [0x00000000d7100000, 0x00000000d9a00000, 0x0000000100000000)
  eden space 31744K, 6% used [0x00000000d7100000,0x00000000d72dc420,0x00000000d9000000)
  from space 5120K, 0% used [0x00000000d9500000,0x00000000d9500000,0x00000000d9a00000)
  to   space 5120K, 0% used [0x00000000d9000000,0x00000000d9000000,0x00000000d9500000)
 ParOldGen       total 84992K, used 0K [0x0000000085200000, 0x000000008a500000, 0x00000000d7100000)
  object space 84992K, 0% used [0x0000000085200000,0x0000000085200000,0x000000008a500000)
 Metaspace       used 2653K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 287K, capacity 386K, committed 512K, reserved 1048576K

```
将堆栈信息保存到文件里,方便分析
```
[root@test test]# jstack -l 19161 >log.txt
```
#### 定位代码
将占用CPU资源最多的线程pid转成16进制，19176 -> 4ae8  
thread dump中，每个线程都有一个nid  
找到4ae8对应的线程  发现一直是RUNNABLE状态,并确定代码位置JstackCase.java:21  
```
"pool-1-thread-1" #9 prio=5 os_prio=0 tid=0x00007f5d800f8000 nid=0x4ae8 runnable [0x00007f5d707f6000]
   java.lang.Thread.State: RUNNABLE
        at com.test.JstackCase$MyTask.run(JstackCase.java:21)
        - locked <0x00000000d715c208> (a java.lang.Object)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - <0x00000000d715da20> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"Service Thread" #8 daemon prio=9 os_prio=0 tid=0x00007f5d800d0800 nid=0x4ae6 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

```
