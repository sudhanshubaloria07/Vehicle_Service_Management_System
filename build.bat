@echo off
REM =====================================================================
REM Vehicle Service Management System (VSMS) - Windows Build & Run Script
REM =====================================================================

title AutoCare VSMS - Build ^& Run
echo =====================================================================
echo           AUTOCARE VEHICLE SERVICE MANAGEMENT SYSTEM (VSMS)          
echo               B.Tech CSE 2nd Year - OOPJ Semester Project            
echo =====================================================================
echo.

REM Create output directory
if not exist "bin" mkdir bin

echo [1/2] Compiling Java source files...
javac -encoding UTF-8 -cp "lib/*" -d bin src\service\exception\*.java src\service\vehicle\*.java src\service\billing\*.java src\service\core\*.java src\service\database\*.java src\service\gui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please review the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

echo [1/2] Compilation successful!
echo.
echo [2/2] Launching AutoCare VSMS Application...
echo.

java -cp "bin;lib/*" service.core.MainApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTE] Application exited with code %ERRORLEVEL%.
    pause
)
