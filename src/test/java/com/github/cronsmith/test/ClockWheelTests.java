package com.github.cronsmith.test;

import org.junit.Test;
import com.github.cronsmith.scheduler.ClockWheel;

/**
 * 
 * @Description: ClockTests
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class ClockWheelTests {

    @Test
    public void testTask() {
        ClockWheel clockWheel = new ClockWheel();
        clockWheel.schedule(new TestTask(), "Tomcat");
        clockWheel.start();

        try {
            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clockWheel.close();
    }

}
