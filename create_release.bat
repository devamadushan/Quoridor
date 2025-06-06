@echo off
echo ====================================
echo   QUORIDOR RELEASE BUILDER v1.0
echo ====================================
echo.

REM Configuration - Update these when needed
set VERSION=1.0.2
set MAIN_CLASS=com.dryt.quoridor.app.QuoridorLauncher
set APP_NAME=Quoridor
set VENDOR=DRYT
set DESCRIPTION=Quoridor Strategy Board Game
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot

echo Step 1: Building shaded JAR...
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo ✓ Shaded JAR created successfully

echo.
echo Step 2: Preparing clean input directory...
if exist temp_input rmdir /s /q temp_input
mkdir temp_input
copy target\%APP_NAME%-%VERSION%-shaded.jar temp_input\
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to copy shaded JAR!
    pause
    exit /b 1
)
echo ✓ Clean input directory prepared

echo.
echo Step 3: Creating app-image with jpackage...
if exist target\dist rmdir /s /q target\dist
"%JAVA_HOME%\bin\jpackage.exe" ^
    --type app-image ^
    --input temp_input ^
    --main-jar %APP_NAME%-%VERSION%-shaded.jar ^
    --main-class %MAIN_CLASS% ^
    --name %APP_NAME% ^
    --app-version %VERSION% ^
    --vendor "%VENDOR%" ^
    --description "%DESCRIPTION%" ^
    --dest target\dist ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "-Xms256m" ^
    --java-options "-Xmx1024m"

if %ERRORLEVEL% neq 0 (
    echo ERROR: jpackage failed!
    pause
    exit /b 1
)
echo ✓ App-image created successfully

echo.
echo Step 4: Adding README file...
copy RELEASE_README.txt target\dist\%APP_NAME%\README.txt
echo ✓ README added

echo.
echo Step 5: Creating release ZIP...
if exist "%APP_NAME%-v%VERSION%-Windows.zip" del "%APP_NAME%-v%VERSION%-Windows.zip"
powershell -Command "Add-Type -assembly 'system.io.compression.filesystem'; [io.compression.zipfile]::CreateFromDirectory('target\dist\%APP_NAME%', '%APP_NAME%-v%VERSION%-Windows.zip')"
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create ZIP file!
    pause
    exit /b 1
)
echo ✓ Release ZIP created

echo.
echo Step 6: Cleaning up...
rmdir /s /q temp_input
echo ✓ Temporary files cleaned

echo.
echo ====================================
echo        RELEASE COMPLETE! 🎉
echo ====================================
echo.
echo Release file: %APP_NAME%-v%VERSION%-Windows.zip
for %%A in ("%APP_NAME%-v%VERSION%-Windows.zip") do echo Size: %%~zA bytes (%%~zA bytes)

echo.
echo Location: %CD%\%APP_NAME%-v%VERSION%-Windows.zip
echo Executable: target\dist\%APP_NAME%\%APP_NAME%.exe
echo.
echo To test: .\target\dist\%APP_NAME%\%APP_NAME%.exe
echo.
pause 