@echo off

setlocal enabledelayedexpansion

set java_min_version=1.5.0_00

if exist gs3-setup.bat goto start
  echo This script must be run from within the Greenstone 3 home directory
  goto exit

:start
:: ---- Set some relevant environment variables ----

:: this is the default
:: greenstone3 (!GSDL3SRCHOME!)
:: +-- web (!GSDL3HOME!)
:: +-- packages
::      +-- tomcat (!TOMCAT_HOME!)
::      +-- ant (!ANT_HOME!)

:: set GSDLSRCHOME to the current directory
cd | winutil\setvar.exe GSDL3SRCHOME > !TMP!\setgsdl3.bat
call !TMP!\setgsdl3.bat
del !TMP!\setgsdl3.bat

:: set GSDLHOME to the 'web' subdirectory
set GSDL3HOME=!GSDL3SRCHOME!\web

:: change if using external tomcat or ant
set TOMCAT_HOME=!GSDL3SRCHOME!\packages\tomcat
if exist "!GSDL3SRCHOME!\packages\ant\*.*" set ANT_HOME=!GSDL3SRCHOME!\packages\ant

:: other important environment variables
set GSDLOS=windows

:: ---- Set the CLASSPATH and PATH environment variables ----
if "!GS_CP_SET!" == "yes" goto skipSetCp
set CLASSPATH=!GSDL3HOME!\WEB-INF\classes;!GSDL3SRCHOME!\resources\java;!GSDL3SRCHOME!\cp.jar;!CLASSPATH!;
set PATH=!PATH!;!GSDL3SRCHOME!\bin;!GSDL3SRCHOME!\bin\script;!GSDL3SRCHOME!\lib\jni;!ANT_HOME!\bin

:: Override Imagemagick and Ghostscript paths to the bundled applications shipped with greenstone if they exists otherwise use default environment variables.
if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\gsdll32.dll" set GS_DLL=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\gsdll32.dll
if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\lib\*.*" set GS_LIB=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\lib
if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\*.*" set PATH=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin;!PATH!
if exist "!GSDL3SRCHOME!\gs2build\bin\windows\imagemagick\*.*" set PATH=!GSDL3SRCHOME!\gs2build\bin\windows\imagemagick;!PATH!

:: a little dynamic set cp stuff
if exist "!TMP!\setcp.bat" del "!TMP!\setcp.bat"

