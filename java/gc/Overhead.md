## GC overhead limit exceeded异常
全称:java.lang.OutOfMemoryError: GC overhead limit exceeded  
**业务场景:**
> 服务A将每天的增量数据发送给服务B,服务A偶尔会出现"java.lang.OutOfMemoryError: GC overhead limit exceeded "  
#### 问题排查
发现服务A给服务B发数据有两种方式,一种是定时发送,一种是前端页面点击按钮然后另起线程同步发送给服务B    
每次发送的数据量均比较多,当调用服务B接口等待时间过长时,服务A的线程会进入阻塞状态  
这时候如果发送数据的线程大量增加的话,会导致堆内存不够用,而且gc也无法回收当前活动线程内的对象  
当Java进程花费98%以上的时间执行GC,并且每次只有不到2%的堆被恢复,则JVM抛出"GC overhead limit exceeded "异常  

#### 解决办法
1.控制httpclient线程池数量
2.完善发送数据并发问题,当前数据未发送完毕,则不会发送下次数据
3.避免定时任务和手动发送数据操作时机重合
4.避免同一线程调用多次http接口,减少阻塞时间
