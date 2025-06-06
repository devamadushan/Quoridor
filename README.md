# Quoridor JavaFX - Installation et Compilation Complète (Windows, Linux, macOS)

Ce guide explique comment configurer un environnement Java/Maven complet sur Windows, Linux et macOS, compiler le projet Quoridor, et l'exécuter.

---

##  Étape 1 : Installer Java (JDK 21)

### Windows
1. Télécharger Java JDK 21 : https://jdk.java.net/21/
2. Installer le JDK (ex. `C:\Program Files\Java\jdk-21`)
3. Configurer les variables d'environnement (via ligne de commande) :

Ouvrir un terminal en mode administrateur (CMD ou PowerShell) et exécuter :
```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

Pour PowerShell :
```powershell
$oldPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = "$oldPath;%JAVA_HOME%\bin"
setx PATH "$newPath"
```
4. Redémarrer le terminal (ou l’ordinateur si nécessaire)
5. Vérification :
```bash
java -version
javac -version
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### macOS
```bash
brew install openjdk@21
sudo ln -sfn /opt/homebrew/opt/openjdk@21 /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```
Ajouter à votre shell (~/.zshrc ou ~/.bash_profile) :
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"
```
Puis :
```bash
source ~/.zshrc  # ou ~/.bash_profile
```

---

## 📦 Étape 2 : Installer Maven

### Windows
1. Télécharger Maven : https://maven.apache.org/download.cgi
2. Extraire Maven, par exemple dans `C:\maven\apache-maven-3.9.6`
3. Configurer les variables d'environnement (via ligne de commande) :
```cmd
setx M2_HOME "C:\maven\apache-maven-3.9.6"
setx PATH "%PATH%;%M2_HOME%\bin"
```
4. Redémarrer le terminal, puis vérifier :
```bash
mvn -version
```

### Linux
```bash
sudo apt install maven
mvn -version
```

### macOS
```bash
brew install maven
mvn -version
```

---

## Étape 3 : Télécharger le projet

1. Télécharger l’archive `Quoridor-main.zip`
2. Extraire-la dans un dossier, par exemple `~/Quoridor-main`

---

## Étape 4 : Compiler le projet

```bash
cd ~/Quoridor-main
mvn clean install
```

Résultat attendu : `target/Quoridor-1.0.2.jar`

---

## Étape 5 : Exécuter l'application

### Option 1 : via Maven
```bash
mvn javafx:run
```

### Option 2 : via le JAR

#### Windows
```bash
java -jar target/Quoridor-1.0.2.jar
```
(Si erreur JavaFX)
```bash
java --module-path C:/chemin/javafx-sdk-21/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/Quoridor-1.0.2.jar
```

#### Linux/macOS
```bash
java --module-path /chemin/javafx-sdk-21/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/Quoridor-1.0.2.jar
```

---
