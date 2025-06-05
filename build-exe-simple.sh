#!/bin/bash

echo "==========================================="
echo "    SIMPLE QUORIDOR EXE BUILDER"
echo "==========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Step 1: Build JAR
echo -e "${YELLOW}[1/3] Building JAR with Maven...${NC}"
if mvn clean package -q; then
    if [ -f "target/Quoridor-1.0.2-shaded.jar" ]; then
        echo -e "${GREEN}✅ JAR built successfully${NC}"
    else
        echo -e "${RED}❌ ERROR: JAR file not found after Maven build${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ ERROR: Maven build failed${NC}"
    exit 1
fi

# Step 2: Create batch launcher for Windows
echo -e "${YELLOW}[2/3] Creating Windows batch launcher...${NC}"
mkdir -p target/windows-dist

# Create the batch file
cat > "target/windows-dist/Quoridor.bat" << 'EOF'
@echo off
title Quoridor Game
cd /d "%~dp0"

echo Starting Quoridor...
echo Checking for Java...

REM Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 or higher from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check Java version (simplified check)
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo Found Java version: %JAVA_VERSION%

REM Run the game with optimizations
java -Dfile.encoding=UTF-8 -Dprism.forceGPU=true -Djavafx.animation.fullspeed=true -Xmx1024m -jar "Quoridor-shaded.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to start Quoridor
    echo Make sure you have Java 21 or higher installed
    pause
)
EOF

echo -e "${GREEN}✅ Batch launcher created${NC}"

# Step 3: Copy JAR and create a simple EXE wrapper
echo -e "${YELLOW}[3/3] Creating Windows executable package...${NC}"

# Copy the JAR
cp "target/Quoridor-1.0.2-shaded.jar" "target/windows-dist/Quoridor-shaded.jar"

# Create a PowerShell script that can be converted to EXE
cat > "target/windows-dist/Quoridor.ps1" << 'EOF'
Add-Type -AssemblyName System.Windows.Forms

$jarPath = Join-Path $PSScriptRoot "Quoridor-shaded.jar"
if (-not (Test-Path $jarPath)) {
    [System.Windows.Forms.MessageBox]::Show("Quoridor JAR file not found!", "Error", [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Error)
    exit 1
}

try {
    $process = Start-Process -FilePath "java" -ArgumentList @(
        "-Dfile.encoding=UTF-8",
        "-Dprism.forceGPU=true", 
        "-Djavafx.animation.fullspeed=true",
        "-Xmx1024m",
        "-jar",
        $jarPath
    ) -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru
    
    $process.WaitForExit()
} catch {
    [System.Windows.Forms.MessageBox]::Show("Failed to start Quoridor. Please ensure Java 21+ is installed.`n`nError: $($_.Exception.Message)", "Quoridor Error", [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Error)
}
EOF

# Create an installer script
cat > "target/windows-dist/INSTALL.bat" << 'EOF'
@echo off
echo ==========================================
echo    QUORIDOR GAME INSTALLER
echo ==========================================
echo.
echo This will set up Quoridor on your system.
echo.

REM Check for Java
echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Java not found!
    echo Please install Java 21 or higher from: https://adoptium.net/
    echo.
    pause
    echo.
    echo You can still continue - Java will be checked when you run the game.
    echo.
)

echo Setting up desktop shortcut...
set "SHORTCUT_PATH=%USERPROFILE%\Desktop\Quoridor.lnk"
set "TARGET_PATH=%~dp0Quoridor.bat"

REM Create shortcut using PowerShell
powershell.exe -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT_PATH%'); $Shortcut.TargetPath = '%TARGET_PATH%'; $Shortcut.WorkingDirectory = '%~dp0'; $Shortcut.Save()"

if exist "%SHORTCUT_PATH%" (
    echo ✅ Desktop shortcut created successfully
) else (
    echo ⚠️  Could not create desktop shortcut
)

echo.
echo ==========================================
echo    INSTALLATION COMPLETE!
echo ==========================================
echo.
echo You can now run Quoridor by:
echo 1. Double-clicking the desktop shortcut
echo 2. Running Quoridor.bat in this folder
echo 3. Running: java -jar Quoridor-shaded.jar
echo.
pause
EOF

# Create README
cat > "target/windows-dist/README.txt" << 'EOF'
QUORIDOR GAME FOR WINDOWS
=========================

REQUIREMENTS:
- Java 21 or higher (Download from: https://adoptium.net/)
- Windows 7/8/10/11

QUICK START:
1. Run INSTALL.bat to set up desktop shortcut
2. Double-click the desktop shortcut to play
3. Or run Quoridor.bat directly

FILES:
- Quoridor.bat       - Main launcher (double-click to play)
- Quoridor-shaded.jar - Game JAR file  
- INSTALL.bat        - Creates desktop shortcut
- README.txt         - This file

TROUBLESHOOTING:
- If Java error occurs, install Java 21+ from https://adoptium.net/
- Make sure all files stay in the same folder
- Run from folder containing Quoridor-shaded.jar

Enjoy the game!
EOF

# Calculate total size
TOTAL_SIZE=$(du -sh "target/windows-dist" | cut -f1)

echo -e "${GREEN}✅ Windows executable package created!${NC}"
echo ""
echo -e "${CYAN}=========================================${NC}"
echo -e "${CYAN}     BUILD COMPLETE!${NC}"
echo -e "${CYAN}=========================================${NC}"
echo ""
echo -e "${GREEN}Windows distribution package: target/windows-dist/${NC}"
echo -e "${GRAY}Total size: $TOTAL_SIZE${NC}"
echo ""
echo -e "${YELLOW}Package contents:${NC}"
ls -la "target/windows-dist/"
echo ""
echo -e "${YELLOW}To distribute:${NC}"
echo -e "${GRAY}1. Zip the entire 'windows-dist' folder${NC}"
echo -e "${GRAY}2. Users extract and run INSTALL.bat${NC}"
echo -e "${GRAY}3. Or users can run Quoridor.bat directly${NC}"
echo ""

# Offer to create a zip
read -p "Create distribution ZIP file? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Creating ZIP distribution...${NC}"
    cd target
    zip -r "Quoridor-Windows-Distribution.zip" windows-dist/
    if [ $? -eq 0 ]; then
        ZIP_SIZE=$(du -sh "Quoridor-Windows-Distribution.zip" | cut -f1)
        echo -e "${GREEN}✅ Distribution ZIP created: target/Quoridor-Windows-Distribution.zip${NC}"
        echo -e "${GRAY}ZIP size: $ZIP_SIZE${NC}"
    else
        echo -e "${RED}❌ Failed to create ZIP${NC}"
    fi
    cd ..
fi

echo ""
echo -e "${GREEN}Your Windows distribution is ready!${NC}" 