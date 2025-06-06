# Quoridor Native Build Guide

This guide explains how to create **self-contained executables** for Windows and macOS that **don't require Java to be pre-installed** on the target machine.

## 🎯 What You'll Get

- **Windows**: `Quoridor-1.0.2.exe` installer (~60-80MB)
- **macOS**: `Quoridor-1.0.2.dmg` installer (~60-80MB)
- Both include embedded Java runtime + JavaFX
- Users can run immediately after installation

## 📋 Prerequisites

### For Building (Development Machine)
- **Java 21+** (JDK, not just JRE) - [Download from Adoptium](https://adoptium.net/)
- **Maven 3.6+** - [Download from Apache Maven](https://maven.apache.org/)

### Platform Requirements
- **Windows executable**: Must build on Windows machine
- **macOS executable**: Must build on macOS machine
- **Cross-platform building is not supported** by jpackage

## 🚀 Quick Start

### Windows Build (on Windows machine)
```cmd
# Double-click or run in Command Prompt:
build-windows-native.bat
```

### macOS Build (on macOS machine)
```bash
# Run in Terminal:
./build-mac-native.sh
```

## 📁 Output Files

After successful build, you'll find:
- **Windows**: `target/dist/Quoridor-1.0.2.exe`
- **macOS**: `target/dist/Quoridor-1.0.2.dmg`

## 🎮 End User Experience

### Windows
1. Download `Quoridor-1.0.2.exe`
2. Double-click to install
3. Creates desktop shortcut + start menu entry
4. No Java installation required!

### macOS
1. Download `Quoridor-1.0.2.dmg`
2. Open DMG and drag to Applications
3. Double-click in Applications to run
4. No Java installation required!

## 🔧 Manual Build Commands

If you prefer command line:

### Windows
```cmd
mvn clean package
mvn jpackage:jpackage -P windows
```

### macOS
```bash
mvn clean package
mvn jpackage:jpackage -P mac
```

## 🛠️ Technical Details

### What's Included
- Java 21 Runtime (embedded)
- JavaFX 21 libraries
- All game dependencies
- Optimized JVM settings

### Maven Profiles
- `windows`: Creates Windows .exe installer
- `mac`: Creates macOS .dmg installer

### JVM Optimizations
- `-Xms256m -Xmx1024m`: Memory settings
- `-Dprism.forceGPU=true`: Hardware acceleration
- `-Djavafx.animation.fullspeed=true`: Smooth animations
- `-Dfile.encoding=UTF-8`: Character encoding

## 🐛 Troubleshooting

### Build Errors

**"Java not found"**
- Install Java 21+ JDK (not JRE)
- Verify with: `java -version`

**"Maven not found"**
- Install Maven 3.6+
- Verify with: `mvn -version`

**"jpackage failed"**
- Ensure you're building on the target platform
- Windows .exe must be built on Windows
- macOS .dmg must be built on macOS

### Runtime Issues

**"App won't start"**
- Rebuilding usually fixes corrupt packages
- Check file permissions on macOS

**Large file size**
- Normal - includes full Java runtime (~60-80MB)
- Alternative: Use launch4j for smaller Windows exe (but requires Java on target)

## 🔄 Alternative Solutions

If you need smaller executables or have other requirements:

### Launch4j (Windows only)
- Creates smaller .exe (~5MB)
- Requires Java on target machine
- Good for technical users

### GraalVM Native Image
- Creates very small executables (~20MB)
- Requires code modifications
- Complex setup for JavaFX

### jlink + jpackage
- Custom JRE with only needed modules
- Smaller size but more complex
- Current setup already optimized

## 📦 Distribution

### For End Users
- Distribute the single .exe or .dmg file
- No additional files needed
- Users don't need Java installed

### For Developers
- Keep source code and build scripts
- Document any custom modifications
- Test on clean machines without Java

## 🎯 Summary

The current setup using **jpackage with Maven profiles** is the best approach for JavaFX applications because:

✅ **No Java required** on target machines  
✅ **Single file distribution**  
✅ **Native OS integration** (shortcuts, menus)  
✅ **Professional appearance**  
✅ **Cross-platform support** (when built on respective platforms)  

This is the industry standard for distributing JavaFX applications in 2024. 