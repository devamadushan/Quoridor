package com.dryt.quoridor.app;

/**
 * Launcher class for Quoridor game
 * Cette classe sert de point d'entrée principal pour jpackage
 * Version simplifiée pour éviter les problèmes Windows
 */
public class QuoridorLauncher {
    public static void main(String[] args) {
        try {
            // Configuration système de base
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("javafx.animation.fullspeed", "true");
            
            // Lancer directement l'application JavaFX
            JeuQuoridor.main(args);
            
        } catch (Exception e) {
            // En cas d'erreur, essayer le lancement alternatif
            System.err.println("Première tentative échouée, essai alternatif...");
            try {
                javafx.application.Application.launch(JeuQuoridor.class, args);
            } catch (Exception e2) {
                System.err.println("Erreur critique: " + e2.getMessage());
                e2.printStackTrace();
                
                // Attendre avant de fermer pour que l'utilisateur puisse voir l'erreur
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
                
                System.exit(1);
            }
        }
    }
} 