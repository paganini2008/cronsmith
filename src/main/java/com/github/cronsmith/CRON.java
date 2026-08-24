
package com.github.cronsmith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.DayOfWeek;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.CronExpressionContext;
import com.github.cronsmith.parser.CronParserException;
import com.github.cronsmith.parser.CronSyntaxErrorListener;
import com.github.cronsmith.utils.SerializationException;
import com.github.cronsmith.utils.SerializationUtils;
/**
 * 
 * CRON is the entry class of cronsmith library. Start your work from here.
 * 
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class CRON {

    /**
     * The default {@link CronBuilder} keeps its start time in UTC, so the "is it still in the
     * future" check has to be made against the very same zone. Comparing with the system default
     * zone would either reject valid dates or accept dates the builder later rejects, depending on
     * which side of UTC the caller lives.
     */
    static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

    public static CronExpression atFuture(LocalDate ld) {
        if (!ld.isAfter(LocalDate.now(DEFAULT_ZONE_ID))) {
            throw new IllegalArgumentException("Past date: " + ld);
        }
        return atFuture(ld.atStartOfDay());
    }

    public static CronExpression atFuture(LocalDateTime ldt) {
        if (ldt.isBefore(LocalDateTime.now(DEFAULT_ZONE_ID).withNano(0))) {
            throw new IllegalArgumentException("Past datetime: " + ldt);
        }
        return new CronBuilder().month(ldt.getYear(), ldt.getMonthValue()).day(ldt.getDayOfMonth())
                .at(ldt.getHour(), ldt.getMinute(), ldt.getSecond());
    }

    public static CronExpression setInterval(LocalTime lt) {
        return new CronBuilder().everyDay().at(lt.getHour(), lt.getMinute(), lt.getSecond());
    }

    public static CronExpression setInterval(int interval, TimeUnit timeUnit) {
        return setInterval(interval, interval, timeUnit);
    }

    public static CronExpression setInterval(long initialDelay, int interval, TimeUnit timeUnit) {
        if (initialDelay < 0 || interval <= 0) {
            throw new IllegalArgumentException("Invalid parameter 'initialDelay' or 'interval'");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Null timeUnit");
        }
        LocalDateTime ldt = initialDelay > 0 ? LocalDateTime.now()
                .plus(TimeUnit.MILLISECONDS.convert(initialDelay, timeUnit), ChronoUnit.MILLIS)
                : LocalDateTime.now();
        CronBuilder builder = new CronBuilder().setStartTime(ldt);
        switch (timeUnit) {
            case SECONDS:
                return builder.everySecond(interval);
            case MINUTES:
                return builder.everyMinute(interval);
            case HOURS:
                return builder.everyHour(interval);
            case DAYS:
                return builder.everyDay(interval);
            default:
                throw new UnsupportedOperationException("timeUnit: " + timeUnit.name());
        }
    }

    /** Number of fields in a Unix crontab line: minute, hour, day-of-month, month, day-of-week. */
    private static final int UNIX_FIELD_COUNT = 5;

    /**
     * Parses a cron expression.
     * <p>
     * Six and seven field expressions are read the Quartz way, starting at seconds and numbering
     * the day-of-week SUN=1 .. SAT=7. A five field expression is taken for a Unix crontab line: it
     * starts at minutes and numbers the day-of-week MON=1 .. SAT=6, with Sunday written as either
     * 0 or 7.
     */
    public static CronExpression parse(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().length() == 0) {
            throw new CronParserException("Empty cron expression");
        }
        String text = cronExpression.trim();
        String[] parts = text.split("\\s+");
        boolean unix = parts.length == UNIX_FIELD_COUNT;
        if (unix) {
            text = fromCrontabLine(parts);
        }
        CharStream input = CharStreams.fromString(text);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(CronSyntaxErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(CronSyntaxErrorListener.INSTANCE);
        ParseTree tree = parser.cron();
        CronExpressionContext context = new CronExpressionContext();
        context.setUnixDayOfWeek(unix);
        CronExpression parsed = context.visit(tree);
        if (unix) {
            // The two numbering conventions disagree about 7, so a crontab line is printed back
            // with weekday names, which mean the same thing to every scheduler.
            parsed.getBuilder().setUseDayOfWeekAsNumber(false);
        }
        return parsed;
    }

    /**
     * Turns a crontab line into the six-field form: a leading seconds field, and a {@code ?} in
     * whichever of the two day fields carries no restriction.
     * <p>
     * Crontab lets both day fields be restricted at once and then fires on either of them, which
     * has no equivalent here, so that combination is reported instead of being silently narrowed.
     */
    private static String fromCrontabLine(String[] parts) {
        String dayOfMonth = parts[2];
        String dayOfWeek = parts[4];
        if ("*".equals(dayOfWeek)) {
            dayOfWeek = "?";
        } else if ("*".equals(dayOfMonth)) {
            dayOfMonth = "?";
        } else {
            throw new CronParserException(
                    "Restricting both day-of-month and day-of-week is a crontab-only 'or' rule: "
                            + String.join(" ", parts));
        }
        return "0 " + parts[0] + " " + parts[1] + " " + dayOfMonth + " " + parts[3] + " "
                + dayOfWeek;
    }

    public static String printParseTree(String cronExpression) {
        CharStream input = CharStreams.fromString(cronExpression);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(CronSyntaxErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(CronSyntaxErrorListener.INSTANCE);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        return cronExpression;
    }

    private static void printTree(ParseTree tree, Parser parser, int indent) {
        String indentString = repeat("  ", indent);
        String nodeText = Trees.getNodeText(tree, parser);
        System.out.println(indentString + nodeText);
        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), parser, indent + 2);
        }
    }

    private static String repeat(String str, int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result = result.concat(str);
        }
        return result;
    }

    /**
     * Prints the expression in the flavour of another scheduler.
     *
     * @throws UnsupportedOperationException when that flavour cannot express this schedule
     */
    public static String toCronString(CronExpression cronExpression, CronDialect dialect) {
        return dialect.render(toCronFields(cronExpression));
    }

    /** Shorthand for {@link CronDialect#QUARTZ}. */
    public static String toQuartzString(CronExpression cronExpression) {
        return toCronString(cronExpression, CronDialect.QUARTZ);
    }

    /** Shorthand for {@link CronDialect#SPRING}. */
    public static String toSpringString(CronExpression cronExpression) {
        return toCronString(cronExpression, CronDialect.SPRING);
    }

    /** Shorthand for {@link CronDialect#AWS}. */
    public static String toAwsString(CronExpression cronExpression) {
        return toCronString(cronExpression, CronDialect.AWS);
    }

    /** Shorthand for {@link CronDialect#UNIX}. */
    public static String toUnixString(CronExpression cronExpression) {
        return toCronString(cronExpression, CronDialect.UNIX);
    }

    public static String toCronString(CronExpression cronExpression) {
        return CronDialect.CRONSMITH.render(toCronFields(cronExpression));
    }

    /**
     * Renders the expression field by field, in the canonical order second, minute, hour,
     * day-of-month, month, day-of-week, year.
     * <p>
     * The array always has {@link CronDialect#FIELD_COUNT} entries; the last one is {@code null}
     * when the schedule places no restriction on the year. Working from the fields is what lets a
     * dialect drop, reorder or add one without taking a finished expression apart again.
     *
     * @throws UnsupportedOperationException when part of the schedule has no cron field to live in
     */
    public static String[] toCronFields(CronExpression cronExpression) {
        CronExpression copy = cronExpression.copy();
        if (copy instanceof Year) {
            copy = ((Year) copy).Jan().day(1).at(0, 0, 0);
        } else if (copy instanceof Month) {
            copy = ((Month) copy).day(1).at(0, 0, 0);
        } else if (copy instanceof Week) {
            copy = ((Week) copy).Mon().at(0, 0, 0);
        } else if (copy instanceof Day) {
            copy = ((Day) copy).at(0, 0, 0);
        } else if (copy instanceof Hour) {
            copy = ((Hour) copy).at(0, 0);
        } else if (copy instanceof Minute) {
            copy = ((Minute) copy).second(0);
        }

        final String[] fields = new String[CronDialect.FIELD_COUNT];
        CronExpression second = copy;
        CronExpression minute = second.getParent();
        CronExpression hour = minute.getParent();
        fields[CronDialect.SECOND] = render(second);
        fields[CronDialect.MINUTE] = render(minute);
        fields[CronDialect.HOUR] = render(hour);

        CronExpression day = hour.getParent();
        boolean hasDayOfWeek = day instanceof DayOfWeek;
        fields[CronDialect.DAY_OF_MONTH] = hasDayOfWeek ? "?" : render(day);

        CronExpression month;
        if (hasDayOfWeek && day.getParent() instanceof Week) {
            // The week sits between the day-of-week and the month without a cron field of its own,
            // so it is only checked, never printed.
            CronExpression week = day.getParent();
            checkRenderable(week);
            month = week.getParent();
        } else {
            month = day.getParent();
        }
        fields[CronDialect.MONTH] = render(month);
        fields[CronDialect.DAY_OF_WEEK] = hasDayOfWeek ? render(day) : "?";

        CronExpression year = month.getParent();
        if (year instanceof Year) {
            String yearPart = render(year);
            fields[CronDialect.YEAR] = "*".equals(yearPart) ? null : yearPart;
        }
        return fields;
    }

    /**
     * Renders one field of the expression. Not every expression maps onto cron syntax - a day of
     * the year or a week of the year has no cron field to live in - and such a node is reported
     * rather than silently printed in the wrong place.
     */
    private static String render(CronExpression node) {
        checkRenderable(node);
        return node.toCronString();
    }

    private static void checkRenderable(CronExpression node) {
        if (!node.supportCronString()) {
            throw new UnsupportedOperationException(node.getClass().getSimpleName()
                    + " cannot be represented as a cron expression");
        }
    }

    public static String saveAsTmpFile(CronExpression cronExpression) throws IOException {
        String tmpPath = System.getProperty("java.io.tmpdir");
        String slash = File.separator;
        if (!tmpPath.endsWith(slash)) {
            tmpPath = tmpPath + slash;
        }
        String filePath = tmpPath + UUID.randomUUID().toString();
        saveAsFile(cronExpression, filePath);
        return filePath;
    }

    public static void saveAsFile(CronExpression cronExpression, String filePath)
            throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(new File(filePath).toPath())) {
            saveAs(cronExpression, outputStream);
        }
    }

    public static void saveAs(CronExpression cronExpression, OutputStream outputStream)
            throws IOException {
        outputStream.write(toByteArray(cronExpression));
        outputStream.flush();
    }

    /**
     * Serializes the expression, including where it currently stands, into a snapshot that can be
     * stored and restored later - a BLOB column, typically.
     * <p>
     * The bytes carry a small header naming the snapshot format, so a snapshot written by an
     * incompatible build is rejected outright by {@link #load(byte[])} instead of being read back
     * as a half-populated object that only fails once the schedule is iterated.
     */
    public static byte[] toByteArray(CronExpression cronExpression) {
        return withHeader(SerializationUtils.serialize(cronExpression));
    }

    public static CronExpression loadFromFile(String filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(new File(filePath).toPath())) {
            return load(inputStream);
        }
    }

    public static CronExpression load(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return load(baos.toByteArray());
        }
    }

    /**
     * Restores a snapshot written by {@link #toByteArray(CronExpression)}.
     *
     * @throws SerializationException when the bytes are not a cronsmith snapshot, or were written
     *         in a format this build cannot read
     */
    public static CronExpression load(byte[] bytes) {
        return SerializationUtils.deserialize(stripHeader(bytes));
    }

    /*
     * Snapshot format: a four byte marker, then the format version, then the serialized expression.
     * The version is bumped whenever the stored shape changes, which is what turns "silently reads
     * back wrong" into "refuses to read".
     */
    private static final byte[] SNAPSHOT_MAGIC = {'C', 'R', 'S', 'M'};
    private static final int SNAPSHOT_HEADER_LENGTH = SNAPSHOT_MAGIC.length + 2;

    /** Version of the snapshot format this build writes and reads. */
    public static final int SNAPSHOT_VERSION = 1;

    private static byte[] withHeader(byte[] payload) {
        byte[] snapshot = new byte[SNAPSHOT_HEADER_LENGTH + payload.length];
        System.arraycopy(SNAPSHOT_MAGIC, 0, snapshot, 0, SNAPSHOT_MAGIC.length);
        snapshot[SNAPSHOT_MAGIC.length] = (byte) (SNAPSHOT_VERSION >>> 8);
        snapshot[SNAPSHOT_MAGIC.length + 1] = (byte) SNAPSHOT_VERSION;
        System.arraycopy(payload, 0, snapshot, SNAPSHOT_HEADER_LENGTH, payload.length);
        return snapshot;
    }

    private static byte[] stripHeader(byte[] snapshot) {
        if (snapshot == null || snapshot.length < SNAPSHOT_HEADER_LENGTH) {
            throw new SerializationException("Not a cronsmith snapshot: too short");
        }
        for (int i = 0; i < SNAPSHOT_MAGIC.length; i++) {
            if (snapshot[i] != SNAPSHOT_MAGIC[i]) {
                throw new SerializationException("Not a cronsmith snapshot: bad marker");
            }
        }
        int version = ((snapshot[SNAPSHOT_MAGIC.length] & 0xFF) << 8)
                | (snapshot[SNAPSHOT_MAGIC.length + 1] & 0xFF);
        if (version != SNAPSHOT_VERSION) {
            throw new SerializationException("Snapshot was written in format v" + version
                    + ", this build reads v" + SNAPSHOT_VERSION + "; rebuild it from its definition");
        }
        return Arrays.copyOfRange(snapshot, SNAPSHOT_HEADER_LENGTH, snapshot.length);
    }

}
