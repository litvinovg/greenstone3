@echo off
setlocal enabledelayedexpansion

:: AFFECTED FILES:
:: gli\gems.bat
:: gli\gli.bat
:: build.properties.in
:: build.properties
:: build.xml
:: gs3-server.bat
:: gs3-setup.bat
:: userweb.cmd

set INSTALLDIR=!GSDL3SRCHOME!
set USERWEB=
set IS_TMP_TOMCAT=

:: Loosely based on http://stackoverflow.com/questions/7708681/how-to-read-from-a-properties-file-using-batch-script 
:: Replace forward slashes in web.home with back slashes 
:: http://scripts.dragon-it.co.uk/scripts.nsf/docs/batch-search-replace-substitute!OpenDocument&ExpandSection=3&BaseTarget=East&AutoFramed
for /F "tokens=1,2 delims==" %%G in (!GSDL3SRCHOME!\build.properties) do ( 
	if "%%G"=="web.home" set USERWEB=%%H& set USERWEB=!USERWEB:/=\!
	if "%%G"=="gsdl3home.isreadonly" set IS_TMP_TOMCAT=%%H
)
echo USERWEB: !USERWEB!
echo IS_TMP_TOMCAT: !IS_TMP_TOMCAT!

if "!USERWEB!" == "" echo No userweb directory provided & echo. & goto done

:: add exclusion of log folder
robocopy /e "!INSTALLDIR!\web" "!USERWEB!" /xd "!INSTALLDIR!\web\sites\localsite\collect" "!INSTALLDIR!\web\WEB-INF" > CON
robocopy "!INSTALLDIR!\web\WEB-INF" "!USERWEB!\WEB-INF" > CON
md !USERWEB!\sites\localsite\collect


:done
endlocal & set USERWEB=%USERWEB%

:end