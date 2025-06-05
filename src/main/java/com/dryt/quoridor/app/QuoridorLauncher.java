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
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
            System.out.println("OS: " + System.getProperty("os.name"));
            
            // Propriétés système optimisées pour JavaFX
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("prism.verbose", "false"); // Désactiver les logs verbeux
            System.setProperty("prism.forceGPU", "true");  // Forcer l'utilisation du GPU
            System.setProperty("javafx.animation.fullspeed", "true");
            System.setProperty("glass.accessible.force", "false"); // Éviter problèmes d'accessibilité
            
            // Vérifier que JavaFX est disponible
            try {
                Class.forName("javafx.application.Application");
                System.out.println("✅ JavaFX runtime detected");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ JavaFX runtime not found!");
                throw new RuntimeException("JavaFX not available", e);
            }
            
            // Check for fallback mode argument
            boolean fallbackMode = false;
            for (String arg : args) {
                if ("--javafx-fallback-mode".equals(arg)) {
                    fallbackMode = true;
                    break;
                }
            }
            
            if (fallbackMode) {
                System.out.println("🔄 Using JavaFX fallback mode...");
                javafx.application.Application.launch(JeuQuoridor.class, args);
            } else {
                // Lancer l'application JavaFX normalement
                System.out.println("🚀 Launching Quoridor via main method...");
                JeuQuoridor.main(args);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement de Quoridor: " + e.getMessage());
            e.printStackTrace();
            
            // Tentative alternative en lançant directement la classe JavaFX
            try {
                System.out.println("🔄 Tentative de lancement direct de JavaFX...");
                javafx.application.Application.launch(JeuQuoridor.class, args);
            } catch (Exception e2) {
                System.err.println("💥 Échec complet du lancement: " + e2.getMessage());
                e2.printStackTrace();
                
                // Pause pour que l'utilisateur puisse voir l'erreur
                System.err.println("\nAppuyez sur Entrée pour fermer...");
                try {
                    System.in.read();
                } catch (Exception ignored) {}
                
                System.exit(1);
            }
        }
    }
} 