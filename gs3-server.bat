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

set USE_TMPDIR_FOR_TOMCAT=
for /F "tokens=1,2 delims==" %%G in (%GSDL3SRCHOME%\build.properties) do ( 
	if "%%G"=="gsdl3home.isreadonly" set USE_TMPDIR_FOR_TOMCAT=%%H
)

:: If the gsdl3.home readonly property is not already set to true, then
:: See if Greenstone3 web folder is writable
if "%USE_TMPDIR_FOR_TOMCAT%" == "false" (
  echo.
  echo Checking if the Greenstone3 web directory is writable ...
  (echo This is a temporary file. It is safe to delete it. > "%GSDL3HOME%\testing.tmp" ) 2>nul
  if exist "%GSDL3HOME%\testing.tmp" goto isWritable
)

:: Is read-only
  set gsdl3_writablehome=%TMP%\greenstone\web
  set opt_properties="-Dgsdl3home.isreadonly=true" -Dgsdl3.writablehome="%gsdl3_writablehome%"
  if "%USE_TMPDIR_FOR_TOMCAT%" == "false" echo ... no.
  echo Setting Greenstone3 web home writable area to be: %gsdl3_writablehome%

  goto runJava

:isWritable
  del "%GSDL3HOME%\testing.tmp"
  set opt_properties="-Dgsdl3.writablehome=%GSDL3HOME%"
  echo  ... yes.

:runJava

:: Before running the Java program, 
:: call ant target to initialize the gsdl3-writablehome area (if it doesn't already exist)
:: ... including the all important global.properties.

cmd /c ant.bat %opt_properties% configure-web


set GSDL3PATH=
set USE_TMPDIR_FOR_TOMCAT=

"%RUNJAVA%" -cp "%CLASSPATH%" %opt_properties% org.greenstone.server.Server3 "%GSDL3SRCHOME%"




 