
@echo off

:: If you wish to use a JDK instead of the provided JRE then please use the line below
:: e.g. set _JDK_HOME=C:\Program Files\Java\jdk1.6.0_10
set _JDK_HOME=

pushd "%CD%"
CD /D "%~dp0"
set GAILANG=en

set PROGNAME=Greenstone Administrator Interface 
set PROGABBR=GAI

echo.
if "%GLILANG%" == "en" echo %PROGNAME% (%PROGABBR%)
if "%GLILANG%" == "en" echo Copyright (C) 2005, New Zealand Digital Library Project, University Of Waikato
if "%GLILANG%" == "en" echo %PROGABBR% comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt
if "%GLILANG%" == "en" echo This is free software, and you are welcome to redistribute it

::  ---- Determine GSDL3HOME ----
:setvars
set GSDL3PATH=

:: Some users may set the above line manually, or it may be set as an argument
if not "%GSDL3PATH%" == "" goto setgsdl3
	:: Check the environment variable first
	if not "%GSDL3SRCHOME%" == "" set GSDL3PATH=%GSDL3SRCHOME%
	if not "%GSDL3SRCHOME%" == "" goto setgsdl3
		
		if "%GSDL3SRCHOME%" == "" set GSDL3PATH="%CD%"

cd ..
call gs3-setup.bat
cd admin

:setgsdl3
:: Setup Greenstone3, unless it has already been done
if "%GSDL3HOME%" == "" call "%GSDL3PATH%\gs3-setup.bat" SetEnv > nul
set GSDL3PATH=

IF NOT "%_JDK_HOME%" == "" SET JAVA_HOME=%_JDK_HOME%
IF NOT "%_JDK_HOME%" == "" SET CLASSPATH=%_JDK_HOME%\lib\tools.jar;%CLASSPATH%
IF NOT "%_JDK_HOME%" == "" SET RUNJAVA=%_JDK_HOME%\bin\java.exe

echo gsdl3srchome: %GSDL3SRCHOME%
echo gsdl3srchome: %GSDL3HOME%
echo javahome: %JAVA_HOME%
echo classpath: %CLASSPATH%
echo.

:startup
java -cp "%CLASSPATH%" org.greenstone.admin.GAI "%GSDL3SRCHOME%" "%GSDL3HOME%"

:exit
echo.
popd
:done
:: ---- Clean up ----