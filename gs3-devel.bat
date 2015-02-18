@echo off

call gs3-setup.bat

if exist gs2build\devel.bat (
	echo.
	echo Sourcing gs2build\devel.bat
	cd gs2build && call devel.bat && cd ..
)

if not "gsopt_noexts" == "" (
	if exist ext (
		for /D %%e IN ("ext/*") do (		
			cd ext\%%e
			if EXIST gs3-devel.bat (
				call gs3-devel.bat
			) else (
			  if EXIST devel.bat call devel.bat
			)
			cd ..\..
		)
	)
)

if exist local (
	if exist local\gs3-devel.bat (
		echo.
		echo Sourcing local\gs3-develbat
		cd local && call gs3-devel.bat && cd ..
	)
)

echo Adding devel\bin\script to PATH

set PATH=%PATH%;%GSDL3SRCHOME%\devel\bin\script

echo.
