@echo off

rem This script will echo the IPv4 of this windows machine. E.g. 100.200.300.45
rem If passed -format-for-tomcat-context, it will echo the same with a pipe
rem symbol up front and all the . escaped with a "\". E.g. "|100\.200\.300\.45"

for /f "usebackq tokens=2 delims=:" %%a in (`ipconfig ^| find "IPv4"`) do (set result=%%a)

:: To lop off the single extra space in front of IPv4 number, end with:
::do (set result=%a& echo %result:~1%)
:: Alternative: for /f "tokens=2 delims=:" %a in ('ipconfig ^| findstr /C:"IPv4 Address"') do echo %a
:: still need to remove space up front for that too.

:: removes extra space in front
set result=%result:~1%

:: The above produces the IPv4 number, e.g. 100.200.300.45


rem Check if we're requested to format the IPv4 for the solr.xml tomcat context file
if [%1]==[] goto done
if not "%1"=="-format-for-tomcat-context" goto done

:: Still need to replace . with \.
:: http://scripts.dragon-it.co.uk/scripts.nsf/docs/batch-search-replace-substitute!OpenDocument&ExpandSection=3&BaseTarget=East&AutoFramed
:: Syntax: %string:SEARCH=REPLACE%
set result=%result:.=\.%

:: http://stackoverflow.com/questions/2541767/what-is-the-proper-way-to-test-if-variable-is-empty-in-a-batch-file-if-not-1
:: Make sure it is not the empty string or a string of spaces
:: Test by setting result to space
::set result= 
set result=%result: =%

::if not "%result%" == "" 
::set result=^|%result%

echo ^|%result%
goto fin


:done
:: Echo a vertical bar up front, regardless of whether the result is empty
echo %result%

:fin
