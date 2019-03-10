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

start javaw -cp LIB_DIR\%JAR_NAME% se.alipsa.ride.splash.SplashScreen

java -Djava.library.path="%LIB_DIR%" -jar ant-launcher.jar -f ride.xml
