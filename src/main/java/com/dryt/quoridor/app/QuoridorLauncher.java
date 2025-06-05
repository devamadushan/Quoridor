package com.dryt.quoridor.app;

/**
 * Launcher class for Quoridor game
 * Cette classe sert de point d'entrée principal pour jpackage
 * Elle évite les problèmes de lancement avec JavaFX Application
 */
public class QuoridorLauncher {
    public static void main(String[] args) {
        try {
            System.out.println("🎮 Starting Quoridor Game...");
            
            // Propriétés système optimisées pour JavaFX
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("prism.verbose", "false"); // Désactiver les logs verbeux
            System.setProperty("prism.forceGPU", "true");  // Forcer l'utilisation du GPU
            
            // Lancer l'application JavaFX
            JeuQuoridor.main(args);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement de Quoridor: " + e.getMessage());
            e.printStackTrace();
            
            // Tentative alternative en lançant directement la classe JavaFX
            try {
                System.out.println("🔄 Tentative de lancement direct de JavaFX...");
                javafx.application.Application.launch(JeuQuoridor.class, args);
            } catch (Exception e2) {
                System.err.println("❌ Échec complet du lancement: " + e2.getMessage());
                e2.printStackTrace();
                System.exit(1);
            }
        }
    }
} 