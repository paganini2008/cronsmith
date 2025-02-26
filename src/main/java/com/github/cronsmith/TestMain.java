package com.github.cronsmith;

import java.time.format.DateTimeFormatter;
import com.github.cronsmith.cron.CronExpression;

public class TestMain {

    public static void main(String[] args) {
        String cron = "0 1,3,5-20 12-16 1,3,20,LW MAR-SEP ? 2026/1";
        CronExpression cronExpression = CRON.parse(cron);
        int N = 1000; // Retrieve 1000 items
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        cronExpression.consume(ldt -> {
            System.out.println(ldt.format(dtf));
        }, N);


    }
    // 2025-09-29 12:00:00
    // 2025-09-30 12:00:00
    // 2025-10-01 12:00:00
    // 2025-10-02 12:00:00
    // 2025-10-03 12:00:00
    // 2025-11-03 12:00:00
    // 2025-11-04 12:00:00
    // 2025-11-05 12:00:00
    // 2025-11-06 12:00:00
    // 2025-11-07 12:00:00
    // 2026-09-28 12:00:00
    // 2026-09-29 12:00:00
    // 2026-09-30 12:00:00
    // 2026-10-01 12:00:00
    // 2026-10-02 12:00:00

}
