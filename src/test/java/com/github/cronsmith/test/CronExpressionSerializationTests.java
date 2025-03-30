package com.github.cronsmith.test;

import java.io.IOException;
import java.time.LocalDateTime;
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
}
