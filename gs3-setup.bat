@echo off

setlocal enabledelayedexpansion

set java_min_version=1.5.0_00

if exist gs3-setup.bat goto prelim
  echo This script must be run from within the Greenstone 3 home directory
  goto done

:prelim
if "!GSDL3SRCHOME!" == "" goto start
if "!GSDL3SRCHOME!" == "!CD!" (
	echo Your environment is already set up for Greenstone3
	goto done
) else (
    echo Your environment was set up for Greenstone 3 in !GSDL3SRCHOME!.
    echo Overwriting that set up for the current Greenstone 3 in !CD!.
)

:start
:: ---- Set some relevant environment variables ----

:: this is the default
:: greenstone3 (!GSDL3SRCHOME!)
:: +-- web (!GSDL3HOME!)
:: +-- packages
::      +-- tomcat (!TOMCAT_HOME!)
::      +-- ant (!ANT_HOME!)

:: set GSDL3SRCHOME to the current directory
cd | winutil\setvar.exe GSDL3SRCHOME > !TMP!\setgsdl3.bat
call !TMP!\setgsdl3.bat
del !TMP!\setgsdl3.bat

:: set GSDL3HOME to the 'web' subdirectory
set GSDL3HOME=!GSDL3SRCHOME!\web
set WEB_CONTAINING_CLASSES=!GSDL3SRCHOME!\web
:: set GSDL3HOME to any web.home property provided, and create that folder if it doesn't exist
:: Replace forward slashes in web.home with back slashes 
:: http://scripts.dragon-it.co.uk/scripts.nsf/docs/batch-search-replace-substitute!OpenDocument&ExpandSection=3&BaseTarget=East&AutoFramed
if exist "!GSDL3SRCHOME!\build.properties" for /F "usebackq tokens=1,2 delims==" %%G in ("!GSDL3SRCHOME!\build.properties") do ( 
	if "%%G"=="web.home" (
		set GSDL3HOME=%%H
		set GSDL3HOME=!GSDL3HOME:/=\!
		set WEB_CONTAINING_CLASSES=!GSDL3HOME!		
		if not exist "!GSDL3HOME!" cmd /c "!GSDL3SRCHOME!\userweb.cmd"
		goto foundwebhome
	)	
)

:foundwebhome
:: Whatever the web directory is, it should contain the WEB-INF\classes folder, else go back to using default for this
:: The WEB-INF\classes folder will be absent in a userweb folder, but will be present if GSDL3HOME=GSDL3SRCHOME\web
:: or if web.home points to GS3 as a webapp inside tomcat
if not exist "!GSDL3HOME!\WEB-INF\classes" set WEB_CONTAINING_CLASSES=!GSDL3SRCHOME!\web

:: change if using external tomcat or ant
set TOMCAT_HOME=!GSDL3SRCHOME!\packages\tomcat
if exist "!GSDL3SRCHOME!\packages\ant\*.*" set ANT_HOME=!GSDL3SRCHOME!\packages\ant

:: other important environment variables
set GSDLOS=windows

:: ---- Set the CLASSPATH and PATH environment variables ----
if "!GS_CP_SET!" == "yes" goto skipSetCp
set CLASSPATH=!WEB_CONTAINING_CLASSES!\WEB-INF\classes;!GSDL3SRCHOME!\resources\java;!GSDL3SRCHOME!\cp.jar;!CLASSPATH!;
set PATH=!GSDL3SRCHOME!\bin;!GSDL3SRCHOME!\bin\script;!GSDL3SRCHOME!\lib\jni;!ANT_HOME!\bin;!PATH!

:: Override Imagemagick and Ghostscript paths to the bundled applications shipped with greenstone if they exists otherwise use default environment variables.
:: if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\gsdll32.dll" set GS_DLL=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\gsdll32.dll
:: if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\lib\*.*" set GS_LIB=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\lib
:: if exist "!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin\*.*" set PATH=!GSDL3SRCHOME!\gs2build\bin\windows\ghostscript\bin;!PATH!
:: if exist "!GSDL3SRCHOME!\gs2build\bin\windows\imagemagick\*.*" set PATH=!GSDL3SRCHOME!\gs2build\bin\windows\imagemagick;!PATH!

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
endlocal & set RUNJAVA=%RUNJAVA%& set PATH=%PATH%& set GSDLOS=%GSDLOS%& set GSDLHOME=%GSDLHOME%& set GSDL3HOME=%GSDL3HOME%& set GSDL3SRCHOME=%GSDL3SRCHOME%& set JAVA_HOME=%JAVA_HOME%& set JRE_HOME=%JRE_HOME%& set ANT_HOME=%ANT_HOME%& set CLASSPATH=%CLASSPATH%

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
        if EXIST gs3-setup.bat (
	  call gs3-setup.bat
	) else (
          if EXIST setup.bat call setup.bat
	)
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

setlocal enabledelayedexpansion

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


echo.
echo ********************************************************************

