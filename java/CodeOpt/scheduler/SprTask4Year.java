package com.zxu.scheduler;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SprTask4Year extends SprBaseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(SprTask4Year.class);

    @Override
    public void executeTask () {
        LOGGER.info("年度任务开启 p=" + JSON.toJSONString(paramsMap) + " t=" + sleepTime);
    }

}
