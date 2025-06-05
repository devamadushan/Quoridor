# 🚀 QUORIDOR - Guide Rapide

## 📁 **Fichiers Importants**
- `cross-platform-release.sh` - **Script principal de release**
- `download-and-play.bat` - **Script pour vos joueurs**

## ⚡ **Workflow Ultra-Simple**

### **1. Modifier votre code**
```bash
# Testez localement
mvn clean javafx:run
```

### **2. Créer une release**
```bash
./cross-platform-release.sh
```
Le script fait TOUT :
- ✅ Incrémente la version automatiquement
- ✅ Build l'installeur Mac (.dmg)
- ✅ Optionnel : ajoute un .exe Windows si disponible  
- ✅ Crée `resources.zip` avec tous les assets
- ✅ Commit et push sur GitHub avec tag

### **3. Release GitHub (2 minutes)**
1. **GitHub** → **Releases** → **Create new release**
2. **Sélectionnez le tag** créé par le script
3. **Uploadez les fichiers** du dossier `release/`
4. **Publish release**

### **4. Partager avec vos joueurs**
Ils téléchargent **SEULEMENT** : `download-and-play.bat`
- ✅ Détecte automatiquement Mac/Windows
- ✅ Télécharge le bon installeur  
- ✅ Lance le jeu automatiquement

## 🎯 **C'est tout !**

**Workflow complet :** Code → `./cross-platform-release.sh` → Upload GitHub → Partage ! 

**Temps total :** ~5 minutes ⚡ 