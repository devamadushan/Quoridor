package com.dryt.quoridor.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.ai.DifficulteIA;
import com.dryt.quoridor.controller.ControleurJeu;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class JeuQuoridor extends Application {
    // Variables statiques pour la gestion des scènes et du jeu
    public static Stage primaryStage;
    public static Scene sceneMenu;
    public static Scene sceneOptions;
    public static Scene sceneChoixJoueurs;

    private static int nombreJoueurs = 2;
    private static boolean isVsAI = false;
    private static int nombreIA4Joueurs = 0;
    private static Plateau plateau;
    private static DifficulteIA difficulteIA = DifficulteIA.MOYEN;
    private static List<DifficulteIA> difficultesIA = new ArrayList<>();
    private static int[] selectedSkins = new int[4];
    
    private static double screenWidth;
    private static double screenHeight;
    private static double scaleFactorX = 1.0;
    private static double scaleFactorY = 1.0;
    private static boolean isMaximized = true;
    private static double currentResolutionWidth = 1920.0;
    private static double currentResolutionHeight = 1080.0;
    
    private static String optionsPreviousContext = "menu";
    private static ControleurJeu currentGameController = null;
    private static Scene currentGameScene = null;

    private static String currentBackgroundFileName = null;
    
    private static MediaPlayer globalBackgroundMusic = null;
    private static boolean isMusicMuted = false;
    private static double savedMusicVolume = 0.3;
    
    private static String[] musicPlaylist = {
        "Highland Hymn Bonnie Grace.mp3",
        "Fresh Findings.mp3", 
        "Fantasy Music Goblinized.mp3"
    };
    private static int currentSongIndex = 0;
    
    private static boolean backgroundWasPreserved = false;

    // Mode sécurisé pour exécutable standalone
    private static boolean safeMode = false;
    private static boolean standaloneMode = false;

    // Initialise et démarre l'application JavaFX
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Détecter le mode d'exécution
        detectExecutionMode();
        
        try {
        detectScreenResolution();
        } catch (Exception e) {
            System.err.println("Erreur détection résolution, utilisation valeurs par défaut: " + e.getMessage());
            setDefaultResolution();
        }
        
        double windowWidth = screenWidth;
        double windowHeight = screenHeight;

        // Chargement sécurisé des scènes
        loadApplicationScenes(windowWidth, windowHeight);

        // Configuration de la fenêtre principale
        setupPrimaryStage(stage);
        
        // Initialisation de la musique en mode sécurisé
        if (!safeMode) {
            try {
                initializeGlobalMusic();
            } catch (Exception e) {
                System.err.println("Impossible d'initialiser la musique (mode sécurisé activé): " + e.getMessage());
            }
        }
    
        stage.show();
        
        logStartupInfo();
    }
    
    /**
     * Détecte le mode d'exécution (standalone, safe mode, etc.)
     */
    private void detectExecutionMode() {
        // Vérifier les arguments de lancement
        List<String> params = getParameters().getRaw();
        safeMode = params.contains("--safe-mode") || params.contains("--javafx-fallback-mode");
        
        // Détecter le mode standalone
        standaloneMode = "true".equals(System.getProperty("launcher.standalone")) ||
                        System.getProperty("java.class.path", "").contains(".exe");
        
        if (safeMode) {
            System.out.println("🔒 Mode sécurisé activé - fonctionnalités limitées");
            // Désactiver certaines optimisations en mode sécurisé
            System.setProperty("prism.forceGPU", "false");
            System.setProperty("prism.order", "sw");
        }
        
        if (standaloneMode) {
            System.out.println("📦 Mode exécutable standalone détecté");
        }
    }
    
    /**
     * Charge les scènes de l'application de manière sécurisée
     */
    private void loadApplicationScenes(double windowWidth, double windowHeight) throws Exception {
        try {
            Parent menuRoot = loadFXMLSafely("/com/dryt/quoridor/views/menu.fxml");
        sceneMenu = new Scene(menuRoot, windowWidth, windowHeight);

            Parent optionsRoot = loadFXMLSafely("/com/dryt/quoridor/views/options.fxml");
        sceneOptions = new Scene(optionsRoot, windowWidth, windowHeight);

            Parent choixRoot = loadFXMLSafely("/com/dryt/quoridor/views/choix_joueurs.fxml");
        sceneChoixJoueurs = new Scene(choixRoot, windowWidth, windowHeight);

            // Application des styles CSS de manière sécurisée
            applySafeStylesheets();
            
            // Configuration des événements clavier
            setupKeyboardEvents();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des scènes: " + e.getMessage());
            throw new RuntimeException("Impossible de charger l'interface utilisateur", e);
        }
    }
    
    /**
     * Charge un fichier FXML de manière sécurisée
     */
    private Parent loadFXMLSafely(String fxmlPath) throws Exception {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new RuntimeException("Ressource FXML introuvable: " + fxmlPath);
        }
        
        FXMLLoader loader = new FXMLLoader(resource);
        return loader.load();
    }
    
    /**
     * Applique les feuilles de style de manière sécurisée
     */
    private void applySafeStylesheets() {
        try {
            String menuCSS = getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm();
            sceneMenu.getStylesheets().add(menuCSS);
            sceneOptions.getStylesheets().add(menuCSS);
            sceneChoixJoueurs.getStylesheets().add(menuCSS);
        } catch (Exception e) {
            System.err.println("Impossible de charger les styles CSS: " + e.getMessage());
            // Continuer sans styles CSS si nécessaire
        }
    }
    
    /**
     * Configure les événements clavier
     */
    private void setupKeyboardEvents() {
        sceneMenu.setOnKeyPressed(e -> handleKeyPress(e));
        sceneOptions.setOnKeyPressed(e -> handleKeyPress(e));
        sceneChoixJoueurs.setOnKeyPressed(e -> handleKeyPress(e));
    }
    
    /**
     * Configure la fenêtre principale
     */
    private void setupPrimaryStage(Stage stage) {
        stage.setTitle("Jeu Quoridor v1.0.2" + (standaloneMode ? " (Standalone)" : ""));
        stage.setScene(sceneMenu);
        stage.setResizable(false);
        
        // Application de la résolution sauvegardée
        try {
        applySavedResolution();
        } catch (Exception e) {
            System.err.println("Erreur application résolution: " + e.getMessage());
            // Utiliser la résolution par défaut
            stage.setMaximized(true);
            isMaximized = true;
        }
    }
    
    /**
     * Définit une résolution par défaut en cas d'erreur
     */
    private void setDefaultResolution() {
        screenWidth = 1920.0;
        screenHeight = 1080.0;
        scaleFactorX = 1.0;
        scaleFactorY = 1.0;
        System.out.println("Résolution par défaut appliquée: " + screenWidth + "x" + screenHeight);
    }
    
    /**
     * Affiche les informations de démarrage
     */
    private void logStartupInfo() {
        System.out.println("=== QUORIDOR DÉMARRÉ ===");
        System.out.println("Résolution détectée: " + screenWidth + "x" + screenHeight);
        System.out.println("Mode: " + (isMaximized ? "Maximisé" : "Fenêtré"));
        System.out.println("Mode sécurisé: " + (safeMode ? "Activé" : "Désactivé"));
        System.out.println("Mode standalone: " + (standaloneMode ? "Activé" : "Désactivé"));
        System.out.println("Raccourcis: F11 (plein écran), Échap (sortir du plein écran)");
        System.out.println("========================");
    }
    
    // Détecte la résolution de l'écran et calcule les facteurs d'échelle
    private static void detectScreenResolution() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = screenBounds.getWidth();
        screenHeight = screenBounds.getHeight();
        
        scaleFactorX = screenWidth / 1920.0;
        scaleFactorY = screenHeight / 1080.0;
        
        double uniformScale = Math.min(scaleFactorX, scaleFactorY);
        scaleFactorX = uniformScale;
        scaleFactorY = uniformScale;
    }
    
    // Gère les événements clavier pour les raccourcis
    private static void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.F11) {
            isMaximized = !isMaximized;
            primaryStage.setMaximized(isMaximized);
        } else if (event.getCode() == KeyCode.ESCAPE && primaryStage.isMaximized()) {
            primaryStage.setMaximized(false);
            isMaximized = false;
        }
    }
    
    // Calcule la taille optimale de la fenêtre
    private static double[] calculateOptimalWindowSize() {
        double baseWidth = 1400.0;
        double baseHeight = 900.0;
        
        double maxScale = 1.2;
        double effectiveScaleX = Math.min(scaleFactorX, maxScale);
        double effectiveScaleY = Math.min(scaleFactorY, maxScale);
        
        double targetWidth = Math.min(baseWidth * effectiveScaleX, screenWidth * 0.8);
        double targetHeight = Math.min(baseHeight * effectiveScaleY, screenHeight * 0.8);
        
        targetWidth = Math.max(targetWidth, 1000.0);
        targetHeight = Math.max(targetHeight, 700.0);
        
        if (screenWidth >= 2560 || screenHeight >= 1440) {
            targetWidth = Math.min(targetWidth, 1600.0);
            targetHeight = Math.min(targetHeight, 1000.0);
        }
        
        return new double[]{targetWidth, targetHeight};
    }
    
    // Centre la fenêtre sur l'écran
    private static void centerStageOnScreen(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - width) / 2.0;
        double centerY = (screenBounds.getHeight() - height) / 2.0;
        
        stage.setX(centerX);
        stage.setY(centerY);
    }

    // Retourne la scène principale
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    // Retourne le facteur d'échelle horizontal
    public static double getScaleFactorX() {
        return scaleFactorX;
    }
    
    // Retourne le facteur d'échelle vertical
    public static double getScaleFactorY() {
        return scaleFactorY;
    }
    
    // Retourne la largeur de l'écran
    public static double getScreenWidth() {
        return screenWidth;
    }
    
    // Retourne la hauteur de l'écran
    public static double getScreenHeight() {
        return screenHeight;
    }

    // Définit le nombre de joueurs
    public static void setNombreJoueurs(int nb) {
        nombreJoueurs = nb;
        if (nb == 2) {
             isVsAI = false;
        }
    }

    // Retourne le nombre de joueurs
    public static int getNombreJoueurs() {
        return nombreJoueurs;
    }

    // Définit si le mode 1v1 est contre une IA
    public static void setIsVsAI(boolean vsAI) {
        isVsAI = vsAI;
    }

    // Retourne si le mode 1v1 est contre une IA
    public static boolean getIsVsAI() {
        return isVsAI;
    }

    // Définit le nombre d'IA en mode 4 joueurs
    public static void setNombreIA4Joueurs(int nb) {
        nombreIA4Joueurs = nb;
    }

    // Retourne le nombre d'IA en mode 4 joueurs
    public static int getNombreIA4Joueurs() {
        return nombreIA4Joueurs;
    }

    // Définit les skins sélectionnés
    public static void setSelectedSkins(int[] skins) {
        selectedSkins = Arrays.copyOf(skins, skins.length);
    }

    // Retourne les skins sélectionnés
    public static int[] getSelectedSkins() {
        return Arrays.copyOf(selectedSkins, selectedSkins.length);
    }

    // Retourne le plateau de jeu
    public static Plateau getPlateau() {
        return plateau;
    }

    // Démarre une nouvelle partie
    public static void startGame() throws Exception {
        startGame(null);
    }
    
    // Démarre une nouvelle partie avec un fond préservé
    public static void startGame(String preserveCurrentBackground) throws Exception {
        try {
            logGameStartInfo();

        double windowWidth = screenWidth;
        double windowHeight = screenHeight;

        URL resource = JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml");
        if (resource == null) {
            throw new Exception("Impossible de trouver jeu.fxml");
        }
        
        FXMLLoader loader = new FXMLLoader(resource);
        Parent gameRoot = loader.load();
        
        ControleurJeu controleur = loader.getController();
        currentGameController = controleur;
        
        Scene sceneJeu = new Scene(gameRoot, windowWidth, windowHeight);
        
            // Application sécurisée des styles
            applySafeGameStyles(sceneJeu);
        
            sceneJeu.setOnKeyPressed(e -> handleKeyPress(e));
            currentGameScene = sceneJeu;
        primaryStage.setScene(sceneJeu);
        
        applySavedResolution();
        System.out.println("Jeu démarré avec la résolution : " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
        
            // Application du fond d'écran
            applyGameBackground(preserveCurrentBackground);
            
            // Création du plateau
            createGamePlateau();
            
            controleur.setupPlateauAndDisplay(plateau);
            
            // Démarrage de la musique si pas en mode sécurisé
            if (!safeMode) {
                startGlobalMusic(true);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du jeu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Applique les styles de jeu de manière sécurisée
     */
    private static void applySafeGameStyles(Scene sceneJeu) {
        try {
            String gameCSS = JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm();
            sceneJeu.getStylesheets().add(gameCSS);
        } catch (Exception e) {
            System.err.println("Impossible de charger les styles de jeu: " + e.getMessage());
            // Continuer sans styles si nécessaire
        }
    }
    
    /**
     * Applique le fond d'écran du jeu
     */
    private static void applyGameBackground(String preserveCurrentBackground) {
        try {
            String backgroundToApply;
            if (preserveCurrentBackground != null && !preserveCurrentBackground.isEmpty()) {
                backgroundToApply = preserveCurrentBackground;
                backgroundWasPreserved = true;
                System.out.println("Préservation du fond actuel : " + backgroundToApply);
            } else {
                backgroundToApply = com.dryt.quoridor.utils.UserPreferences.getSelectedBackground();
                backgroundWasPreserved = false;
                System.out.println("Utilisation du fond sauvegardé : " + backgroundToApply);
            }
            
            if (backgroundToApply != null && !backgroundToApply.isEmpty()) {
                updateGameBackground(backgroundToApply);
            }
        } catch (Exception e) {
            System.err.println("Impossible d'appliquer le fond : " + e.getMessage());
        }
        }
        
    /**
     * Crée le plateau de jeu selon la configuration
     */
    private static void createGamePlateau() {
        if (nombreJoueurs == 2) {
            if (isVsAI) {
                plateau = new Plateau(22, 1);
            } else {
                plateau = new Plateau(21, 0);
            }
        } else {
            plateau = new Plateau(4, nombreIA4Joueurs);
        }
    }
    
    /**
     * Affiche les informations de démarrage du jeu
     */
    private static void logGameStartInfo() {
        System.out.println("=== DÉMARRAGE DU JEU ===");
        if (nombreJoueurs == 2) {
            System.out.println("Mode: " + (isVsAI ? "1 VS 1 IA" : "1 VS 1 Humain"));
        } else {
            System.out.println("Mode: 4 joueurs avec " + nombreIA4Joueurs + " IA");
        }

        System.out.println("Configuration des joueurs :");
        if (nombreJoueurs == 2) {
            System.out.println("Joueur 1 - Humain");
            System.out.println("Joueur 2 - " + (isVsAI ? "IA" : "Humain"));
            } else {
            for (int i = 1; i <= 4; i++) {
                boolean isAI = i > (4 - nombreIA4Joueurs);
                System.out.println("Joueur " + i + " - " + (isAI ? "IA" : "Humain"));
            }
        }
        System.out.println("=======================");
    }

    // Initialise la musique de fond globale - Version sécurisée
    public static void initializeGlobalMusic() {
        if (safeMode) {
            System.out.println("Musique désactivée en mode sécurisé");
            return;
        }
        
        try {
            if (globalBackgroundMusic != null) {
                globalBackgroundMusic.stop();
                globalBackgroundMusic.dispose();
                System.out.println("Musique globale précédente libérée");
            }
            
            String currentSong = musicPlaylist[currentSongIndex];
            URL musicResource = JeuQuoridor.class.getResource("/com/dryt/quoridor/sounds/" + currentSong);
            
            if (musicResource == null) {
                System.err.println("Fichier musical introuvable: " + currentSong);
                return;
            }
            
            String musicPath = musicResource.toExternalForm();
            Media music = new Media(musicPath);
            globalBackgroundMusic = new MediaPlayer(music);
            
            globalBackgroundMusic.setVolume(savedMusicVolume);
            globalBackgroundMusic.setAutoPlay(false);
            
            globalBackgroundMusic.setOnEndOfMedia(() -> {
                advanceToNextSong();
            });
            
            globalBackgroundMusic.setOnError(() -> {
                System.err.println("Erreur de lecture musicale: " + globalBackgroundMusic.getError());
            });
            
            System.out.println("Musique de fond globale initialisée : " + currentSong);
        } catch (Exception e) {
            System.err.println("Échec de l'initialisation de la musique globale : " + e.getMessage());
            // Continuer sans musique
        }
    }
    
    // Passe à la chanson suivante
    private static void advanceToNextSong() {
        currentSongIndex = (currentSongIndex + 1) % musicPlaylist.length;
        System.out.println("Passage à la chanson suivante : " + musicPlaylist[currentSongIndex]);
        
        boolean wasPlaying = (globalBackgroundMusic != null && globalBackgroundMusic.getStatus() == MediaPlayer.Status.PLAYING);
        initializeGlobalMusic();
        if (wasPlaying && !isMusicMuted) {
            globalBackgroundMusic.play();
        }
    }
    
    // Démarre la musique de fond
    public static void startGlobalMusic() {
        startGlobalMusic(false);
    }
    
    // Démarre la musique de fond avec option nouvelle partie
    public static void startGlobalMusic(boolean isNewGame) {
        if (isNewGame) {
            currentSongIndex = (currentSongIndex + 1) % musicPlaylist.length;
            System.out.println("Nouvelle partie - changement vers : " + musicPlaylist[currentSongIndex]);
            initializeGlobalMusic();
        }
        
        if (globalBackgroundMusic != null && !isMusicMuted) {
            try {
                globalBackgroundMusic.play();
                System.out.println("Musique de fond globale démarrée : " + musicPlaylist[currentSongIndex]);
        } catch (Exception e) {
                System.err.println("Échec du démarrage de la musique globale : " + e.getMessage());
            }
        }
    }
    
    // Arrête la musique de fond
    public static void stopGlobalMusic() {
        if (globalBackgroundMusic != null) {
            try {
                globalBackgroundMusic.stop();
                System.out.println("Musique de fond globale arrêtée");
        } catch (Exception e) {
                System.err.println("Échec de l'arrêt de la musique globale : " + e.getMessage());
            }
        }
    }
    
    // Met en pause la musique de fond
    public static void pauseGlobalMusic() {
        if (globalBackgroundMusic != null) {
            try {
                globalBackgroundMusic.pause();
                System.out.println("Musique de fond globale mise en pause");
        } catch (Exception e) {
                System.err.println("Échec de la mise en pause de la musique globale : " + e.getMessage());
            }
        }
    }
    
    // Définit le volume de la musique
    public static void setGlobalMusicVolume(double volume) {
        savedMusicVolume = volume;
        if (globalBackgroundMusic != null && !isMusicMuted) {
            globalBackgroundMusic.setVolume(volume);
        }
    }
    
    // Bascule la sourdine de la musique
    public static void toggleGlobalMusicMute() {
        isMusicMuted = !isMusicMuted;
        if (globalBackgroundMusic != null) {
            globalBackgroundMusic.setVolume(isMusicMuted ? 0.0 : savedMusicVolume);
        }
        System.out.println("Musique globale en sourdine : " + isMusicMuted);
    }
    
    // Retourne si la musique est en sourdine
    public static boolean isGlobalMusicMuted() {
        return isMusicMuted;
    }
    
    // Retourne le volume de la musique
    public static double getGlobalMusicVolume() {
        return savedMusicVolume;
    }

    // Retourne si le fond a été préservé
    public static boolean wasBackgroundPreserved() {
        return backgroundWasPreserved;
    }
    
    // Réinitialise le drapeau de préservation du fond
    public static void resetBackgroundPreservedFlag() {
        backgroundWasPreserved = false;
    }

    // Déclenche le redimensionnement du plateau
    public static void triggerGameBoardResize() {
        if (currentGameController != null) {
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(e -> {
                javafx.application.Platform.runLater(() -> {
                    try {
                        System.out.println("Déclenchement du redimensionnement du plateau après changement de résolution");
                        currentGameController.triggerBoardResize();
                    } catch (Exception ex) {
                        System.err.println("Erreur lors du redimensionnement du plateau : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            });
            delay.play();
        }
    }

    // Retourne si l'application est en mode sécurisé
    public static boolean isSafeMode() {
        return safeMode;
    }
    
    // Retourne si l'application est en mode standalone
    public static boolean isStandaloneMode() {
        return standaloneMode;
    }

    // Point d'entrée principal de l'application - Version améliorée
    public static void main(String[] args) {
        try {
            System.out.println("🎮 Démarrage de Quoridor...");
            
            // Vérification des arguments spéciaux
            boolean hasSpecialArgs = Arrays.asList(args).stream()
                .anyMatch(arg -> arg.startsWith("--") || arg.startsWith("-"));
            
            if (hasSpecialArgs) {
                System.out.println("Arguments spéciaux détectés: " + Arrays.toString(args));
            }
            
            // Tentative de lancement standard JavaFX
            launch(args);
            
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Application launch must not be called more than once")) {
                System.err.println("Application déjà lancée, ignoring additional launch attempt");
                return;
            }
            
            System.err.println("Erreur IllegalStateException: " + e.getMessage());
            handleAlternativeLaunch(args);
            
        } catch (RuntimeException e) {
            if (e.getCause() instanceof UnsupportedOperationException && 
                e.getMessage() != null && e.getMessage().contains("JavaFX")) {
                System.err.println("JavaFX non supporté dans cet environnement");
                System.exit(1);
            }
            
            System.err.println("Erreur RuntimeException lors du lancement: " + e.getMessage());
            handleAlternativeLaunch(args);
            
        } catch (Exception e) {
            System.err.println("Erreur générale lors du lancement: " + e.getMessage());
            e.printStackTrace();
            handleAlternativeLaunch(args);
        }
    }
    
    /**
     * Gère les méthodes de lancement alternatives
     */
    private static void handleAlternativeLaunch(String[] args) {
        try {
            System.out.println("🔄 Tentative de lancement alternatif...");
            
            // Vérifier si nous pouvons créer un nouveau thread d'application
            Thread launchThread = new Thread(() -> {
                try {
                    Application.launch(JeuQuoridor.class, args);
                } catch (Exception e) {
                    System.err.println("Échec du lancement dans nouveau thread: " + e.getMessage());
                }
            });
            
            launchThread.setDaemon(false);
            launchThread.start();
            launchThread.join();
            
        } catch (Exception e2) {
            System.err.println("💥 Tous les méthodes de lancement ont échoué: " + e2.getMessage());
            e2.printStackTrace();
            
            // Message d'erreur final pour l'utilisateur
            showCriticalErrorMessage();
            System.exit(1);
        }
    }
    
    /**
     * Affiche un message d'erreur critique
     */
    private static void showCriticalErrorMessage() {
        System.err.println("\n" + "=".repeat(60));
        System.err.println("         IMPOSSIBLE DE DÉMARRER QUORIDOR");
        System.err.println("=".repeat(60));
        System.err.println("Le système JavaFX n'a pas pu être initialisé.");
        System.err.println("\nSolutions possibles:");
        System.err.println("1. Redémarrer l'application");
        System.err.println("2. Redémarrer votre ordinateur");
        System.err.println("3. Exécuter en tant qu'administrateur");
        System.err.println("4. Vérifier que Java est correctement installé");
        System.err.println("5. Mettre à jour vos pilotes graphiques");
        System.err.println("=".repeat(60));
        
        // Pause pour laisser le temps de lire
        try {
            System.err.println("\nAppuyez sur Entrée pour fermer...");
            System.in.read();
        } catch (Exception ignored) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored2) {}
        }
    }

    // Met à jour le fond du jeu
    public static void updateGameBackground(String backgroundFileName) {
        try {
            if (backgroundFileName != null && backgroundFileName.equals(currentBackgroundFileName) && !backgroundWasPreserved) {
                System.out.println("Fond déjà chargé : " + backgroundFileName + " - mise à jour ignorée");
                return;
            }
            
            if (currentGameScene != null) {
                currentGameScene.getStylesheets().clear();
                
                currentGameScene.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
                
                if (currentGameScene.getRoot() != null) {
                    String backgroundUrl = JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/background/" + backgroundFileName).toExternalForm();
                    
                    String backgroundSize = determineBestBackgroundSize(backgroundFileName);
                    
                    String optimizations = "";
                    if (backgroundFileName.toLowerCase().endsWith(".gif")) {
                        optimizations = "-fx-effect: null; ";
                    }
                    
                    currentGameScene.getRoot().setStyle(
                        String.format("-fx-background-image: url('%s'); " +
                                      "-fx-background-size: %s; " +
                                      "-fx-background-position: center center; " +
                                      "-fx-background-repeat: no-repeat; " +
                                      "-fx-background-color: #2B3A4A; " +
                                      "%s",
                                      backgroundUrl, backgroundSize, optimizations)
                    );
                }
                
                currentBackgroundFileName = backgroundFileName;
                
                if (backgroundWasPreserved) {
                    backgroundWasPreserved = false;
                    System.out.println("Fond préservé et appliqué avec succès : " + backgroundFileName + " - drapeau réinitialisé");
                } else {
                    System.out.println("Fond du jeu mis à jour vers : " + backgroundFileName + " avec dimensionnement optimal");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du fond : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Détermine la meilleure taille pour le fond
    private static String determineBestBackgroundSize(String backgroundFileName) {
        String extension = backgroundFileName.toLowerCase();
        
        double screenAspectRatio = screenWidth / screenHeight;
        
        if (extension.endsWith(".gif")) {
            if (screenAspectRatio > 1.8) {
                return "100% auto";
            } else if (screenAspectRatio < 1.5) {
                return "auto 100%";
            } else {
                return "cover";
            }
        } else if (extension.endsWith(".png")) {
            return "contain";
        } else {
            return "cover";
        }
    }
    
    // Retourne les styles CSS de base du jeu
    private static String getBaseGameCSS() {
        return """
            .board-container {
                -fx-background-color: rgba(20, 20, 30, 0.3);
                -fx-border-color: rgba(100, 100, 120, 0.8);
                -fx-border-width: 3;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0.6, 2, 2),
                            innershadow(gaussian, rgba(60,60,80,0.4), 6, 0.4, 0, 0);
                -fx-opacity: 1;
            }
            """;
    }

    // Applique la résolution sauvegardée
    private static void applySavedResolution() {
        try {
            String savedResolution = com.dryt.quoridor.utils.UserPreferences.getSelectedResolution();
            System.out.println("Application de la résolution sauvegardée : " + savedResolution);
            
            if ("Dynamique".equals(savedResolution)) {
                primaryStage.setMaximized(true);
                isMaximized = true;
            } else {
                String[] parts = savedResolution.split("x");
                if (parts.length == 2) {
                    double width = Double.parseDouble(parts[0]);
                    double height = Double.parseDouble(parts[1]);
                    setResolution(width, height, false);
                } else {
                    primaryStage.setMaximized(true);
                    isMaximized = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'application de la résolution sauvegardée, utilisation par défaut : " + e.getMessage());
            primaryStage.setMaximized(true);
            isMaximized = true;
        }
    }

    // Définit la difficulté de l'IA
    public static void setDifficulteIA(DifficulteIA difficulte) {
        difficulteIA = difficulte;
    }

    // Retourne la difficulté de l'IA
    public static DifficulteIA getDifficulteIA() {
        return difficulteIA;
    }

    // Définit les difficultés des IA
    public static void setDifficultesIA(List<DifficulteIA> difficultes) {
        difficultesIA.clear();
        difficultesIA.addAll(difficultes);
    }

    // Retourne les difficultés des IA
    public static List<DifficulteIA> getDifficultesIA() {
        return new ArrayList<>(difficultesIA);
    }

    // Définit le plateau de jeu
    public static void setPlateau(Plateau p) {
        plateau = p;
    }

    // Définit la résolution de la fenêtre
    public static void setResolution(double width, double height) {
        setResolution(width, height, false);
    }
    
    // Définit la résolution avec option de maximisation
    public static void setResolution(double width, double height, boolean maximized) {
        currentResolutionWidth = width;
        currentResolutionHeight = height;
        isMaximized = maximized;
        
        if (primaryStage != null) {
            String preserveBackground = currentBackgroundFileName;
            
            if (maximized) {
                primaryStage.setMaximized(true);
                primaryStage.setResizable(false);
                System.out.println("Résolution définie en mode dynamique (maximisé) : " + width + "x" + height);
            } else {
                primaryStage.setMaximized(false);
                primaryStage.setResizable(false);
                
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
                centerStageOnScreen(primaryStage, width, height);
                System.out.println("Résolution définie en mode fixe : " + width + "x" + height);
            }
            
            if (preserveBackground != null && !preserveBackground.isEmpty() && currentGameScene != null) {
                backgroundWasPreserved = true;
                updateGameBackground(preserveBackground);
                System.out.println("Fond restauré après changement de résolution : " + preserveBackground);
            }
            
            if (currentGameController != null && currentGameScene != null) {
                triggerGameBoardResize();
                System.out.println("Redimensionnement du plateau déclenché pour la nouvelle résolution");
            }
        }
    }
    
    // Met à jour la résolution de toutes les scènes
    private static void updateAllScenesResolution(double width, double height) {
        if (sceneMenu != null) {
            updateSceneSize(sceneMenu, width, height);
        }
        if (sceneOptions != null) {
            updateSceneSize(sceneOptions, width, height);
        }
        if (sceneChoixJoueurs != null) {
            updateSceneSize(sceneChoixJoueurs, width, height);
        }
        if (currentGameScene != null) {
            updateSceneSize(currentGameScene, width, height);
        }
    }
    
    // Met à jour la taille d'une scène
    private static void updateSceneSize(Scene scene, double width, double height) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle(String.format(
                "-fx-pref-width: %.0f; -fx-pref-height: %.0f; -fx-min-width: %.0f; -fx-min-height: %.0f;",
                width, height, width, height
            ));
        }
    }

    // Retourne la largeur de la résolution actuelle
    public static double getCurrentResolutionWidth() {
        return currentResolutionWidth;
    }
    
    // Retourne la hauteur de la résolution actuelle
    public static double getCurrentResolutionHeight() {
        return currentResolutionHeight;
    }
    
    // Retourne si la fenêtre est maximisée
    public static boolean isMaximized() {
        return isMaximized;
    }

    // Affiche le menu de jeu en superposition
    public static void showGameMenuOverlay() {
        if (currentGameController != null) {
            currentGameController.showMenuOverlay();
        }
    }

    // Retourne la scène de jeu actuelle
    public static Scene getCurrentGameScene() {
        return currentGameScene;
    }
}
