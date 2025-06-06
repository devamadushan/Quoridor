@echo off
title Building Quoridor Native Windows Executable

echo ==========================================
echo    QUORIDOR NATIVE WINDOWS BUILD
echo ==========================================
echo.
echo This will create a standalone Windows .exe 
echo that includes Java runtime (no Java needed on target PC)
echo.

echo [1/4] Checking build requirements...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java 21+ required for building
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven required for building  
    echo Download from: https://maven.apache.org/
    pause
    exit /b 1
)

echo ✅ Build requirements OK
echo.

echo [2/4] Cleaning previous builds...
mvn clean -q

echo [3/4] Building shaded JAR...
mvn package -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to build JAR
    pause
    exit /b 1
)

echo [4/4] Creating native Windows executable...
echo This may take several minutes...
mvn jpackage:jpackage -P windows

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create native executable
    echo Make sure you're running this on Windows with JDK 21+
    pause
    exit /b 1
)

echo.
echo ==========================================
echo      BUILD COMPLETED SUCCESSFULLY!
echo ==========================================
echo.

if exist "target\dist\Quoridor-*.exe" (
    echo Your Windows installer is ready:
    dir "target\dist\Quoridor-*.exe"
    echo.
    echo This .exe file:
    echo ✅ Includes Java runtime (no Java needed on target PC)
    echo ✅ Includes JavaFX libraries
    echo ✅ Creates Start Menu shortcut
    echo ✅ Creates desktop shortcut
    echo ✅ Can be distributed as a single file
    echo.
    echo File size: 
    for %%I in ("target\dist\Quoridor-*.exe") do echo %%~zI bytes (%%~nI%%~xI)
) else (
    echo WARNING: Executable not found in expected location
    echo Check target\dist\ folder manually
)

echo.
echo Press any key to exit...
pause >nul 