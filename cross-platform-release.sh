#!/bin/bash

echo "🌍 QUORIDOR - CROSS-PLATFORM RELEASE"
echo "====================================="
echo

# Get current version
CURRENT_VERSION=$(grep -o '<version>.*</version>' pom.xml | head -1 | sed 's/<version>\(.*\)<\/version>/\1/')
echo "📋 Current version: $CURRENT_VERSION"
echo

# Ask for new version
read -p "🔢 Enter new version (ex: 1.0.1): " NEW_VERSION

if [ -z "$NEW_VERSION" ]; then
    echo "❌ No version provided. Exiting..."
    exit 1
fi

echo
echo "📝 Updating version from $CURRENT_VERSION to $NEW_VERSION..."
sed -i '' "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/" pom.xml
echo "✅ Version updated in pom.xml"
echo

# Test compilation
echo "🧪 Testing compilation..."
mvn clean compile > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Compilation failed! Fix errors before release."
    sed -i '' "s/<version>$NEW_VERSION<\/version>/<version>$CURRENT_VERSION<\/version>/" pom.xml
    exit 1
fi
echo "✅ Compilation successful"
echo

# Build macOS version
echo "🍎 Building macOS installer..."
./build-mac.sh

if [ $? -ne 0 ]; then
    echo "❌ macOS build failed!"
    sed -i '' "s/<version>$NEW_VERSION<\/version>/<version>$CURRENT_VERSION<\/version>/" pom.xml
    exit 1
fi

# Prepare release with macOS files
./prepare-release-mac.sh

echo
echo "✅ macOS release prepared!"
echo

# Check if Windows exe is available
echo "🔍 Checking for Windows executable..."
read -p "📱 Do you have Quoridor.exe from Windows build? (y/n): " HAS_WINDOWS_EXE

if [[ $HAS_WINDOWS_EXE =~ ^[Yy]$ ]]; then
    read -p "📂 Enter path to Quoridor.exe: " WINDOWS_EXE_PATH
    
    if [ -f "$WINDOWS_EXE_PATH" ]; then
        echo "📋 Adding Windows exe to release..."
        cp "$WINDOWS_EXE_PATH" "release/Quoridor.exe"
        echo "✅ Windows exe added!"
        RELEASE_TYPE="CROSS-PLATFORM"
    else
        echo "❌ File not found: $WINDOWS_EXE_PATH"
        RELEASE_TYPE="MAC-ONLY"
    fi
else
    echo "ℹ️  Continuing with Mac-only release"
    RELEASE_TYPE="MAC-ONLY"
fi

echo
echo "🎯 Release Type: $RELEASE_TYPE"
echo

# Git operations
echo "📤 Committing changes..."
git add .

if [ "$RELEASE_TYPE" = "CROSS-PLATFORM" ]; then
    COMMIT_MSG="🚀 Release v$NEW_VERSION (Cross-Platform)

✅ macOS installer: Quoridor-Mac.dmg
✅ Windows executable: Quoridor.exe  
✅ Resources archive: resources.zip
✅ Smart installer: download-and-play.bat

Platform support:
- macOS: Native .dmg installer
- Windows: Native .exe executable
- All platforms: Shared resources

Ready for distribution!"
else
    COMMIT_MSG="🚀 Release v$NEW_VERSION (macOS)

✅ macOS installer: Quoridor-Mac.dmg
✅ Resources archive: resources.zip
✅ Smart installer: download-and-play.bat

Platform support:
- macOS: Native .dmg installer
- Windows: Java fallback via resources.zip

Ready for distribution!"
fi

git commit -m "$COMMIT_MSG"
git tag "v$NEW_VERSION"
git push origin main --tags

echo
echo "✅ ========================================="
echo "    CROSS-PLATFORM RELEASE COMPLETE!"
echo "========================================="
echo
echo "📁 Files ready in: release/"

if [ "$RELEASE_TYPE" = "CROSS-PLATFORM" ]; then
    echo "   ├── Quoridor-Mac.dmg    (macOS installer)"
    echo "   ├── Quoridor.exe        (Windows executable)"
    echo "   ├── resources.zip       (shared resources)"
    echo "   └── download-and-play.bat (smart installer)"
    echo
    echo "🎉 COMPLETE cross-platform support!"
else
    echo "   ├── Quoridor-Mac.dmg    (macOS installer)"
    echo "   ├── resources.zip       (shared resources)"
    echo "   └── download-and-play.bat (smart installer)"
    echo
    echo "📝 Note: Windows users will need Java or you can add .exe later"
fi

echo
echo "🌐 Next steps:"
echo "1. Go to: https://github.com/devamadushan/Quoridor/releases"
echo "2. Click 'Create a new release'"
echo "3. Select tag: v$NEW_VERSION"
echo "4. Upload ALL files from release/ folder"
echo "5. Publish release"
echo
echo "🎯 Your players can then use download-and-play.bat for automatic installation!" 