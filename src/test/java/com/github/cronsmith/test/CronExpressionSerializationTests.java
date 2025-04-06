package com.github.cronsmith.test;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: CronExpressionSerializationTests
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class CronExpressionSerializationTests {

    private static String filePath;

    @Test
    public void testSerialization() throws IOException {
        CronExpression cronExpression = new CronBuilder().everyMinute(2);
        System.out.println(cronExpression.toString());
        LocalDateTime nextFiredTime = cronExpression.getNextFiredDateTime();
        System.out.println("NextFiredTime: " + nextFiredTime);
        filePath = CRON.saveAsTmpFile(cronExpression);
        System.out.println(filePath);
    }

    @Test
    public void testDeserialization() throws IOException {
        CronExpression cronExpression = CRON.loadFromFile(filePath);
        System.out.println(cronExpression.toString());
        LocalDateTime nextFiredTime = cronExpression.getNextFiredDateTime();
        System.out.println("NextFiredTime: " + nextFiredTime);
    }

    @Test
    public void testCopy1() {
        CronExpression cronExpression = new CronBuilder().everySecond(3);
        List<LocalDateTime> list =
                cronExpression.sync().list(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
        list.forEach(l -> {
            System.out.println("Copy1: " + l);
        });
    }

    @Test
    public void testCopy2() {
        CronExpression cronExpression = new CronBuilder().everyMinute(1);
        List<LocalDateTime> list = new ArrayList<>();
        System.out.println("=========================================");
        cronExpression.sync().consume(l -> {
            System.out.println("Copy2: " + l);
            list.add(l);
        }, 10);
        System.out.println("=========================================");
        LocalDateTime ldt = cronExpression.getNextFiredDateTime();
        System.out.println(ldt);
        assertTrue(list.get(0).equals(ldt));
    }
}
