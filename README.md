# Cronsmith

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.java.com)
[![Quartz](https://img.shields.io/badge/Quartz-Compatible-brightgreen.svg)](https://www.quartz-scheduler.org/)
[![Spring](https://img.shields.io/badge/Spring%20Scheduling-Compatible-brightgreen.svg)](https://spring.io/)
[![AWS](https://img.shields.io/badge/AWS%20EventBridge-Compatible-brightgreen.svg)](https://docs.aws.amazon.com/eventbridge/)
[![crontab](https://img.shields.io/badge/Unix%20crontab-Compatible-brightgreen.svg)](https://man7.org/linux/man-pages/man5/crontab.5.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Build, parse and run cron in Java with a fluent API — never hand-type a cron string again.**

Cronsmith lets you describe *when* something should happen in plain method calls and get back a cron
expression, the exact date-times it fires at, or a running task. It speaks the dialects you already
deploy against (**Quartz**, **Spring Scheduling**, **AWS EventBridge**, **Unix crontab**), extends
classic cron with multi-value `L` and `#`, and adds a year-based dialect (**YCRON**) for schedules the
month-based line can't express.

```java
// "the last Friday of every month at 18:00"
CronExpression cron = new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);

cron.toString();              // 0 0 18 ? * FRIL
CRON.toAwsString(cron);       // 0 18 ? * FRIL *
cron.getNextFiredDateTime();  // the next last-Friday-of-the-month, 18:00
```

## Features

| Capability | What it gives you |
|------------|-------------------|
| **Fluent OO builder** | Describe a schedule in method calls; get the string with `toString()` — no error-prone hand-typing |
| **Parse & reverse-engineer** | `CRON.parse(..)` turns any string back into an inspectable, iterable `CronExpression` |
| **Cross-scheduler output** | One schedule printed for Quartz / Spring / AWS EventBridge / Unix crontab; anything a target can't express is **reported, not silently rewritten** |
| **YCRON — year-based** | "the 100th day of the year", "Monday of ISO week 20", "every other year" — things month-based cron can't say |
| **Beyond standard cron** | ISO-8601 duration → cron, and multi-value `L` / `#` that Quartz/Spring/AWS allow only one at a time |
| **Inspect before you trust** | Iterate the real fire times with `consume(..)` / `getNextFiredDateTime()` |
| **Built-in scheduler** | Run a schedule in-process on a timing wheel, with the full task lifecycle |
| **Serializable & durable** | `CronExpression` is `Serializable`; catching up from an old snapshot is O(1) per level |

## Requirements

- **JDK 17+**
- The core parser's only runtime dependency is **ANTLR** — no Spring, HTTP or database.

## Quick Start

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

```java
// Input: describe it. Output: a cron string + the next fire time.
CronExpression weekdays9 = new CronBuilder().everyWeek().Mon().toFri().at(9, 0);
weekdays9.toString();              // 0 0 9 ? * MON-FRI
weekdays9.getNextFiredDateTime();  // next weekday at 09:00
```

## Examples

### Build — describe the schedule, don't assemble strings

```java
new CronBuilder().everySecond(5);                                            // */5 * * * * ?
new CronBuilder().everyMonth().day(10).andDay(15).andLastDay().everyHour(2).everyMinute(5);
// 0 */5 */2 10,15,L * ?
new CronBuilder().everyMonth().everyWeek().Mon().toFri().at(15, 10);         // 0 10 15 ? * MON-FRI
new CronBuilder().everyMonth().dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2);// 0 0 */2 ? * SAT#3  (3rd Sat)
new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0); // 0 0 18 ? * FRIL
new CronBuilder().everyMonth().lastDay(3).at(23, 30);                        // 0 30 23 L-3 * ?
new CronBuilder().everyMonth().latestWeekday(15).at(9, 0);                   // 0 0 9 15W * ?
```

Every expression iterates, so you can **look at a schedule instead of trusting it**:

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .everyMonth().latestWeekday(15).at(9, 0)
        .consume(System.out::println, 5);
// 2027-01-15T09:00 … 2027-05-14T09:00  <- the 15th is a Saturday, so it moves to the nearest weekday
```

`consume(..)` walks a copy; `getNextFiredDateTime()` advances the expression and returns the first
occurrence strictly after the reference time.

### Parse & reverse-engineer

```java
CRON.parse("0 0 12 ? * FRIL");               // 0 0 12 ? * FRIL
CRON.parse("*/5 * * * *");                    // 0 */5 * * * ?      a crontab line, seconds filled in
CRON.parse("0 9 * * 1-5");                    // 0 0 9 ? * MON-FRI  crontab: 1 is Monday
CRON.parse("0 0 12 ? * 1");                   // 0 0 12 ? * SUN     Quartz:  1 is Sunday
```

The **field count** decides how a string is read:

| Fields | Read as | Day-of-week numbering |
|--------|---------|------------------------|
| 5 | Unix crontab (`min hour dom month dow`) | MON=1 … SAT=6, Sunday is 0 or 7 |
| 6 | Quartz without a year (`sec min hour dom month dow`) | SUN=1 … SAT=7 |
| 7 | Quartz with a year | SUN=1 … SAT=7 |

### YCRON — year-based schedules

Seven fields: `<sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]`. Pick the date by
**day-of-week + week-of-year**, or by **day-of-year** alone; the unused field becomes `?`.

```java
new CronBuilder().year(2026).day(100).at(12, 0, 0);        // 0 0 12 ? ? 100 2026   (100th day)
new CronBuilder().year(2026).week(20).Mon().at(9, 0, 0);   // 0 0 9 MON 20 ? 2026   (Mon of ISO week 20)

CronExpression a = YCRON.parse("0 0 12 ? ? 100 2026");     // its own grammar; the classic path is untouched
a.getCronType();                                           // CronType.YCRON
```

An absent (or `*`) year means every year.

### Beyond standard cron

```java
// ISO-8601 duration → cron (fires once now, then every interval)
CRON.setInterval("PT30S");   // every 30s      CRON.setInterval("PT2H");  // every 2h
// PT1H30M / PT90M / below a second are rejected, not silently rounded.

// Multi-value L and # that other dialects allow only one at a time
new CronBuilder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).and(3, DayOfWeek.WEDNESDAY).andLastFri().at(9, 0);
// 0 0 9 ? * TUE#2,WED#3,FRIL
```

### Built-in scheduler

Run a schedule in-process on a timing wheel, onto a `ScheduledExecutorService` you provide:

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

CronFuture future = new CronBuilder().everySecond(5)
        .scheduler(executor)
        .runTask(() -> System.out.println("tick"), 10);     // run ten times

CronScheduler scheduler = new CronBuilder().everyMinute(5).scheduler(executor);
scheduler.runTask(job, LocalDateTime.now().plusHours(2));   // until a point in time
scheduler.runTask(job, (task, reason) -> reason != null);   // until it first fails
scheduler.pauseTask(job); scheduler.resumeTask(job); scheduler.removeTask(job);
```

Subscribe a `CronSchedulerListener` for `onTaskFinished` / `onTaskFailed` to see the next fire time or
the failure reason.

### More worked examples

```java
new CronBuilder().everyMonth().lastWeekday().at(23, 30);                 // 0 30 23 LW * ?  end-of-month billing
new CronBuilder().everyMonth().latestWeekday(15).andLastWeekday().at(6, 0); // 0 0 6 15W,LW * ?  payroll off weekends
new CronBuilder().everyMonth(3).day(1).at(2, 0);                         // 0 0 2 1 */3 ?   quarterly
CRON.atFuture(LocalDateTime.of(2027, 12, 1, 12, 15, 0));                 // 0 15 12 1 DEC ? 2027  one-off
CRON.parse("15 10 * * MON-FRI");                                        // migrate a crontab line
```

## Cross-scheduler compatibility

One schedule, printed for whichever scheduler you deploy to. Unsupported features are reported rather
than silently rewritten into something that fires at different times.

| Schedule | Quartz | Spring | AWS EventBridge | Unix crontab |
|----------|--------|--------|-----------------|--------------|
| every day 09:30 | `0 30 9 * * ?` | `0 30 9 * * ?` | `30 9 * * ? *` | `30 9 * * *` |
| weekdays 09:00 | `0 0 9 ? * MON-FRI` | `0 0 9 ? * MON-FRI` | `0 9 ? * MON-FRI *` | `0 9 * * MON-FRI` |
| every 15 minutes | `0 */15 * * * ?` | `0 */15 * * * ?` | `*/15 * * * ? *` | `*/15 * * * *` |
| every 15 seconds | `*/15 * * * * ?` | `*/15 * * * * ?` | no seconds field | no seconds field |
| last day of month | `0 59 23 L * ?` | `0 59 23 L * ?` | `59 23 L * ? *` | no `L` |
| 2nd Tuesday | `0 0 10 ? * TUE#2` | `0 0 10 ? * TUE#2` | `0 10 ? * TUE#2 *` | no `#` |
| 3rd-from-last day | `0 0 0 L-3 * ?` | no `L-n` | no `L-n` | no `L` |
| restricted to 2027-2029 | `… 2027-2029` | no year field | `… 2027-2029` | no year field |

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);
CRON.toQuartzString(daily);  // 0 30 9 * * ?
CRON.toAwsString(daily);     // 30 9 * * ? *
CRON.toUnixString(daily);    // 30 9 * * *
```

Day-of-week is always printed **by name** (`MON` is Monday everywhere), so the SUN=1 vs MON=1
numbering split never bites you.

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

| Tag | Field | Meaning |
|-----|-------|---------|
| `*` / `?` | any / day fields | every value / no restriction (exactly one day field carries `?`) |
| `a-b` · `a/n` · `a,b,c` | any | range · step from `a` · list (entries may be ranges or steps) |
| `L` · `L-n` · `LW` | day-of-month | last day · `n` days before last · last weekday |
| `nW` | day-of-month | weekday nearest the `n`th, without leaving the month |
| `<dow>L` · `<dow>#n` | day-of-week | last `<dow>` (`FRIL`) · `n`th `<dow>` (`TUE#2`; skipped in months without an `n`th) |

**Best practices:** pin `setStartTime(..)` when a year-bearing result must be reproducible; set
`setZoneId(..)` so "09:00" is the instant you mean (default UTC, DST-safe); prefer weekday names to
numbers; call the target dialect (`toUnixString` throwing beats a crontab line that drops your `L`);
and store the serialized `CronExpression`, not the next fire time.

## Documentation

- **Distributed, persistent, clustered scheduling** (a `TaskManager` with JDBC/jOOQ stores, server-driven
  retries/timeouts, and a cluster that dispatches to executors) lives in
  **`cronsmith-spring-boot-starter`**, part of the [cronflower](https://github.com/paganini2008/cronflower) monorepo.
- Guide: *Stop hand-writing cron — build and parse it in Java* (`docs/blogger/`).

## Contributing & License

Issues and PRs are welcome — open an issue to discuss a change first and keep PRs focused with tests.
Licensed under the **Apache License 2.0**; see [`LICENSE`](LICENSE).
