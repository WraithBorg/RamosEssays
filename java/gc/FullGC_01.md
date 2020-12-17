# Full GC 示例
#### 故障描述
大量的 Full GC 的告警

#### 堆内存快照
生成堆内存快照
`jmap -dump:live,format=b,file=xxx.hprof {pid}`  
并使用visualVM分析，visualVM中安装visual GC 插件

#### 场景再现
定位到httpasyncclient ，
业务场景是，将千条数据放入缓存列表并循环发出去   
使用visualVM观察，发现FutureCallback不断被创建，不断有对象进入老年代，  
说明在http回调结束后，FutureCallBack对象没有被即时回收   
查看源码发现httpasyncclient 部实现是将回调类塞入到了http的请求类中，而请求类是放在在缓存队列中，
所以导致回调类的引用关系没有解除，大量的回调类晋升到了old区，最终导致 Full GC 产生

