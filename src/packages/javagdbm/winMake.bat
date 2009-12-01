@echo off

set MAKE=nmake
set MAKE_OPTIONS=/f

if ""%1"" == """" goto all
if ""%1"" == ""compile"" goto go
if ""%1"" == ""install"" goto go
if ""%1"" == ""clean"" goto go

:unknown
	echo Error: Unrecognized argument %1.
	goto done

:go
	if ""%2"" == ""javaonly"" goto java

:all
	cd jni
	%MAKE% %MAKE_OPTIONS% win32.mak %1
	cd ..

:java
	cd java
	call winMake.bat %1
	cd ..
	goto done

:done
