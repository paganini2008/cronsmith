# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0-RC2] - 2026-08-18

A release candidate. It brings cronsmith's semantics in line with the schedulers people actually
deploy against, adds cross-scheduler output, and puts a test suite behind all of it.

The corrections below are wide-reaching enough to change when existing schedules fire, so they go
out as a candidate rather than as 1.0.0 final - give them a run against real schedules before the
stable tag.

**If you are upgrading from `1.0.0-RC1`, read [Breaking changes](#breaking-changes) first.** Several
corrections change *when an existing expression fires*, not just how it prints. Re-check any
schedule that uses a slashed field, a numeric day-of-week, or the `#` and `L` tags.

### Breaking changes

#### Slashed fields now start at their own starting point

`*/n` used to fire at `from + n - 1`, one whole step late. It now behaves the way every other cron
implementation does.

| Expression | Before | After |
|---|---|---|
| `*/15` (seconds) | :14, :29, :44, :59 | :00, :15, :30, :45 |
| `0 0 */6 * * ?` | 05:00, 11:00, 17:00, 23:00 | 00:00, 06:00, 12:00, 18:00 |
| `0 0 0 */5 * ?` | 5th, 10th, 15th … | 1st, 6th, 11th … |
| `0 0 0 1 */3 ?` | Mar, Jun, Sep, Dec | Jan, Apr, Jul, Oct |
| `everyMonth(2)` | February, April, June … | January, March, May … |

**What to do:** if you relied on the old offset, shift the starting point explicitly —
`everySecond(14, 15)` reproduces the previous `*/15`.

#### Numeric day-of-week follows cron numbering

Numbers in the day-of-week field are read and written as `SUN=1 … SAT=7`, the Quartz and AWS
EventBridge convention. They used to be `java.time` values (`MON=1 … SUN=7`), which meant a Quartz
expression pasted into cronsmith fired one day off.

```
"0 0 12 ? * 1"    before: Monday     after: Sunday
"0 0 12 ? * 2-6"  before: TUE-SAT    after: MON-FRI
"0 0 12 ? * 6L"   before: Saturday   after: last Friday
```

A range that wraps once translated, such as `1-5` (SUN-THU), is expanded into a list rather than
rejected. Five-field crontab lines keep their own convention (`MON=1`, Sunday as `0` or `7`).

**What to do:** prefer weekday names. `MON` means Monday in every scheduler; the numbers do not.

#### `#` and `L` count occurrences in the month

Both tags were built on ISO week-of-month, whose first week can start in the previous month. They now
mean what Quartz, Spring and AWS mean by them: `FRI#1` is the first Friday **inside** the month, and
`FRIL` is the last one.

| Builder call | Before | After |
|---|---|---|
| `everyMonth().week(1).Mon()` | `MON#5`, fired on the 26th | `MON#1`, fires on the 5th |
| `everyMonth().dayOfWeek(1, MONDAY)` | first firing in the *previous year* | the first Monday of the month |
| `everyMonth().lastWeek().Fri()` | `5L`, not the last Friday | `FRIL`, the last Friday |
| `everyMonth().week(1).toWeek(3).Mon()` | collapsed to a single `MON#5` | `MON#1,MON#2,MON#3` |
| `everyMonth().everyWeek(2).Mon()` | `MON#2,MON#4` | `MON#1,MON#3,MON#5` |

A month without the requested occurrence is now skipped instead of rolling into the next one, so
`FRI#5` only fires in months that actually have five Fridays. Occurrence lists such as
`week(1).andWeek(3)` no longer produce a schedule that runs backwards.

#### `L` in the day-of-week field prints by name

`0 0 12 ? * FRIL` instead of `0 0 12 ? * 5L`. The numeric form was ambiguous across schedulers;
`FRIL` is accepted by Quartz and Spring, and both forms are still parsed. `Month.lastDayOfWeek(n)`
now selects the last occurrence rather than the one in the ISO-numbered final week.

#### Expressions that have no cron form are reported

`CRON.toCronString(..)` used to emit a syntactically invalid string for a schedule it could not
represent — a week of the year rendered as `0 0 12 ? 2026 MON#1`, with the year in the month field.
It now throws `UnsupportedOperationException`. Day-of-year and week-of-year schedules still iterate
and serialize normally; only their cron rendering is refused.

#### Other behavioural changes

- `getNextFiredDateTime()` no longer skips the current period — see [Fixed](#fixed). Code that
  compensated for the old off-by-one will now be one period early.
- `CRON.atFuture(LocalDate)` rejects today, and compares against UTC to match the builder's own
  clock. It previously accepted today and failed later with a different message.
- `Year.lastDay()` is recomputed per year, so a leap year fires on 31 December rather than the 30th.
- `ThisWeek.getWeek()` returns the occurrence ordinal instead of the ISO week-of-month.
- `LastWeekOfMonth.toCronString()` and `LastWeekOfYear.toCronString()` return `L` instead of `1L`.

### Added

#### Cross-scheduler output

`CronDialect` renders one schedule for Quartz, Spring Scheduling, AWS EventBridge or Unix crontab.
Anything a target cannot express is reported rather than silently rewritten.

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);

CRON.toQuartzString(daily);   // 0 30 9 * * ?
CRON.toSpringString(daily);   // 0 30 9 * * ?
CRON.toAwsString(daily);      // 30 9 * * ? *
CRON.toUnixString(daily);     // 30 9 * * *
CRON.toCronString(daily, CronDialect.AWS);
```

Refusals are explicit: AWS and crontab have no seconds field, crontab has none of the Quartz
extensions, Spring has no year field and no `L-n`.

#### Unix crontab input

`CRON.parse(..)` reads a five-field line as a crontab entry, including its own weekday numbering
(`MON=1`, Sunday as `0` or `7`) and the `*`/`?` day-field convention.

```java
CRON.parse("*/5 * * * *");   // 0 */5 * * * ?
CRON.parse("0 9 * * 1-5");   // 0 0 9 ? * MON-FRI
```

Restricting both day fields at once is crontab's "or" rule, which has no equivalent here and is
reported as a `CronParserException`.

#### Versioned snapshot format

`CRON.toByteArray(..)` and `CronExpression.serialize()` now prefix the serialized expression with a
short header naming the snapshot format. `CRON.load(..)` verifies it and refuses anything written in
a format it cannot read.

This matters because a snapshot stores *where the schedule currently stands*, not just its
definition, and is meant to live in a BLOB column between runs. Java's own serialization silently
fills unknown fields with `null` when a class has been reshaped, so without the header a snapshot
from an incompatible build reads back as a half-populated object that only fails later, in whichever
thread happens to iterate it. It now fails at load time, with a message saying so.

`copy()` is unaffected: it clones inside the process and never leaves it, so it carries no header.

The `serialVersionUID` of every class whose stored shape changed in this release was updated as a
second line of defence, so even a snapshot that bypasses the header is rejected rather than
misread.

#### Other additions

- `CRON.toCronFields(CronExpression)` returns the canonical seven-field array a dialect is rendered
  from, so no consumer has to take a finished expression apart again.
- Grammar support for `<month>/<step>`, `<weekday>/<step>` and `<weekday>L`. `FEB/3` used to fail to
  parse and `MON/2` used to silently drop its step.
- `TheDayOfWeek.andMon()`, which was missing while `andTues()` … `andSun()` existed.
- `AbbreviationUtils.toCronDayOfWeek(int)` / `fromCronDayOfWeek(int)` for translating between
  `java.time` and cron weekday numbering.
- JaCoCo in the build, with `mvn verify` failing below 80% instruction coverage.

### Fixed

#### Fixed-time schedules skipped one firing

The most serious defect in this release. Asking a fixed-time expression for its next run returned the
period *after* the one that was still due:

```java
// at 10:00
new CronBuilder().everyDay().at(12, 0).getNextFiredDateTime(now);
// before: tomorrow 12:00     after: today 12:00
```

Every schedule built from a fixed time — `at(..)`, `hour(..)`, `day(..)`, `month(..)` — lost one run
each time it was started or restarted. Interval schedules (`everyMinute(n)` and friends) were
unaffected. The cause was that only the interval expressions held their synchronized value back for
the next step; the fixed-time ones now do the same.

#### Catching up on an old start time was linear

`sync(..)` walked one occurrence at a time from the start time to the target, so a per-second
expression whose start time lay a few years back needed tens of millions of iterations. Each level
now hands the catching up to its parent in one hop.

- `getNextFiredDateTime(..)` on a three-year-old start time: **2064 ms → 0 ms**
- `CronScheduler.runTask(..)` with a start time from 2020: **10135 ms → 5 ms**

Behaviour is unchanged and covered by an equivalence test that compares the fast path against the
original step-by-step walk. A schedule whose start time lies in the past still begins at its next due
run — the backlog is never replayed.

#### Scheduler drifted by an hour across a daylight-saving switch

The delay until the next run was computed by subtracting two `LocalDateTime` values, which ignores
the offset change. On the night the clocks move, an hour of wall clock is worth two hours of real
time or none at all. The delay is now resolved against the zone first.

#### Task life cycle

- `FINISHED` events were delivered to `onTaskScheduled(..)`, so `onTaskFinished(..)` never fired.
- `CronScheduledEvent.toString()` printed `…false` instead of `, Ended: true`, an operator
  precedence slip in `str += ", Ended: " + nextFiredDateTime == null`.
- `DebugCronSchedulerListener.onTaskFailed(..)` threw a `NullPointerException` when the event carried
  no reason.

#### Parsing

- Any malformed expression threw a `NullPointerException`; it now raises a `CronParserException`
  naming the field that could not be read.
- `*/n` in the day-of-month field started at 0, so `0 0 12 */5 * ?` failed outright.
- `*/n` in the day-of-week field dereferenced a null expression.
- `* * * ? * *` threw a `ClassCastException`.

#### Calendar edge cases

- `31W` threw `Invalid date 'FEBRUARY 31'` in short months; it now falls back to the month's last day.
- Building a year-scoped expression after its year iterator was spent produced a null parent and a
  `NullPointerException`.
- `TimeSlot` rounded one-based fields down to 0 and threw for the first slot of every cycle. The
  whole enum was unreachable from the public API and had never been exercised.
- `ThisWeekOfYear.next()` discarded the result of `withYear(..)`, leaving the year unsynchronized.
- `ThisWeekOfYear.day(..)` and `everyDay(..)` mutated the original expression instead of a copy.

### Testing

The suite went from 5 test classes (14 failing, one that could never terminate) to 18 classes and
332 tests, at 90.6% instruction coverage.

- **Hard-coded years removed.** Tests pinned to 2025 started failing on 1 January 2026. Every
  expectation is now derived from the current year, and builders pin their start time so a run that
  straddles midnight on New Year's Eve stays stable.
- **`CronSchedulerTests.testScheduler1` no longer hangs the build.** It awaited a latch of
  `Integer.MAX_VALUE` counts, which could never be reached; every wait is now bounded.
- New coverage for cron semantics (slashed fields, `#`, `L`, weekday numbering, crontab input),
  dialect rendering, synchronization equivalence and speed, time zones, daylight-saving switches in
  both hemispheres, and schedules that have no cron representation.
- Every expression printed in `README.md` is asserted by `ReadmeExamplesTests`, so the documentation
  cannot drift away from what the library produces.

### Documentation

`README.md` was rewritten around the new capabilities: a dialect comparison table, a cron syntax
reference, a best-practices section, and worked examples for end-of-month billing, payroll, nth-
weekday reporting and crontab migration.

---

[1.0.0-RC2]: https://github.com/paganini2008/cronsmith/releases/tag/v1.0.0-RC2
