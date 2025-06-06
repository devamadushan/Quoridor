#!/bin/bash

echo "==========================================="
echo "  SMART AUTO-INSTALLER BUILDER"
echo "==========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Step 1: Build JAR first
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

# Step 2: Create smart installer directory
echo -e "${YELLOW}[2/3] Creating smart installer package...${NC}"
INSTALLER_DIR="target/quoridor-smart-installer"
rm -rf "$INSTALLER_DIR"
mkdir -p "$INSTALLER_DIR"

# Copy source code and build files
echo -e "${GRAY}   Copying source code...${NC}"
cp -r src "$INSTALLER_DIR/"
cp pom.xml "$INSTALLER_DIR/"
cp "target/Quoridor-1.0.2-shaded.jar" "$INSTALLER_DIR/Quoridor-PREBUILT.jar"

# Step 3: Create the smart Windows installer
echo -e "${YELLOW}[3/3] Creating smart Windows installer...${NC}"

cat > "$INSTALLER_DIR/PLAY-QUORIDOR.bat" << 'EOF'
@echo off
setlocal EnableDelayedExpansion

echo ==========================================
echo      QUORIDOR SMART AUTO-INSTALLER
echo ==========================================
echo.
echo This will automatically set up everything needed to play Quoridor!
echo The setup only happens once, then you can play anytime.
echo.
echo What will be installed (if not already present):
echo - Eclipse Temurin JDK 21 (reliable OpenJDK distribution)
echo - JavaFX 21 runtime (for graphics and UI)
echo - Apache Maven (for building)
echo - Game dependencies
echo.
echo All files will be installed in this folder only.
echo No system changes, no registry modifications.
echo.
pause

REM Create tools directory
if not exist "tools" mkdir tools
cd tools

REM Check if we have a working Java
echo.
echo [1/5] Checking for Java...

set "JAVA_HOME=%~dp0tools\jdk"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "MAVEN_HOME=%~dp0tools\maven"
set "MVN_EXE=%MAVEN_HOME%\bin\mvn.cmd"

if exist "%JAVA_EXE%" (
    echo ✅ Java found: %JAVA_EXE%
    goto check_javafx
)

echo ⬇️  Downloading Eclipse Temurin JDK 21...
echo This may take a few minutes depending on your internet speed...

REM Download Eclipse Temurin JDK 21 (reliable source)
set "JDK_ZIP=temurin-jdk.zip"

REM Try multiple reliable JDK download URLs
echo Trying Adoptium download...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.4_7.zip', '%JDK_ZIP%')"

if not exist "%JDK_ZIP%" (
    echo Trying alternative Adoptium URL...
    powershell -Command "Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile '%JDK_ZIP%'"
)

if not exist "%JDK_ZIP%" (
    echo ❌ Failed to download Java. Please check your internet connection.
    echo.
    echo Alternative solutions:
    echo 1. Download Java manually from: https://adoptium.net/temurin/releases/
    echo 2. Extract it to tools\jdk folder
    echo 3. Run this script again
    pause
    exit /b 1
)

echo ✅ Java downloaded successfully!
echo 📦 Extracting Java runtime...

powershell -Command "Expand-Archive -Path '%JDK_ZIP%' -DestinationPath '.' -Force"

REM Find the extracted JDK directory and rename
for /d %%d in (jdk*) do (
    if exist "%%d\bin\java.exe" (
        if exist "jdk" rmdir /s /q jdk
        move "%%d" "jdk" > nul 2>&1
    )
)

del "%JDK_ZIP%" > nul 2>&1

if not exist "%JAVA_EXE%" (
    echo ❌ Java extraction failed
    pause
    exit /b 1
)

echo ✅ Java installed successfully!

:check_javafx
echo.
echo [2/5] Checking for JavaFX...

if exist "%JAVA_HOME%\lib\javafx.base.jar" (
    echo ✅ JavaFX found in JDK
    goto check_maven
)

if exist "javafx" (
    echo ✅ JavaFX found: javafx\
    goto check_maven
)

echo ⬇️  Downloading JavaFX 21...

set "JAVAFX_ZIP=javafx-sdk.zip"

REM Download JavaFX from OpenJFX
echo Downloading JavaFX runtime...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://download2.gluonhq.com/openjfx/21.0.4/openjfx-21.0.4_windows-x64_bin-sdk.zip', '%JAVAFX_ZIP%')"

