/**
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.cronsmith;


import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.DayOfWeek;
import com.github.cronsmith.cron.EveryYear;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.CronExpressionContext;

/**
 * 
 * @Description: CRON
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public abstract class CRON {

    public static TemporalField NON_ISO_WEEKS_FIELD =
            WeekFields.of(java.time.DayOfWeek.MONDAY, 7).dayOfWeek();

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
        if (!(year instanceof EveryYear)) {
            cron.append(" ").append(year.toCronString());
        }
        return cron.toString();
    }

    public static CronExpression parse(String cronString) {
        CharStream input = CharStreams.fromString(cronString);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        System.out.println(tree);
        CronExpressionContext context = new CronExpressionContext();
        return context.visit(tree);
    }

    public static void main(String[] args) {
        String cronString =
                "2,3,4,5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? 1-8,11,DEC TUE#2,WED#3,5L 2025-2030,2035/2";
        System.out.println(parse(cronString));
    }

}
