# Cronsmith

[![Java](https://img.shields.io/badge/Java-Compatible-orange.svg)](https://www.java.com)
[![Cron](https://img.shields.io/badge/Cron-Supported-blue.svg)](https://en.wikipedia.org/wiki/Cron)
[![Spring Quartz](https://img.shields.io/badge/Spring%20Quartz-Compatible-brightgreen.svg)](https://spring.io/projects/spring-quartz)



**Cronsmith** is a powerful and versatile Java utility library designed for handling cron expressions in an intuitive, object-oriented manner. It offers a highly flexible and user-friendly API for generating, parsing, and scheduling cron-based tasks with ease.

Built for seamless integration, Cronsmith provides full support for both Spring and Quartz cron expressions, ensuring compatibility with widely used scheduling frameworks. Moreover, it extends traditional cron syntax by introducing advanced patterns—such as multiple numbers combined with 'L' (last) and 'W' (weekday)—which were previously unsupported, offering greater precision and flexibility in scheduling.


## Features

### 1. Object-Oriented <code>CronExpression</code> Builder

**Cronsmith** allows developers to construct complex cron expressions in an intuitive, object-oriented manner. This approach enables easy customization and avoids the need for manual string construction.

#### Example:

```java
 
@Test
public void test1() {
        CronExpression cronExpression = new CronBuilder()
            .everyMinute(5)
            .second(5)
            .andSecond(10)
            .toSecond(30);
        
        System.out.println(cronExpression.toString());
        assertEquals("5,10-30 */5 * * * ?", cronExpression.toString());
    }

@Test
public void test2() {
        CronExpression cronExpression = new CronBuilder()
            .everyMonth()
            .lastWeekday()
            .hour(10)
            .minute(1)
            .toMinute(15);
        
        System.out.println(cronExpression.toString());
        assertEquals("0 1-15 10 LW * ?", cronExpression.toString());
    }

@Test
public void test3() {
        CronExpression cronExpression = new CronBuilder()
            .year(2025)
            .toYear(2028)
            .everyMonth(2)
            .lastDay()
            .hour(12);
        
        System.out.println(cronExpression.toString());
        assertEquals("0 0 12 L */2 ? 2025-2028", cronExpression.toString());
}

@Test
public void test4() {
    CronExpression cronExpression = new CronBuilder()
        .everyYear(2026, 4)
        .everyMonth(5, 7, 1)
        .day(10).andDay(15).andDay(20).andLastDay(2)
        .everyHour(10, 15, 1)
        .at(10, 0)
        .andSecond(15).andSecond(30).andSecond(45);
    
    System.out.println(cronExpression.toString());
    assertEquals("0,15,30,45 10 10-15 10,15,20,L-2 MAY-JUL ? 2026/4",
            cronExpression.toString());
}

@Test
public void testK() {
        CronExpression cronExpression = new CronBuilder()
                .year(2025).toYear(2030).andYear(2035).toEnd(2)
                .everyMonth(2, 12, 2)
                .dayOfWeek(2, DayOfWeek.TUESDAY)
                .and(3, DayOfWeek.WEDNESDAY).andLastFri()
                .hour(2).andHour(3).andHour(4).toHour(17, 2)
                .minute(0).toMinute(12, 3).andMinute(15).toMinute(40, 2).andMinute(46)
                .andMinute(48).andMinute(50)
                .everySecond(5);
    
        System.out.println(cronExpression.toString());
        assertEquals("*/5 0-12/3,15-40/2,46,48,50 2,3,4-17/2 ? 2/2 TUE#2,WED#3,5L 2025-2030,2035/2",
                cronExpression.toString());
 }
```

### 2. Cron Expression String Parsing and Reverse Engineering to <code>CronExpression</code>

**Cronsmith** includes a powerful parser built with ANTLR, allowing you to parse an existing cron expression string and convert it back into a structured `CronExpression` object.

#### Example:

```java
@Test
public void test1() {
        String cron = "0 15 10 ? * MON-FRI";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
}

@Test
public void test2() {
        String cron = "0 10,20,30 9-17 L * ?";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
}

@Test
public void test3() {
        String cron = "1,3,5,7,9 3-30/3 12-16 ? * TUE#1";
        CronExpression cronExpression = CRON.parse(cron);
        System.out.println(cronExpression);
        assertEquals(cron, cronExpression.toString());
}

@Test
public void test4() {
    String cron = "5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JUL MON-THU/2 2025-2033";
    CronExpression cronExpression = CRON.parse(cron);
    System.out.println(cronExpression);
    assertEquals(cron, cronExpression.toString());
}
```

Parse Tree: 

``` shell
cron
    second
        secondField
            rangeWithStep
                5
                -
                30
                /
                7
    <missing SPACE>
    minute
        minuteField
            rangeWithStep
                0
                -
                12
                /
                3
        ,
        minuteField
            rangeWithStep
                15
                -
                45
                /
                2
    <missing SPACE>
    hour
        hourField
            2
        ,
        hourField
            3
        ,
        hourField
            rangeWithStep
                4
                -
                17
                /
                2
    <missing SPACE>
    dayOfMonth
        dayOfMonthField
            ?
    <missing SPACE>
    month
        monthField
            monthRange
                monthName
                    JAN
                -
                monthName
                    JUL
    <missing SPACE>
    dayOfWeek
        dayOfWeekField
            weekdayRangeWithStep
                dayOfWeekName
                    MON
                -
                dayOfWeekName
                    THU
                /
                2
    year
        yearField
            yearRange
                2025
                -
                2030
    <EOF>
```

Retrieve next date and times from  <code>CronExpression</code>

```java
public static void main(String[] args) {
   String cron = "0 1,3,5-20 12-16 1,3,20,LW MAR-SEP ? 2026/1";
   CronExpression cronExpression = CRON.parse(cron);
   int N = 1000; // Retrieve 1000 items
   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   cronExpression.consume(ldt -> {
      System.out.println(ldt.format(dtf));
   }, N);
}

// Console:
// 2026-03-01 12:01:00
// 2026-03-01 12:03:00
// 2026-03-01 12:05:00
// 2026-03-01 12:06:00
// 2026-03-01 12:07:00
// 2026-03-01 12:08:00
// 2026-03-01 12:09:00
// 2026-03-01 12:10:00
// 2026-03-01 12:11:00
// 2026-03-01 12:12:00
// 2026-03-01 12:13:00
// 2026-03-01 12:14:00
// 2026-03-01 12:15:00
// 2026-03-01 12:16:00
// 2026-03-01 12:17:00
// 2026-03-01 12:18:00
// 2026-03-01 12:19:00
// 2026-03-01 12:20:00
// 2026-03-01 13:01:00
// 2026-03-01 13:03:00
// 2026-03-01 13:05:00
// ...

```



### 3. Built-in Scheduler

**Cronsmith** provides a built-in scheduler that allows you to execute tasks at specified intervals based on cron expressions. This feature makes it easy to schedule repetitive tasks efficiently.

#### Example: Scheduling a Task Every 5 Seconds

```java
private ScheduledExecutorService scheduledExecutorService;

@Before
public void start() {
    scheduledExecutorService =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
}

@Test
public void testSchedulerAndRunTenTimes() {
    int N = 10; // Run 10 times
    final CountDownLatch latch = new CountDownLatch(N);
    final AtomicInteger counter = new AtomicInteger();
    
    CronFuture future = new CronBuilder()
        .everySecond(5)
        .scheduler(scheduledExecutorService)
        .setDebuged(false)
        .runTask(() -> {
            System.out.println("Run task_" + counter.incrementAndGet());
            latch.countDown();
        }, N);
    
    try {
        latch.await();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    
    future.cancel(true);
    assertTrue(future.isDone() && counter.get() == N);
}

@After
public void release() {
    scheduledExecutorService.shutdown();
}
```

### 4. Advanced Part

**Can specify day of year**

``` java
public static void main(String[] args) {
   int N = 1000;
   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   new CronBuilder().setZoneId(ZoneId.of("UTC")).year(2025).day(208).andDay(330).toLastDay()
                .at(12, 0).consume(ldt -> {
                    System.out.println(ldt.format(dtf));
                }, N);
}
// Console: 
// 2025-07-27 12:00:00
// 2025-11-26 12:00:00
// 2025-11-27 12:00:00
// 2025-11-28 12:00:00
// 2025-11-29 12:00:00
// 2025-11-30 12:00:00
// 2025-12-01 12:00:00
// 2025-12-02 12:00:00
// 2025-12-03 12:00:00
// 2025-12-04 12:00:00
// 2025-12-05 12:00:00
// 2025-12-06 12:00:00
// ...

```

**Can specify week of year**

``` java
public static void main(String[] args) {
    int N = 1000;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    new CronBuilder().setZoneId(ZoneId.of("UTC")).everyYear().week(40).andWeek(45).Mon().toFri()
                .at(12, 0).consume(ldt -> {
                    System.out.println(ldt.format(dtf));
                }, N);
}
// Console: 
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
// ...
```



## Installation

Support Jdk1.8 or later

To use **Cronsmith** in your Java project, add the following dependency to your `pom.xml` (if using Maven):

```xml
<dependency>
    <groupId>com.github.paganini2008</groupId>
    <artifactId>cronsmith</artifactId>
    <version>1.0.0</version>
</dependency>
```

If using Gradle:

```gradle
dependencies {
    implementation 'com.github.paganini2008:cronsmith:1.0.0'
}
```

## Getting Started

1. Add Cronsmith as a dependency in your project.
2. Use `CronBuilder` to create complex cron expressions.
3. Parse existing cron expressions using `CRON.parse()`.
4. Schedule tasks using the built-in scheduler.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Download

[Click here to download the latest release](https://github.com/yourusername/cronsmith/releases)
