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
import java.util.Arrays; // Importation pour Arrays.copyOf
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class JeuQuoridor extends Application {
    public static Stage primaryStage;
    public static Scene sceneMenu;
    public static Scene sceneOptions;
    public static Scene sceneChoixJoueurs;

    private static int nombreJoueurs = 2; // 2 pour 1v1, 4 pour 4 joueurs
    private static boolean isVsAI = false; // Pour le mode 1v1: true si contre IA, false si 1v1 humain
    private static int nombreIA4Joueurs = 0; // Variable pour stocker le nombre d'IA en mode 4 joueurs
    private static Plateau plateau;
    private static DifficulteIA difficulteIA = DifficulteIA.MOYEN; // Difficulté par défaut pour 1v1 IA
    private static List<DifficulteIA> difficultesIA = new ArrayList<>(); // Difficultés pour les IA en mode 4 joueurs
    private static int[] selectedSkins = new int[4]; // Tableau pour stocker les skins sélectionnés par chaque joueur (index 0 pour joueur 1, etc.)
    
    // Screen resolution variables
    private static double screenWidth;
    private static double screenHeight;
    private static double scaleFactorX = 1.0;
    private static double scaleFactorY = 1.0;
    private static boolean isMaximized = true; // Use maximized window instead of fullscreen
    private static double currentResolutionWidth = 1920.0; // Default game resolution
    private static double currentResolutionHeight = 1080.0;
    
    // Context management for options navigation
    private static String optionsPreviousContext = "menu"; // "menu" or "game"
    private static ControleurJeu currentGameController = null;
    private static Scene currentGameScene = null;

    private static String currentBackgroundFileName = null; // Cache pour éviter les recharges inutiles
    
    // Global music management
    private static MediaPlayer globalBackgroundMusic = null;
    private static boolean isMusicMuted = false;
    private static double savedMusicVolume = 0.3; // Default volume at 30%
    
    // Playlist management
    private static String[] musicPlaylist = {
        "Highland Hymn Bonnie Grace.mp3",
        "Fresh Findings.mp3", 
        "Fantasy Music Goblinized.mp3"
    };
    private static int currentSongIndex = 0;
    
    // Flag to prevent overriding preserved background
    private static boolean backgroundWasPreserved = false;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Detect screen resolution
        detectScreenResolution();
        
        // Use full screen dimensions for all scenes for consistency
        double windowWidth = screenWidth;
        double windowHeight = screenHeight;

        Parent menuRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/menu.fxml"));
        sceneMenu = new Scene(menuRoot, windowWidth, windowHeight);

        Parent optionsRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/options.fxml"));
        sceneOptions = new Scene(optionsRoot, windowWidth, windowHeight);

        Parent choixRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/choix_joueurs.fxml"));
        sceneChoixJoueurs = new Scene(choixRoot, windowWidth, windowHeight);

        sceneMenu.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
        sceneOptions.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
        sceneChoixJoueurs.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());

        // Add keyboard shortcuts to all initial scenes
        sceneMenu.setOnKeyPressed(e -> handleKeyPress(e));
        sceneOptions.setOnKeyPressed(e -> handleKeyPress(e));
        sceneChoixJoueurs.setOnKeyPressed(e -> handleKeyPress(e));

        stage.setTitle("Jeu Quoridor");
        stage.setScene(sceneMenu);
        stage.setResizable(false);
        
        // Appliquer la résolution sauvegardée
        applySavedResolution();
        
        // Initialize global music system
        initializeGlobalMusic();
        
        stage.show();
        
        System.out.println("🖥️ Screen detected: " + screenWidth + "x" + screenHeight);
        System.out.println("🎮 Game started in maximized mode");
        System.out.println("⌨️ Press F11 to toggle maximized, Escape to exit maximized");
    }
    
    private static void detectScreenResolution() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = screenBounds.getWidth();
        screenHeight = screenBounds.getHeight();
        
        // Calculate scale factors based on a reference resolution (1920x1080)
        scaleFactorX = screenWidth / 1920.0;
        scaleFactorY = screenHeight / 1080.0;
        
        // Use the smaller scale factor to maintain aspect ratio
        double uniformScale = Math.min(scaleFactorX, scaleFactorY);
        scaleFactorX = uniformScale;
        scaleFactorY = uniformScale;
    }
    
    private static void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.F11) {
            isMaximized = !isMaximized;
            primaryStage.setMaximized(isMaximized);
            // Only log once, no repeated console messages
        } else if (event.getCode() == KeyCode.ESCAPE && primaryStage.isMaximized()) {
            primaryStage.setMaximized(false);
            isMaximized = false;
        }
    }
    
    private static double[] calculateOptimalWindowSize() {
        // Base window size for 1920x1080
        double baseWidth = 1400.0;
        double baseHeight = 900.0;
        
        // For screens larger than 1920x1080, cap the scaling
        double maxScale = 1.2; // Don't scale more than 20% above base size
        double effectiveScaleX = Math.min(scaleFactorX, maxScale);
        double effectiveScaleY = Math.min(scaleFactorY, maxScale);
        
        // Scale based on screen size, but cap at 80% of screen size (was 90%)
        double targetWidth = Math.min(baseWidth * effectiveScaleX, screenWidth * 0.8);
        double targetHeight = Math.min(baseHeight * effectiveScaleY, screenHeight * 0.8);
        
        // Ensure minimum size for small screens
        targetWidth = Math.max(targetWidth, 1000.0);  // Reduced from 1200
        targetHeight = Math.max(targetHeight, 700.0); // Reduced from 800
        
        // For very large screens, use a more conservative approach
        if (screenWidth >= 2560 || screenHeight >= 1440) {
            targetWidth = Math.min(targetWidth, 1600.0);
            targetHeight = Math.min(targetHeight, 1000.0);
        }
        
        return new double[]{targetWidth, targetHeight};
    }
    
    private static void centerStageOnScreen(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - width) / 2.0;
        double centerY = (screenBounds.getHeight() - height) / 2.0;
        
        stage.setX(centerX);
        stage.setY(centerY);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static double getScaleFactorX() {
        return scaleFactorX;
    }
    
    public static double getScaleFactorY() {
        return scaleFactorY;
    }
    
    public static double getScreenWidth() {
        return screenWidth;
    }
    
    public static double getScreenHeight() {
        return screenHeight;
    }

    public static void setNombreJoueurs(int nb) {
        nombreJoueurs = nb;
        if (nb == 2) { // En mode 1v1, on initialise isVsAI à false par défaut
             isVsAI = false;
        }
    }

    public static int getNombreJoueurs() {
        return nombreJoueurs;
    }

    // Méthode pour définir si le mode 1v1 est contre une IA
    public static void setIsVsAI(boolean vsAI) {
        isVsAI = vsAI;
    }

    public static boolean getIsVsAI() {
        return isVsAI;
    }

    public static void setNombreIA4Joueurs(int nb) {
        nombreIA4Joueurs = nb;
    }

    public static int getNombreIA4Joueurs() {
        return nombreIA4Joueurs;
    }

    // Méthode pour définir les skins sélectionnés
    public static void setSelectedSkins(int[] skins) {
        // Copier le tableau pour éviter les modifications externes directes
        selectedSkins = Arrays.copyOf(skins, skins.length);
    }

    // Méthode pour obtenir les skins sélectionnés
    public static int[] getSelectedSkins() {
        // Retourner une copie du tableau pour éviter les modifications externes directes
        return Arrays.copyOf(selectedSkins, selectedSkins.length);
    }

    public static Plateau getPlateau() {
        // Le contrôleur du jeu doit obtenir le plateau via cette méthode après qu'il soit créé dans startGame
        return plateau;
    }

    public static void startGame() throws Exception {
        startGame(null); // Utiliser le background sauvegardé par défaut
    }
    
    public static void startGame(String preserveCurrentBackground) throws Exception {
        // 🎮 Start game with appropriate player configuration
        if (nombreJoueurs == 2) {
            if (isVsAI) {
                System.out.println("Mode 1 VS 1 IA sélectionné");
            } else {
                System.out.println("Mode 1 VS 1 Humain sélectionné");
            }
        } else {
            System.out.println("Mode 4 joueurs sélectionné avec " + nombreIA4Joueurs + " IA");
        }

        // Print player configuration
        System.out.println("🎮 Configuration des joueurs :");
        if (nombreJoueurs == 2) {
            System.out.println("Joueur 1 - Humain");
            System.out.println("Joueur 2 - " + (isVsAI ? "IA" : "Humain"));
        } else {
            for (int i = 1; i <= 4; i++) {
                boolean isAI = i > (4 - nombreIA4Joueurs);
                System.out.println("Joueur " + i + " - " + (isAI ? "IA" : "Humain"));
            }
        }

        // Use full screen dimensions for consistency
        double windowWidth = screenWidth;
        double windowHeight = screenHeight;

        URL resource = JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml");
        if (resource == null) {
            throw new Exception("Cannot find jeu.fxml");
        }
        
        FXMLLoader loader = new FXMLLoader(resource);
        Parent gameRoot = loader.load();
        
        // Store the controller for later use
        ControleurJeu controleur = loader.getController();
        currentGameController = controleur;
        
        Scene sceneJeu = new Scene(gameRoot, windowWidth, windowHeight);
        sceneJeu.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
        sceneJeu.setOnKeyPressed(e -> handleKeyPress(e));
        
        // Store the game scene for later use
        currentGameScene = sceneJeu;
        
        primaryStage.setScene(sceneJeu);
        
        // Appliquer la résolution sauvegardée au lieu de forcer maximized
        applySavedResolution();
        System.out.println("🎮 Game started with saved resolution: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
        
        // Apply background - either preserve current or use saved preference
        try {
            String backgroundToApply;
            if (preserveCurrentBackground != null && !preserveCurrentBackground.isEmpty()) {
                backgroundToApply = preserveCurrentBackground;
                backgroundWasPreserved = true; // Marquer que le background a été préservé
                System.out.println("🖼️ Preserving current background: " + backgroundToApply);
            } else {
                backgroundToApply = com.dryt.quoridor.utils.UserPreferences.getSelectedBackground();
                backgroundWasPreserved = false; // Reset du flag
                System.out.println("🖼️ Using saved background preference: " + backgroundToApply);
            }
            
            if (backgroundToApply != null && !backgroundToApply.isEmpty()) {
                updateGameBackground(backgroundToApply);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not apply background: " + e.getMessage());
        }
        
        // Create plateau based on game configuration
        if (nombreJoueurs == 2) {
            if (isVsAI) {
                plateau = new Plateau(22, 1); // 1v1 Humain vs IA
            } else {
                plateau = new Plateau(21, 0); // 1v1 Humain vs Humain
            }
        } else {
            plateau = new Plateau(4, nombreIA4Joueurs); // Mode 4 joueurs
        }
        
        controleur.setupPlateauAndDisplay(plateau);
        
        // Start global music when game starts
        startGlobalMusic(true);
    }
    
    public static void restartCurrentGame() {
        try {
            System.out.println("🔄 Restarting current game with same parameters...");
            
            // Préserver le background actuel en utilisant le cache au lieu de l'extraction CSS
            String preserveBackground = currentBackgroundFileName;
            if (preserveBackground != null && !preserveBackground.isEmpty()) {
                System.out.println("🖼️ Preserving current background: " + preserveBackground);
                startGame(preserveBackground);
            } else {
                System.out.println("🖼️ No current background to preserve, using default behavior");
                startGame(null);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to restart game: " + e.getMessage());
            e.printStackTrace();
            // Fallback to menu if restart fails
            goMenu();
        }
    }
    
    public static String getCurrentGameBackground() {
        return currentBackgroundFileName;
    }

    public static void goMenu() {
        primaryStage.setScene(sceneMenu);
        // Ne pas redimensionner - utiliser la résolution actuelle
        // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
    }

    public static void goOptions() {
        optionsPreviousContext = "menu"; // Called from main menu
        primaryStage.setScene(sceneOptions);
        // Ne pas redimensionner - utiliser la résolution actuelle
        // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
    }
    
    public static void goOptionsFromGame() {
        optionsPreviousContext = "game"; // Called from game menu
        primaryStage.setScene(sceneOptions);
        // Ne pas redimensionner - utiliser la résolution actuelle
        // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
    }
    
    public static String getOptionsPreviousContext() {
        return optionsPreviousContext;
    }

    public static void goChoixJoueurs() {
        primaryStage.setScene(sceneChoixJoueurs);
        // Ne pas redimensionner - utiliser la résolution actuelle
        // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
    }

    // Nouvelle méthode pour naviguer vers le choix des skins
    public static void goChoixSkins() {
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_skins.fxml"));
            Parent skinsRoot = loader.load();

            Scene sceneSkins = new Scene(skinsRoot, screenWidth, screenHeight);
            sceneSkins.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            sceneSkins.setOnKeyPressed(e -> handleKeyPress(e));
            primaryStage.setScene(sceneSkins);
            // Ne pas redimensionner - utiliser la résolution actuelle
            // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Nouvelle méthode pour naviguer vers le choix de difficulté de l'IA (mode 1v1 IA)
    public static void goChoixDifficulteIA() {
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_difficulte_ia.fxml"));
            Parent difficulteRoot = loader.load();

            Scene sceneDifficulte = new Scene(difficulteRoot, screenWidth, screenHeight);
            sceneDifficulte.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            sceneDifficulte.setOnKeyPressed(e -> handleKeyPress(e));
            primaryStage.setScene(sceneDifficulte);
            // Ne pas redimensionner - utiliser la résolution actuelle
            // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Nouvelle méthode pour naviguer vers le choix du nombre et de la difficulté des IA (mode 4 joueurs)
    public static void goChoixNbIADifficulte() {
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_nb_ia_difficulte.fxml"));
            Parent nbDifficulteRoot = loader.load();

            Scene sceneNbDifficulte = new Scene(nbDifficulteRoot, screenWidth, screenHeight);
            sceneNbDifficulte.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            sceneNbDifficulte.setOnKeyPressed(e -> handleKeyPress(e));
            primaryStage.setScene(sceneNbDifficulte);
            // Ne pas redimensionner - utiliser la résolution actuelle
            // applySavedResolution(); // SUPPRIMÉ pour éviter les animations
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDifficulteIA(DifficulteIA difficulte) {
        difficulteIA = difficulte;
    }

    public static DifficulteIA getDifficulteIA() {
        return difficulteIA;
    }

    public static void setDifficultesIA(List<DifficulteIA> difficultes) {
        difficultesIA.clear();
        difficultesIA.addAll(difficultes);
    }

    public static List<DifficulteIA> getDifficultesIA() {
        return new ArrayList<>(difficultesIA);
    }

    public static void setPlateau(Plateau p) {
        plateau = p;
    }

    public static void setResolution(double width, double height) {
        setResolution(width, height, false);
    }
    
    public static void setResolution(double width, double height, boolean maximized) {
        currentResolutionWidth = width;
        currentResolutionHeight = height;
        isMaximized = maximized;
        
        if (primaryStage != null) {
            // Préserver le background actuel avant changement de résolution
            String preserveBackground = currentBackgroundFileName;
            
            if (maximized) {
                // Mode dynamique : maximiser la fenêtre
                primaryStage.setMaximized(true);
                primaryStage.setResizable(false); // Empêcher le redimensionnement manuel
                System.out.println("🖥️ Resolution set to Dynamic (Maximized): " + width + "x" + height);
            } else {
                // Mode résolution fixe : fenêtre de taille spécifique
                primaryStage.setMaximized(false);
                primaryStage.setResizable(false); // Empêcher le redimensionnement manuel
                
                // Éviter les animations en appliquant la taille directement sans updateAllScenesResolution
                // updateAllScenesResolution(width, height); // SUPPRIMÉ pour éviter les animations
                
                // Redimensionner la fenêtre de façon plus fluide
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
                centerStageOnScreen(primaryStage, width, height);
                System.out.println("🖥️ Resolution set to Fixed: " + width + "x" + height);
            }
            
            // Restaurer le background après changement de résolution
            if (preserveBackground != null && !preserveBackground.isEmpty() && currentGameScene != null) {
                // Marquer le background comme préservé pour éviter qu'il soit overridé
                backgroundWasPreserved = true;
                updateGameBackground(preserveBackground);
                System.out.println("🖼️ Background restored after resolution change: " + preserveBackground);
            }
            
            // Si on est en cours de jeu, déclencher le redimensionnement du plateau
            if (currentGameController != null && currentGameScene != null) {
                triggerGameBoardResize();
                System.out.println("🎯 Game board resize triggered for new resolution");
            }
        }
    }
    
    private static void updateAllScenesResolution(double width, double height) {
        // Mettre à jour toutes les scènes pour qu'elles aient la même résolution
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
    
    private static void updateSceneSize(Scene scene, double width, double height) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle(String.format(
                "-fx-pref-width: %.0f; -fx-pref-height: %.0f; -fx-min-width: %.0f; -fx-min-height: %.0f;",
                width, height, width, height
            ));
        }
    }

    // Add getters for current resolution
    public static double getCurrentResolutionWidth() {
        return currentResolutionWidth;
    }
    
    public static double getCurrentResolutionHeight() {
        return currentResolutionHeight;
    }
    
    public static boolean isMaximized() {
        return isMaximized;
    }

    public static void showGameMenuOverlay() {
        if (currentGameController != null) {
            currentGameController.showMenuOverlay();
        }
    }

    public static Scene getCurrentGameScene() {
        return currentGameScene;
    }

    public static void updateGameBackground(String backgroundFileName) {
        try {
            // Éviter les recharges inutiles du même background, sauf si le background a été préservé
            if (backgroundFileName != null && backgroundFileName.equals(currentBackgroundFileName) && !backgroundWasPreserved) {
                System.out.println("🖼️ Background already loaded: " + backgroundFileName + " - skipping update");
                return;
            }
            
            if (currentGameScene != null) {
                // Remove existing stylesheets
                currentGameScene.getStylesheets().clear();
                
                // Apply the updated stylesheet first
                currentGameScene.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
                
                // Apply the dynamic background style inline with better sizing options
                if (currentGameScene.getRoot() != null) {
                    String backgroundUrl = JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/background/" + backgroundFileName).toExternalForm();
                    
                    // Déterminer automatiquement le meilleur mode de taille
                    String backgroundSize = determineBestBackgroundSize(backgroundFileName);
                    
                    // Pour les GIFs, ajouter des optimisations CSS supplémentaires
                    String optimizations = "";
                    if (backgroundFileName.toLowerCase().endsWith(".gif")) {
                        optimizations = "-fx-effect: null; "; // Désactiver les effets pour les GIFs
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
                
                // Mettre à jour le cache
                currentBackgroundFileName = backgroundFileName;
                
                // Reset the flag après application successful
                if (backgroundWasPreserved) {
                    backgroundWasPreserved = false;
                    System.out.println("🖼️ Background preserved and applied successfully: " + backgroundFileName + " - flag reset");
                } else {
                    System.out.println("🖼️ Game background updated to: " + backgroundFileName + " with optimal sizing");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating game background: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String determineBestBackgroundSize(String backgroundFileName) {
        String extension = backgroundFileName.toLowerCase();
        
        // Calculer le ratio d'aspect de l'écran
        double screenAspectRatio = screenWidth / screenHeight;
        
        if (extension.endsWith(".gif")) {
            // Pour les GIFs pixel art, adapter selon le ratio d'écran
            if (screenAspectRatio > 1.8) {
                // Écrans ultra-larges (21:9)
                return "100% auto";
            } else if (screenAspectRatio < 1.5) {
                // Écrans plus carrés (4:3)
                return "auto 100%";
            } else {
                // Écrans standard (16:9, 16:10)
                return "cover";
            }
        } else if (extension.endsWith(".png")) {
            // Pour les PNGs, utiliser contain pour préserver la qualité
            return "contain";
        } else {
            // Pour les JPGs, toujours utiliser cover pour remplir l'écran
            return "cover";
        }
    }
    
    private static String getBaseGameCSS() {
        // Return basic styles that shouldn't be overridden by background changes
        return """
            /* Preserve essential game styles */
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

    private static void applySavedResolution() {
        try {
            String savedResolution = com.dryt.quoridor.utils.UserPreferences.getSelectedResolution();
            System.out.println("🖥️ Applying saved resolution: " + savedResolution);
            
            if ("Dynamique".equals(savedResolution)) {
                // Mode dynamique : maximiser la fenêtre
                primaryStage.setMaximized(true);
                isMaximized = true;
            } else {
                // Mode résolution fixe
                String[] parts = savedResolution.split("x");
                if (parts.length == 2) {
                    double width = Double.parseDouble(parts[0]);
                    double height = Double.parseDouble(parts[1]);
                    setResolution(width, height, false);
                } else {
                    // Fallback vers mode dynamique si résolution invalide
                    primaryStage.setMaximized(true);
                    isMaximized = true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error applying saved resolution, using default: " + e.getMessage());
            // Fallback vers mode dynamique
            primaryStage.setMaximized(true);
            isMaximized = true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Global music management methods
    public static void initializeGlobalMusic() {
        try {
            // Stop and dispose existing music if any
            if (globalBackgroundMusic != null) {
                globalBackgroundMusic.stop();
                globalBackgroundMusic.dispose();
                System.out.println("🎵 Previous global music disposed");
            }
            
            // Load the current song from playlist
            String currentSong = musicPlaylist[currentSongIndex];
            String musicPath = JeuQuoridor.class.getResource("/com/dryt/quoridor/sounds/" + currentSong).toExternalForm();
            Media music = new Media(musicPath);
            globalBackgroundMusic = new MediaPlayer(music);
            
            // Set music properties
            globalBackgroundMusic.setVolume(savedMusicVolume);
            globalBackgroundMusic.setAutoPlay(false);
            
            // Set up auto-advance to next song when current song ends
            globalBackgroundMusic.setOnEndOfMedia(() -> {
                advanceToNextSong();
            });
            
            System.out.println("🎵 Global background music initialized: " + currentSong);
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize global music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void advanceToNextSong() {
        // Move to next song in playlist
        currentSongIndex = (currentSongIndex + 1) % musicPlaylist.length;
        System.out.println("🎵 Advancing to next song: " + musicPlaylist[currentSongIndex]);
        
        // Reinitialize with new song and continue playing
        boolean wasPlaying = (globalBackgroundMusic != null && globalBackgroundMusic.getStatus() == MediaPlayer.Status.PLAYING);
        initializeGlobalMusic();
        if (wasPlaying && !isMusicMuted) {
            globalBackgroundMusic.play();
        }
    }
    
    public static void startGlobalMusic() {
        startGlobalMusic(false);
    }
    
    public static void startGlobalMusic(boolean isNewGame) {
        // If it's a new game, advance to next song
        if (isNewGame) {
            currentSongIndex = (currentSongIndex + 1) % musicPlaylist.length;
            System.out.println("🎵 New game - switching to: " + musicPlaylist[currentSongIndex]);
            initializeGlobalMusic();
        }
        
        if (globalBackgroundMusic != null && !isMusicMuted) {
            try {
                globalBackgroundMusic.play();
                System.out.println("🎵 Global background music started: " + musicPlaylist[currentSongIndex]);
            } catch (Exception e) {
                System.err.println("❌ Failed to start global music: " + e.getMessage());
            }
        }
    }
    
    public static void stopGlobalMusic() {
        if (globalBackgroundMusic != null) {
            try {
                globalBackgroundMusic.stop();
                System.out.println("🎵 Global background music stopped");
            } catch (Exception e) {
                System.err.println("❌ Failed to stop global music: " + e.getMessage());
            }
        }
    }
    
    public static void pauseGlobalMusic() {
        if (globalBackgroundMusic != null) {
            try {
                globalBackgroundMusic.pause();
                System.out.println("🎵 Global background music paused");
            } catch (Exception e) {
                System.err.println("❌ Failed to pause global music: " + e.getMessage());
            }
        }
    }
    
    public static void setGlobalMusicVolume(double volume) {
        savedMusicVolume = volume;
        if (globalBackgroundMusic != null && !isMusicMuted) {
            globalBackgroundMusic.setVolume(volume);
        }
    }
    
    public static void toggleGlobalMusicMute() {
        isMusicMuted = !isMusicMuted;
        if (globalBackgroundMusic != null) {
            globalBackgroundMusic.setVolume(isMusicMuted ? 0.0 : savedMusicVolume);
        }
        System.out.println("🎵 Global music muted: " + isMusicMuted);
    }
    
    public static boolean isGlobalMusicMuted() {
        return isMusicMuted;
    }
    
    public static double getGlobalMusicVolume() {
        return savedMusicVolume;
    }

    public static boolean wasBackgroundPreserved() {
        return backgroundWasPreserved;
    }
    
    public static void resetBackgroundPreservedFlag() {
        backgroundWasPreserved = false;
    }

    public static void triggerGameBoardResize() {
        // Déclencher le redimensionnement du plateau de jeu via le contrôleur
        if (currentGameController != null) {
            // Attendre un petit délai pour s'assurer que le redimensionnement de la fenêtre est terminé
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(e -> {
                javafx.application.Platform.runLater(() -> {
                    try {
                        System.out.println("🎯 Triggering board resize after resolution change");
                        // Appeler la méthode publique du contrôleur pour déclencher le redimensionnement
                        currentGameController.triggerBoardResize();
                    } catch (Exception ex) {
                        System.err.println("⚠️ Error triggering game board resize: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            });
            delay.play();
        }
    }
}
