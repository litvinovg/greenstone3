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


:: See if Greenstone3 web folder is writable
  echo.
  echo Checking if the Greenstone3 web directory is writable ...
  (echo This is a temporary file. It is safe to delete it. > "%GSDL3HOME%\testing.tmp" ) 2>nul
  if exist "%GSDL3HOME%\testing.tmp" goto isWritable

:: Is read-only
  set gsdl3_writablehome=%TMP%\greenstone\web
  set opt_properties=-Dgsdl3home.isreadonly=true -Dgsdl3.writablehome=%gsdl3_writablehome%
  echo ... no.
  echo Setting Greenstone3 web home writable area to be: %gsdl3_writablehome%
  goto runJava

:isWritable
  del "%GSDL3HOME%\testing.tmp"
  set opt_properties=
  echo  ... yes.

:runJava

set GSDL3PATH=

"%RUNJAVA%" -cp "%CLASSPATH%" %opt_properties% org.greenstone.server.Server3 "%GSDL3SRCHOME%"




 