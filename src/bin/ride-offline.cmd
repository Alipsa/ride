@echo off
set DIR=%~dp0%.
cd %DIR%

set "LIB_DIR=%DIR%\lib"
set "PATH=%PATH%;%LIB_DIR%"

java -Djava.library.path="%LIB_DIR%" -jar ant-launcher.jar -f ride.xml
