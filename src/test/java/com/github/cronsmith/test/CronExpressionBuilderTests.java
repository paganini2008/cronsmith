package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: CronExpressionBuilderTests
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CronExpressionBuilderTests {

    @Test
    public void testA() {
        CronExpression cronExpression = new CronBuilder().everySecond(5);
        System.out.println(cronExpression.toString());
        assertEquals("*/5 * * * * ?", cronExpression.toString());
    }

    @Test
    public void testB() {
        CronExpression cronExpression = new CronBuilder().everyMinute(5).second(5).andSecond(10)
                .toSecond(30).andSecond(32).toSecond(59, 2);
        System.out.println(cronExpression.toString());
        assertEquals("5,10-30,32/2 */5 * * * ?", cronExpression.toString());
    }

    @Test
    public void testC() {
        CronExpression cronExpression = new CronBuilder().everyMonth().day(10).andDay(15).andDay(16)
                .andLastDay().everyHour(2).everyMinute(5);
        System.out.println(cronExpression.toString());
        assertEquals("0 */5 */2 10,15,16,L * ?", cronExpression.toString());
    }

    @Test
    public void testD() {
        CronExpression cronExpression = new CronBuilder().everyMonth(3).day(10).andLastWeekday()
                .hour(12).minute(1).toMinute(15, 1);
        System.out.println(cronExpression.toString());
        assertEquals("0 1-15 12 10,LW */3 ?", cronExpression.toString());
    }

    @Test
    public void testE() {
        CronExpression cronExpression =
                new CronBuilder().everyMonth().everyWeek().Mon().toFri().at(15, 10);
        System.out.println(cronExpression.toString());
        assertEquals("0 10 15 ? * MON-FRI", cronExpression.toString());
    }

    @Test
    public void testF() {
        CronExpression cronExpression = new CronBuilder().everyMonth().dayOfWeek(3, 6).everyHour(2);
        System.out.println(cronExpression.toString());
        assertTrue(cronExpression.toString().equals("0 0 */2 ? * SAT#3"));
    }

    @Test
    public void testG() {
        CronExpression cronExpression =
                new CronBuilder().year(2025).Mar().toSept().everyWeek().everyWeekday().at(9, 10);
        System.out.println(cronExpression.toString());
        assertTrue(cronExpression.toString().equals("0 10 9 ? MAR-SEP MON-FRI 2025"));
    }

    @Test
    public void testH() {
        CronExpression cronExpression =
                new CronBuilder().year(2025).toYear(2028).everyMonth(2).lastDay().hour(12);
        System.out.println(cronExpression.toString());
        assertEquals("0 0 12 L */2 ? 2025-2028", cronExpression.toString());
    }

    @Test
    public void testI() {
        CronExpression cronExpression = new CronBuilder().everyYear().June().andJuly().andAug()
                .latestWeekday(15).hour(10).toHour(18, 2);
        System.out.println(cronExpression.toString());
        assertEquals("0 0 10-18/2 15W JUN,JUL,AUG ?", cronExpression.toString());
    }

    @Test
    public void testJ() {
        CronExpression cronExpression = new CronBuilder().everyYear(2).Mar().andApr().andMay()
                .toDec().everyWeek(2).Tues().toFri().hour(10).andHour(12).toHour(22).everyMinute()
                .second(10).andSecond(20).andSecond(30);
        System.out.println(cronExpression.toString());
        assertEquals("10,20,30 * 10,12-22 ? MAR,APR,MAY-DEC TUE-FRI 2025/2",
                cronExpression.toString());
    }

    @Test
    public void testK() {
        CronExpression cronExpression = new CronBuilder().year(2025).toYear(2030).andYear(2035)
                .toEnd(2).everyMonth(2, 2).dayOfWeek(2, DayOfWeek.TUESDAY)
                .and(3, DayOfWeek.WEDNESDAY).andLastFri().hour(2).andHour(3).andHour(4)
                .toHour(17, 2).minute(0).toMinute(12, 3).andMinute(15).toMinute(40, 2).andMinute(46)
                .andMinute(48).andMinute(50).everySecond(5);
        System.out.println(cronExpression.toString());
        assertEquals(
                "*/5 0-12/3,15-40/2,46,48,50 2,3,4-17/2 ? FEB-DEC/2 TUE#2,WED#3,5L 2025-2030,2035/2",
                cronExpression.toString());
    }

    @Test
    public void testL() {
        CronExpression cronExpression = new CronBuilder().everyYear(2025, 4).everyMonth(5, 1)
                .day(10).andDay(15).andDay(20).andLastDay(2).hour(10).toHour(15, 1).at(10, 0)
                .andSecond(15).andSecond(30).andSecond(45);
        System.out.println(cronExpression.toString());
        assertEquals("0,15,30,45 10 10-15 10,15,20,L-2 MAY-DEC ? 2025/4",
                cronExpression.toString());
    }

    @Test
    public void testM() {
        CronExpression cronExpression = new CronBuilder().year().andYear(2026).andYear(2030)
                .toEnd(2).Mar().andJuly().andSept().dayOfWeek(1, DayOfWeek.SATURDAY)
                .and(2, DayOfWeek.THURSDAY).andLast(DayOfWeek.FRIDAY).hour(0).toHour(12, 3)
                .everyMinute(10).second(0).andSecond(15).andSecond(30).andSecond(45).toSecond(59);
        System.out.println(cronExpression.toString());
        assertEquals("0,15,30,45/1 */10 0-12/3 ? MAR,JUL,SEP SAT#1,THU#2,5L 2025,2026,2030/2",
                cronExpression.toString());
    }

}
