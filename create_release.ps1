# QUORIDOR RELEASE BUILDER v1.0
# PowerShell version for better reliability

param(
    [string]$Version = "1.0.2"
)

$ErrorActionPreference = "Stop"

# Configuration
$MainClass = "com.dryt.quoridor.app.QuoridorLauncher"
$AppName = "Quoridor"
$Vendor = "DRYT"
$Description = "Quoridor Strategy Board Game"
$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "   QUORIDOR RELEASE BUILDER v1.0" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

try {
    # Step 1: Build shaded JAR
    Write-Host "Step 1: Building shaded JAR..." -ForegroundColor Yellow
    & mvn clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed!" }
    Write-Host "✓ Shaded JAR created successfully" -ForegroundColor Green
    Write-Host ""

    # Step 2: Prepare clean input directory
    Write-Host "Step 2: Preparing clean input directory..." -ForegroundColor Yellow
    if (Test-Path "temp_input") { Remove-Item "temp_input" -Recurse -Force }
    New-Item -ItemType Directory -Name "temp_input" | Out-Null
    Copy-Item "target\$AppName-$Version-shaded.jar" "temp_input\"
    Write-Host "✓ Clean input directory prepared" -ForegroundColor Green
    Write-Host ""

    # Step 3: Create app-image
    Write-Host "Step 3: Creating app-image with jpackage..." -ForegroundColor Yellow
    if (Test-Path "target\dist") { Remove-Item "target\dist" -Recurse -Force }
    
    $jpackageArgs = @(
        "--type", "app-image"
        "--input", "temp_input"
        "--main-jar", "$AppName-$Version-shaded.jar"
        "--main-class", $MainClass
        "--name", $AppName
        "--app-version", $Version
        "--vendor", $Vendor
        "--description", $Description
        "--dest", "target\dist"
        "--java-options", "-Dfile.encoding=UTF-8"
        "--java-options", "-Xms256m"
        "--java-options", "-Xmx1024m"
    )
    
    & "$JavaHome\bin\jpackage.exe" @jpackageArgs
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed!" }
    Write-Host "✓ App-image created successfully" -ForegroundColor Green
    Write-Host ""

    # Step 4: Add README
    Write-Host "Step 4: Adding README file..." -ForegroundColor Yellow
    if (Test-Path "RELEASE_README.txt") {
        Copy-Item "RELEASE_README.txt" "target\dist\$AppName\README.txt"
        Write-Host "✓ README added" -ForegroundColor Green
    } else {
        Write-Host "⚠ RELEASE_README.txt not found - skipping" -ForegroundColor Yellow
    }
    Write-Host ""

    # Step 5: Create release ZIP
    Write-Host "Step 5: Creating release ZIP..." -ForegroundColor Yellow
    $zipName = "$AppName-v$Version-Windows.zip"
    if (Test-Path $zipName) { Remove-Item $zipName }
    
    Add-Type -assembly "system.io.compression.filesystem"
    [io.compression.zipfile]::CreateFromDirectory("target\dist\$AppName", $zipName)
    Write-Host "✓ Release ZIP created" -ForegroundColor Green
    Write-Host ""

    # Step 6: Cleanup
    Write-Host "Step 6: Cleaning up..." -ForegroundColor Yellow
    Remove-Item "temp_input" -Recurse -Force
    Write-Host "✓ Temporary files cleaned" -ForegroundColor Green
    Write-Host ""

    # Success summary
    Write-Host "====================================" -ForegroundColor Green
    Write-Host "        RELEASE COMPLETE! 🎉" -ForegroundColor Green
    Write-Host "====================================" -ForegroundColor Green
    Write-Host ""
    
    $zipFile = Get-Item $zipName
    $sizeMB = [math]::Round($zipFile.Length / 1MB, 2)
    
    Write-Host "Release file: $zipName" -ForegroundColor White
    Write-Host "Size: $($zipFile.Length) bytes ($sizeMB MB)" -ForegroundColor White
    Write-Host "Location: $($zipFile.FullName)" -ForegroundColor White
    Write-Host "Executable: target\dist\$AppName\$AppName.exe" -ForegroundColor White
    Write-Host ""
    Write-Host "To test: .\target\dist\$AppName\$AppName.exe" -ForegroundColor Cyan
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "ERROR: $_" -ForegroundColor Red
    Write-Host ""
    exit 1
}

Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 