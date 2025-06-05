@echo off
title Building Quoridor for Windows

echo ==========================================
echo    QUORIDOR WINDOWS BUILD SCRIPT
echo ==========================================
echo.

echo Checking Java installation...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found!
    echo Please install Java 21 or later
    pause
    exit /b 1
)

echo.
echo Checking Maven installation...
mvn -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found!
    echo Please install Maven
    pause
    exit /b 1
)

echo.
echo Building Quoridor...
echo [1/3] Cleaning previous builds...
mvn clean

echo [2/3] Compiling and packaging...
mvn package

echo [3/3] Creating Windows executable...
mvn jpackage:jpackage

echo.
echo ========================================
echo      BUILD COMPLETED!
echo ========================================
echo.
echo Your Windows executable is ready in:
echo target\dist\Quoridor-*.exe
echo.
dir target\dist\*.exe

echo.
echo Press any key to exit...
pause >nul 