if not exist "%JAVAFX_ZIP%" (
    echo Trying alternative JavaFX URL...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://gluonhq.com/download/javafx-21-0-4-sdk-windows/', '%JAVAFX_ZIP%')"
)

if not exist "%JAVAFX_ZIP%" (
    echo ❌ Failed to download JavaFX. Will try to run without it.
    goto check_maven
)

echo ✅ JavaFX downloaded successfully!
echo 📦 Extracting JavaFX...

powershell -Command "Expand-Archive -Path '%JAVAFX_ZIP%' -DestinationPath '.' -Force"

REM Find the extracted JavaFX directory and rename
for /d %%d in (javafx*) do (
    if exist "%%d\lib" (
        if exist "javafx" rmdir /s /q javafx
        move "%%d" "javafx" > nul 2>&1
    )
)

del "%JAVAFX_ZIP%" > nul 2>&1

if exist "javafx\lib" (
    echo ✅ JavaFX installed successfully!
) else (
    echo ⚠️  JavaFX extraction incomplete, will try to run without it
)

:check_maven
echo.
echo [3/5] Checking for Maven...

if exist "%MVN_EXE%" (
    echo ✅ Maven found: %MVN_EXE%
    goto test_setup
)

echo ⬇️  Downloading Apache Maven...

set "MAVEN_ZIP=maven.zip"

REM Download Apache Maven from reliable Apache mirrors
echo Downloading Maven...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip', '%MAVEN_ZIP%')"

if not exist "%MAVEN_ZIP%" (
    echo Trying backup Maven URL...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip', '%MAVEN_ZIP%')"
)

if not exist "%MAVEN_ZIP%" (
    echo ❌ Failed to download Maven. Please check your internet connection.
    pause
    exit /b 1
)

echo ✅ Maven downloaded successfully!
echo 📦 Extracting Maven...

powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '.' -Force"

REM Find the extracted Maven directory
for /d %%d in (apache-maven*) do (
    if exist "%%d\bin\mvn.cmd" (
        if exist "maven" rmdir /s /q maven
        move "%%d" "maven" > nul 2>&1
    )
)

del "%MAVEN_ZIP%" > nul 2>&1

if not exist "%MVN_EXE%" (
    echo ❌ Maven extraction failed
    pause
    exit /b 1
)

echo ✅ Maven installed successfully!

:test_setup
cd ..

echo.
echo [4/5] Testing Java and JavaFX setup...

"%JAVA_EXE%" -version
if %ERRORLEVEL% neq 0 (
    echo ❌ Java test failed
    pause
    exit /b 1
)

REM Test JavaFX - check multiple ways
echo Testing JavaFX availability...
set "JAVAFX_DETECTED=false"

REM Check if JavaFX is bundled in JDK
"%JAVA_EXE%" --list-modules | findstr javafx > nul
if %ERRORLEVEL% equ 0 (
    echo ✅ JavaFX modules detected in JDK!
    set "JAVAFX_DETECTED=true"
)

REM Check if JavaFX is in separate folder
if exist "tools\javafx\lib" (
    echo ✅ JavaFX runtime detected in tools\javafx\lib!
    set "JAVAFX_DETECTED=true"
)

if "%JAVAFX_DETECTED%"=="false" (
    echo ⚠️  JavaFX not detected, but will try to run anyway...
    echo Some graphics features may not work properly.
)

echo.
echo [5/5] Launching Quoridor...

REM First try to run the prebuilt JAR
if exist "Quoridor-PREBUILT.jar" (
    echo 🎮 Starting Quoridor (prebuilt version)...
    echo.
    
    REM Set up JavaFX classpath if available
    set "JAVAFX_CLASSPATH="
    if exist "tools\javafx\lib" (
        set "JAVAFX_CLASSPATH=--module-path tools\javafx\lib --add-modules javafx.controls,javafx.fxml,javafx.media"
    )
    
    "%JAVA_EXE%" ^
        -Dfile.encoding=UTF-8 ^
        -Dprism.order=sw,d3d ^
        -Dprism.verbose=false ^
        -Djavafx.animation.fullspeed=false ^
        -Dprism.forceGPU=false ^
        -Dglass.platform=win ^
        --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
        --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
        --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
        !JAVAFX_CLASSPATH! ^
        -jar "Quoridor-PREBUILT.jar"
    
    set GAME_EXIT_CODE=%ERRORLEVEL%
    
    if !GAME_EXIT_CODE! equ 0 (
        echo.
        echo 🎉 Game completed successfully!
        goto end
    ) else (
        echo.
        echo ⚠️  Prebuilt version failed (exit code !GAME_EXIT_CODE!). Building from source...
    )
)

