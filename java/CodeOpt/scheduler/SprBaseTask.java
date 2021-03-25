package com.zxu.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class SprBaseTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SprBaseTask.class);
    int exit = 1; // 启停标记:0启动,1停止
    public Map<String, Object> paramsMap;// 参数列表
    public int sleepTime;// 睡眠时间
    private TimeUnit timeUnit;// 时间单位

    /**
     * 执行定时任务
     */
    @Override
    public void run () {
        while (true) {
            if (exit == 1) {
                LOGGER.info(this.getClass().getName() + "定时任务已关闭");
                return;
            }
            executeTask();
            sleepBySeconds(sleepTime);
        }
    }

    /**
     * 执行内单任务内容
     */
    public abstract void executeTask ();

    /**
     * 查询定时任务状态
     */
    public boolean doStatus () {
        return exit == 0;
    }

    /**
     * 关闭定时任务
     */
    public void doStop () {
        exit = 1;
    }

    /**
     * 开启定时任务
     */
    public void doStart (Map<String, Object> map, TimeUnit unit, int time) {
        synchronized (this) {
            if (exit == 0) {
                return;
            }
        }
        exit = 0;
        paramsMap = map;
        sleepTime = time;
        timeUnit = unit;
        SprBaseTaskExecutors.getInstance().start(this);
    }

    /**
     * 睡眠
     */
    void sleepBySeconds (int time) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
