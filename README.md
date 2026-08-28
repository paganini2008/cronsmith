# Cronsmith - The Ultimate Cron Expression Generator & Parser

[![Java](https://img.shields.io/badge/Java-1.8%2B-orange.svg)](https://www.java.com)
[![Cron](https://img.shields.io/badge/Cron-Supported-blue.svg)](https://en.wikipedia.org/wiki/Cron)
[![Quartz](https://img.shields.io/badge/Quartz-Compatible-brightgreen.svg)](https://www.quartz-scheduler.org/)
[![Spring](https://img.shields.io/badge/Spring%20Scheduling-Compatible-brightgreen.svg)](https://spring.io/)
[![AWS](https://img.shields.io/badge/AWS%20EventBridge-Compatible-brightgreen.svg)](https://docs.aws.amazon.com/eventbridge/)
[![crontab](https://img.shields.io/badge/Unix%20crontab-Compatible-brightgreen.svg)](https://man7.org/linux/man-pages/man5/crontab.5.html)

### Are cron expressions sometimes difficult to understand? Let's try a different approach to creating scheduled tasks.

**Cronsmith** is a Java library for building, parsing and running cron schedules through a fluent,
object-oriented API instead of hand-written strings. You describe *when* something should happen in
plain method calls, and Cronsmith gives you back a cron expression, the list of date-times it fires
at, or a running task.

It speaks the cron dialects you already deploy against — **Quartz**, **Spring Scheduling**,
**AWS EventBridge** and **Unix crontab** — and it extends the classic syntax with multi-value forms
of `L` and `#` that none of them offer on their own.

```java
// "the last Friday of every month at 18:00"
CronExpression cron = new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);

cron.toString();               // 0 0 18 ? * FRIL
CRON.toAwsString(cron);        // 0 18 ? * FRIL *
cron.getNextFiredDateTime();   // the next last-Friday-of-the-month, 18:00
```

---

## Table of contents

- [Features](#features)
  - [1. Object-oriented builder](#1-object-oriented-cronexpression-builder)
  - [2. Parsing and reverse engineering](#2-parsing-and-reverse-engineering)
  - [3. Cross-scheduler output](#3-cross-scheduler-output)
  - [4. Built-in scheduler](#4-built-in-scheduler)
  - [5. Beyond standard cron](#5-beyond-standard-cron)
- [Stateful task scheduling](#stateful-task-scheduling)
- [Cron syntax reference](#cron-syntax-reference)
- [Best practices](#best-practices)
- [Worked examples](#worked-examples)
- [Installation](#installation)
- [License](#license)

---

## Features

### 1. Object-Oriented <code>CronExpression</code> Builder

Build complex schedules by describing them, not by assembling strings.

```java
new CronBuilder().everySecond(5);
// */5 * * * * ?

new CronBuilder().everyMinute(5).second(5).andSecond(10).toSecond(30).andSecond(32).toSecond(59, 2);
// 5,10-30,32/2 */5 * * * ?

new CronBuilder().everyMonth().day(10).andDay(15).andDay(16).andLastDay().everyHour(2).everyMinute(5);
// 0 */5 */2 10,15,16,L * ?

new CronBuilder().everyMonth(3).day(10).andLastWeekday().hour(12).minute(1).toMinute(15, 1);
// 0 1-15 12 10,LW */3 ?

new CronBuilder().everyMonth().everyWeek().Mon().toFri().at(15, 10);
// 0 10 15 ? * MON-FRI

new CronBuilder().everyMonth().dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2);
// 0 0 */2 ? * SAT#3

new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
// 0 0 18 ? * FRIL

new CronBuilder().everyMonth().lastDay(3).at(23, 30);
// 0 30 23 L-3 * ?

new CronBuilder().everyMonth().latestWeekday(15).at(9, 0);
// 0 0 9 15W * ?

new CronBuilder().year().Mar().toSept().everyWeek().everyWeekday().at(9, 10);
// 0 10 9 ? MAR-SEP MON-FRI <year>      (year() is the year the builder starts in)
```

Every expression is also an `Iterator`, so you can look at the schedule instead of trusting it:

```java
CronExpression cron = new CronBuilder()
        .setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .everyMonth().latestWeekday(15).at(9, 0);
cron.consume(System.out::println, 5);
// 2027-01-15T09:00
// 2027-02-15T09:00
// 2027-03-15T09:00
// 2027-04-15T09:00
// 2027-05-14T09:00   <- the 15th is a Saturday, so it moves to the nearest weekday
```

`consume(..)` walks a copy and leaves the original untouched. `getNextFiredDateTime()` advances the
expression itself and answers the first occurrence strictly after the reference time.

### 2. Parsing and Reverse Engineering

`CRON.parse(..)` turns a string back into a `CronExpression` you can inspect, iterate and re-render.

```java
CRON.parse("0 0 12 ? * FRIL");                    // 0 0 12 ? * FRIL
CRON.parse("0 0 12 ? * TUE#2");                   // 0 0 12 ? * TUE#2
CRON.parse("0 0 12 LW * ?");                      // 0 0 12 LW * ?
CRON.parse("0 15 10 ? * MON-FRI 2027-2030");      // 0 15 10 ? * MON-FRI 2027-2030
```

The field count decides how the string is read:

| Fields | Read as | Day-of-week numbering |
|---|---|---|
| 5 | Unix crontab (`min hour dom month dow`) | MON=1 … SAT=6, Sunday is 0 or 7 |
| 6 | Quartz without a year (`sec min hour dom month dow`) | SUN=1 … SAT=7 |
| 7 | Quartz with a year | SUN=1 … SAT=7 |

```java
CRON.parse("*/5 * * * *");        // 0 */5 * * * ?      a crontab line
CRON.parse("0 9 * * 1-5");        // 0 0 9 ? * MON-FRI  crontab: 1 is Monday
CRON.parse("0 0 12 ? * 1");       // 0 0 12 ? * SUN     Quartz:  1 is Sunday
```

### 3. Cross-Scheduler Output

One schedule, printed for whichever scheduler you are deploying to. Anything a target cannot express
is reported rather than silently rewritten into something that fires at different times.

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);

CRON.toQuartzString(daily);   // 0 30 9 * * ?
CRON.toSpringString(daily);   // 0 30 9 * * ?
CRON.toAwsString(daily);      // 30 9 * * ? *
CRON.toUnixString(daily);     // 30 9 * * *

// or explicitly
CRON.toCronString(daily, CronDialect.AWS);
```

| Schedule | Quartz | Spring | AWS EventBridge | Unix crontab |
|---|---|---|---|---|
| every day 09:30 | `0 30 9 * * ?` | `0 30 9 * * ?` | `30 9 * * ? *` | `30 9 * * *` |
| weekdays 09:00 | `0 0 9 ? * MON-FRI` | `0 0 9 ? * MON-FRI` | `0 9 ? * MON-FRI *` | `0 9 * * MON-FRI` |
| every 15 minutes | `0 */15 * * * ?` | `0 */15 * * * ?` | `*/15 * * * ? *` | `*/15 * * * *` |
| every 15 seconds | `*/15 * * * * ?` | `*/15 * * * * ?` | no seconds field | no seconds field |
| last day of month | `0 59 23 L * ?` | `0 59 23 L * ?` | `59 23 L * ? *` | no `L` |
| 2nd Tuesday | `0 0 10 ? * TUE#2` | `0 0 10 ? * TUE#2` | `0 10 ? * TUE#2 *` | no `#` |
| 3rd-from-last day | `0 0 0 L-3 * ?` | no `L-n` | no `L-n` | no `L` |
| restricted to 2027-2029 | `… 2027-2029` | no year field | `… 2027-2029` | no year field |

Day-of-week is always printed by name, because the numeric conventions disagree with one another —
Quartz and AWS count `SUN=1`, Spring and crontab count `MON=1` — while `MON` means Monday everywhere.

### 4. Built-in Scheduler

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

CronFuture future = new CronBuilder()
        .everySecond(5)
        .scheduler(executor)
        .setDebuged(false)
        .runTask(() -> System.out.println("tick"), 10);   // run ten times

future.cancel(true);
```

The scheduler exposes the whole task life cycle:

```java
CronScheduler scheduler = new CronBuilder().everyMinute(5).scheduler(executor);

scheduler.subscribe(new CronSchedulerListener() {
    @Override
    public void onTaskFinished(CronScheduledEvent event) {
        System.out.println("next run: " + event.getNextFiredDateTime());
    }

    @Override
    public void onTaskFailed(CronScheduledEvent event) {
        log.error("task failed", event.getReason());
    }
});

CronFuture future = scheduler.runTaskForEver(job);
scheduler.pauseTask(job);
scheduler.resumeTask(job);
scheduler.removeTask(job);
```

Other ways to bound a run:

```java
scheduler.runTask(job, 10);                                  // ten times
scheduler.runTask(job, LocalDateTime.now().plusHours(2));     // until a point in time
scheduler.runTask(job, (task, reason) -> reason != null);     // until it first fails
scheduler.runTaskForEver(job);
```

### 5. Beyond Standard Cron

Cronsmith accepts several things classic cron has no syntax for. These schedules iterate normally;
they simply have no cron string, and asking for one raises an `UnsupportedOperationException`
instead of returning something misleading.

**Day of the year**

```java
new CronBuilder().setZoneId(ZoneId.of("UTC"))
        .year(2027).day(208).andDay(330).toLastDay().at(12, 0)
        .consume(System.out::println, 6);
// 2027-07-27T12:00
// 2027-11-26T12:00
// 2027-11-27T12:00
// 2027-11-28T12:00
// 2027-11-29T12:00
// 2027-11-30T12:00
```

**Week of the year**

```java
new CronBuilder().setZoneId(ZoneId.of("UTC"))
        .everyYear().week(40).andWeek(45).Mon().toFri().at(12, 0)
        .consume(System.out::println, 12);
// 2027-10-04T12:00 .. 2027-10-08T12:00
// 2027-11-08T12:00 .. 2027-11-12T12:00
// 2028-10-02T12:00 ...
```

**ISO-8601 durations**

Turn a plain interval — an ISO-8601 duration or a `java.time.Duration` — into a schedule. Fires once
now, then every interval thereafter.

```java
CRON.setInterval("PT30S");                 // every 30 seconds
CRON.setInterval("PT5M");                  // every 5 minutes
CRON.setInterval("PT2H");                  // every 2 hours
CRON.setInterval("P1D");                   // every day
CRON.setInterval(Duration.ofHours(2));     // same as "PT2H"
```

A cron field steps within its own range, so the coarsest exact unit is chosen — `PT120M` becomes
every two hours, not 7200 seconds. An interval that is not an exact multiple of a single unit that
fits (`PT1H30M`, `PT90M`, `PT25H`, or anything below a second) is rejected rather than silently
rounded.

**Multi-value `L` and `#`**

```java
new CronBuilder().everyMonth()
        .dayOfWeek(2, DayOfWeek.TUESDAY).and(3, DayOfWeek.WEDNESDAY).andLastFri().at(9, 0);
// 0 0 9 ? * TUE#2,WED#3,FRIL

new CronBuilder().everyMonth().week(1).andLastWeek().Mon().at(8, 0);
// 0 0 8 ? * MON#1,MONL

new CronBuilder().everyMonth().day(10).andDay(15).andLatestWeekday(25).andLastDay().at(0, 0);
// 0 0 0 10,15,25W,L * ?
```

---

## Stateful task scheduling

The features above are about cron *expressions* — building, parsing and computing fire times. Cronsmith
also ships a lightweight in-process **`CronScheduler`** (see *Built-in Scheduler* above) that runs a
schedule on a timing wheel, entirely in memory.

**Persistent, distributed task scheduling** — a `TaskManager` that keeps task state across restarts
(in-memory, or a JDBC / jOOQ store on H2 · SQLite · MySQL · PostgreSQL), server-driven retries and
timeouts, and a cluster that dispatches runs to executors — now lives in the
**`cronsmith-spring-boot-starter`** (part of the
[cronflower](https://github.com/paganini2008/cronflower) monorepo). Keeping it there lets this library
stay a pure cron **parser / builder** toolkit with no database, HTTP or Spring dependency.

---

## Cron syntax reference

```
 ┌───────────── second        0-59
 │ ┌─────────── minute        0-59
 │ │ ┌───────── hour          0-23
 │ │ │ ┌─────── day-of-month  1-31, L, LW, L-n, nW, ?
 │ │ │ │ ┌───── month         1-12 or JAN-DEC
 │ │ │ │ │ ┌─── day-of-week   1-7 (SUN=1) or SUN-SAT, nL, n#m, ?
 │ │ │ │ │ │ ┌─ year          optional, 1970-2099
 │ │ │ │ │ │ │
 * * * * * ? *
```

| Tag | Field | Meaning |
|---|---|---|
| `*` | any | every value |
| `?` | day-of-month, day-of-week | no restriction; exactly one of the two day fields must carry it |
| `a-b` | any | a range |
| `a/n` | any | from `a`, every `n`th value — `*/15` in seconds fires at :00, :15, :30, :45 |
| `a,b,c` | any | a list; entries may themselves be ranges or steps |
| `L` | day-of-month | the last day of the month |
| `L-n` | day-of-month | `n` days before the last day |
| `LW` | day-of-month | the last weekday of the month |
| `nW` | day-of-month | the weekday nearest the `n`th, without leaving the month |
| `<dow>L` | day-of-week | the last `<dow>` of the month, e.g. `FRIL` |
| `<dow>#n` | day-of-week | the `n`th `<dow>` of the month, e.g. `TUE#2`; months without an `n`th are skipped |

`#` and `L` count **occurrences inside the month**, which is what Quartz, Spring and AWS all mean:
`FRI#1` is the first Friday that falls in the month, and `FRI#5` only fires in months that have five
Fridays.

---

## Best practices

**Pin the start time when the result has to be reproducible.** A builder starts from *now* in UTC,
so an expression carrying a year depends on when it was built. Tests and fixtures should say so:

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay()).year().toYear(2030);
```

**Set the zone the schedule is meant to be read in.** A cron expression is wall-clock time; the zone
decides which instant "09:00" is. The default is UTC.

```java
new CronBuilder().setZoneId(ZoneId.of("Europe/Berlin")).everyDay().at(9, 0);
```

Across a daylight-saving switch the wall clock stays put — 09:00 is always 09:00 — while the real
interval between two runs becomes 23 or 25 hours. The scheduler accounts for that, so a daily job
does not drift by an hour twice a year.

**Prefer weekday names to numbers.** `MON` is Monday in every scheduler; `1` is Sunday in Quartz and
AWS but Monday in Spring and crontab. Cronsmith prints names by default — leave it that way unless
you are matching an existing expression character for character.

**Ask the target dialect before you deploy.** `CRON.toUnixString(..)` failing is a much better
outcome than a crontab entry that silently drops the `L` you needed:

```java
try {
    deploy(CRON.toUnixString(cron));
} catch (UnsupportedOperationException e) {
    // this schedule needs a Quartz-class scheduler
}
```

**Store the expression, not the next fire time.** `CronExpression` is `Serializable`, so a schedule
survives a restart:

```java
byte[] snapshot = cron.serialize();
CronExpression restored = CronExpression.deserialize(snapshot);
```

Catching up from a snapshot taken long ago is O(1) per level rather than one step per elapsed second,
so restoring a per-second schedule from last year is still instantaneous.

**Use `consume(..)` to review a schedule before trusting it**, especially for `L`, `W` and `#`, where
month lengths and weekends change the answer:

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .everyMonth().dayOfWeek(5, DayOfWeek.FRIDAY).at(12, 0)
        .consume(System.out::println, 5);
// 2027-01-29T12:00
// 2027-04-30T12:00   <- February and March have no fifth Friday
// 2027-07-30T12:00
// 2027-10-29T12:00
// 2027-12-31T12:00
```

**Give the scheduler its own executor and shut it down.** Cronsmith schedules onto the
`ScheduledExecutorService` you hand it and never creates threads behind your back.

---

## Worked examples

**End-of-month billing, weekdays only**

```java
// 23:30 on the last weekday of every month
CronExpression billing = new CronBuilder().everyMonth().lastWeekday().at(23, 30);
// 0 30 23 LW * ?
```

**Payroll on the 15th and the last day, moved off weekends**

```java
CronExpression payroll = new CronBuilder().everyMonth()
        .latestWeekday(15).andLastWeekday().at(6, 0);
// 0 0 6 15W,LW * ?
```

**Weekly report every second Tuesday and the last Friday**

```java
CronExpression report = new CronBuilder().everyMonth()
        .dayOfWeek(2, DayOfWeek.TUESDAY).andLastFri().at(17, 0);
// 0 0 17 ? * TUE#2,FRIL
```

**Business hours health check, every 15 minutes**

```java
CronExpression healthCheck = new CronBuilder().everyMonth().everyWeek()
        .Mon().toFri().hour(9).toHour(18).everyMinute(15);
// 0 */15 9-18 ? * MON-FRI
```

**Quarterly job, first day of the quarter**

```java
CronExpression quarterly = new CronBuilder().everyMonth(3).day(1).at(2, 0);
// 0 0 2 1 */3 ?
```

**A campaign bounded by a date range**

```java
CronExpression campaign = new CronBuilder()
        .setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .year(2027).toYear(2029)
        .June().andJuly().andAug()
        .everyWeek().Sat().andSun().at(10, 0);
// 0 0 10 ? JUN,JUL,AUG SAT,SUN 2027-2029
```

**One-off run at a fixed moment**

```java
CronExpression once = CRON.atFuture(LocalDateTime.of(2027, 12, 1, 12, 15, 0));
// 0 15 12 1 DEC ? 2027
```

**Fixed interval without writing cron at all**

```java
CRON.setInterval(5, TimeUnit.MINUTES);          // 0 */5 * * * ?
CRON.setInterval(LocalTime.of(23, 45, 30));     // 30 45 23 * * ?
```

**Migrating an existing crontab line**

```java
CronExpression cron = CRON.parse("15 10 * * MON-FRI");   // 0 15 10 ? * MON-FRI
CRON.toQuartzString(cron);                               // 0 15 10 ? * MON-FRI
CRON.toAwsString(cron);                                  // 15 10 ? * MON-FRI *
```

---

## Installation

Requires JDK 17 or later.

```xml
<dependency>
    <groupId>com.github.paganini2008</groupId>
    <artifactId>cronsmith</artifactId>
    <version>1.0.0-RC2</version>
</dependency>
```

```gradle
dependencies {
    implementation 'com.github.paganini2008:cronsmith:1.0.0-RC2'
}
```

The core parser has no runtime dependencies beyond ANTLR. The
[stateful task scheduler](#stateful-task-scheduling) uses a few libraries that are declared
`optional`, so you only pull in what you actually use:

| You want to…                                   | Add                                                         |
| ---------------------------------------------- | ---------------------------------------------------------- |
| run tasks in memory                            | nothing — it works out of the box                          |
| persist tasks in a database                    | `org.jooq:jooq` **and** your JDBC driver (H2, SQLite, PostgreSQL, MySQL, …) |
| use `SimpleTask` to call HTTP endpoints        | `com.squareup.okhttp3:okhttp` and `com.fasterxml.jackson.core:jackson-databind` |

```xml
<!-- example: persistence on PostgreSQL -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq</artifactId>
    <version>3.19.15</version>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>
```

## Getting Started

1. Add Cronsmith as a dependency in your project.
2. Use `CronBuilder` to describe the schedule.
3. Print it for your scheduler with `CRON.toQuartzString(..)`, `toSpringString(..)`,
   `toAwsString(..)` or `toUnixString(..)`, or run it with the built-in scheduler.
4. Read existing expressions — six/seven-field Quartz or five-field crontab — with `CRON.parse(..)`.
5. Need to actually run, track and persist jobs? Jump to
   [stateful task scheduling](#stateful-task-scheduling) for a three-line quick start.

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.

## Download

[Click here to download the latest release](https://github.com/paganini2008/cronsmith)
