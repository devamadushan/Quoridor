@echo off
echo Creating functional Quoridor executable...

REM Delete existing dist folder
if exist target\dist rmdir /s /q target\dist

REM Create the app-image using Java 21 jpackage
echo Step 1: Creating base app-image...
"C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\jpackage.exe" ^
  --type app-image ^
  --input target ^
  --main-jar Quoridor-1.0.2-shaded.jar ^
  --main-class com.dryt.quoridor.app.QuoridorLauncher ^
  --name Quoridor ^
  --app-version 1.0.2 ^
  --vendor DRYT ^
  --description "Quoridor Strategy Board Game" ^
  --dest target\dist ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Dprism.forceGPU=true" ^
  --java-options "-Djavafx.animation.fullspeed=true" ^
  --java-options "-Xms256m" ^
  --java-options "-Xmx1024m"

REM Fix the missing JAR issue
echo Step 2: Copying shaded JAR to app directory...
copy target\Quoridor-1.0.2-shaded.jar target\dist\Quoridor\app\

REM Fix the configuration file
echo Step 3: Fixing configuration file...
echo [Application] > target\dist\Quoridor\app\Quoridor.cfg
echo app.classpath=$APPDIR\Quoridor-1.0.2-shaded.jar >> target\dist\Quoridor\app\Quoridor.cfg
echo app.mainclass=com.dryt.quoridor.app.QuoridorLauncher >> target\dist\Quoridor\app\Quoridor.cfg
echo. >> target\dist\Quoridor\app\Quoridor.cfg
echo [JavaOptions] >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Djpackage.app-version=1.0.2 >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Dfile.encoding=UTF-8 >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Dprism.forceGPU=true >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Djavafx.animation.fullspeed=true >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Xms256m >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Xmx1024m >> target\dist\Quoridor\app\Quoridor.cfg

echo.
echo ✓ Executable created successfully!
echo ✓ Location: target\dist\Quoridor\Quoridor.exe
echo ✓ Configuration file fixed
echo ✓ The executable is now ready for distribution
echo.
echo To test: .\target\dist\Quoridor\Quoridor.exe
pause 