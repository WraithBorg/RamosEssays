package com.zxu.scheduler;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SprTask4Month extends SprBaseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(SprTask4Month.class);

    /**
     * 需要定时执行的业务
     */
    @Override
    public void executeTask () {
        LOGGER.info("月度任务开启 p=" + JSON.toJSONString(paramsMap) + " t=" + sleepTime);

    }
}
