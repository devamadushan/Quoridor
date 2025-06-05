package com.dryt.quoridor.utils;

import java.util.ArrayList;
import java.util.List;

public class BackgroundManager {
    
    public static class BackgroundInfo {
        private final String fileName;
        private final String displayName;
        
        public BackgroundInfo(String fileName, String displayName) {
            this.fileName = fileName;
            this.displayName = displayName;
        }
        
        public String getFileName() { 
            return fileName; 
        }
        
        public String getDisplayName() { 
            return displayName; 
        }
    }
    
    private static final List<BackgroundInfo> AVAILABLE_BACKGROUNDS = new ArrayList<>();
    
    static {
        // Ajouter tous les fonds d'écran disponibles
        AVAILABLE_BACKGROUNDS.add(new BackgroundInfo("zudarts-lee-150831-13.jpg", "Paysage mystique"));
        AVAILABLE_BACKGROUNDS.add(new BackgroundInfo("montagne-medievale.jpg", "Montagnes médiévales"));
        AVAILABLE_BACKGROUNDS.add(new BackgroundInfo("chateau-fantastique.jpg", "Château fantastique"));
        AVAILABLE_BACKGROUNDS.add(new BackgroundInfo("paysage-anime.gif", "Paysage animé"));
        
        // Ajouter d'autres backgrounds ici si nécessaire :
        // AVAILABLE_BACKGROUNDS.add(new BackgroundInfo("autre-background.jpg", "Autre thème"));
    }
    
    /**
     * Retourne la liste de tous les backgrounds disponibles
     */
    public static List<BackgroundInfo> getAvailableBackgrounds() {
        return new ArrayList<>(AVAILABLE_BACKGROUNDS);
    }
    
    /**
     * Trouve un background par son nom de fichier
     */
    public static BackgroundInfo getBackgroundByFileName(String fileName) {
        return AVAILABLE_BACKGROUNDS.stream()
                .filter(bg -> bg.getFileName().equals(fileName))
                .findFirst()
                .orElse(AVAILABLE_BACKGROUNDS.get(0)); // retourne le premier par défaut
    }
    
    /**
     * Retourne l'index d'un background dans la liste
     */
    public static int getBackgroundIndex(String fileName) {
        for (int i = 0; i < AVAILABLE_BACKGROUNDS.size(); i++) {
            if (AVAILABLE_BACKGROUNDS.get(i).getFileName().equals(fileName)) {
                return i;
            }
        }
        return 0; // retourne 0 par défaut si non trouvé
    }
    
    /**
     * Retourne un background par son index
     */
    public static BackgroundInfo getBackgroundByIndex(int index) {
        if (index >= 0 && index < AVAILABLE_BACKGROUNDS.size()) {
            return AVAILABLE_BACKGROUNDS.get(index);
        }
        return AVAILABLE_BACKGROUNDS.get(0); // retourne le premier par défaut
    }
    
    /**
     * Retourne le nombre total de backgrounds disponibles
     */
    public static int getBackgroundCount() {
        return AVAILABLE_BACKGROUNDS.size();
    }
    
    /**
     * Retourne le background par défaut
     */
    public static BackgroundInfo getDefaultBackground() {
        return AVAILABLE_BACKGROUNDS.get(0);
    }
    
    /**
     * Vérifie si un background existe
     */
    public static boolean backgroundExists(String fileName) {
        return AVAILABLE_BACKGROUNDS.stream()
                .anyMatch(bg -> bg.getFileName().equals(fileName));
    }
}
