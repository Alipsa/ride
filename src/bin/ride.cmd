@echo off
set DIR=%~dp0%.
cd %DIR%

set PROPERTY_FILE=version.properties

FOR /F "tokens=1,2 delims==" %%G IN (%PROPERTY_FILE%) DO (set %%G=%%H)

set VERSION=%version%

set JAR_NAME=%jar.name%

set RELEASE_TAG=%release.tag%

set "LIB_DIR=%DIR%\lib"

set "PATH=%PATH%;%LIB_DIR%"

:: This is just to avoid warnings about missing native BLAS libs when running the REPL
:: i.e. we will fall back to pure java
set "BLAS=com.github.fommil.netlib.F2jBLAS"
set "LAPACK=com.github.fommil.netlib.F2jLAPACK"
set "ARPACK=com.github.fommil.netlib.F2jARPACK"

:: Allow for any kind of customization of variables or paths etc. without having to change this script
:: which would otherwise be overwritten on a subsequent install.
if exist %DIR%\env.cmd (
    call %DIR%\env.cmd
)

if defined JAVA_HOME (
	set JAVA_CMD=%JAVA_HOME%\bin\javaw
) else (
	set JAVA_CMD=javaw
)

start %JAVA_CMD% -cp %JAR_NAME% %JAVA_OPTS% se.alipsa.ride.splash.SplashScreen

:: it is possible to force the initial packageloader by adding:
:: -DConsoleComponent.PackageLoader=ClasspathPackageLoader
:: to the command below
:: also if you dont want the console to remain, do start javaw instead of java

start %JAVA_CMD% -Djava.library.path="%LIB_DIR%" -cp %JAR_NAME%;%LIB_DIR%\* ^
-Dcom.github.fommil.netlib.BLAS=%BLAS% ^
-Dcom.github.fommil.netlib.LAPACK=%LAPACK% ^
-Dcom.github.fommil.netlib.ARPACK=%ARPACK% ^
%JAVA_OPTS% ^
se.alipsa.ride.Ride
