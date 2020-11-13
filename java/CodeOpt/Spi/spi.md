# SPI机制
> 探究log4j和logback加载机制
#### 测试代码
```java
// 新建 META-INF/services/com.spi.LoggerService文件，内容为com.spi.LogbackServiceImpl
public class SpiTest {
    public static void main(String[] args) {
        ServiceLoader<LoggerService> loaders = ServiceLoader.load(LoggerService.class);
        LoggerService d = loaders.iterator().next();
        d.error();
    }
}
```
#### 其他代码
```java
public interface LoggerService {
    void error();
}
public class Log4jServiceImpl implements LoggerService{
    @Override
    public void error() {
        System.out.println("Log4j日志,输出错误信息");
    }
}
public class LogbackServiceImpl implements LoggerService{
    @Override
    public void error() {
        System.out.println("LOGBACK日志,输出错误信息");
    }
}
```
