````java
/**
 * 添加响应中断的定时任务
 */
public class TestTimer {
    public static void main (String[] args) throws InterruptedException {
        SprTaskC.builder(1, TimeUnit.SECONDS, 1).doStart(null);// 启动定时任务C
        SprTaskD.builder(2, TimeUnit.SECONDS, 1).doStart(null); // 启动定时任务D
        Thread.sleep(3000);
        SprTaskC.getInstance().doStop(); // 关闭定时任务C
        Thread.sleep(3000);
        SprBaseTimerExecutors.exit(); // 关闭线程池
    }
}
/**
 * 定时任务调度器
 */
public class SprBaseTimerExecutors {
    private static final Logger LOGGER = LoggerFactory.getLogger(SprBaseTimerExecutors.class);
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static ConcurrentHashMap<String, ScheduledFuture<?>> conMap = new ConcurrentHashMap<>();
    public static synchronized void startCron (SprBaseTimerTask task) {
        if (conMap.containsKey(task.getTaskName())) {
            return;
        }
        LOGGER.info("添加定时任务{}",task.getTaskName());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod(), task.getTimeUnit());
        conMap.put(task.getTaskName(), future);
    }
    public static synchronized void closeCron (SprBaseTimerTask task) {
        ScheduledFuture<?> future = conMap.get(task.getTaskName());
        if (future != null) {
            future.cancel(true);
            conMap.remove(task.getTaskName());
        }
    }
    /**
     * 退出tomcat容器时 需要执行exit方法,关闭线程池
     */
    public static void exit(){
        scheduler.shutdown();
    }
}
/**
 * 定时任务模版
 */
public abstract class SprBaseTimerTask implements Runnable {
    private String taskName;// 任务名,要求唯一
    private int interrupt = 0;// 中断标识:0不中断,1中断,在业务方法内使用,方便即时停止线程
    private int period = 0;// 定时时长
    private int delay = 0;// 初次执行延时时间
    private TimeUnit timeUnit;// 定时单位
    private Map<String, Object> paramMap;// 定时任务传参
    @Override
    public void run () {
        runTask();
    }
    abstract protected void runTask ();
    public void doStart (Map<String, Object> pm) {
        this.paramMap = pm;
        this.interrupt = 0;
        SprBaseTimerExecutors.startCron(this);
    }
    public void doStop () {
        this.interrupt = 1;
        SprBaseTimerExecutors.closeCron(this);
    }
    public String getTaskName () {
        return taskName;
    }
    public Map<String, Object> getParamMap () {
        return paramMap;
    }
    public void setParamMap (Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }
    public int getPeriod () {
        return period;
    }
    public void setPeriod (int period) {
        this.period = period;
    }
    public void setTaskName (String taskName) {
        this.taskName = taskName;
    }
    public TimeUnit getTimeUnit () {
        return timeUnit;
    }
    public void setTimeUnit (TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    public int getDelay () {
        return delay;
    }
    public void setDelay (int delay) {
        this.delay = delay;
    }
    public int getInterrupt () {
        return interrupt;
    }
    public void setInterrupt (int interrupt) {
        this.interrupt = interrupt;
    }
}
/**
 * 任务C
 */
public class SprTaskC extends SprBaseTimerTask {
    /**
     * 执行业务方法
     */
    @Override
    protected void runTask () {
        System.out.println(getTaskName());
    }
    /************************************* Constructor *************************************/
    private static class Holder {
        public static SprTaskC task = new SprTaskC();
    }
    public static SprTaskC getInstance () {
        return Holder.task;
    }
    public static SprTaskC builder (int period, TimeUnit timeUnit, int delay) {
        Holder.task.setTaskName("任务A");
        Holder.task.setPeriod(period);
        Holder.task.setDelay(delay);
        Holder.task.setTimeUnit(timeUnit);
        return Holder.task;
    }
}
/**
 * 任务D
 */
public class SprTaskD extends SprBaseTimerTask {
    /**
     * 执行业务方法
     */
    @Override
    protected void runTask () {
        System.out.println(getTaskName());
    }
    /************************************* Constructor *************************************/
    private static class Holder {
        private static SprTaskD task = new SprTaskD();
    }
    public static SprTaskD getInstance () {
        return Holder.task;
    }
    public static SprTaskD builder (int period, TimeUnit timeUnit, int delay) {
        Holder.task.setPeriod(period);
        Holder.task.setDelay(delay);
        Holder.task.setTaskName("任务B");
        Holder.task.setTimeUnit(timeUnit);
        return Holder.task;
    }
}
````
