@echo off
title Quoridor - Download and Play

echo.
echo  ██████  ██    ██  ██████  ██████  ██ ██████   ██████  ██████  
echo ██    ██ ██    ██ ██    ██ ██   ██ ██ ██   ██ ██    ██ ██   ██ 
echo ██    ██ ██    ██ ██    ██ ██████  ██ ██   ██ ██    ██ ██████  
echo ██ ▄▄ ██ ██    ██ ██    ██ ██   ██ ██ ██   ██ ██    ██ ██   ██ 
echo  ██████   ██████   ██████  ██   ██ ██ ██████   ██████  ██   ██ 
echo     ▀▀                                                         
echo.
echo Welcome to Quoridor Game Installer!
echo.

REM Configuration - CHANGEZ CES VALEURS AVEC VOTRE REPOSITORY GITHUB
set GITHUB_USER=devamadushan
set GITHUB_REPO=Quoridor
set DOWNLOAD_URL=https://github.com/%GITHUB_USER%/%GITHUB_REPO%/releases/latest/download

REM Créer un dossier temporaire sur le bureau
set "GAME_DIR=%USERPROFILE%\Desktop\Quoridor-Game"
echo Creating game directory: %GAME_DIR%
if exist "%GAME_DIR%" rmdir /s /q "%GAME_DIR%"
mkdir "%GAME_DIR%"

echo.
echo Downloading Quoridor game files...
echo Please wait, this may take a few moments...
echo.

REM Télécharger avec PowerShell - essayer d'abord l'exe Windows, puis le DMG Mac
powershell -Command "& {
    $ProgressPreference = 'SilentlyContinue'
    try {
        Write-Host '▓▓▓       Downloading Quoridor game...'
        
        # Try Windows exe first
        try {
            Invoke-WebRequest -Uri '%DOWNLOAD_URL%/Quoridor.exe' -OutFile '%GAME_DIR%\Quoridor.exe'
            Write-Host '▓▓▓▓▓     Windows executable downloaded successfully!'
            $downloadedExe = $true
        } catch {
            Write-Host '▓▓▓       Windows exe not available, trying macOS installer...'
            try {
                Invoke-WebRequest -Uri '%DOWNLOAD_URL%/Quoridor-Mac.dmg' -OutFile '%GAME_DIR%\Quoridor-Mac.dmg'
                Write-Host '▓▓▓▓▓     macOS installer downloaded successfully!'
                $downloadedDmg = $true
            } catch {
                throw 'Neither Windows exe nor macOS dmg could be downloaded'
            }
        }
        
        Write-Host '▓▓▓▓▓▓    Downloading game resources...'
        Invoke-WebRequest -Uri '%DOWNLOAD_URL%/resources.zip' -OutFile '%GAME_DIR%\resources.zip'
        
        Write-Host '▓▓▓▓▓▓▓▓  Extracting resources...'
        Expand-Archive -Path '%GAME_DIR%\resources.zip' -DestinationPath '%GAME_DIR%' -Force
        Remove-Item '%GAME_DIR%\resources.zip' -Force
        
        Write-Host '▓▓▓▓▓▓▓▓▓ Download completed!'
        exit 0
    } catch {
        Write-Host 'ERROR: Could not download game files'
        Write-Host 'Please check:'
        Write-Host '- Your internet connection'
        Write-Host '- The GitHub repository is accessible'
        Write-Host ''
        Write-Host 'Error details: ' $_.Exception.Message
        exit 1
    }
}"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Download failed!
    echo.
    echo Troubleshooting:
    echo 1. Check your internet connection
    echo 2. Make sure you have the latest version of this script
    echo 3. Try running as administrator
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Download successful!
echo.
echo Game installed to: %GAME_DIR%
echo.

REM Check what was downloaded and launch accordingly
if exist "%GAME_DIR%\Quoridor.exe" (
    echo 🎮 Ready to play! Launching Quoridor Windows version...
    echo.
    cd /d "%GAME_DIR%"
    start "" "Quoridor.exe"
    echo.
    echo 🎯 Quoridor has been launched!
) else if exist "%GAME_DIR%\Quoridor-Mac.dmg" (
    echo 🍎 macOS installer downloaded!
    echo.
    echo Please double-click on Quoridor-Mac.dmg to install the game.
    echo Location: %GAME_DIR%\Quoridor-Mac.dmg
    echo.
    start "" explorer "%GAME_DIR%"
) else (
    echo ❌ No executable found!
)

echo.
echo Enjoy your game! The game files are saved in:
echo %GAME_DIR%
echo.
echo You can create a desktop shortcut to the game executable for easy access.
echo.
pause 