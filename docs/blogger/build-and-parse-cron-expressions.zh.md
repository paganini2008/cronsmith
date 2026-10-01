# 别再手写 cron：用 Java 构建和解析它

**Cronsmith** 是一个 Java 库，用流式、面向对象的 API 来构建、解析和运行 cron，取代手写字符串。你用方法调用描述「什么时候」，拿回一个 cron 表达式、它会触发的那些具体时刻、或者一个正在运行的任务，并且横跨 Quartz、Spring、AWS EventBridge 和 crontab。

```java
// 「每月最后一个周五 18:00」—— 可读，而且能告诉你下次触发
CronExpression cron = new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
cron.toString();              // 0 0 18 ? * FRIL
cron.getNextFiredDateTime();  // 下一个「每月最后一个周五」18:00
```

## 它解决什么问题？

快问快答：`0 0 18 ? * FRIL` 到底什么时候触发？cron 字符串是「只写不读」的：容易写错、难以 review、更没法用肉眼做单元测试。更糟的是，「cron」根本不是一种语言——Quartz、Spring、AWS EventBridge、Unix crontab 在字段、在「`1` 是周日还是周一」、在支不支持 `L` / `#` / `W` 上各执一词。把一行 Quartz 复制进 crontab，它可能在不同的时间悄悄触发。

Cronsmith 把排期变成**可读、可迭代、可移植的代码**：用方法调用构建它，按你要部署的目标打印它，并在信任它之前先问它接下来会在哪些时刻触发。

## 快速开始

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
weekdays9.getNextFiredDateTime();  // 下一个工作日 09:00
```

## 环境要求

- **JDK 17+**
- 核心解析器唯一的运行时依赖是 **ANTLR**，不依赖 Spring、HTTP 或数据库。

## 工作原理

`CronBuilder` 构建出一个 `CronExpression`（排期的可迭代、可序列化模型）。从它出发，你可以按某种方言渲染，或者遍历它的触发时刻：

```
CronBuilder  ──构建──▶  CronExpression  ──┬── toString / toQuartz / toAws / toUnix  （渲染）
   或 CRON.parse(str) ──────────────▶      ├── consume(..) / getNextFiredDateTime()  （迭代）
                                           └── serialize() / deserialize(..)         （存储）
```

## 代码示例

### 构建：描述它，而不是拼字符串

**输入 → 输出：**

```java
new CronBuilder().everySecond(5);                                       // */5 * * * * ?
new CronBuilder().everyMonth().day(10).andDay(15).andLastDay().everyHour(2).everyMinute(5);
// 0 */5 */2 10,15,L * ?
new CronBuilder().everyMonth().dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2);  // 0 0 */2 ? * SAT#3（第3个周六）
new CronBuilder().everyMonth().lastDay(3).at(23, 30);                   // 0 30 23 L-3 * ?
new CronBuilder().everyMonth().latestWeekday(15).at(9, 0);              // 0 0 9 15W * ?（离15号最近的工作日）
```

### 看一眼排期，而不是盲信它

```java
new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay())
        .everyMonth().latestWeekday(15).at(9, 0)
        .consume(System.out::println, 5);