REM Fallback: build from source if prebuilt fails
if exist "src" if exist "pom.xml" (
    echo 🔨 Building Quoridor from source...
    
    set "PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%"
    
    "%MVN_EXE%" clean package -q
    
    if !ERRORLEVEL! neq 0 (
        echo ❌ Build failed
        pause
        exit /b 1
    )
    
    echo ✅ Build successful!
    echo 🎮 Starting Quoridor (fresh build)...
    echo.
    
    REM Set up JavaFX classpath if available
    set "JAVAFX_CLASSPATH="
    if exist "tools\javafx\lib" (
        set "JAVAFX_CLASSPATH=--module-path tools\javafx\lib --add-modules javafx.controls,javafx.fxml,javafx.media"
    )
    
    "%JAVA_EXE%" ^
        -Dfile.encoding=UTF-8 ^
        -Dprism.order=sw,d3d ^
        -Dprism.verbose=false ^
        -Djavafx.animation.fullspeed=false ^
        -Dprism.forceGPU=false ^
        -Dglass.platform=win ^
        --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
        --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
        --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
        !JAVAFX_CLASSPATH! ^
        -jar "target\Quoridor-1.0.2-shaded.jar"
        
    set GAME_EXIT_CODE=%ERRORLEVEL%
) else (
    echo ❌ Source code not found for rebuilding
    set GAME_EXIT_CODE=1
)

:end
echo.
if !GAME_EXIT_CODE! equ 0 (
    echo ✅ Thanks for playing Quoridor!
) else (
    echo ❌ Game encountered an error (exit code: !GAME_EXIT_CODE!)
    echo.
    echo Troubleshooting suggestions:
    echo 1. Make sure your graphics drivers are up to date
    echo 2. Try running as administrator
    echo 3. Check Windows Defender/antivirus isn't blocking Java
    echo 4. If you have an older computer, graphics acceleration may not work
    echo.
    echo If problems persist, please report the issue with the error messages above.
)

echo.
echo Press any key to close...
pause > nul
EOF

# Create a simple launcher that works after setup
cat > "$INSTALLER_DIR/QuoridorGame.bat" << 'EOF'
@echo off
echo 🎮 Starting Quoridor...

if exist "tools\jdk\bin\java.exe" (
    REM Use installed Java
    set "JAVAFX_CLASSPATH="
    if exist "tools\javafx\lib" (
        set "JAVAFX_CLASSPATH=--module-path tools\javafx\lib --add-modules javafx.controls,javafx.fxml,javafx.media"
    )
    
    "tools\jdk\bin\java.exe" ^
        -Dfile.encoding=UTF-8 ^
        -Dprism.order=sw,d3d ^
        -Dprism.verbose=false ^
        --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
        --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
        --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED ^
        --add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
        %JAVAFX_CLASSPATH% ^
        -jar "Quoridor-PREBUILT.jar"
) else (
    echo ⚠️  Please run PLAY-QUORIDOR.bat first to set up the game!
    pause
)
EOF

# Create README
cat > "$INSTALLER_DIR/README.txt" << 'EOF'
QUORIDOR GAME - SMART AUTO-INSTALLER v2.0
==========================================

🎮 INTELLIGENT AUTOMATIC SETUP
Zero manual installation! Everything is downloaded and configured automatically!

🚀 HOW TO PLAY:

FIRST TIME:
1. Double-click: PLAY-QUORIDOR.bat
   (Downloads Java, JavaFX, Maven, sets up everything)
   (Only needs internet connection once)

AFTER SETUP:
- Double-click: QuoridorGame.bat (quick launch)
- Or run: PLAY-QUORIDOR.bat again

📁 WHAT HAPPENS AUTOMATICALLY:
- Downloads Eclipse Temurin JDK 21 (~200MB) - most reliable OpenJDK
- Downloads JavaFX 21 runtime (~50MB) - for graphics and UI
- Downloads Apache Maven (~10MB) - for building
- Builds game from source if needed
- Configures everything for optimal JavaFX performance
- Creates portable installation in this folder only

