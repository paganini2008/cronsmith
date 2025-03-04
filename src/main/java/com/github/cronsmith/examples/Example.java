/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.cronsmith.examples;

import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;

/**
 * 
 * Example
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public abstract class Example {

	// */5 * * * * ?
	public static CronExpression getCron1() {
		return CronExpressionUtils.everySecond(5);
	}

	// 0 */2 * * * ?
	public static CronExpression getCron2() {
		return CronExpressionUtils.everyMinute(2);
	}

	// 0 26,29,33 * * * ?
	public static CronExpression getCron3() {
		return CronExpressionUtils.everyHour().minute(26).andMinute(29).andMinute(33);
	}

	// 0 * 14 * * ?
	public static CronExpression getCron4() {
		return CronExpressionUtils.everyDay().hour(14).everyMinute();
	}

	// 0 0-10 15 * * ?
	public static CronExpression getCron5() {
		return CronExpressionUtils.everyDay().hour(15).minute(0).toMinute(10);
	}

	// 0 0 23 * * ?
	public static CronExpression getCron6() {
		return CronExpressionUtils.everyDay().at(23, 0);
	}

	// 0 15 12 * * ?
	public static CronExpression getCron7() {
		return CronExpressionUtils.everyDay().hour(12).minute(15);
	}

	// 0 0 0,13,18,21 * * ?
	public static CronExpression getCron8() {
		return CronExpressionUtils.hour(13).andHour(18).andHour(21);
	}

	// 0 15 10 ? * 6L
	public static CronExpression getCron9() {
		return CronExpressionUtils.everyMonth().lastWeek().Fri().at(10, 15);
	}

	// 0 15 10 ? * MON-FRI
	public static CronExpression getCron10() {
		return CronExpressionUtils.everyWeek().Mon().toFri().at(10, 15, 0);
	}

	// 0 0/5 12,18 * * ?
	public static CronExpression getCron11() {
		return CronExpressionUtils.hour(12).andHour(18).everyMinute(5);
	}

	// 0 30 23 L * ?
	public static CronExpression getCron12() {
		return CronExpressionUtils.everyMonth().lastDay().at(23, 30);
	}

	// 0 10,20,30 12 ? 7-11 6L 2021-2025
	public static CronExpression getCron13() {
		return CronExpressionUtils.year(2021).toYear(2025).Aug().toDec().lastWeek().Fri().hour(12).minute(10).andMinute(20).andMinute(30);
	}

	// 0 10 23 ? * 6#3
	public static CronExpression getCron14() {
		return CronExpressionUtils.everyMonth().week(3).Fri().at(23, 10);
	}

	// 0 15-50/2 0-6 10-28 * ?
	public static CronExpression getCron15() {
		return CronExpressionUtils.everyMonth().day(10).toDay(28).hour(0).toHour(6).minute(15).toMinute(50, 2);
	}

	public static void test6() {
		System.out.println(CRON.parse("*/5 * * * * ?"));
		System.out.println(CRON.parse("0 */2 * * * ?"));
		System.out.println(CRON.parse("0 15 10 LW * ?"));
		System.out.println(CRON.parse("0 0 12 10W * ?"));
		System.out.println(CRON.parse("0 15 10 ? * MON-FRI"));
		System.out.println(CRON.parse("0 26,29,33 * * * ?"));
		System.out.println(CRON.parse("0 15-50/2 0-6 10-28 * ?"));
		System.out.println(CRON.parse("0 10 23 ? * 6#3"));
		System.out.println(CRON.parse("0 10,20,30 12 ? 7-11 6L 2021-2025"));
	}

	public static void main(String[] args) throws Exception {
		test6();
	}

}