rem Check if any Java found matches the bitness of the Greenstone installation's binaries
rem The sort of output we want:
:: Installed GS as 32 bit
:: Detected java is 64 bit
:: Changing to use the GS bundled 32 bit jre
:: We've detected a mismatch, this will only affect MG/MGPP collections for searching and GDBM database collections

:: 1. What bit-ness are this Greenstone installation's binaries?
:: GNUfile: http://stackoverflow.com/questions/2689168/checking-if-file-is-32bit-or-64bit-on-windows
:: http://gnuwin32.sourceforge.net/packages/file.htm
:: Also http://stackoverflow.com/questions/4089641/programatically-determine-if-native-exe-is-32-bit-or-64-bit
:: http://stackoverflow.com/questions/2062020/how-can-i-tell-if-im-running-in-64-bit-jvm-or-32-bit-jvm-from-within-a-program
:: Messy way: http://superuser.com/questions/358434/how-to-check-if-a-binary-is-32-or-64-bit-on-windows

:: "%GSDLHOME%\bin\windows\GNUfile\bin\file.exe" "%GSDLHOME%\bin\windows\wvWare.exe"
:: But we'll test the bitness of gdbmjava.dll itself as it's guaranteed to be present in GS3 and is also dependent on JNI
:: No need to have the right bitness for GS2, since it doesn't use JNI

:: See https://ss64.com/nt/for_cmd.html for using batch FOR to loop against the results of another command.
:: Running
::		for /f "usebackq delims=" %%G IN (`"gs2build\bin\windows\GNUfile\bin\file.exe" gs2build\bin\windows\wvWare.exe`) do echo %%G
:: prints out the entire output, e.g.:
:: 		gs2build\bin\windows\wvWare.exe; PE32 executable for MS Windows (console) Intel 80386 32-bit
:: To just get the "PE32" part of that output, set the delimiter char to space and request only the 2nd token:
:: Note: Using call before the command to allow 2 sets of double quotes, see 
:: http://stackoverflow.com/questions/6474738/batch-file-for-f-doesnt-work-if-path-has-spaces
:: Could use shortfilenames, see http://stackoverflow.com/questions/10227144/convert-long-filename-to-short-filename-8-3-using-cmd-exe
for /f "usebackq tokens=2 delims= " %%G IN (`call "%GSDLHOME%\bin\windows\GNUfile\bin\file.exe" "%GSDL3SRCHOME%\lib\jni\gdbmjava.dll"`) do set bitness=%%G

if "%bitness%" == "PE32+" (
	set bitness=64
	echo The installed Greenstone is 64 bit
) else (
	if "%bitness%" == "PE32" (
		set bitness=32
		echo The installed Greenstone is 32 bit
	) else (
		echo WARNING: Greenstone installation is of unknown bitness. "%bitness%" is neither 32 nor 64 bit& goto bundledjre
		set bitness=UNKNOWN
	)
)

:: 2. What bitness are any JAVA_HOME else JRE_HOME found by search4j?
:: If you run the non-existent program "pinky" from batch or the DOS console, the exit value is 9009
:: The same must be true if java is not installed and therefore not found. echo %errorlevel% produces 9009
:: If java exists and is 32 bit, then running "java -d32 -version" has a return value of 1. echo %errorlevel% (1)
:: If java exists and is 64 bit, then running "java -d32 -version" has a return value of 0. echo %errorlevel% (0)

:testjavahome
:: http://www.robvanderwoude.com/errorlevel.php
:: https://ss64.com/nt/errorlevel.html
if DEFINED FOUNDJAVAHOME  (
	echo *** Testing bitness of JAVA_HOME found at !FOUNDJAVAHOME!:
	"!FOUNDJAVAHOME!\bin\java.exe" -d%bitness% -version 2> nul
	if !ERRORLEVEL! equ 1 echo *** The detected system JDK java is an incompatible bit architecture& goto testjre	
	if !ERRORLEVEL! equ 0 (
		echo *** The detected system JDK java is a matching %bitness% bit		
		goto setupjavahome
	)	
)

:testjre
if DEFINED FOUNDJREHOME  (
	echo *** Testing bitness of JRE_HOME found at !FOUNDJREHOME!:
	"!FOUNDJREHOME!\bin\java.exe" -d%bitness% -version 2> nul
	if !ERRORLEVEL! equ 1 echo *** The detected JRE java is an incompatible bit architecture& goto bundledjre
	if !ERRORLEVEL! equ 0 (	
		rem The JRE_HOME found by search4j may be the bundled JRE, overriding any system JRE_HOME,
		rem because the bundled JRE_HOME was provided as HINT to search4j.		
		echo *** The detected JRE java is a matching %bitness% bit			
		goto setupjrehome
	)	
)

