# JAVA常见BUG

## static静态变量依赖spring实例化变量，可能导致初始化出错
```
// 错误示例
private static UserService userService = SpringContextUtils.getBean(UserService.class);
```
userService有可能获取不到的，因为类加载顺序不是确定的，正确的写法如下：
```
private static UserService  userService =null;
 
 //使用到的时候采取获取
 public static UserService getUserService(){
   if(userService==null){
      userService = SpringContextUtils.getBean(UserService.class);
   }
   return userService;
 }
```

## 使用ThreadLocal，线程重用导致信息错乱
程序运行在 Tomcat 中，执行程序的线程是 Tomcat 的工作线程，而 Tomcat 的工作线程是基于线程池的.  
一旦线程重用，那么很可能首次从 ThreadLocal 获取的值是之前其他用户的请求遗留的值

## 生产环境不要使用e.printStackTrace()
因为它占用太多内存，造成锁死，并且，日志交错混合，也不易读 

## 使用Executors声明线程池，newFixedThreadPool的OOM问题
