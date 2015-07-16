@echo off
for /f "usebackq tokens=2 delims=:" %%a in (`ipconfig ^| find "IPv4"`) do (set result=%%a)

:: To lop off extra space in front of IPv4 number, end with:
::do (set result=%a& echo %result:~1%)
:: Alternative: for /f "tokens=2 delims=:" %a in ('ipconfig ^| findstr /C:"IPv4 Address"') do echo %a
:: still need to remove space up front for that too.

:: removes extra space in front
set result=%result:~1%

:: The above produces the IPv4 number, e.g. 100.200.300.45
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

:: Echo a vertical bar up front, regardless of whether the result is empty
echo ^|%result%