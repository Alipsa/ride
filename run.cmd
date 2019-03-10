@echo off
set DIR=%~dp0%
cd %DIR%

set PROPERTY_FILE=version.properties

FOR /F "tokens=1,2 delims==" %%G IN (%PROPERTY_FILE%) DO (set %%G=%%H)

set VERSION=%version%

set JAR_NAME=%jar.name%

set RELEASE_TAG=%release.tag%

set TARGET=%DIR%\target\%jar.name%

cd src/bin

call mvn initialize -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG%

start javaw -cp ~\.m2\repository\se\alipsa\ride\%VERSION%\%JAR_NAME% se.alipsa.ride.splash.SplashScreen

call mvn "exec:java" -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG%