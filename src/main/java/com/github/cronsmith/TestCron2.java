package com.github.cronsmith;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;

public class TestCron2 {

    public static void main(String[] args) {
        CronExpression cronExpression =
                CronExpressionUtils.everyYear().month(3).lastWeek().everyDay();
        System.out.println(cronExpression);
        cronExpression.forEach(l -> {
            System.out.println(l);
        }, 10);
    }

}
