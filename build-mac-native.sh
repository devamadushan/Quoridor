#!/bin/bash

echo "=========================================="
echo "    QUORIDOR NATIVE macOS BUILD"
echo "=========================================="
echo ""
echo "This will create a standalone macOS .dmg"
echo "that includes Java runtime (no Java needed on target Mac)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Check requirements
echo -e "${YELLOW}[1/4] Checking build requirements...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java 21+ required for building${NC}"
    echo "Download from: https://adoptium.net/"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}ERROR: Maven required for building${NC}"
    echo "Download from: https://maven.apache.org/"
    exit 1
fi

echo -e "${GREEN}✅ Build requirements OK${NC}"
echo ""

# Clean
echo -e "${YELLOW}[2/4] Cleaning previous builds...${NC}"
mvn clean -q

# Build JAR
echo -e "${YELLOW}[3/4] Building shaded JAR...${NC}"
mvn package -q
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Failed to build JAR${NC}"
    exit 1
fi

# Create native package
echo -e "${YELLOW}[4/4] Creating native macOS package...${NC}"
echo "This may take several minutes..."
mvn jpackage:jpackage -P mac

if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Failed to create native package${NC}"
    echo "Make sure you're running this on macOS with JDK 21+"
    exit 1
fi

echo ""
echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}     BUILD COMPLETED SUCCESSFULLY!${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

if ls target/dist/Quoridor-*.dmg 1> /dev/null 2>&1; then
    echo -e "${GREEN}Your macOS installer is ready:${NC}"
    ls -lh target/dist/Quoridor-*.dmg
    echo ""
    echo "This .dmg file:"
    echo "✅ Includes Java runtime (no Java needed on target Mac)"
    echo "✅ Includes JavaFX libraries"
    echo "✅ Creates Applications folder shortcut"
    echo "✅ Can be distributed as a single file"
    echo ""
    
    # Get file size in human readable format
    SIZE=$(ls -lh target/dist/Quoridor-*.dmg | awk '{print $5}')
    echo "File size: $SIZE"
else
    echo -e "${YELLOW}WARNING: DMG not found in expected location${NC}"
    echo "Check target/dist/ folder manually"
fi

echo ""
echo "Build complete!" 