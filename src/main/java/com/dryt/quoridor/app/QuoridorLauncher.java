package com.dryt.quoridor.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Launcher principal pour Quoridor - Optimisé pour exécutable standalone
 * Cette classe gère le lancement de l'application JavaFX avec diagnostics avancés
 */
public class QuoridorLauncher {
    
    private static final String LOG_FILE_NAME = "QuoridorLauncher.log";
    private static File logFile;
    private static boolean debugMode = false;
    
    static {
        // Initialiser le fichier de log
        try {
            String userHome = System.getProperty("user.home");
            logFile = new File(userHome, LOG_FILE_NAME);
            debugMode = Arrays.asList(System.getProperty("java.class.path", "").split(File.pathSeparator))
                    .stream().anyMatch(path -> path.contains("target") || path.contains("classes"));
        } catch (Exception e) {
            System.err.println("Impossible d'initialiser le logging: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        logInfo("=== DÉMARRAGE QUORIDOR LAUNCHER ===");
        logInfo("Version Java: " + System.getProperty("java.version"));
        logInfo("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        logInfo("Architecture: " + System.getProperty("os.arch"));
        logInfo("Arguments: " + Arrays.toString(args));
        
        try {
            // Configuration système pour optimiser JavaFX
            setupSystemProperties();
            
            // Vérifications pré-lancement
            performPreLaunchChecks();
            
            // Lancement de l'application
            launchApplication(args);
            
        } catch (Throwable e) {
            logError("Erreur critique lors du lancement", e);
            handleCriticalError(e, args);
        }
    }
    
    /**
     * Configure les propriétés système pour optimiser JavaFX et l'exécutable
     */
    private static void setupSystemProperties() {
        logInfo("Configuration des propriétés système...");
        
        // Propriétés d'encodage
        setSystemProperty("file.encoding", "UTF-8");
        setSystemProperty("sun.jnu.encoding", "UTF-8");
        
        // Propriétés JavaFX optimisées pour performance
        setSystemProperty("prism.verbose", "false");
        setSystemProperty("prism.allowhidpi", "true");
        setSystemProperty("prism.order", "d3d,sw");
        setSystemProperty("prism.vsync", "true");
        setSystemProperty("glass.accessible.force", "false");
        setSystemProperty("javafx.animation.fullspeed", "true");
        setSystemProperty("javafx.accessibility.screen_magnifier_threshold", "0");
        
        // Propriétés de compatibilité Windows
        setSystemProperty("java.awt.headless", "false");
        setSystemProperty("sun.java2d.d3d", "true");
        setSystemProperty("sun.java2d.dpiaware", "true");
        setSystemProperty("sun.java2d.uiScale.enabled", "true");
        
        // Optimisations mémoire
        setSystemProperty("java.util.concurrent.ForkJoinPool.common.parallelism", 
                         String.valueOf(Runtime.getRuntime().availableProcessors()));
        
        // Propriétés pour jpackage
        setSystemProperty("jpackage.app-version", "1.0.2");
        if (isRunningAsExecutable()) {
            setSystemProperty("javafx.runtime.path", System.getProperty("java.home"));
            setSystemProperty("launcher.standalone", "true");
        }
        
        logInfo("Propriétés système configurées avec succès");
    }
    
    /**
     * Définit une propriété système avec logging
     */
    private static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
        if (debugMode) {
            logInfo("Propriété définie: " + key + " = " + value);
        }
    }
    
    /**
     * Effectue les vérifications pré-lancement
     */
    private static void performPreLaunchChecks() throws Exception {
        logInfo("Vérifications pré-lancement...");
        
        // Vérifier JavaFX
        checkJavaFXAvailability();
        
        // Vérifier la mémoire disponible
        checkMemoryAvailability();
        
        // Vérifier les ressources critiques
        checkCriticalResources();
        
        logInfo("Toutes les vérifications pré-lancement ont réussi");
    }
    
    /**
     * Vérifie la disponibilité de JavaFX
     */
    private static void checkJavaFXAvailability() throws Exception {
        try {
            Class.forName("javafx.application.Application");
            Class.forName("javafx.stage.Stage");
            Class.forName("javafx.scene.Scene");
            logInfo("✅ JavaFX runtime détecté et accessible");
        } catch (ClassNotFoundException e) {
            logError("❌ JavaFX runtime non trouvé", e);
            throw new RuntimeException("JavaFX runtime non disponible. Vérifiez votre installation Java.", e);
        }
    }
    
    /**
     * Vérifie la mémoire disponible
     */
    private static void checkMemoryAvailability() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        logInfo("Mémoire max: " + (maxMemory / 1024 / 1024) + " MB");
        logInfo("Mémoire totale: " + (totalMemory / 1024 / 1024) + " MB");
        logInfo("Mémoire libre: " + (freeMemory / 1024 / 1024) + " MB");
        
        if (maxMemory < 256 * 1024 * 1024) { // Moins de 256 MB
            logInfo("⚠️ Mémoire limitée détectée, optimisations activées");
            System.setProperty("prism.forceGPU", "false");
            System.setProperty("prism.order", "sw");
        }
    }
    
    /**
     * Vérifie les ressources critiques
     */
    private static void checkCriticalResources() {
        try {
            // Vérifier les FXML
            String[] criticalFXML = {
                "/com/dryt/quoridor/views/menu.fxml",
                "/com/dryt/quoridor/views/jeu.fxml"
            };
            
            for (String fxml : criticalFXML) {
                if (QuoridorLauncher.class.getResource(fxml) == null) {
                    throw new RuntimeException("Ressource critique manquante: " + fxml);
                }
            }
            
            // Vérifier les CSS
            String[] criticalCSS = {
                "/com/dryt/quoridor/styles/style_menu.css",
                "/com/dryt/quoridor/styles/style_jeu.css"
            };
            
            for (String css : criticalCSS) {
                if (QuoridorLauncher.class.getResource(css) == null) {
                    logInfo("⚠️ Ressource CSS optionnelle manquante: " + css);
                }
            }
            
            logInfo("✅ Vérification des ressources critiques réussie");
            
        } catch (Exception e) {
            logError("❌ Échec de la vérification des ressources", e);
            throw new RuntimeException("Ressources critiques manquantes", e);
        }
    }
    
    /**
     * Lance l'application JavaFX
     */
    private static void launchApplication(String[] args) throws Exception {
        logInfo("Lancement de l'application JavaFX...");
        
        try {
            // Tentative de lancement standard
            logInfo("Tentative de lancement via JeuQuoridor.main()");
            JeuQuoridor.main(args);
            logInfo("✅ Lancement réussi via main()");
            
        } catch (Exception e) {
            logError("Échec du lancement via main(), tentative alternative", e);
            
            try {
                // Tentative alternative via Application.launch
                logInfo("Tentative de lancement via Application.launch()");
                javafx.application.Application.launch(JeuQuoridor.class, args);
                logInfo("✅ Lancement réussi via Application.launch()");
                
            } catch (Exception e2) {
                logError("Échec du lancement alternatif", e2);
                throw new RuntimeException("Impossible de lancer l'application JavaFX", e2);
            }
        }
    }
    
    /**
     * Gère les erreurs critiques avec tentatives de récupération
     */
    private static void handleCriticalError(Throwable error, String[] args) {
        logError("=== ERREUR CRITIQUE ===", error);
        
        try {
            // Tentative de récupération avec mode sécurisé
            logInfo("Tentative de récupération en mode sécurisé...");
            
            // Réinitialiser certaines propriétés problématiques
            System.setProperty("prism.forceGPU", "false");
            System.setProperty("prism.order", "sw");
            System.setProperty("glass.accessible.force", "false");
            
            // Essayer le lancement avec des paramètres conservateurs
            String[] safeArgs = Arrays.copyOf(args, args.length + 1);
            safeArgs[safeArgs.length - 1] = "--safe-mode";
            
            javafx.application.Application.launch(JeuQuoridor.class, safeArgs);
            logInfo("✅ Récupération réussie en mode sécurisé");
            
        } catch (Exception recoveryError) {
            logError("❌ Échec de la récupération", recoveryError);
            
            // Affichage final d'erreur à l'utilisateur
            showFinalErrorMessage(error);
            System.exit(1);
        }
    }
    
    /**
     * Affiche le message d'erreur final à l'utilisateur
     */
    private static void showFinalErrorMessage(Throwable error) {
        System.err.println("\n" + "=".repeat(60));
        System.err.println("         ÉCHEC DU LANCEMENT DE QUORIDOR");
        System.err.println("=".repeat(60));
        System.err.println("Erreur: " + error.getMessage());
        System.err.println("\nSolutions possibles:");
        System.err.println("1. Redémarrer l'application");
        System.err.println("2. Redémarrer votre ordinateur");
        System.err.println("3. Exécuter en tant qu'administrateur");
        System.err.println("4. Vérifier que votre pilote graphique est à jour");
        System.err.println("5. Consulter le log: " + (logFile != null ? logFile.getAbsolutePath() : "Indisponible"));
        System.err.println("\nSi le problème persiste, contactez le support avec le fichier de log.");
        System.err.println("=".repeat(60));
        
        // Pause pour laisser le temps de lire
        try {
            System.err.println("\nAppuyez sur Entrée pour fermer...");
            System.in.read();
        } catch (IOException ignored) {
            try {
                Thread.sleep(10000); // Attendre 10 secondes
            } catch (InterruptedException ignored2) {}
        }
    }
    
    /**
     * Détermine si l'application s'exécute comme exécutable packagé
     */
    private static boolean isRunningAsExecutable() {
        String javaHome = System.getProperty("java.home");
        String classPath = System.getProperty("java.class.path");
        
        // Vérifier si nous sommes dans un environnement jpackage
        return (classPath.contains(".exe") || classPath.contains("app/") || 
                javaHome.contains("runtime") || javaHome.contains("app/"));
    }
    
    /**
     * Enregistre un message d'information
     */
    private static void logInfo(String message) {
        String logMessage = formatLogMessage("INFO", message);
        System.out.println(logMessage);
        writeToLogFile(logMessage);
    }
    
    /**
     * Enregistre une erreur
     */
    private static void logError(String message, Throwable throwable) {
        String logMessage = formatLogMessage("ERROR", message);
        System.err.println(logMessage);
        if (throwable != null) {
            System.err.println("Détails: " + throwable.getMessage());
        }
        writeToLogFile(logMessage);
        if (throwable != null) {
            writeToLogFile("Détails: " + throwable.toString());
            writeStackTraceToLog(throwable);
        }
    }
    
    /**
     * Formate un message de log
     */
    private static String formatLogMessage(String level, String message) {
        return String.format("[%s] %s - %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                level, message);
    }
    
    /**
     * Écrit dans le fichier de log
     */
    private static void writeToLogFile(String message) {
        if (logFile != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(message);
            } catch (IOException e) {
                // Ignorer les erreurs de logging
            }
        }
    }
    
    /**
     * Écrit la stack trace dans le log
     */
    private static void writeStackTraceToLog(Throwable throwable) {
        if (logFile != null && throwable != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                throwable.printStackTrace(writer);
            } catch (IOException e) {
                // Ignorer les erreurs de logging
            }
        }
    }
} 