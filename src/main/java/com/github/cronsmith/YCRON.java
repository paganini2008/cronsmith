package com.github.cronsmith;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.cronsmith.antlr.YCronExpressionLexer;
import com.github.cronsmith.antlr.YCronExpressionParser;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronType;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.DayOfWeek;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.parser.ycron.YCronExpressionContext;
import com.github.cronsmith.parser.ycron.YCronParserException;
import com.github.cronsmith.parser.ycron.YCronSyntaxErrorListener;

/**
 * The year-based cron extension - cronsmith only. Where {@link CRON} renders the traditional,
 * month-based line, YCRON renders its year-based sibling.
 *
 * <p>
 * Field order, seven positions:
 *
 * <pre>
 *   &lt;sec&gt; &lt;min&gt; &lt;hour&gt; &lt;day-of-week&gt; &lt;week-of-year&gt; &lt;day-of-year&gt; [&lt;year&gt;]
 * </pre>
 *
 * <p>
 * Two mutually exclusive ways to pick the date, mirroring how traditional cron plays day-of-month
 * against day-of-week with {@code ?}:
 * <ul>
 * <li><b>day-of-week + week-of-year</b> travel together - "Monday of ISO week 20". The day-of-year
 * field is then {@code ?}.</li>
 * <li><b>day-of-year</b> stands alone - "the 100th day of the year". Both day-of-week and
 * week-of-year are then {@code ?}.</li>
 * </ul>
 *
 * <p>
 * Everything below the date (hour / minute / second) and the optional trailing year carry the same
 * meaning as in {@link CRON}; an absent or {@code *} year means every year. This class is kept
 * wholly separate from {@link CRON} so the traditional path is never touched.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public abstract class YCRON {

    /** Field positions in the year-based line. */
    private static final int SECOND = 0;
    private static final int MINUTE = 1;
    private static final int HOUR = 2;
    private static final int DAY_OF_WEEK = 3;
    private static final int WEEK_OF_YEAR = 4;
    private static final int DAY_OF_YEAR = 5;
    private static final int YEAR = 6;
    private static final int FIELD_COUNT = 7;

    /** Placeholder for whichever date field is not in force, as {@code ?} in traditional cron. */
    private static final String NA = "?";

    /**
     * Render a year-based expression as a YCRON string. Only meaningful for expressions whose
     * {@link CronExpression#getCronType()} is {@link CronType#YCRON}; the traditional path stays in
     * {@link CRON}.
     */
    /**
     * Parses a year-based (YCRON) expression - the mirror of {@link CRON#parse(String)}, on its own
     * grammar and its own parser so the traditional path is untouched.
     *
     * <pre>
     *   &lt;sec&gt; &lt;min&gt; &lt;hour&gt; &lt;day-of-week&gt; &lt;week-of-year&gt; &lt;day-of-year&gt; [&lt;year&gt;]
     * </pre>
     */
    public static CronExpression parse(String ycronExpression) {
        if (ycronExpression == null || ycronExpression.trim().length() == 0) {
            throw new YCronParserException("Empty ycron expression");
        }
        CharStream input = CharStreams.fromString(ycronExpression.trim());
        YCronExpressionLexer lexer = new YCronExpressionLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(YCronSyntaxErrorListener.INSTANCE);
        YCronExpressionParser parser = new YCronExpressionParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(YCronSyntaxErrorListener.INSTANCE);
        ParseTree tree = parser.ycron();
        return new YCronExpressionContext().visit(tree);
    }

    public static String toYCronString(CronExpression cronExpression) {
        String[] fields = toYCronFields(cronExpression);
        StringBuilder sb = new StringBuilder();
        for (int i = SECOND; i <= YEAR; i++) {
            if (fields[i] == null) {
                // Unrestricted year: leave it off, an absent year means every year.
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(fields[i]);
        }
        return sb.toString();
    }

    /**
     * Break a year-based expression into its seven fields, in the order the YCRON line prints them.
     * The last entry is {@code null} when the schedule places no restriction on the year.
     */
    public static String[] toYCronFields(CronExpression cronExpression) {
        CronExpression copy = cronExpression.copy();
        // A partial node (a bare week-of-year, day-of-year, hour ...) is completed downward with
        // the same zero defaults CRON uses, so a lone tier still renders a whole line.
        if (copy instanceof Week) {
            copy = ((Week) copy).Mon().at(0, 0, 0);
        } else if (copy instanceof Day) {
            copy = ((Day) copy).at(0, 0, 0);
        } else if (copy instanceof Hour) {
            copy = ((Hour) copy).at(0, 0);
        } else if (copy instanceof Minute) {
            copy = ((Minute) copy).second(0);
        }

        final String[] fields = new String[FIELD_COUNT];
        CronExpression second = copy;
        CronExpression minute = second.getParent();
        CronExpression hour = minute.getParent();
        fields[SECOND] = render(second);
        fields[MINUTE] = render(minute);
        fields[HOUR] = render(hour);

        CronExpression date = hour.getParent();
        CronExpression year;
        if (date instanceof DayOfWeek) {
            // day-of-week + week-of-year travel together; day-of-year steps aside.
            CronExpression dayOfWeek = date;
            CronExpression weekOfYear = dayOfWeek.getParent();
            fields[DAY_OF_WEEK] = render(dayOfWeek);
            fields[WEEK_OF_YEAR] = render(weekOfYear);
            fields[DAY_OF_YEAR] = NA;
            year = weekOfYear.getParent();
        } else {
            // day-of-year stands alone; day-of-week and week-of-year step aside.
            CronExpression dayOfYear = date;
            fields[DAY_OF_WEEK] = NA;
            fields[WEEK_OF_YEAR] = NA;
            fields[DAY_OF_YEAR] = render(dayOfYear);
            year = dayOfYear.getParent();
        }

        String yearPart = render(year);
        fields[YEAR] = "*".equals(yearPart) ? null : yearPart;
        return fields;
    }

    /**
     * Render one field. Unlike {@link CRON}, YCRON owns the year-based tiers and calls their
     * {@code toCronString()} directly - they intentionally report
     * {@link CronExpression#supportCronString()} as {@code false} so the traditional path keeps
     * refusing them, while YCRON knows how to place them.
     */
    private static String render(CronExpression node) {
        return node.toCronString();
    }

}
