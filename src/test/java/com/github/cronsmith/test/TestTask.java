package com.github.cronsmith.test;

import java.util.Date;
import java.util.UUID;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.scheduler.Task;

public class TestTask implements Task {

    @Override
    public CronExpression getCronExpression() {
        return new CronBuilder().everySecond(3);
    }

    @Override
    public Object execute(String parameter) {
        return parameter + ": " + UUID.randomUUID().toString();
    }

    @Override
    public void handleResult(Object result, Throwable reason) {
        System.out.println(String.format("%s, %s", new Date(), result));
        if (reason != null) {
            reason.printStackTrace();
        }
    }



}
