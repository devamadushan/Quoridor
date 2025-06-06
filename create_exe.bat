@echo off
echo Creating Quoridor executable...

REM Delete existing dist folder
if exist target\dist rmdir /s /q target\dist

REM Create the app-image using Java 21 jpackage
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

echo Done! Executable created at target\dist\Quoridor\Quoridor.exe
pause 