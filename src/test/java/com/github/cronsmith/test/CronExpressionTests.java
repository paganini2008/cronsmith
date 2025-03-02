package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import java.time.format.DateTimeFormatter;
import org.junit.Test;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;

/**
 * 
 * @Description: CronExpressionTests
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class CronExpressionTests {

    @Test
    public void test1() {
        CronExpression cronExpression = CronExpressionUtils.everySecond(5, 30, 7);
        System.out.println(cronExpression);
        cronExpression.forEach(ldt -> {
            // System.out.println(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }, 100);
        assertEquals(true, true);
    }

    @Test
    public void test2() {
        CronExpression cronExpression =
                CronExpressionUtils.everyDay(1, 10, 2).at(12, 8).everySecond(0, 30, 6);
        System.out.println(cronExpression);
        cronExpression.forEach(ldt -> {
            // System.out.println(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }, 100);
        assertEquals(true, true);
    }

    @Test
    public void test3() {
        CronExpression cronExpression = CronExpressionUtils.everyMonth(2).everyDay(5).hour(1)
                .andHour(10).andHour(18).everyMinute(10).second(5).andSecond(10).andSecond(15);
        System.out.println(cronExpression);
        cronExpression.forEach(ldt -> {
            System.out.println(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }, 100);
        assertEquals(true, true);
    }

}
