::@echo off
set DIR=%~dp0%
cd %DIR%

call mvn -DskipTests clean install

set PROPERTY_FILE=version.properties

FOR /F "tokens=1,2 delims==" %%G IN (%PROPERTY_FILE%) DO (set %%G=%%H)

set VERSION=%version%

set JAR_NAME=%jar.name%

set RELEASE_TAG=%release.tag%

::set TARGET=%DIR%\target\%jar.name%
set TARGET=%DIR%\target\%JAR_NAME%

:: Allow for any kind of customization of variables or paths etc. without having to change this script
:: which would otherwise be overwritten on a subsequent install.
if exist %DIR%\env.cmd (
    call %DIR%\env.cmd
)

start javaw -cp %TARGET% %JAVA_OPTS% se.alipsa.ride.splash.SplashScreen

::call mvn initialize -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG%

call mvn %JAVA_OPTS% "exec:java"