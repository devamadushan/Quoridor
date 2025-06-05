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

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Detect screen resolution
        detectScreenResolution();
        
        // Use maximized window by default
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
        stage.setResizable(true);
        
        // Start in maximized mode
        stage.setMaximized(true);
        
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
        // Create plateau based on game configuration
        if (nombreJoueurs == 2) {
            if (isVsAI) {
                // Mode 1v1 Humain vs IA
                plateau = new Plateau(22, 1);
            } else {
                // Mode 1v1 Humain vs Humain
                plateau = new Plateau(21, 0);
            }
        } else if (nombreJoueurs == 4) {
            // Mode 4 joueurs
            plateau = new Plateau(4, nombreIA4Joueurs);
        } else {
            throw new IllegalStateException("Nombre de joueurs non géré pour le démarrage du jeu : " + nombreJoueurs);
        }
        
        FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml"));
        Parent gameRoot = loader.load();
        
        // Get the controller and set up the game
        ControleurJeu controleur = loader.getController();
        
        Scene gameScene = new Scene(gameRoot, screenWidth, screenHeight);
        gameScene.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
        gameScene.setOnKeyPressed(e -> handleKeyPress(e));
        
        primaryStage.setScene(gameScene);
        // Smooth maximized transition
        if (isMaximized) {
            primaryStage.setMaximized(true);
        }
        
        // Set up the game plateau and display
        controleur.setupPlateauAndDisplay(plateau);
        
        System.out.println("🎮 Game started in maximized: " + screenWidth + "x" + screenHeight);
    }

    public static void goMenu() {
        primaryStage.setScene(sceneMenu);
        // Maintain maximized state without animation
        if (!primaryStage.isMaximized() && isMaximized) {
            primaryStage.setMaximized(true);
        }
    }

    public static void goOptions() {
        primaryStage.setScene(sceneOptions);
        // Maintain maximized state without animation  
        if (!primaryStage.isMaximized() && isMaximized) {
            primaryStage.setMaximized(true);
        }
    }

    public static void goChoixJoueurs() {
        primaryStage.setScene(sceneChoixJoueurs);
        // Maintain maximized state without animation
        if (!primaryStage.isMaximized() && isMaximized) {
            primaryStage.setMaximized(true);
        }
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
            // Smooth maximized transition
            if (isMaximized) {
                primaryStage.setMaximized(true);
            }
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
            // Smooth maximized transition
            if (isMaximized) {
                primaryStage.setMaximized(true);
            }
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
            // Smooth maximized transition
            if (isMaximized) {
                primaryStage.setMaximized(true);
            }
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
        currentResolutionWidth = width;
        currentResolutionHeight = height;
        
        if (primaryStage != null) {
            Scene currentScene = primaryStage.getScene();
            if (currentScene != null) {
                // Update the scene size
                currentScene.getRoot().setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + ";");
                
                if (isMaximized) {
                    // In maximized, we keep maximized but the content adapts to the target resolution
                    primaryStage.setMaximized(true);
                    System.out.println("🔧 Resolution set to: " + width + "x" + height + " (maximized)");
                } else {
                    // In windowed mode, change the actual window size
                    primaryStage.setWidth(width);
                    primaryStage.setHeight(height);
                    centerStageOnScreen(primaryStage, width, height);
                    System.out.println("🔧 Resolution set to: " + width + "x" + height + " (windowed)");
                }
            }
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

    public static void main(String[] args) {
        launch(args);
    }
}
