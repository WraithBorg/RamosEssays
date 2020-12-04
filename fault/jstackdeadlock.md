# 用jstack排查死锁
`Author:zxu`   

死锁是指两个或两个以上的线程在执行过程中，因争夺资源而造成的一种互相等待的现象  
#### 死锁代码
```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class DeathLockTest {
    private static Lock lock1 = new ReentrantLock();
    private static Lock lock2 = new ReentrantLock();
    public static void main(String[] args) {
        deathLock();
    }
    private static void deathLock() {
        Thread t1 = new Thread(() -> {
            try {
                lock1.lock();
                System.out.println(Thread.currentThread().getName() + " get lock1");
                Thread.sleep(1000);
                lock2.lock();
                System.out.println(Thread.currentThread().getName() + " get lock2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                lock2.lock();
                System.out.println(Thread.currentThread().getName() + " get lock2");
                Thread.sleep(1000);
                lock1.lock();
                System.out.println(Thread.currentThread().getName() + " get lock1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.setName("THE FIRST");
        t2.setName("THE SECOND");
        t1.start();
        t2.start();
    }
}
```
模拟线上环境
```
cd /test
mkdir -p mydemo/bin/classes
mkdir -p mydemo/src/com/test
-- 将java文件放到test文件夹下,略
javac mydemo/src/com/test/DeathLockTest.java -d mydemo/bin/classes/
java -cp mydemo/bin/classes com.test.DeathLockTest &
```
#### jps -l 查看java进程信息

```
[root@test test]# jps -l
6466 sun.tools.jps.Jps
5539 com.test.DeathLockTest
10188 org.apache.catalina.startup.Bootstrap
5214 com.test.DeathLockTest
```

#### jstack -l {pid} 查看锁信息
```
[root@test test]# jstack -l 5539
2020-12-03 20:39:43
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.181-b13 mixed mode):

"Attach Listener" #12 daemon prio=9 os_prio=0 tid=0x00007f2e50001000 nid=0x1bc1 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"DestroyJavaVM" #11 prio=5 os_prio=0 tid=0x00007f2e90009000 nid=0x15a4 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"THE SECOND" #10 prio=5 os_prio=0 tid=0x00007f2e9016b000 nid=0x15b3 waiting on condition [0x00007f2e801f0000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000d715bcd8> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at java.util.concurrent.locks.ReentrantLock$NonfairSync.lock(ReentrantLock.java:209)
	at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
	at com.test.DeathLockTest.lambda$deathLock$1(DeathLockTest.java:30)
	at com.test.DeathLockTest$$Lambda$2/1044036744.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x00000000d715bd08> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)

"THE FIRST" #9 prio=5 os_prio=0 tid=0x00007f2e90169000 nid=0x15b2 waiting on condition [0x00007f2e802f1000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000d715bd08> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at java.util.concurrent.locks.ReentrantLock$NonfairSync.lock(ReentrantLock.java:209)
	at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
	at com.test.DeathLockTest.lambda$deathLock$0(DeathLockTest.java:19)
	at com.test.DeathLockTest$$Lambda$1/303563356.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x00000000d715bcd8> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)

"Service Thread" #8 daemon prio=9 os_prio=0 tid=0x00007f2e900c8800 nid=0x15b0 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C1 CompilerThread2" #7 daemon prio=9 os_prio=0 tid=0x00007f2e900bd000 nid=0x15af waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C2 CompilerThread1" #6 daemon prio=9 os_prio=0 tid=0x00007f2e900bb800 nid=0x15ae waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C2 CompilerThread0" #5 daemon prio=9 os_prio=0 tid=0x00007f2e900b8800 nid=0x15ad waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Signal Dispatcher" #4 daemon prio=9 os_prio=0 tid=0x00007f2e900b6800 nid=0x15ac runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Finalizer" #3 daemon prio=8 os_prio=0 tid=0x00007f2e90083800 nid=0x15ab in Object.wait() [0x00007f2e809f8000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000000d7108ed0> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
	- locked <0x00000000d7108ed0> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
	at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)

   Locked ownable synchronizers:
	- None

"Reference Handler" #2 daemon prio=10 os_prio=0 tid=0x00007f2e90081000 nid=0x15aa in Object.wait() [0x00007f2e80af9000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000000d7106bf8> (a java.lang.ref.Reference$Lock)
	at java.lang.Object.wait(Object.java:502)
	at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
	- locked <0x00000000d7106bf8> (a java.lang.ref.Reference$Lock)
	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)

   Locked ownable synchronizers:
	- None

"VM Thread" os_prio=0 tid=0x00007f2e90077800 nid=0x15a9 runnable 

"GC task thread#0 (ParallelGC)" os_prio=0 tid=0x00007f2e9001e800 nid=0x15a5 runnable 

"GC task thread#1 (ParallelGC)" os_prio=0 tid=0x00007f2e90020000 nid=0x15a6 runnable 

"GC task thread#2 (ParallelGC)" os_prio=0 tid=0x00007f2e90022000 nid=0x15a7 runnable 

"GC task thread#3 (ParallelGC)" os_prio=0 tid=0x00007f2e90024000 nid=0x15a8 runnable 

"VM Periodic Task Thread" os_prio=0 tid=0x00007f2e900cd000 nid=0x15b1 waiting on condition 

JNI global references: 310


Found one Java-level deadlock:
=============================
"THE SECOND":
  waiting for ownable synchronizer 0x00000000d715bcd8, (a java.util.concurrent.locks.ReentrantLock$NonfairSync),
  which is held by "THE FIRST"
"THE FIRST":
  waiting for ownable synchronizer 0x00000000d715bd08, (a java.util.concurrent.locks.ReentrantLock$NonfairSync),
  which is held by "THE SECOND"

Java stack information for the threads listed above:
===================================================
"THE SECOND":
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000d715bcd8> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at java.util.concurrent.locks.ReentrantLock$NonfairSync.lock(ReentrantLock.java:209)
	at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
	at com.test.DeathLockTest.lambda$deathLock$1(DeathLockTest.java:30)
	at com.test.DeathLockTest$$Lambda$2/1044036744.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:748)
"THE FIRST":
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000d715bd08> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at java.util.concurrent.locks.ReentrantLock$NonfairSync.lock(ReentrantLock.java:209)
	at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
	at com.test.DeathLockTest.lambda$deathLock$0(DeathLockTest.java:19)
	at com.test.DeathLockTest$$Lambda$1/303563356.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:748)

Found 1 deadlock.

```
#### 发现一处死锁,  
"THE SECOND"线程等待锁0x000000076b6aedc0,这个锁由"THE FIRST"线程持有,  
"THE FIRST"线程等待锁"0x000000076b6aedf0",这个锁由"THE SECOND"线程持有
```
Found one Java-level deadlock:
=============================
"THE SECOND":
  waiting for ownable synchronizer 0x00000000d715bcd8, (a java.util.concurrent.locks.ReentrantLock$NonfairSync),
  which is held by "THE FIRST"
"THE FIRST":
  waiting for ownable synchronizer 0x00000000d715bd08, (a java.util.concurrent.locks.ReentrantLock$NonfairSync),
  which is held by "THE SECOND"

Java stack information for the threads listed above:
===================================================
```
