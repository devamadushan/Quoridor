#!/bin/bash

echo "==========================================="
echo "    QUORIDOR EXE BUILDER (macOS → Windows)"
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
echo -e "${YELLOW}[1/5] Building JAR with Maven...${NC}"
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

# Step 2: Create temp directory
echo -e "${YELLOW}[2/5] Setting up build environment...${NC}"
TEMP_DIR="temp-build"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Step 3: Download Launch4j with multiple fallback options
echo -e "${YELLOW}[3/5] Downloading Launch4j (cross-platform)...${NC}"
LAUNCH4J_ZIP="$TEMP_DIR/launch4j.zip"

# List of URLs to try
URLS=(
    "https://deac-fra.dl.sourceforge.net/project/launch4j/launch4j-3/3.50/launch4j-3.50.zip"
    "https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50.zip/download"
    "https://netcologne.dl.sourceforge.net/project/launch4j/launch4j-3/3.50/launch4j-3.50.zip"
)

DOWNLOAD_SUCCESS=false

for url in "${URLS[@]}"; do
    echo -e "${GRAY}   Trying: $url${NC}"
    if curl -L -A "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)" -o "$LAUNCH4J_ZIP" "$url" --connect-timeout 30 --max-time 120; then
        # Verify download
        if [ -f "$LAUNCH4J_ZIP" ]; then
            FILE_SIZE=$(stat -f%z "$LAUNCH4J_ZIP" 2>/dev/null || stat -c%s "$LAUNCH4J_ZIP" 2>/dev/null)
            echo -e "${GRAY}   Downloaded $FILE_SIZE bytes${NC}"
            
            if [ "$FILE_SIZE" -gt 2000000 ]; then  # Should be at least 2MB
                echo -e "${GREEN}✅ Launch4j downloaded successfully${NC}"
                DOWNLOAD_SUCCESS=true
                break
            else
                echo -e "${YELLOW}   File too small, trying next URL...${NC}"
                rm -f "$LAUNCH4J_ZIP"
            fi
        fi
    else
        echo -e "${YELLOW}   Download failed, trying next URL...${NC}"
    fi
done

if [ "$DOWNLOAD_SUCCESS" = false ]; then
    echo -e "${RED}❌ All automatic downloads failed${NC}"
    echo ""
    echo -e "${YELLOW}MANUAL DOWNLOAD REQUIRED:${NC}"
    echo -e "${YELLOW}1. Go to: https://launch4j.sourceforge.net/${NC}"
    echo -e "${YELLOW}2. Download 'launch4j-3.50.zip' (NOT the Windows installer)${NC}"
    echo -e "${YELLOW}3. Save it as: $PWD/$LAUNCH4J_ZIP${NC}"
    echo ""
    read -p "Press Enter when the file is downloaded and ready..."
    
    if [ ! -f "$LAUNCH4J_ZIP" ] || [ $(stat -f%z "$LAUNCH4J_ZIP" 2>/dev/null || stat -c%s "$LAUNCH4J_ZIP" 2>/dev/null) -lt 2000000 ]; then
        echo -e "${RED}❌ File not found or too small. Exiting.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Manual download verified${NC}"
fi

# Step 4: Extract Launch4j
echo -e "${YELLOW}[4/5] Extracting Launch4j...${NC}"
if unzip -q "$LAUNCH4J_ZIP" -d "$TEMP_DIR"; then
    # Find launch4j JAR (cross-platform version)
    LAUNCH4J_JAR=$(find "$TEMP_DIR" -name "launch4j.jar" | head -1)
    if [ -z "$LAUNCH4J_JAR" ]; then
        echo -e "${RED}❌ ERROR: launch4j.jar not found in extracted files${NC}"
        echo -e "${GRAY}Contents of extracted archive:${NC}"
        find "$TEMP_DIR" -name "*.jar" -o -name "launch4j*"
        exit 1
    fi
    echo -e "${GREEN}✅ Launch4j extracted successfully${NC}"
    echo -e "${GRAY}   Found: $LAUNCH4J_JAR${NC}"
else
    echo -e "${RED}❌ ERROR: Failed to extract Launch4j${NC}"
    echo -e "${GRAY}Trying to examine the downloaded file...${NC}"
    file "$LAUNCH4J_ZIP"
    exit 1
fi

# Step 5: Create Launch4j configuration
echo -e "${YELLOW}[5/5] Creating Windows EXE with Launch4j...${NC}"

JAR_PATH=$(pwd)/target/Quoridor-1.0.2-shaded.jar
EXE_PATH=$(pwd)/target/Quoridor.exe

cat > "$TEMP_DIR/launch4j-config.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>$JAR_PATH</jar>
  <outfile>$EXE_PATH</outfile>
  <errTitle>Quoridor - Java Error</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/temurin/releases/</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon></icon>
  <jre>
    <path></path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>21</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64/32</runtimeBits>
    <opt>-Dfile.encoding=UTF-8 -Dprism.forceGPU=true -Djavafx.animation.fullspeed=true -Xmx1024m</opt>
  </jre>
  <messages>
    <startupErr>An error occurred while starting Quoridor. Please ensure Java 21+ is installed.</startupErr>
    <bundledJreErr>This application requires Java 21 or higher. Please install Java and try again.</bundledJreErr>
    <jreVersionErr>This application requires Java 21 or higher. Your Java version is too old.</jreVersionErr>
    <launcherErr>Unable to launch Quoridor.</launcherErr>
  </messages>
</launch4jConfig>
EOF

# Run Launch4j to create the EXE
echo -e "${GRAY}   Running Launch4j to create Windows executable...${NC}"
if java -jar "$LAUNCH4J_JAR" "$TEMP_DIR/launch4j-config.xml"; then
    if [ -f "$EXE_PATH" ]; then
        EXE_SIZE=$(stat -f%z "$EXE_PATH" 2>/dev/null || stat -c%s "$EXE_PATH" 2>/dev/null)
        EXE_SIZE_MB=$(echo "scale=2; $EXE_SIZE/1024/1024" | bc 2>/dev/null || echo "Unknown")
        echo -e "${GREEN}✅ Windows EXE created successfully!${NC}"
        echo -e "${GRAY}   File: $EXE_PATH${NC}"
        echo -e "${GRAY}   Size: ${EXE_SIZE_MB} MB${NC}"
    else
        echo -e "${RED}❌ ERROR: EXE file was not created${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ ERROR: Failed to create EXE with Launch4j${NC}"
    exit 1
fi

# Cleanup
echo -e "${GRAY}Cleaning up temporary files...${NC}"
rm -rf "$TEMP_DIR"

echo ""
echo -e "${CYAN}=========================================${NC}"
echo -e "${CYAN}     BUILD COMPLETE!${NC}"
echo -e "${CYAN}=========================================${NC}"
echo ""
echo -e "${GREEN}Your Windows executable is ready: target/Quoridor.exe${NC}"
echo -e "${GRAY}This .exe file will run on Windows systems with Java 21+${NC}"
echo ""
echo -e "${YELLOW}Note: The .exe was created on macOS but is designed for Windows${NC}"
echo -e "${YELLOW}You can test it by copying to a Windows machine${NC}"
echo ""
read -p "Press Enter to finish..." 