grammar CronExpression;

@header {
package com.github.cronsmith.antlr;
}

cron
    : second SPACE minute SPACE hour SPACE dayOfMonth SPACE month SPACE dayOfWeek SPACE? year? EOF
    ;


second   : secondField (',' secondField)* ;
minute   : minuteField (',' minuteField)* ;
hour     : hourField (',' hourField)* ;
dayOfMonth : dayOfMonthField (',' dayOfMonthField)* ;
month    : monthField (',' monthField)* ;
dayOfWeek : dayOfWeekField (',' dayOfWeekField)* ;
year     : yearField (',' yearField)* ;


secondField
    : rangeWithStep  
    | range          
    | INT '/' INT   
    | '*'            
    | INT      
    ;

minuteField
    : rangeWithStep  
    | range          
    | INT '/' INT   
    | '*'            
    | INT      
    ;
    
hourField
    : rangeWithStep  
    | range          
    | INT '/' INT   
    | '*'            
    | INT      
    ;
    
dayOfMonthField
    : rangeWithStep 
    | range
    | '*'            
    | INT      
    | '?' 
    | 'LW'      
    | 'L' INT? 
    | INT 'W'     
    | INT '/' INT   
    ;

monthField
    : rangeWithStep
    | range
    | '*'            
    | INT      
    | monthName
    | monthNameRange
    ;
    
dayOfWeekField
    : rangeWithStep
    | range
    | '*'    
    | INT '#' INT 
    | INT 'L'              
    | INT      
    | weekdayRange
    | dayOfWeekName '#' INT
    | '?'
    ;
    
yearField
    : rangeWithStep  
    | range          
    | INT '/' INT   
    | '*'            
    | INT      
    ;

rangeWithStep : INT '-' INT '/' INT ; 
range         : INT '-' INT ;

weekdayRange  : dayOfWeekName ('-' dayOfWeekName)? (',' dayOfWeekName ('-' dayOfWeekName)? )* ;
monthNameRange  : monthName ('-' monthName)? (',' monthName ('-' monthName)? )* ;

dayOfWeekName    : 'SUN' | 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' ;
monthName        : 'JAN' | 'FEB' | 'MAR' | 'APR' | 'MAY' | 'JUN' | 'JUL' | 'AUG' | 'SEP' | 'OCT' | 'NOV' | 'DEC'  ;

INT : [0-9]+ ;
INT_L : [0-9]+ 'L' ;
SPACE : [ \t]+ -> skip ;
