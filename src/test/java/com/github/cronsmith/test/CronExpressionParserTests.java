package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: CronExpressionParserTests
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CronExpressionParserTests {

    @Test
    public void testA() {
        String cron = "0 0 12 * * ?";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testB() {
        String cron = "0 15 10 ? * MON-FRI";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testC() {
        String cron = "0 10,20,30 9-17 L * ?";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testD() {
        String cron = "1,3,5,7,9 3-30/3 12-16 ? * TUE#1";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testE() {
        String cron = "0 0 6 ? 1-5 MON,WED,FRI";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testF() {
        String cron = "*/5 1 12,15,18-22 LW * ? 2025";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testG() {
        String cron = "0 1 0-12,15,16-22/2 ? * 1-5 2025-2028";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testH() {
        String cron = "0 1,5,7,13,29,45 12/2 5,10,27W,L-1 APR-NOV ?";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testI() {
        String cron = "5/1 */5 12 ? 1-9 3#1,5#2,6L";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testJ() {
        String cron = "*/5 1,3,5-59 12-16 1,3,20,LW MAR-SEP ? 2026/1";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

    @Test
    public void testK() {
        String cron = "5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JUL MON-THU/2 2025-2033";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
    }

}
