@echo off
echo Creating simple Quoridor executable...

REM Create basic app-image
"C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\jpackage.exe" --type app-image --input target --main-jar Quoridor-1.0.2-shaded.jar --main-class com.dryt.quoridor.app.QuoridorLauncher --name Quoridor --dest target\dist

REM Copy the JAR to fix missing file issue
copy target\Quoridor-1.0.2-shaded.jar target\dist\Quoridor\app\

echo Done! Test with: .\target\dist\Quoridor\Quoridor.exe
pause 