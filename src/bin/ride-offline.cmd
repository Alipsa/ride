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

start javaw -cp LIB_DIR\%JAR_NAME% se.alipsa.ride.splash.SplashScreen

java -Djava.library.path="%LIB_DIR%" -jar ant-launcher.jar -f ride.xml ^
-Dcom.github.fommil.netlib.BLAS=%BLAS% ^
-Dcom.github.fommil.netlib.LAPACK=%LAPACK% ^
-Dcom.github.fommil.netlib.ARPACK=%ARPACK%
