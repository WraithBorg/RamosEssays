package com.zxu.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务执行器
 */
public class SprBaseTaskExecutors<T extends SprBaseTask> {
    // 当前任务执行状态
    private ConcurrentHashMap<String, Boolean> conMap = new ConcurrentHashMap<>();

    // 开启任务
    public void start (T task) {
        Class<? extends SprBaseTask> aClass = task.getClass();
        String taskKey = aClass.getName();
        synchronized (taskKey) {//启停定时任务不存在高并发,所有可以加锁
            if (conMap.containsKey(taskKey) && conMap.get(taskKey)) return;
            conMap.put(taskKey, true);
        }
        CompletableFuture.runAsync((task)).whenCompleteAsync((x, y) -> conMap.put(taskKey, false));
    }

    /************************************* Constructor *************************************/
    private SprBaseTaskExecutors () {
    }

    private static class Holder {
        private static SprBaseTaskExecutors instance = new SprBaseTaskExecutors();
    }

    public static SprBaseTaskExecutors getInstance () {
        return SprBaseTaskExecutors.Holder.instance;
    }
}