🎯 FEATURES:
- ZERO manual steps required
- No system Java installation needed
- No registry changes
- No administrator rights required  
- Downloads from most reliable sources (Adoptium, Apache)
- Multiple fallback URLs for maximum reliability
- Builds from source for maximum compatibility
- Works offline after first setup
- Supports separate JavaFX download for better compatibility

💻 REQUIREMENTS:
- Windows 7/8/10/11
- Internet connection (first run only)
- ~300MB free disk space
- No other requirements!

🛠️ TROUBLESHOOTING:
- If download fails: Check internet connection, try again
- If Java fails: Run as administrator
- If graphics issues: Update graphics drivers
- All downloads are cached - safe to retry
- Uses multiple download sources for maximum reliability

🔧 TECHNICAL DETAILS:
- Uses Eclipse Temurin OpenJDK (most stable distribution)
- Downloads JavaFX separately for better compatibility
- Software rendering prioritized for compatibility
- Full Maven build system included
- Source code included for transparency
- Completely portable - copy folder to run anywhere

CHANGELOG v2.0:
- Fixed download URLs (uses reliable Adoptium + OpenJFX sources)
- Added multiple fallback download options
- Better JavaFX detection and configuration
- Improved error handling and diagnostics
- More reliable downloads from trusted sources

This is the ultimate "just works" game installer!
Enjoy Quoridor! 🎯
EOF

# Calculate sizes
SOURCE_SIZE=$(du -sh src 2>/dev/null | cut -f1 || echo "~1MB")
JAR_SIZE=$(du -sh "$INSTALLER_DIR/Quoridor-PREBUILT.jar" | cut -f1)
TOTAL_SIZE=$(du -sh "$INSTALLER_DIR" | cut -f1)

echo -e "${GREEN}✅ Smart auto-installer created!${NC}"
echo ""
echo -e "${CYAN}=========================================${NC}"
echo -e "${CYAN}     SMART INSTALLER COMPLETE!${NC}"
echo -e "${CYAN}=========================================${NC}"
echo ""
echo -e "${GREEN}Smart installer package: $INSTALLER_DIR/${NC}"
echo -e "${GRAY}Source code size: $SOURCE_SIZE${NC}"
echo -e "${GRAY}Prebuilt JAR size: $JAR_SIZE${NC}"
echo -e "${GRAY}Total package size: $TOTAL_SIZE${NC}"
echo ""
echo -e "${YELLOW}Package contents:${NC}"
ls -la "$INSTALLER_DIR/"
echo ""
echo -e "${YELLOW}This smart installer will:${NC}"
echo -e "${GRAY}• Auto-download OpenJDK 21 with JavaFX${NC}"
echo -e "${GRAY}• Auto-download Maven build system${NC}"
echo -e "${GRAY}• Auto-configure everything for optimal performance${NC}"
echo -e "${GRAY}• Work completely offline after first setup${NC}"
echo -e "${GRAY}• Require no manual user intervention${NC}"
echo ""

# Create ZIP distribution
read -p "Create smart installer ZIP? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Creating smart installer ZIP...${NC}"
    cd target
    zip -r "Quoridor-Smart-Installer.zip" "quoridor-smart-installer/" -x "*.DS_Store"
    if [ $? -eq 0 ]; then
        ZIP_SIZE=$(du -sh "Quoridor-Smart-Installer.zip" | cut -f1)
        echo -e "${GREEN}✅ Smart installer ZIP created: target/Quoridor-Smart-Installer.zip${NC}"
        echo -e "${GRAY}ZIP size: $ZIP_SIZE${NC}"
        echo ""
        echo -e "${YELLOW}🎉 SMART INSTALLER READY!${NC}"
        echo -e "${GRAY}User downloads, extracts, runs PLAY-QUORIDOR.bat - everything else is automatic!${NC}"
    else
        echo -e "${RED}❌ Failed to create ZIP${NC}"
    fi
    cd ..
fi

echo ""
echo -e "${GREEN}Your smart auto-installer is ready! 🚀${NC}"
echo -e "${YELLOW}Users just run one file and everything is handled automatically!${NC}" 