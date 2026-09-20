# Cronsmith — the ultimate cron expression generator & parser

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.java.com)
[![Quartz](https://img.shields.io/badge/Quartz-Compatible-brightgreen.svg)](https://www.quartz-scheduler.org/)
[![Spring](https://img.shields.io/badge/Spring%20Scheduling-Compatible-brightgreen.svg)](https://spring.io/)
[![AWS](https://img.shields.io/badge/AWS%20EventBridge-Compatible-brightgreen.svg)](https://docs.aws.amazon.com/eventbridge/)
[![crontab](https://img.shields.io/badge/Unix%20crontab-Compatible-brightgreen.svg)](https://man7.org/linux/man-pages/man5/crontab.5.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Cronsmith** is a Java library for building, parsing and running cron schedules through a fluent,
object-oriented API instead of hand-written strings. You describe *when* something should happen in
plain method calls, and get back a cron expression, the list of date-times it fires at, or a running
task.

It speaks the dialects you already deploy against — **Quartz**, **Spring Scheduling**,
**AWS EventBridge** and **Unix crontab** — extends classic cron with multi-value `L` and `#`, and
adds a year-based dialect, **YCRON**, for schedules the month-based line can't express.

```java
// "the last Friday of every month at 18:00"
CronExpression cron = new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);

cron.toString();              // 0 0 18 ? * FRIL
CRON.toAwsString(cron);       // 0 18 ? * FRIL *
cron.getNextFiredDateTime();  // the next last-Friday-of-the-month, 18:00
```

## Table of contents

- [1. Object-oriented builder](#1-object-oriented-builder)
- [2. Parsing and reverse engineering](#2-parsing-and-reverse-engineering)
- [3. Cross-scheduler output](#3-cross-scheduler-output)
- [4. YCRON — year-based schedules](#4-ycron--year-based-schedules)
- [5. Beyond standard cron](#5-beyond-standard-cron)
- [6. Built-in scheduler](#6-built-in-scheduler)
- [Stateful task scheduling](#stateful-task-scheduling)
- [Cron syntax reference](#cron-syntax-reference)
- [Best practices](#best-practices)
- [Worked examples](#worked-examples)
- [Installation](#installation)

---

## 1. Object-oriented builder

Build complex schedules by describing them, not by assembling strings. Chain the rule the way you'd
say it out loud; call `toString()` for the string.

```java
new CronBuilder().everySecond(5);
// */5 * * * * ?

new CronBuilder().everyMinute(5).second(5).andSecond(10).toSecond(30).andSecond(32).toSecond(59, 2);
// 5,10-30,32/2 */5 * * * ?

new CronBuilder().everyMonth().day(10).andDay(15).andDay(16).andLastDay().everyHour(2).everyMinute(5);
// 0 */5 */2 10,15,16,L * ?

new CronBuilder().everyMonth().everyWeek().Mon().toFri().at(15, 10);
// 0 10 15 ? * MON-FRI

new CronBuilder().everyMonth().dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2);
// 0 0 */2 ? * SAT#3   (3rd Saturday)

new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
// 0 0 18 ? * FRIL     (last Friday)

new CronBuilder().everyMonth().lastDay(3).at(23, 30);
// 0 30 23 L-3 * ?     (3rd-to-last day)

new CronBuilder().everyMonth().latestWeekday(15).at(9, 0);
// 0 0 9 15W * ?       (nearest weekday to the 15th)
```

Every expression also iterates, so you can look at a schedule instead of trusting it:

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
expression itself and returns the first occurrence strictly after the reference time.

## 2. Parsing and reverse engineering

`CRON.parse(..)` turns a string back into a `CronExpression` you can inspect, iterate and re-render.

```java
CRON.parse("0 0 12 ? * FRIL");               // 0 0 12 ? * FRIL
CRON.parse("0 0 12 ? * TUE#2");              // 0 0 12 ? * TUE#2
CRON.parse("0 0 12 LW * ?");                 // 0 0 12 LW * ?
CRON.parse("0 15 10 ? * MON-FRI 2027-2030"); // 0 15 10 ? * MON-FRI 2027-2030
```

The field count decides how the string is read:

| Fields | Read as                                              | Day-of-week numbering           |
|--------|------------------------------------------------------|---------------------------------|
| 5      | Unix crontab (`min hour dom month dow`)              | MON=1 … SAT=6, Sunday is 0 or 7 |
| 6      | Quartz without a year (`sec min hour dom month dow`) | SUN=1 … SAT=7                   |
| 7      | Quartz with a year                                   | SUN=1 … SAT=7                   |

```java
CRON.parse("*/5 * * * *");   // 0 */5 * * * ?      a crontab line, seconds filled in
CRON.parse("0 9 * * 1-5");   // 0 0 9 ? * MON-FRI  crontab: 1 is Monday
CRON.parse("0 0 12 ? * 1");  // 0 0 12 ? * SUN     Quartz:  1 is Sunday
```

## 3. Cross-scheduler output

One schedule, printed for whichever scheduler you deploy to. Anything a target can't express is
reported rather than silently rewritten into something that fires at different times.

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);

CRON.toQuartzString(daily);  // 0 30 9 * * ?
CRON.toSpringString(daily);  // 0 30 9 * * ?
CRON.toAwsString(daily);     // 30 9 * * ? *
CRON.toUnixString(daily);    // 30 9 * * *
CRON.toCronString(daily, CronDialect.AWS);   // 30 9 * * ? *
```

| Schedule                | Quartz              | Spring              | AWS EventBridge     | Unix crontab      |
|-------------------------|---------------------|---------------------|---------------------|-------------------|
| every day 09:30         | `0 30 9 * * ?`      | `0 30 9 * * ?`      | `30 9 * * ? *`      | `30 9 * * *`      |
| weekdays 09:00          | `0 0 9 ? * MON-FRI` | `0 0 9 ? * MON-FRI` | `0 9 ? * MON-FRI *` | `0 9 * * MON-FRI` |
| every 15 minutes        | `0 */15 * * * ?`    | `0 */15 * * * ?`    | `*/15 * * * ? *`    | `*/15 * * * *`    |
| every 15 seconds        | `*/15 * * * * ?`    | `*/15 * * * * ?`    | no seconds field    | no seconds field  |
| last day of month       | `0 59 23 L * ?`     | `0 59 23 L * ?`     | `59 23 L * ? *`     | no `L`            |
| 2nd Tuesday             | `0 0 10 ? * TUE#2`  | `0 0 10 ? * TUE#2`  | `0 10 ? * TUE#2 *`  | no `#`            |
| 3rd-from-last day       | `0 0 0 L-3 * ?`     | no `L-n`            | no `L-n`            | no `L`            |
| restricted to 2027-2029 | `… 2027-2029`       | no year field       | `… 2027-2029`       | no year field     |

Day-of-week is always printed by name: Quartz and AWS count `SUN=1`, Spring and crontab count
`MON=1`, but `MON` means Monday everywhere.

## 4. YCRON — year-based schedules

Standard cron thinks in months. Plenty of real schedules don't — "the 100th day of the year",
"Monday of ISO week 20", "every other year". **YCRON** is a separate, year-based line with seven
fields:

```
<sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]
```

Pick the date one of two ways; the field you're not using becomes `?`:

- **day-of-week + week-of-year** together — "Monday of week 20". Day-of-year is then `?`.
- **day-of-year** alone — "the 100th day". Day-of-week and week-of-year are then `?`.

Build it with the same `CronBuilder`, starting from a year:

```java
new CronBuilder().year(2026).day(100).at(12, 0, 0);        // 0 0 12 ? ? 100 2026
new CronBuilder().year(2026).week(20).Mon().at(9, 0, 0);   // 0 0 9 MON 20 ? 2026
new CronBuilder().everyYear().week(40).everyDay().at(0, 0, 0);
```

Parse it through `YCRON.parse` — its own grammar, so the traditional path is untouched:

```java
CronExpression a = YCRON.parse("0 0 12 ? ? 100 2026");  // day 100 of 2026, noon
CronExpression b = YCRON.parse("0 0 9 MON 20 ? 2026");  // Monday of ISO week 20, 2026, 09:00

a.getCronType();                 // CronType.YCRON
YCRON.toYCronString(a);          // 0 0 12 ? ? 100 2026
```

An absent (or `*`) year means every year. Everything below the date and the trailing year carries
the same meaning as in `CRON`.

## 5. Beyond standard cron

A few more things classic cron has no syntax for. Year- and week-based ones render as **YCRON**
(above); the rest iterate normally.

**ISO-8601 durations** — turn a plain interval into a schedule. Fires once now, then every interval.

```java
CRON.setInterval("PT30S");             // every 30 seconds
CRON.setInterval("PT5M");              // every 5 minutes
CRON.setInterval("PT2H");              // every 2 hours
CRON.setInterval("P1D");               // every day
CRON.setInterval(Duration.ofHours(2)); // same as "PT2H"
```

A cron field steps within its own range, so the coarsest exact unit is chosen — `PT120M` becomes
every two hours, not 7200 seconds. Anything that isn't an exact multiple of a single fitting unit
(`PT1H30M`, `PT90M`, `PT25H`, or below a second) is rejected rather than silently rounded.

**Multi-value `L` and `#`** — combine last-of and nth-of forms that Quartz, Spring and AWS only
allow one at a time:

```java
new CronBuilder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).and(3, DayOfWeek.WEDNESDAY).andLastFri().at(9, 0);
// 0 0 9 ? * TUE#2,WED#3,FRIL

new CronBuilder().everyMonth().week(1).andLastWeek().Mon().at(8, 0);
// 0 0 8 ? * MON#1,MONL

new CronBuilder().everyMonth().day(10).andDay(15).andLatestWeekday(25).andLastDay().at(0, 0);
// 0 0 0 10,15,25W,L * ?
```

## 6. Built-in scheduler

An in-process scheduler that runs a schedule on a timing wheel, entirely in memory, onto a
`ScheduledExecutorService` you provide.

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

CronFuture future = new CronBuilder()
        .everySecond(5)
        .scheduler(executor)
        .runTask(() -> System.out.println("tick"), 10);   // run ten times

future.cancel(true);
```

It exposes the whole task life cycle, and several ways to bound a run:

```java
CronScheduler scheduler = new CronBuilder().everyMinute(5).scheduler(executor);

scheduler.subscribe(new CronSchedulerListener() {
    @Override public void onTaskFinished(CronScheduledEvent event) {
        System.out.println("next run: " + event.getNextFiredDateTime());
    }
    @Override public void onTaskFailed(CronScheduledEvent event) {
        log.error("task failed", event.getReason());
    }
});

scheduler.runTask(job, 10);                                 // ten times
scheduler.runTask(job, LocalDateTime.now().plusHours(2));   // until a point in time
scheduler.runTask(job, (task, reason) -> reason != null);   // until it first fails
CronFuture future = scheduler.runTaskForEver(job);

scheduler.pauseTask(job);
scheduler.resumeTask(job);
scheduler.removeTask(job);
```

---

## Stateful task scheduling

The library above is a pure cron **parser / builder** — no database, HTTP or Spring dependency.

**Persistent, distributed** task scheduling — a `TaskManager` that keeps task state across restarts
(in-memory, or a JDBC / jOOQ store on H2 · SQLite · MySQL · PostgreSQL), server-driven retries and
timeouts, and a cluster that dispatches runs to executors — lives in
**`cronsmith-spring-boot-starter`**, part of the
[cronflower](https://github.com/paganini2008/cronflower) monorepo.

## Cron syntax reference

```
 ┌───────────── second        0-59
 │ ┌─────────── minute        0-59
 │ │ ┌───────── hour          0-23
 │ │ │ ┌─────── day-of-month  1-31, L, LW, L-n, nW, ?
 │ │ │ │ ┌───── month         1-12 or JAN-DEC
 │ │ │ │ │ ┌─── day-of-week   1-7 (SUN=1) or SUN-SAT, nL, n#m, ?
 │ │ │ │ │ │ ┌─ year          optional, 1970-2099
 * * * * * ? *

 YCRON:  <sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]
```

| Tag       | Field        | Meaning                                                                           |
|-----------|--------------|-----------------------------------------------------------------------------------|
| `*`       | any          | every value                                                                       |
| `?`       | day fields   | no restriction; exactly one of day-of-month / day-of-week must carry it           |
| `a-b`     | any          | a range                                                                           |
| `a/n`     | any          | from `a`, every `n`th value — `*/15` in seconds fires at :00, :15, :30, :45       |
| `a,b,c`   | any          | a list; entries may themselves be ranges or steps                                 |
| `L`       | day-of-month | the last day of the month                                                         |
| `L-n`     | day-of-month | `n` days before the last day                                                      |
| `LW`      | day-of-month | the last weekday of the month                                                     |
| `nW`      | day-of-month | the weekday nearest the `n`th, without leaving the month                          |
| `<dow>L`  | day-of-week  | the last `<dow>` of the month, e.g. `FRIL`                                         |
| `<dow>#n` | day-of-week  | the `n`th `<dow>` of the month, e.g. `TUE#2`; months without an `n`th are skipped |

`#` and `L` count occurrences **inside the month**, as Quartz, Spring and AWS all mean: `FRI#5` only
fires in months that have five Fridays.

## Best practices

**Pin the start time when the result must be reproducible.** A builder starts from *now* in UTC, so
an expression carrying a year depends on when it was built:

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay()).year().toYear(2030);
```

**Set the zone the schedule is read in.** A cron expression is wall-clock time; the zone decides
which instant "09:00" is (default UTC). Across a daylight-saving switch the wall clock stays put —
09:00 is always 09:00 — while the real interval becomes 23 or 25 hours, so a daily job doesn't drift.

```java
new CronBuilder().setZoneId(ZoneId.of("Europe/Berlin")).everyDay().at(9, 0);
```

**Prefer weekday names to numbers.** `MON` is Monday everywhere; `1` is Sunday in Quartz/AWS but
Monday in Spring/crontab. Cronsmith prints names by default.

**Ask the target dialect before you deploy.** `CRON.toUnixString(..)` throwing is a better outcome
than a crontab entry that silently drops the `L` you needed:

```java
try {
    deploy(CRON.toUnixString(cron));
} catch (UnsupportedOperationException e) {
    // this schedule needs a Quartz-class scheduler
}
```

**Store the expression, not the next fire time.** `CronExpression` is `Serializable`; catching up
from an old snapshot is O(1) per level, so restoring a per-second schedule from last year is instant:

```java
byte[] snapshot = cron.serialize();
CronExpression restored = CronExpression.deserialize(snapshot);
```

**Review `L`, `W` and `#` with `consume(..)`**, where month lengths and weekends change the answer:

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

## Worked examples

```java
// End-of-month billing, last weekday, 23:30
new CronBuilder().everyMonth().lastWeekday().at(23, 30);
// 0 30 23 LW * ?

// Payroll on the 15th and the last day, moved off weekends, 06:00
new CronBuilder().everyMonth().latestWeekday(15).andLastWeekday().at(6, 0);
// 0 0 6 15W,LW * ?

// Weekly report: 2nd Tuesday and the last Friday, 17:00
new CronBuilder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).andLastFri().at(17, 0);
// 0 0 17 ? * TUE#2,FRIL

// Business-hours health check, every 15 minutes on weekdays
new CronBuilder().everyMonth().everyWeek().Mon().toFri().hour(9).toHour(18).everyMinute(15);
// 0 */15 9-18 ? * MON-FRI

// Quarterly, first day of the quarter, 02:00
new CronBuilder().everyMonth(3).day(1).at(2, 0);
// 0 0 2 1 */3 ?

// A campaign bounded by a date range
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .year(2027).toYear(2029).June().andJuly().andAug().everyWeek().Sat().andSun().at(10, 0);
// 0 0 10 ? JUN,JUL,AUG SAT,SUN 2027-2029

// One-off run at a fixed moment
CRON.atFuture(LocalDateTime.of(2027, 12, 1, 12, 15, 0));
// 0 15 12 1 DEC ? 2027

// Fixed interval without writing cron at all
CRON.setInterval(5, TimeUnit.MINUTES);        // 0 */5 * * * ?
CRON.setInterval(LocalTime.of(23, 45, 30));   // 30 45 23 * * ?

// Migrating an existing crontab line
CronExpression cron = CRON.parse("15 10 * * MON-FRI");  // 0 15 10 ? * MON-FRI
CRON.toAwsString(cron);                                 // 15 10 ? * MON-FRI *
```

## Installation

Requires JDK 17 or later. The core parser has no runtime dependency beyond ANTLR.

```xml
<dependency>
    <groupId>com.github.paganini2008</groupId>
    <artifactId>cronsmith</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```gradle
implementation 'com.github.paganini2008:cronsmith:1.0.0-SNAPSHOT'
```

## License

Licensed under the Apache License, Version 2.0 — see the [LICENSE](LICENSE) file.