:: http://ss64.com/nt/for_cmd.html, how to deal with spaces in the file list in a for command
:: Note that TOMCAT_HOME\common\endorsed only exists for Tomcat 5, not Tomcat 6
:: (where it contains xercesImpl.jar and xml-apis.jar which aren't there in Tomcat 6)
if exist "!TOMCAT_HOME!\common\endorsed\*.jar" for /f %%j in ('dir/b ^"!TOMCAT_HOME!\common\endorsed\*.jar^"') do echo set CLASSPATH=%%CLASSPATH%%;%%TOMCAT_HOME%%\common\endorsed\%%j>> !TMP!\setcp.bat
for /f %%j in ('dir/b ^"!TOMCAT_HOME!\lib\*.jar^"') do echo set CLASSPATH=%%CLASSPATH%%;%%TOMCAT_HOME%%\lib\%%j>> !TMP!\setcp.bat

if exist !TMP!\setcp.bat call !TMP!\setcp.bat
if exist !TMP!\setcp.bat del !TMP!\setcp.bat

set GS_CP_SET=yes
:skipSetCp

:: ---- if gs2build is there, run its setup.bat file ----

if exist gs2build\setup.bat (
  echo.
  echo Running gs2build\setup.bat
  cd gs2build
  call setup.bat 
  cd ..
)

:: ---- Check for any setup files in ext or local folders ----
if exist ext (
    for /D %%e IN ("ext/*") do (
        cd ext\%%e
        if EXIST setup.bat call setup.bat
        cd ..\..
    )
)


if exist local\gs3-setup.bat (
  echo.
  echo Running local\gs3-setup.bat
  cd local
  call gs3-setup.bat 
  cd ..
)

:: ---- Search for java ----
set JAVA_MIN_VERSION=1.5.0_00
set HINT=!CD!\packages\jre
::if search4j is present, use it
set FOUNDJAVAHOME=
set RUNJAVA=
if exist bin\search4j.exe (
  for /F "tokens=*" %%r in ('bin\search4j.exe -d -p "!HINT!" -m !JAVA_MIN_VERSION!') do set FOUNDJAVAHOME=%%r
  for /F "tokens=*" %%r in ('bin\search4j.exe -r -p "!HINT!" -m !JAVA_MIN_VERSION!') do set FOUNDJREHOME=%%r
)

if DEFINED FOUNDJAVAHOME  (
  set JAVA_HOME=!FOUNDJAVAHOME!
  set PATH=!FOUNDJAVAHOME!\bin;!PATH!
  set RUNJAVA=!FOUNDJAVAHOME!\bin\java.exe
  goto summaryThenEnd
)

if DEFINED FOUNDJREHOME (
  set JRE_HOME=!FOUNDJREHOME!
  set PATH=!FOUNDJREHOME!\bin;!PATH!
  set RUNJAVA=!FOUNDJREHOME!\bin\java.exe
  goto summaryThenEnd
)

if exist "!HINT!\bin\java.exe" (
  set JAVA_HOME=!HINT!
  set PATH=!JAVA_HOME!\bin;!PATH!
  set RUNJAVA=!JAVA_HOME!\bin\java.exe
  goto summaryThenEnd
)

if exist "!JAVA_HOME!\bin\java.exe" (
  set PATH=!JAVA_HOME!\bin;!PATH!
  set RUNJAVA=!JAVA_HOME!\bin\java.exe
  echo Using java at !JAVA_HOME!
  echo WARNING: Greenstone has not checked the version number of this java installation
  echo          The source distribution of Greenstone3 requires java 1.5 or greater
  echo          SVN users may still use java 1.4
  goto summaryThenEnd
)

if exist "!JRE_HOME!\bin\java.exe" (
  set PATH=!JRE_HOME!\bin;!PATH!
  set RUNJAVA=!JRE_HOME!\bin\java.exe
  echo Using java at !JRE_HOME!
  echo WARNING: Greenstone has not checked the version number of this Java installation
  echo          The source distribution of Greenstone3 requires Java 1.5 or greater
  echo          SVN users may still use java 1.4
  goto summaryThenEnd
)

echo ERROR: Failed to locate java
echo        Please set JAVA_HOME or JRE_HOME to point to an appropriate java
goto end

:summaryThenEnd

echo GSDL3SRCHOME : !GSDL3SRCHOME!
echo GSDL3HOME    : !GSDL3HOME!
echo JAVA         : !RUNJAVA!

if "!ANT_HOME!" == "" (
   echo.
   echo ANT_HOME is not yet set.
   echo Please make sure you have Ant version 1.7.1 or higher installed
   echo Then set ANT_HOME to the ant installation folder
   echo and add the path to its bin folder to the PATH environment variable
) else (
   echo ANT_HOME     : !ANT_HOME!
)
echo.

:: End localisation of variables that started with the set local/set enabledelayedexpansion command
:: Restore global variables that would otherwise be lost at script's end due to their having been initialised in a 
:: set local/set enabledelayedexpansion section. See http://ss64.com/nt/endlocal.html
endlocal & set RUNJAVA=%RUNJAVA%& set PATH=%PATH%& set GSDL3HOME=%GSDL3HOME%& set GSDL3SRCHOME=%GSDL3SRCHOME%& set JAVA_HOME=%JAVA_HOME%& set JRE_HOME=%JRE_HOME%& set ANT_HOME=%ANT_HOME%& set CLASSPATH=%CLASSPATH%

:end