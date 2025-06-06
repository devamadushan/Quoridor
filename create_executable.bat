@echo off
echo Creating Quoridor standalone executable...

echo Step 1: Creating app-image...
"C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\jpackage.exe" --type app-image --input target --main-jar Quoridor-1.0.2-shaded.jar --main-class com.dryt.quoridor.app.QuoridorLauncher --name Quoridor --dest target\dist

echo Step 2: Copying shaded JAR to fix missing file issue...
copy target\Quoridor-1.0.2-shaded.jar target\dist\Quoridor\app\

echo Step 3: Creating proper configuration...
echo [Application] > target\dist\Quoridor\app\Quoridor.cfg
echo app.classpath=$APPDIR\Quoridor-1.0.2-shaded.jar >> target\dist\Quoridor\app\Quoridor.cfg
echo app.mainclass=com.dryt.quoridor.app.QuoridorLauncher >> target\dist\Quoridor\app\Quoridor.cfg
echo. >> target\dist\Quoridor\app\Quoridor.cfg
echo [JavaOptions] >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=-Dfile.encoding=UTF-8 >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.base/com.sun.javafx.logging=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.graphics/com.sun.prism=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg
echo java-options=--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED >> target\dist\Quoridor\app\Quoridor.cfg

echo.
echo ✓ Standalone executable created successfully!
echo ✓ Location: target\dist\Quoridor\Quoridor.exe
echo ✓ Size: ~6-8 GB (includes complete Java runtime)
echo ✓ Ready for distribution - works on any Windows PC!
echo.
echo To test: .\target\dist\Quoridor\Quoridor.exe
pause 