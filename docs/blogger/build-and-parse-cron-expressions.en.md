# Stop hand-writing cron: build and parse it in Java

**Cronsmith** is a Java library that builds, parses and runs cron through a fluent, object-oriented API
instead of hand-typed strings. You describe *when* in plain method calls and get back a cron expression,
the exact date-times it fires at, or a running task — across Quartz, Spring, AWS EventBridge and crontab.

```java
// "the last Friday of every month at 18:00" — readable, and it tells you the next fire
CronExpression cron = new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
cron.toString();              // 0 0 18 ? * FRIL
cron.getNextFiredDateTime();  // the next last-Friday-of-the-month, 18:00
```

## What problem does it solve?

Quick, what does `0 0 18 ? * FRIL` fire at? Cron strings are write-only: easy to fumble, hard to review,
and impossible to unit-test by eye. Worse, "cron" isn't one language — Quartz, Spring, AWS EventBridge
and Unix crontab disagree on fields, on whether `1` is Sunday or Monday, and on which of `L` / `#` / `W`
they even support. Copy a Quartz line into crontab and it can silently fire at a different time.

Cronsmith makes the schedule **code you can read, iterate and port**: build it with method calls, print
it for the target you deploy to, and ask it for its next fire times before you trust it.

## Quick start

```xml
<dependency>
    <groupId>com.github.paganini2008</groupId>
    <artifactId>cronsmith</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
CronExpression weekdays9 = new CronBuilder().everyWeek().Mon().toFri().at(9, 0);
weekdays9.toString();              // 0 0 9 ? * MON-FRI
weekdays9.getNextFiredDateTime();  // next weekday at 09:00
```

## Requirements

- **JDK 17+**
- The core parser's only runtime dependency is **ANTLR** — no Spring, HTTP or database.

## How it works

A `CronBuilder` builds a `CronExpression` (an iterable, serializable model of the schedule). From there
you render it for a dialect, or walk its fire times:

```
CronBuilder  ──build──▶  CronExpression  ──┬── toString / toQuartz / toAws / toUnix  (render)
   or CRON.parse(str) ───────────────▶     ├── consume(..) / getNextFiredDateTime()  (iterate)
                                            └── serialize() / deserialize(..)         (store)
```

## Code examples

### Build — describe it, don't assemble strings

**Input → Output:**

```java
new CronBuilder().everySecond(5);                                       // */5 * * * * ?
new CronBuilder().everyMonth().day(10).andDay(15).andLastDay().everyHour(2).everyMinute(5);
// 0 */5 */2 10,15,L * ?
new CronBuilder().everyMonth().dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2);  // 0 0 */2 ? * SAT#3 (3rd Sat)
new CronBuilder().everyMonth().lastDay(3).at(23, 30);                   // 0 30 23 L-3 * ?
new CronBuilder().everyMonth().latestWeekday(15).at(9, 0);              // 0 0 9 15W * ? (nearest weekday to 15th)
```

### Look at a schedule instead of trusting it

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .everyMonth().latestWeekday(15).at(9, 0)
        .consume(System.out::println, 5);
// 2027-01-15T09:00 … 2027-05-14T09:00  <- the 15th is a Saturday, so it moves to the nearest weekday
```

### Parse a string back into a model

```java
CRON.parse("*/5 * * * *");    // 0 */5 * * * ?      a crontab line, seconds filled in
CRON.parse("0 9 * * 1-5");    // 0 0 9 ? * MON-FRI  crontab: 1 is Monday
CRON.parse("0 0 12 ? * 1");   // 0 0 12 ? * SUN     Quartz:  1 is Sunday
```

The field count picks the reading: **5** = crontab, **6** = Quartz without a year, **7** = Quartz with a year.

### YCRON — schedules month-based cron can't say

Seven fields: `<sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]`.

```java
new CronBuilder().year(2026).day(100).at(12, 0, 0);        // 0 0 12 ? ? 100 2026  (100th day of the year)
new CronBuilder().year(2026).week(20).Mon().at(9, 0, 0);   // 0 0 9 MON 20 ? 2026  (Mon of ISO week 20)
YCRON.parse("0 0 12 ? ? 100 2026").getCronType();          // CronType.YCRON (its own grammar)
```

### Turn a plain interval into cron, or run it in-process

```java
CRON.setInterval("PT30S");                    // every 30s (PT1H30M / sub-second are rejected, not rounded)

ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);
CronFuture f = new CronBuilder().everySecond(5).scheduler(exec).runTask(() -> doWork(), 10); // run 10x
```

## Configuration — syntax reference

```
 sec min hour day-of-month month day-of-week [year]
  *   *   *       *           *        ?        *
 day-of-month: 1-31, L, LW, L-n, nW, ?     day-of-week: 1-7 (SUN=1) or SUN-SAT, nL, n#m, ?
 YCRON: <sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]
```

| Tag | Meaning |
|-----|---------|
| `L` / `L-n` / `LW` | last day / `n` days before last / last weekday of the month |
| `nW` | weekday nearest the `n`th, without leaving the month |
| `<dow>L` / `<dow>#n` | last `<dow>` (`FRIL`) / `n`th `<dow>` (`TUE#2`, skipped in months without an `n`th) |
| `a-b` / `a/n` / `a,b` | range / step / list (entries may themselves be ranges or steps) |

## How it compares across schedulers

One schedule, printed for whatever you deploy to. Unsupported features are **reported, not silently
rewritten**:

| Schedule | Quartz | Spring | AWS EventBridge | Unix crontab |
|----------|--------|--------|-----------------|--------------|
| weekdays 09:00 | `0 0 9 ? * MON-FRI` | `0 0 9 ? * MON-FRI` | `0 9 ? * MON-FRI *` | `0 9 * * MON-FRI` |
| every 15 seconds | `*/15 * * * * ?` | `*/15 * * * * ?` | no seconds field | no seconds field |
| last day of month | `0 59 23 L * ?` | `0 59 23 L * ?` | `59 23 L * ? *` | no `L` |
| 2nd Tuesday | `0 0 10 ? * TUE#2` | `0 0 10 ? * TUE#2` | `0 10 ? * TUE#2 *` | no `#` |
| restricted to 2027-2029 | `… 2027-2029` | no year field | `… 2027-2029` | no year field |

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);
CRON.toUnixString(daily);   // 30 9 * * *   (throws if the schedule needs L/#/seconds crontab lacks)
```

## Limitations & trade-offs

- It is a **cron builder / parser / in-process runner**, not a distributed job system. For persistent,
  clustered scheduling with retries and a console, use `cronsmith-spring-boot-starter` in
  [cronflower](https://github.com/paganini2008/cronflower).
- Rendering is **honest, not lossy**: a schedule a target can't express **throws** instead of quietly
  changing when it fires.
- ISO-8601 intervals must be an exact multiple of one unit (`PT1H30M`, `PT90M`, sub-second are rejected).

## Summary

- Build cron with a **fluent, readable API**; `toString()` for the string.
- **Parse** any dialect back into an inspectable, iterable `CronExpression`.
- **Port** one schedule to Quartz / Spring / AWS / crontab; unsupported features are reported.
- **YCRON** expresses year-, week- and day-of-year schedules classic cron can't.
- **Iterate** the real fire times (`consume` / `getNextFiredDateTime`) before you trust a schedule.
- **Serializable**, with an in-process timing-wheel scheduler for simple runs.

Get it: [cronsmith](https://github.com/paganini2008/cronflower) · JDK 17+, Apache 2.0.
