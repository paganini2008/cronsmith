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
    
dayOfMonthField
    : rangeWithStep 
    | range
    | '*/' INT  
    | INT '/' INT   
    | '*'     
    | INT 'W'        
    | INT      
    | '?' 
    | 'LW'      
    | 'L'
    | 'L-' INT    
    ;

monthField
    : rangeWithStep
    | range
    | '*/' INT 
    | INT '/' INT    
    | '*'             
    | INT      
    | monthRangeWithStep
    | monthRange
    | monthName
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
    | dayOfWeekName
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
yearRangeWithStep : INT_YEAR '-' INT_YEAR '/' INT  ;   

range         : INT '-' INT ;
yearRange     : INT_YEAR '-' INT_YEAR ;   

weekdayRangeWithStep : dayOfWeekName '-' dayOfWeekName '/' INT ;
monthRangeWithStep : monthName '-' monthName '/' INT ;


weekdayRange  : dayOfWeekName ('-' dayOfWeekName)? (',' dayOfWeekName ('-' dayOfWeekName)? )* ;
monthRange  : monthName ('-' monthName)? (',' monthName ('-' monthName)? )* ;

dayOfWeekName    : 'SUN' | 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' ;
monthName        : 'JAN' | 'FEB' | 'MAR' | 'APR' | 'MAY' | 'JUN' | 'JUL' | 'AUG' | 'SEP' | 'OCT' | 'NOV' | 'DEC'  ;

INT_YEAR : [2][0-9][0-9][0-9] ;
INT : [0-9]+ ;
INT_L : [0-9]+ 'L' ;
SPACE : [ \t]+ -> skip ;