:: 3. Fall back to 32 bit JRE bundled with GS
:bundledjre
:: We bundled a 32 bit JRE, but what if GS was compiled with 64 bit Java?
:: All but MG/MGPP and GDBM should still work with 64 bit java.
if exist "!HINT!\bin\java.exe" (
  echo *** Changing to use the GS bundled 32-bit jre.
  set JRE_HOME=!HINT!
  set PATH=!JAVA_HOME!\bin;!PATH!
  set RUNJAVA=!JAVA_HOME!\bin\java.exe
  goto summaryThenEnd
)

:: 4. If no bundled JRE exists either, we'd still need to check if search4j found a JAVA_HOME or JRE_HOME
:: and use that even if there was a bitness mismatch btw GS3 and the Java found.
:: Label summaryThenEnd will print out warnings on any mismatch
:setupjavahome
if DEFINED FOUNDJAVAHOME  (
	echo *** Using the system JAVA_HOME detected at !FOUNDJAVAHOME!
	set JAVA_HOME=!FOUNDJAVAHOME!
	set PATH=!FOUNDJAVAHOME!\bin;!PATH!
	set RUNJAVA=!FOUNDJAVAHOME!\bin\java.exe
	goto summaryThenEnd
)

:setupjrehome
if DEFINED FOUNDJREHOME (
	echo *** Using the JRE_HOME detected at !FOUNDJREHOME!
	set JRE_HOME=!FOUNDJREHOME!
	set PATH=!FOUNDJREHOME!\bin;!PATH!
	set RUNJAVA=!FOUNDJREHOME!\bin\java.exe
	goto summaryThenEnd
)

:: 5. Last ditch effort: search4j couldn't find any java, but check any Java env vars set anyway
echo *** Search4j could not find an appropriate JAVA or JRE.
echo *** Attempting to use any JAVA_HOME else JRE_HOME in the environment...
	
if exist "!JAVA_HOME!\bin\java.exe" (
  set PATH=!JAVA_HOME!\bin;!PATH!
  set RUNJAVA=!JAVA_HOME!\bin\java.exe
  echo Using Java at !JAVA_HOME!
  echo WARNING: Greenstone has not checked the version number of this Java installation
  echo          The source distribution of Greenstone3 requires Java 1.5 or greater
  echo          SVN users may still use Java 1.4
  echo.
  goto summaryThenEnd
)

if exist "!JRE_HOME!\bin\java.exe" (
  set PATH=!JRE_HOME!\bin;!PATH!
  set RUNJAVA=!JRE_HOME!\bin\java.exe
  echo Using Java at !JRE_HOME!
  echo WARNING: Greenstone has not checked the version number of this JRE installation
  echo          The source distribution of Greenstone3 requires Java 1.5 or greater
  echo          SVN users may still use Java 1.4
  echo.
  goto summaryThenEnd
)

echo ERROR: Failed to locate Java
echo        Please set JAVA_HOME or JRE_HOME to point to an appropriate %bitness% bit Java
goto end

:summaryThenEnd
:: 6. Check that the bitness of any Java found is appropriate and warn if it is not.
"!RUNJAVA!" -d%bitness% -version 2> nul
if !ERRORLEVEL! equ 1 (
	echo *** WARNING: Detected mismatch between the bit-ness of your Greenstone installation ^(%bitness% bit^)
	echo *** and the Java found at %RUNJAVA%.
	echo *** Continuing with this Java anyway:
	echo *** This will only affect MG/MGPP collections for searching, and GDBM database collections
	echo *** Else set JAVA_HOME or JRE_HOME to point to an appropriate %bitness%-bit Java
	echo *** Or recompile GS with your system Java:
	if exist "!JAVA_HOME!" ( echo *** JAVA_HOME at !JAVA_HOME! ) else ( echo *** JRE_HOME at !JRE_HOME! )
)

echo ********************************************************************
echo.

echo GSDL3SRCHOME : !GSDL3SRCHOME!
echo GSDL3HOME    : !GSDL3HOME!
echo JAVA         : !RUNJAVA!

if "!ANT_HOME!" == "" (
   echo.
   echo ANT_HOME is not yet set.
   echo Please make sure you have Ant version 1.7.1 or higher installed
   echo Then set ANT_HOME to the Ant installation folder
   echo and add the path to its bin folder to the PATH environment variable
) else (
   echo ANT_HOME     : !ANT_HOME!
)
echo.

:done
:: End localisation of variables that started with the set local/set enabledelayedexpansion command
:: Restore global variables that would otherwise be lost at script's end due to their having been initialised in a 
:: set local/set enabledelayedexpansion section. See http://ss64.com/nt/endlocal.html
endlocal & set RUNJAVA=%RUNJAVA%& set PATH=%PATH%& set GSDLOS=%GSDLOS%& set GSDLHOME=%GSDLHOME%& set GSDL3HOME=%GSDL3HOME%& set GSDL3SRCHOME=%GSDL3SRCHOME%& set JAVA_HOME=%JAVA_HOME%& set JRE_HOME=%JRE_HOME%& set ANT_HOME=%ANT_HOME%& set CLASSPATH=%CLASSPATH%

:end