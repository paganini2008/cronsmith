package com.github.cronsmith.scheduler;

import java.util.Map;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: AnnotatedTask
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class AnnotatedTask implements ITask {

    public AnnotatedTask(Map<String, Object> info) {
        this.info = info;
    }

    private final Map<String, Object> info;

    @Override
    public CronExpression getCronExpression() {
        byte[] bytes = (byte[]) info.get("cronExpression");
        return null;
    }

    @Override
    public Object execute(String initialParameter) {
        // TODO Auto-generated method stub
        return null;
    }


}
