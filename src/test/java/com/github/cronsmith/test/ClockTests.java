package com.github.cronsmith.test;

import org.junit.Test;
import com.github.cronsmith.scheduler.Clock;

/**
 * 
 * @Description: ClockTests
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class ClockTests {

    @Test
    public void testTask() {
        Clock clock = new Clock();
        clock.getTaskManager().saveTask(new TestTask(), "Tomcat");
        clock.start();

        try {
            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clock.close();
    }

}
