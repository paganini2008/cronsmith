
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
import java.time.temporal.ChronoUnit;
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

/**
 * 
 * CRON is the entry class of cronsmith library. Start your work from here.
 * 
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class CRON {

    public static CronExpression atFuture(LocalDate ld) {
        if (ld.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Past date: " + ld);
        }
        return atFuture(ld.atStartOfDay());
    }

    public static CronExpression atFuture(LocalDateTime ldt) {
        if (ldt.isBefore(LocalDateTime.now())) {
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

    public static CronExpression parse(String cronExpression) {
        CharStream input = CharStreams.fromString(cronExpression);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        CronExpressionContext context = new CronExpressionContext();
        return context.visit(tree);
    }

    public static String printParseTree(String cronExpression) {
        CharStream input = CharStreams.fromString(cronExpression);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
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

    public static String toCronString(CronExpression cronExpression) {
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

        final StringBuilder cron = new StringBuilder();
        CronExpression second = copy;
        CronExpression minute = second.getParent();
        CronExpression hour = minute.getParent();
        cron.append(second.toCronString()).append(" ").append(minute.toCronString()).append(" ")
                .append(hour.toCronString()).append(" ");

        CronExpression day = hour.getParent();
        boolean hasDayOfWeek = false;
        if (day instanceof DayOfWeek) {
            hasDayOfWeek = true;
        }
        if (hasDayOfWeek) {
            cron.append("?").append(" ");
        } else {
            cron.append(day.toCronString()).append(" ");
        }

        CronExpression month;
        if (hasDayOfWeek && day.getParent() instanceof Week) {
            month = day.getParent().getParent();
        } else {
            month = day.getParent();
        }

        cron.append(month.toCronString()).append(" ");

        if (hasDayOfWeek) {
            cron.append(day.toCronString());
        } else {
            cron.append("?");
        }

        CronExpression year = month.getParent();
        if (year instanceof Year) {
            if (!"*".equals(year.toCronString())) {
                cron.append(" ").append(year.toCronString());
            }
        }
        return cron.toString();
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
        byte[] bytes = SerializationUtils.serialize(cronExpression);
        outputStream.write(bytes);
        outputStream.flush();
    }

    public static byte[] toByteArray(CronExpression cronExpression) {
        return SerializationUtils.serialize(cronExpression);
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
            byte[] data = baos.toByteArray();
            return SerializationUtils.deserialize(data);
        }
    }

    public static CronExpression load(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

}
