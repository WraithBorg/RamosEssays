# JVM堆外内存泄漏故障排查示例
故障描述:服务收到告警,服务进程占用容器的物理内存（16G）超过了80%的阈值,并且还在不断上升

#### 查看当前jvm启动参数
`jinfo -flags {pid}`

#### 堆内存分析
导出堆内存快照,并用JVisualVM或MAT分析
`jmap -dump:live,format=b,file=xxx.hprof {pid}`  

#### 排查第三方框架是否存在内存泄漏
1.检查第三方框架是否会申请直接内存

#### 查看进程内存信息
`pmap -x 27857`  
> 没有有用信息 

#### 堆外内存跟踪NMT (NativeMemoryTracking)
NMT必须先通过VM启动参数中打开,但是打开NMT会带来5%-10%的性能损耗  
`-XX:NativeMemoryTracking=[off | summary | detail]`  
使用jcmd(jdk自带)工具来访问NMT的数据
`jcmd pid VM.native_memory detail scale=MB > temp.txt` 
> 没有有用信息  

#### 查看JVM内存实际配置
`jmap -heap {pid}`

#### 利用JVisualVM查看内存变化

#### 查看JVM类加载情况
配置vm参数 `-verbose:class 查看类加载情况 -verbose:gc 查看虚拟机中内存回收情况 -verbose:jni 查看本地方法调用的情况`  
打印类加载信息`jmap -clstats {pid}`

#### 结论
fastjson的SerializeConfig创建时默认会创建一个ASM代理类用来实现对目标对象的序列化,将SerializeConfig作为类的静态变量,问题解决
