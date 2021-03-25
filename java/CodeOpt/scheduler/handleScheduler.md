# 随开随停的定时任务
#### 1.注册定时任务
```
// 1:只需要继承SprBaseTask 并实现executeTask()方法,写上需要定时执行的业务即可

public class SprTask4Month extends SprBaseTask {
    /**
     * 需要定时执行的业务
     */
    @Override
    public void executeTask () {
        LOGGER.info("月度任务开启 p=" + JSON.toJSONString(paramsMap) + " t=" + sleepTime);

    }
}
```
#### 2.使用定时器
```
@Controller
public class TaskController {
    /**
     * 开启定时任务
     */
    @GetMapping("/scheduler/month/start")
    public void scheduler4Month2Start () {
        SprBaseTaskInstance.of(SprTask4Month.class).doStart(null, TimeUnit.SECONDS, 1);
    }
    /**
     * 关闭定时任务
     */
    @GetMapping("/scheduler/month/stop")
    public void scheduler4Month2Stop () {
        SprBaseTaskInstance.of(SprTask4Month.class).doStop();
    }
    
    @GetMapping("/scheduler/year/start")
    public void scheduler4Year2Start () {
        SprBaseTaskInstance.of(SprTask4Year.class).doStart(null, TimeUnit.SECONDS, 1);
    }
    @GetMapping("/scheduler/year/stop")
    public void scheduler4Year2Stop () {
        SprBaseTaskInstance.of(SprTask4Year.class).doStop();
    }
}
```
