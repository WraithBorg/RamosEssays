# 分布式锁的实现
`Author:zxu`
> 利用redis实现全局锁,利用try-with-resource简化了加锁解锁的操作

## 代码简介
#### 全局锁应用
```java
@Controller
public class BillController {
    /**
     * 删除订单
     */
    @RequestMapping(name = "")
    public void deleteBill(String billId){
        try (SprLock lock = new SprLock(billId)){
            // billService.deleteBill(billId);
        }catch (Exception e){
            // return e.getMessage...
        }
    }
}
```
#### 全局锁代码
```java
/**
 * 简化全局锁 不支持分布式可重入锁
 */
public class SprLock implements AutoCloseable {
    private List<String> lockedBillIds = new ArrayList<>();
    public SprLock(String... billIds) {
        if (billIds.length == 0) throw new RuntimeException("加锁失败");
        this.lock(billIds);
    }
    private SprLock() {
    }
    private void lock(String... billIds) {
        if (billIds != null && billIds.length != 0) {
            for (String billId : billIds) {
                if (RedisUtils.put(billId, LoginUserBean.getUserName())) {
                    lockedBillIds.add(billId);
                } else {
                    throw new RuntimeException(RedisUtils.get(billId) + " 正在操作该订单,请稍后重试!");
                }
            }
        }
    }
    @Override
    public void close() {
        if (lockedBillIds != null && lockedBillIds.size() > 0) {
            for (String billId : lockedBillIds) {
                RedisUtils.remove(billId);
            }
        }
    }
}
```
#### 其他代码
```java
/**
 * 模拟redis工具类
 */
public class RedisUtils {
    public static boolean put(String billId, String s) {
    // TODO redis setnx
        return false;
    }
    public static void remove(String billId) {
    }
    public static String get(String billId) {
        return null;
    }
}

/**
 * 当前用户信息
 */
public class LoginUserBean {
    // 获取当前登陆人用户名
    public static String getUserName() {
        return null;
    }
}
```
