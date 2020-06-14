::@echo off
set DIR=%~dp0%
cd %DIR%

call mvn clean install

set PROPERTY_FILE=version.properties

FOR /F "tokens=1,2 delims==" %%G IN (%PROPERTY_FILE%) DO (set %%G=%%H)

set VERSION=%version%

set JAR_NAME=%jar.name%

set RELEASE_TAG=%release.tag%

::set TARGET=%DIR%\target\%jar.name%
set TARGET=%USERPROFILE%\.m2\repository\se\alipsa\ride\%VERSION%\%JAR_NAME

cd src/bin

start javaw -cp %TARGET% se.alipsa.ride.splash.SplashScreen

::call mvn initialize -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG%

call mvn "exec:java" -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG%