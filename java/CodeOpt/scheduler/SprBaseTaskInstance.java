package com.zxu.scheduler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务单例控制器
 */
public class SprBaseTaskInstance {
    private final static ConcurrentHashMap<String, Object> conMap = new ConcurrentHashMap<>();

    public static <T> T of (Class<T> clazz) {
        synchronized (conMap) {
            try {
                if (!conMap.containsKey(clazz.getName())) {
                    conMap.put(clazz.getName(), clazz.newInstance());
                }
                return (T) conMap.get(clazz.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return (T) new Object();
            }
        }
    }
}
