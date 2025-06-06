#!/bin/bash

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}==========================================${NC}"
echo -e "${YELLOW}    CRÉATION DU DMG AUTONOME QUORIDOR${NC}"
echo -e "${YELLOW}==========================================${NC}"
echo ""

# Créer le dossier temporaire
TEMP_DIR="temp_build"
if [ -d "$TEMP_DIR" ]; then
    rm -rf "$TEMP_DIR"
fi
mkdir -p "$TEMP_DIR"
cd "$TEMP_DIR"

# Télécharger et installer JDK 21
echo -e "${YELLOW}[1/5] Téléchargement de JDK 21...${NC}"
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_mac_hotspot_21.0.4_7.tar.gz"
curl -L "$JDK_URL" -o jdk.tar.gz
tar xzf jdk.tar.gz
mv jdk-21.0.4+7 jdk
rm jdk.tar.gz

# Télécharger et installer Maven
echo -e "${YELLOW}[2/5] Téléchargement de Maven...${NC}"
MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
curl -L "$MAVEN_URL" -o maven.tar.gz
tar xzf maven.tar.gz
mv apache-maven-3.9.6 maven
rm maven.tar.gz

# Configurer les variables d'environnement
export JAVA_HOME="$PWD/jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PWD/maven/bin:$PATH"

# Cloner le projet
echo -e "${YELLOW}[3/5] Téléchargement du projet...${NC}"
git clone https://github.com/devamadushan/Quoridor.git
cd Quoridor

# Compiler le projet
echo -e "${YELLOW}[4/5] Compilation du projet...${NC}"
mvn clean package

# Créer le DMG
echo -e "${YELLOW}[5/5] Création du DMG...${NC}"
mvn jpackage:jpackage -P mac -Dmac.sign=false

# Vérifier si le DMG a été créé
DMG_FILE=$(find target/dist -name "Quoridor-*.dmg" -type f)
if [ -n "$DMG_FILE" ]; then
    echo -e "${GREEN}✅ DMG créé avec succès !${NC}"
    echo -e "${GREEN}Le fichier se trouve dans : $DMG_FILE${NC}"
    
    # Copier le DMG dans le dossier parent
    cp "$DMG_FILE" ../../
    echo -e "${GREEN}Le DMG a été copié dans le dossier principal${NC}"
else
    echo -e "${RED}❌ Erreur lors de la création du DMG${NC}"
    exit 1
fi

# Nettoyage
cd ../..
rm -rf "$TEMP_DIR"

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}    CRÉATION TERMINÉE AVEC SUCCÈS !${NC}"
echo -e "${GREEN}==========================================${NC}"
echo ""
echo -e "${YELLOW}Le DMG contient :${NC}"
echo "✅ JDK 21 complet"
echo "✅ JavaFX 21"
echo "✅ Toutes les dépendances"
echo "✅ Application Quoridor"
echo ""
echo -e "${YELLOW}Pour installer :${NC}"
echo "1. Double-cliquez sur le DMG"
echo "2. Glissez l'application dans le dossier Applications"
echo "3. Lancez Quoridor depuis le dossier Applications" 