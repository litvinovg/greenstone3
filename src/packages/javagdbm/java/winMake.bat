@echo off

set JAVAGDBMHOME=..

rem ---- Where to put class files ----
set JAVACLASSDIR=classes

rem ---- Name and location of java programs ----
set JAVAC="%JAVA_HOME%\bin\javac"
set JAVAH="%JAVA_HOME%\bin\javah"
set JAVA="%JAVA_HOME%\bin\java"
set JAVADOC="%JAVA_HOME%\bin\javadoc"
set JAR="%JAVA_HOME%\bin\jar"

set JAVACOPTIONS= -deprecation -g -O


if ""%1"" == """" goto compile
if ""%1"" == ""compile"" goto compile
if ""%1"" == ""install"" goto install
if ""%1"" == ""clean"" goto clean

:unknown
	echo Error: Unrecognized argument %1.
	goto done

:compile
	if not exist %JAVACLASSDIR% mkdir %JAVACLASSDIR%
	echo Compiling...
        %JAVAC% -d %JAVACLASSDIR% %JAVACOPTIONS%  au\com\pharos\gdbm\*.java au\com\pharos\io\*.java au\com\pharos\meta\*.java au\com\pharos\packing\*.java au\com\pharos\test\*.java au\com\pharos\util\*.java
        %JAVAH% -classpath %JAVACLASSDIR% -o %JAVAGDBMHOME%\jni\GdbmFile.h au.com.pharos.gdbm.GdbmFile
        %JAR% cf %JAVAGDBMHOME%\javagdbm.jar -C %JAVACLASSDIR% au
	goto done

:install
	goto done

:clean
	echo Cleaning up...
	if exist %JAVACLASSDIR% rmdir /S /Q %JAVACLASSDIR%
        if exist %JAVAGDBMHOME%\javagdbm.jar del %JAVAGDBMHOME%\javagdbm.jar 
        if exist %JAVAGDBMHOME%\jni\GdbmFile.h del %JAVAGDBMHOME%\jni\GdbmFile.h 
	goto done

:done
