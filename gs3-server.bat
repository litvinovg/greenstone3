@echo off

echo Greenstone 3 Server
echo Copyright (C) 2006, New Zealand Digital Library Project, University Of Waikato
echo This software comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt
echo This is free software, and you are welcome to redistribute it

::  ---- Determine GSDL3HOME ----
:: Some users may set the above line manually
set GSDL3PATH=

:: The default location is the current directory
 if "%GSDL3PATH%" == "" set GSDL3PATH=.

:: Setup Greenstone, unless it has already been done
if "%GSDL3SRCHOME%" == "" call "%GSDL3PATH%\gs3-setup.bat" SetEnv > nul

set GSDL3PATH=

"%RUNJAVA%" -cp "%CLASSPATH%" org.greenstone.server.Server3 "%GSDL3SRCHOME%"




 