grammar YCronExpression;

@header {
package com.github.cronsmith.antlr;
}

// The year-based (YCRON) line - a cronsmith-only extension, kept in its own grammar so the
// traditional CronExpression.g4 is never touched:
//
//   <sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]
//
// day-of-week + week-of-year travel together, day-of-year stands alone, and whichever side is
// idle is written '?', exactly as traditional cron plays day-of-month against day-of-week. The
// mutual-exclusion rule itself is enforced by the parser driver, not here.

ycron
    : second SPACE minute SPACE hour SPACE dayOfWeek SPACE weekOfYear SPACE dayOfYear (SPACE year)? SPACE? EOF
    ;


second     : secondField (',' secondField)* ;
minute     : minuteField (',' minuteField)* ;
hour       : hourField (',' hourField)* ;
dayOfWeek  : dayOfWeekField (',' dayOfWeekField)* ;
weekOfYear : weekOfYearField (',' weekOfYearField)* ;
dayOfYear  : dayOfYearField (',' dayOfYearField)* ;
year       : yearField (',' yearField)* ;


secondField
    : rangeWithStep
    | range
    | '*/' INT
    | INT '/' INT
    | '*'
    | INT
    ;

minuteField
    : rangeWithStep
    | range
    | '*/' INT
    | INT '/' INT
    | '*'
    | INT
    ;

hourField
    : rangeWithStep
    | range
    | '*/' INT
    | INT '/' INT
    | '*'
    | INT
    ;

dayOfWeekField
    : rangeWithStep
    | range
    | '*/' INT
    | INT '/' INT
    | INT_L
    | INT '#' INT
    | INT
    | '*'
    | weekdayRangeWithStep
    | weekdayRange
    | dayOfWeekName '#' INT
    | dayOfWeekNameLast
    | dayOfWeekNameWithStep
    | dayOfWeekName
    | '?'
    ;

// Week of the ISO year, 1-53. 'L' is the year's last ISO week.
weekOfYearField
    : rangeWithStep
    | range
    | INT '/' INT
    | 'L'
    | INT
    | '?'
    ;

// Day of the year, 1-366. Mirrors the day-of-month vocabulary (L, L-n, W, LW, ranges, steps) but
// anchored to the year: 'L' is 31 December, 'LW' the year's last weekday, '100W' the weekday
// nearest the 100th day, 'L-n' the nth day before year end.
dayOfYearField
    : rangeWithStep
    | INT '-' INT 'W' ('/' INT)?
    | INT '-' 'LW' ('/' INT)?
    | INT '-' 'L' ('/' INT)?
    | range
    | '*/' INT
    | INT '/' INT
    | INT 'W'
    | 'LW'
    | 'L-' INT
    | 'L'
    | INT
    | '?'
    ;

yearField
    : yearRangeWithStep
    | yearRange
    | INT_YEAR '/' INT
    | '*/' INT
    | '*'
    | INT_YEAR
    ;

rangeWithStep : INT '-' INT '/' INT ;
yearRangeWithStep : INT_YEAR '-' INT_YEAR '/' INT ;

range     : INT '-' INT ;
yearRange : INT_YEAR '-' INT_YEAR ;

weekdayRangeWithStep : dayOfWeekName '-' dayOfWeekName '/' INT ;

dayOfWeekNameLast : dayOfWeekName 'L' ;
dayOfWeekNameWithStep : dayOfWeekName '/' INT ;

weekdayRange : dayOfWeekName ('-' dayOfWeekName)? (',' dayOfWeekName ('-' dayOfWeekName)? )* ;

dayOfWeekName : 'SUN' | 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' ;

INT_YEAR : [2][0-9][0-9][0-9] ;
INT : [0-9]+ ;
INT_L : [0-9]+ 'L' ;
SPACE : [ \t]+ ;
