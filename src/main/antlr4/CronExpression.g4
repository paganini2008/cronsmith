grammar CronExpression;

@header {
package com.github.cronsmith.antlr;
}

cron
    : second SPACE minute SPACE hour SPACE dayOfMonth SPACE month SPACE dayOfWeek SPACE? year? EOF
    ;

second     : fieldList ;
minute     : fieldList ;
hour       : fieldList ;
dayOfMonth : fieldList | '?' | 'L' | INT 'W';
month      : fieldList ;
dayOfWeek  : fieldList | '?' | weekdayWithHash | INT_L;
year       : fieldList ;

fieldList  : field (',' field)* ;
field      : '*' 
           | INT 
           | STAR_SLASH 
           | rangeWithStep 
           | range
           | weekdayRange 
           | INT_SLASH
           | 'L'
           | INT 'L'
           | INT 'W'
           ;

range      : INT '-' INT ;
rangeWithStep : INT '-' INT '/' INT ;
STAR_SLASH : '*' '/' INT ;
INT_SLASH  : INT '/' INT ;
weekdayRange : WEEKDAY ('-' WEEKDAY)?;
weekdayWithHash : WEEKDAY '#' INT ;

WEEKDAY    : 'SUN' | 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' ;

INT : [0-9]+ ;
INT_L : [0-9]+ 'L' ;
SPACE : [ \t]+ -> skip ;
