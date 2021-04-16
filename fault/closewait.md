# 服务器无响应
`Author:zxu`

> nginx返回499  
> 服务器不响应客户端请求,服务器本地访问tomcat端口不通,  
> CPU和内存利用率很低  
> 生产环境是多台服务器部署,然后通过nginx代理的集群模式  

#### 排查
top命令查看进程资源占用情况,发现cpu利用率和内存消耗都很低  
查看GC log,发现GC也没有问题（GC log）
#### 首先定位问题出现在Tomcat上

#### GC log配置参数
-XX:+PrintGCDetails -Xloggc:C:\home\gc.log -XX:+PrintGCTimeStamps

#### jstack
Java堆栈跟踪工具,可以打印出给定的java进程ID、core file、远程调试服务的Java堆栈信息
jstack -l,打印关于锁的附加信息


#### jps命令
`jps`查看java进程,`jsp -l`查看java进程并输出应用路径

#### linux 运行java文件
