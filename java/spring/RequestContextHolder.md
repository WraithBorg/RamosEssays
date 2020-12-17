# RequestContextHolder解析
`Author:zxu`  
#### 应用场景
在mvc场景下,希望在request请求内 实现类似ThreadLocal机制去存取数据,但是又不能使用ThreadLocal,因为httpRequest线程池会复用request线程    
会导致再一次request请求里会获取到上一次request内的TheadLocal变量,比如每次request请求操作,都会存一个ThreadLocal变量A,  
久而久之,所有request线程都会持有变量A,此时如果程序需要用到判断当前线程是否持有变量A的操作,都会失去意义  
spring 提供ThreadLocalTargetSource来实现ThreadLocal机制,但是和TheadLocal一样,存在相同的问题  
所以采用了RequestContextHolder来实现相关业务逻辑 ,当然也可以使用request.setAttribute来实现相关操作  
但是service,dao层严禁使用request对象,因为有可能倒是jdbc链接不释放或者影响GC   

#### 示例代码
```java
package com.vogue.circle.point.web.springmvc;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
public class SprRequestContextHolder {
    public static void set(String key, String value) {
        RequestContextHolder.currentRequestAttributes().setAttribute(
                key,
                value,
                RequestAttributes.SCOPE_REQUEST);
    }
    public static String get(String key) {
        return (String) RequestContextHolder.currentRequestAttributes()
                .getAttribute(key, RequestAttributes.SCOPE_REQUEST);
    }
}
//
class A{
    public void test(){
        SprRequestContextHolder.set(key, value);
    }
}
```

#### RequestContextHolder原理以及源码分析
RequestContextHolder内部也是由ThreadLocal实现,将RequestAttributes对象放入到ThreadLocal中   
但是每次请求处理完业务逻辑后,都会重置RequestContextHolder设置的RequestAttributes对象  
具体源码如下  
``` java
// FrameworkServlet.class,method:processRequest
protected final void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Throwable failureCause = null;
        LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
        LocaleContext localeContext = this.buildLocaleContext(request);
        RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = this.buildRequestAttributes(request, response, previousAttributes);
        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new FrameworkServlet.RequestBindingInterceptor());
        this.initContextHolders(request, localeContext, requestAttributes);

        try {
            this.doService(request, response);// doService会进入程序处理业务逻辑的方法内
        } catch (ServletException var17) {
            failureCause = var17;
            throw var17;
        } catch (IOException var18) {
            failureCause = var18;
            throw var18;
        } catch (Throwable var19) {
            failureCause = var19;
            throw new NestedServletException("Request processing failed", var19);
        } finally {// 在这里重置RequestContextHolder的RequestAttributes对象
            this.resetContextHolders(request, previousLocaleContext, previousAttributes);
            if (requestAttributes != null) {
                requestAttributes.requestCompleted();
            }

            if (this.logger.isDebugEnabled()) {
                if (failureCause != null) {
                    this.logger.debug("Could not complete request", (Throwable)failureCause);
                } else if (asyncManager.isConcurrentHandlingStarted()) {
                    this.logger.debug("Leaving response open for concurrent processing");
                } else {
                    this.logger.debug("Successfully completed request");
                }
            }

            this.publishRequestHandledEvent(request, startTime, (Throwable)failureCause);
        }

    }
```

