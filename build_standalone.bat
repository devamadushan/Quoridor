@echo off
setlocal enabledelayedexpansion

:: ================================
:: Script de Build Quoridor v1.0.2
:: Génération d'un exécutable standalone Windows
:: ================================

title Building Quoridor Standalone Executable

:: Configuration
set VERSION=1.0.2
set PROJECT_NAME=Quoridor
set MAIN_CLASS=com.dryt.quoridor.app.QuoridorLauncher
set BUILD_DIR=%CD%
set TARGET_DIR=%BUILD_DIR%\target
set DIST_DIR=%TARGET_DIR%\dist
set RUNTIME_DIR=%TARGET_DIR%\runtime

:: Couleurs pour les messages
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set PURPLE=[95m
set CYAN=[96m
set WHITE=[97m
set NC=[0m

echo %CYAN%========================================%NC%
echo %CYAN%   QUORIDOR STANDALONE BUILD SCRIPT    %NC%
echo %CYAN%========================================%NC%
echo.
echo %WHITE%Version:       %GREEN%v%VERSION%%NC%
echo %WHITE%Projet:        %GREEN%%PROJECT_NAME%%NC%
echo %WHITE%Main Class:    %GREEN%%MAIN_CLASS%%NC%
echo %WHITE%Build Dir:     %GREEN%%BUILD_DIR%%NC%
echo %WHITE%Date/Heure:    %GREEN%!date! !time!%NC%
echo.

:: Vérification de Java
echo %YELLOW%[1/10] Vérification de l'environnement Java...%NC%
java -version 2>nul || (
    echo %RED%ERREUR: Java n'est pas installé ou accessible dans PATH%NC%
    echo %WHITE%Veuillez installer Java 21 ou supérieur%NC%
    pause
    exit /b 1
)

javac -version 2>nul || (
    echo %RED%ERREUR: JDK n'est pas installé (javac introuvable)%NC%
    echo %WHITE%Veuillez installer le JDK 21 complet%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Java installé et accessible%NC%

:: Vérification de Maven
echo %YELLOW%[2/10] Vérification de Maven...%NC%
mvn -version 2>nul || (
    echo %RED%ERREUR: Maven n'est pas installé ou accessible dans PATH%NC%
    echo %WHITE%Veuillez installer Apache Maven%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Maven installé et accessible%NC%

:: Affichage des informations système
echo %YELLOW%[3/10] Informations système...%NC%
echo %WHITE%JAVA_HOME: %GREEN%!JAVA_HOME!%NC%
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr "version"') do echo %WHITE%Java: %GREEN%%%i%NC%
for /f "tokens=*" %%i in ('mvn -version 2^>^&1 ^| findstr "Apache Maven"') do echo %WHITE%Maven: %GREEN%%%i%NC%

:: Nettoyage des builds précédents
echo %YELLOW%[4/10] Nettoyage des artefacts précédents...%NC%
if exist "%TARGET_DIR%" (
    echo %WHITE%Suppression du répertoire target...%NC%
    rmdir /s /q "%TARGET_DIR%" 2>nul
)

if exist "%DIST_DIR%" (
    echo %WHITE%Suppression du répertoire dist...%NC%
    rmdir /s /q "%DIST_DIR%" 2>nul
)

echo %GREEN%✓ Nettoyage terminé%NC%

:: Validation du pom.xml
echo %YELLOW%[5/10] Validation de la configuration Maven...%NC%
if not exist "pom.xml" (
    echo %RED%ERREUR: Fichier pom.xml introuvable%NC%
    pause
    exit /b 1
)

mvn help:effective-pom -Doutput=effective-pom.xml -q || (
    echo %RED%ERREUR: Configuration Maven invalide%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Configuration Maven valide%NC%

:: Résolution des dépendances
echo %YELLOW%[6/10] Téléchargement des dépendances...%NC%
mvn dependency:resolve -B -q || (
    echo %RED%ERREUR: Échec du téléchargement des dépendances%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Dépendances téléchargées%NC%

:: Compilation et packaging
echo %YELLOW%[7/10] Compilation et empaquetage...%NC%
mvn clean compile -B -q || (
    echo %RED%ERREUR: Échec de la compilation%NC%
    pause
    exit /b 1
)

mvn package -B -DskipTests=true -q || (
    echo %RED%ERREUR: Échec de l'empaquetage%NC%
    pause
    exit /b 1
)

:: Vérification du JAR généré
if not exist "%TARGET_DIR%\quoridor-%VERSION%.jar" (
    echo %RED%ERREUR: JAR principal non généré%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Compilation et empaquetage réussis%NC%

:: Création du runtime avec jlink
echo %YELLOW%[8/10] Création du runtime personnalisé...%NC%
set JLINK_PATH=%JAVA_HOME%\bin\jlink.exe
if not exist "%JLINK_PATH%" (
    echo %RED%ERREUR: jlink introuvable dans %JLINK_PATH%%NC%
    pause
    exit /b 1
)

echo %WHITE%Création du runtime avec jlink...%NC%
"%JLINK_PATH%" ^
    --module-path "%JAVA_HOME%\jmods" ^
    --add-modules java.base,java.desktop,java.logging,java.xml,java.naming,java.security.sasl,java.security.jgss,java.net.http,javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media ^
    --output "%RUNTIME_DIR%" ^
    --compress=2 ^
    --no-header-files ^
    --no-man-pages ^
    --strip-debug || (
    echo %RED%ERREUR: Échec de la création du runtime%NC%
    pause
    exit /b 1
)

if not exist "%RUNTIME_DIR%" (
    echo %RED%ERREUR: Runtime non créé%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Runtime personnalisé créé%NC%

:: Création de l'exécutable avec jpackage
echo %YELLOW%[9/10] Création de l'exécutable Windows...%NC%
mvn org.panteleyev:jpackage-maven-plugin:1.6.0:jpackage -B -q || (
    echo %RED%ERREUR: Échec de la création de l'exécutable%NC%
    echo %WHITE%Tentative avec paramètres alternatifs...%NC%
    
    :: Tentative alternative avec jpackage direct
    set JPACKAGE_PATH=%JAVA_HOME%\bin\jpackage.exe
    if exist "!JPACKAGE_PATH!" (
        echo %WHITE%Utilisation de jpackage direct...%NC%
        "!JPACKAGE_PATH!" ^
            --name "%PROJECT_NAME%" ^
            --input "%TARGET_DIR%" ^
            --main-jar "quoridor-%VERSION%.jar" ^
            --main-class "%MAIN_CLASS%" ^
            --runtime-image "%RUNTIME_DIR%" ^
            --dest "%DIST_DIR%" ^
            --type exe ^
            --app-version "%VERSION%" ^
            --vendor "DryT Games" ^
            --description "Jeu de stratégie Quoridor avec IA" ^
            --win-console ^
            --win-shortcut ^
            --win-menu ^
            --java-options "-Xms512m" ^
            --java-options "-Xmx1024m" ^
            --java-options "-Dfile.encoding=UTF-8" ^
            --java-options "-Dlauncher.standalone=true" || (
            echo %RED%ERREUR: Impossible de créer l'exécutable%NC%
            pause
            exit /b 1
        )
    ) else (
        echo %RED%ERREUR: jpackage introuvable%NC%
        pause
        exit /b 1
    )
)

:: Vérification de l'exécutable créé
echo %YELLOW%[10/10] Vérification de l'exécutable...%NC%

if not exist "%DIST_DIR%" (
    echo %RED%ERREUR: Répertoire de distribution non créé%NC%
    pause
    exit /b 1
)

:: Recherche de l'exécutable
set EXE_FILE=
for /r "%DIST_DIR%" %%f in (*.exe) do (
    set EXE_FILE=%%f
    goto :found_exe
)

:found_exe
if not defined EXE_FILE (
    echo %RED%ERREUR: Aucun exécutable trouvé%NC%
    pause
    exit /b 1
)

:: Informations sur l'exécutable
for %%f in ("%EXE_FILE%") do (
    set EXE_SIZE=%%~zf
    set EXE_NAME=%%~nxf
)

set /a EXE_SIZE_MB=EXE_SIZE/1048576

echo %GREEN%✓ Exécutable créé avec succès%NC%
echo.

:: Résumé final
echo %CYAN%========================================%NC%
echo %CYAN%          BUILD TERMINÉ AVEC SUCCÈS     %NC%
echo %CYAN%========================================%NC%
echo.
echo %WHITE%📦 Exécutable:     %GREEN%!EXE_NAME!%NC%
echo %WHITE%📁 Emplacement:    %GREEN%!EXE_FILE!%NC%
echo %WHITE%📏 Taille:         %GREEN%!EXE_SIZE_MB! MB%NC%
echo %WHITE%🕒 Durée totale:   %GREEN%!time!%NC%
echo.

:: Affichage du contenu du répertoire de distribution
echo %YELLOW%Contenu du répertoire de distribution:%NC%
dir "%DIST_DIR%" /b /s

echo.
echo %YELLOW%Options disponibles:%NC%
echo %WHITE%[1] Lancer l'exécutable%NC%
echo %WHITE%[2] Ouvrir le dossier de destination%NC%
echo %WHITE%[3] Créer un installateur MSI%NC%
echo %WHITE%[4] Quitter%NC%
echo.

set /p choice="Votre choix (1-4): "

if "%choice%"=="1" (
    echo %GREEN%Lancement de l'exécutable...%NC%
    start "" "!EXE_FILE!"
) else if "%choice%"=="2" (
    echo %GREEN%Ouverture du dossier...%NC%
    explorer "%DIST_DIR%"
) else if "%choice%"=="3" (
    goto :create_msi
) else (
    goto :end
)

goto :end

:create_msi
echo %YELLOW%Création de l'installateur MSI...%NC%

:: Modification temporaire du pom.xml pour MSI
powershell -Command "(Get-Content pom.xml) -replace '<type>EXE</type>', '<type>MSI</type>' | Set-Content pom_temp.xml"
copy pom_temp.xml pom.xml > nul

:: Création de l'installateur MSI
mvn org.panteleyev:jpackage-maven-plugin:1.6.0:jpackage -B -q

:: Restauration du pom.xml original
git checkout pom.xml 2>nul || (
    powershell -Command "(Get-Content pom.xml) -replace '<type>MSI</type>', '<type>EXE</type>' | Set-Content pom.xml"
)

del pom_temp.xml 2>nul

if exist "%DIST_DIR%\*.msi" (
    echo %GREEN%✓ Installateur MSI créé avec succès%NC%
    for %%f in ("%DIST_DIR%\*.msi") do echo %WHITE%📦 MSI: %GREEN%%%~nxf%NC%
) else (
    echo %RED%⚠️ Échec de la création de l'installateur MSI%NC%
)

:end
echo.
echo %GREEN%Build terminé. Merci d'avoir utilisé le script de build Quoridor!%NC%
if exist "effective-pom.xml" del "effective-pom.xml" 2>nul
pause 