package com.dryt.quoridor.utils;

import java.util.prefs.Preferences;

public class UserPreferences {
    private static final String BACKGROUND_KEY = "selected_background";
    private static final String DEFAULT_BACKGROUND = "zudarts-lee-150831-13.jpg";
    
    private static final String RESOLUTION_KEY = "selected_resolution";
    private static final String DEFAULT_RESOLUTION = "Dynamique";
    
    private static final Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);
    
    public static void setSelectedBackground(String backgroundName) {
        prefs.put(BACKGROUND_KEY, backgroundName);
        System.out.println("Arrière-plan sauvegardé : " + backgroundName);
    }
    
    public static String getSelectedBackground() {
        String selected = prefs.get(BACKGROUND_KEY, DEFAULT_BACKGROUND);
        System.out.println("Arrière-plan chargé : " + selected);
        return selected;
    }
    
    public static void setSelectedResolution(String resolution) {
        prefs.put(RESOLUTION_KEY, resolution);
        System.out.println("Résolution sauvegardée : " + resolution);
    }
    
    public static String getSelectedResolution() {
        String selected = prefs.get(RESOLUTION_KEY, DEFAULT_RESOLUTION);
        System.out.println("Résolution chargée : " + selected);
        return selected;
    }
    
    public static void clearPreferences() {
        try {
            prefs.clear();
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression des préférences: " + e.getMessage());
        }
    }
} 