// 2027-01-15T09:00 … 2027-05-14T09:00  <- 15 号是周六，于是顺延到最近的工作日
```

### 把字符串解析回模型

```java
CRON.parse("*/5 * * * *");    // 0 */5 * * * ?      一行 crontab，秒位补齐
CRON.parse("0 9 * * 1-5");    // 0 0 9 ? * MON-FRI  crontab：1 是周一
CRON.parse("0 0 12 ? * 1");   // 0 0 12 ? * SUN     Quartz： 1 是周日
```

字段个数决定怎么读：**5** = crontab，**6** = 不带年的 Quartz，**7** = 带年的 Quartz。

### YCRON：月制 cron 说不出来的排期

七个字段：`<秒> <分> <时> <星期> <年内第几周> <年内第几天> [<年>]`。

```java
new CronBuilder().year(2026).day(100).at(12, 0, 0);        // 0 0 12 ? ? 100 2026（一年第100天）
new CronBuilder().year(2026).week(20).Mon().at(9, 0, 0);   // 0 0 9 MON 20 ? 2026（ISO 第20周的周一）
YCRON.parse("0 0 12 ? ? 100 2026").getCronType();          // CronType.YCRON（独立的语法）
```

### 把普通间隔变成 cron，或在进程内直接跑

```java
CRON.setInterval("PT30S");                    // 每 30 秒（PT1H30M / 亚秒会被拒绝，而不是四舍五入）

ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);
CronFuture f = new CronBuilder().everySecond(5).scheduler(exec).runTask(() -> doWork(), 10); // 跑 10 次
```

## 配置：语法参考

```
 秒  分  时  日           月   星期      [年]
  *   *   *   *            *    ?         *
 日：1-31, L, LW, L-n, nW, ?        星期：1-7 (SUN=1) 或 SUN-SAT, nL, n#m, ?
 YCRON：<秒> <分> <时> <星期> <年内第几周> <年内第几天> [<年>]
```

| 符号 | 含义 |
|------|------|
| `L` / `L-n` / `LW` | 当月最后一天 / 最后一天前 `n` 天 / 当月最后一个工作日 |
| `nW` | 离第 `n` 天最近的工作日，且不跨出当月 |
| `<dow>L` / `<dow>#n` | 当月最后一个 `<dow>`（`FRIL`）/ 第 `n` 个 `<dow>`（`TUE#2`，没有第 `n` 个的月份跳过） |
| `a-b` / `a/n` / `a,b` | 范围 / 步长 / 列表（元素本身也可是范围或步长） |

## 跨调度器对比

一份排期，按你要部署的目标打印。目标表达不了的特性会被**明确报错，而不是悄悄改写**：

| 排期 | Quartz | Spring | AWS EventBridge | Unix crontab |
|------|--------|--------|-----------------|--------------|
| 工作日 09:00 | `0 0 9 ? * MON-FRI` | `0 0 9 ? * MON-FRI` | `0 9 ? * MON-FRI *` | `0 9 * * MON-FRI` |
| 每 15 秒 | `*/15 * * * * ?` | `*/15 * * * * ?` | 没有秒字段 | 没有秒字段 |
| 每月最后一天 | `0 59 23 L * ?` | `0 59 23 L * ?` | `59 23 L * ? *` | 不支持 `L` |
| 第 2 个周二 | `0 0 10 ? * TUE#2` | `0 0 10 ? * TUE#2` | `0 10 ? * TUE#2 *` | 不支持 `#` |
| 限定 2027-2029 | `… 2027-2029` | 没有年字段 | `… 2027-2029` | 没有年字段 |

```java
CronExpression daily = new CronBuilder().everyDay().at(9, 30);
CRON.toUnixString(daily);   // 30 9 * * *  （若排期需要 crontab 没有的 L/#/秒，会抛异常）
```

## 局限与取舍

- 它是一个**cron 构建器 / 解析器 / 进程内运行器**，不是分布式任务系统。要持久化、集群化、带重试和控制台的调度，用 [cronflower](https://github.com/paganini2008/cronflower) 里的 `cronsmith-spring-boot-starter`。
- 渲染是**诚实而非有损的**：目标表达不了的排期会**抛异常**，而不是悄悄改变触发时间。
- ISO-8601 间隔必须是单一单位的整数倍（`PT1H30M`、`PT90M`、亚秒会被拒绝）。

## 小结

- 用**流式、可读的 API** 构建 cron，`toString()` 拿字符串。
- 把任意方言**解析**回可检视、可迭代的 `CronExpression`。
- 一份排期**移植**到 Quartz / Spring / AWS / crontab，表达不了的会报错。
- **YCRON** 表达年制、周制、年内第几天这些经典 cron 说不出的排期。
- 在信任排期前**迭代**出真实触发时刻（`consume` / `getNextFiredDateTime`）。
- **可序列化**，还带一个进程内时间轮调度器跑简单任务。

获取它：[cronsmith](https://github.com/paganini2008/cronflower) · JDK 17+，Apache 2.0。
