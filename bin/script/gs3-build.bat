@echo off

:: ---- Check that %GSDL3HOME% is set ----
if not "%GSDL3HOME%" == "" goto start
    echo "You need to run 'gs3-setup' in the greenstone3 directory before running this script"
    goto exit

:start

if not "%1" == "" goto start2
    echo Usage: gs3-build sitename collname
    goto exit

:start2
if not "%2" == "" goto run
    echo Usage: gs3-build sitename collname
    goto exit

:run

java -cp "%CLASSPATH%" -DGSDL3HOME="%GSDL3HOME%"  -DGSDL3SRCHOME="%GSDL3SRCHOME%" -Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser  org.greenstone.gsdl3.gs3build.Build -site %1 -collect %2

:exit
