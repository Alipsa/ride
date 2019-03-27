@echo off
set DIR=%~dp0%.
cd %DIR%

set PROPERTY_FILE=version.properties

FOR /F "tokens=1,2 delims==" %%G IN (%PROPERTY_FILE%) DO (set %%G=%%H)

set VERSION=%version%

set JAR_NAME=%jar.name%

set RELEASE_TAG=%release.tag%

set TARGET=%DIR%\%jar.name%

set "LIB_DIR=%DIR%\lib"

if not exist %LIB_DIR% mkdir %LIB_DIR%

set "PATH=%PATH%;%LIB_DIR%"

start javaw -cp %JAR_NAME% se.alipsa.ride.splash.SplashScreen

call mvn initialize -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG% --no-snapshot-updates

call mvn "exec:java" -Djava.library.path="%LIB_DIR%" -Dride.jar=%TARGET% -Drelease.tag=%RELEASE_TAG% --no-snapshot-updates
