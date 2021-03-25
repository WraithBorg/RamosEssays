package com.zxu.scheduler;

import java.util.concurrent.TimeUnit;

public class TestTask {
    public static void main (String[] args) {
        SprBaseTaskInstance.of(SprTask4Month.class).doStart(null, TimeUnit.SECONDS,1);
        SprBaseTaskInstance.of(SprTask4Month.class).doStart(null, TimeUnit.SECONDS,1);
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
