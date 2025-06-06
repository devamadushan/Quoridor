package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import com.dryt.quoridor.app.JeuQuoridor;
import com.dryt.quoridor.utils.BackgroundManager;
import com.dryt.quoridor.utils.UserPreferences;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ControleurOptions {
    
    @FXML
    private ComboBox<String> resolutionComboBox;
    
    @FXML
    private Button backgroundPrevButton;
    
    @FXML
    private Button backgroundNextButton;
    
    @FXML
    private ImageView backgroundPreview;
    
    @FXML
    private Text backgroundNameLabel;
    
    private int currentBackgroundIndex = 0;
    
    // Initialise l'interface des options
    @FXML
    public void initialize() {
        double screenWidth = JeuQuoridor.getScreenWidth();
        double screenHeight = JeuQuoridor.getScreenHeight();
        String detectedResolution = (int)screenWidth + "x" + (int)screenHeight;
        
        System.out.println("Résolution d'écran détectée : " + detectedResolution);
        
        // Liste des résolutions disponibles
        List<String> availableResolutions = Arrays.asList(
            "800x600", 
            "1024x768", 
            "1280x720", 
            "1280x800", 
            "1366x768", 
            "1440x900", 
            "1680x1050", 
            "1920x1080", 
            "2560x1440"
        );
        
        // Filtrer les résolutions plus grandes que la résolution native
        List<String> filteredResolutions = availableResolutions.stream()
            .filter(res -> {
                String[] parts = res.split("x");
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                return width <= screenWidth && height <= screenHeight;
            })
            .collect(Collectors.toList());
        
        resolutionComboBox.getItems().addAll(filteredResolutions);
        
        // Ajouter la résolution native si elle n'est pas déjà dans la liste
        if (!resolutionComboBox.getItems().contains(detectedResolution)) {
            resolutionComboBox.getItems().add(detectedResolution + " (Écran natif)");
        } else {
            int index = resolutionComboBox.getItems().indexOf(detectedResolution);
            resolutionComboBox.getItems().set(index, detectedResolution + " (Écran natif)");
        }
        
        String defaultResolution = determineDefaultResolution();
        resolutionComboBox.setValue(defaultResolution);
        resolutionComboBox.setStyle("-fx-text-fill: #c1a57b; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        resolutionComboBox.setOnAction(e -> {
            String resolution = resolutionComboBox.getValue();
            applyResolution(resolution);
        });
        
        initializeBackgroundSystem();
    }
    
    // Compare deux résolutions pour les trier
    private int compareResolutions(String res1, String res2) {
        try {
            String[] parts1 = res1.split("x");
            String[] parts2 = res2.split("x");
            
            int width1 = Integer.parseInt(parts1[0]);
            int height1 = Integer.parseInt(parts1[1]);
            int width2 = Integer.parseInt(parts2[0]);
            int height2 = Integer.parseInt(parts2[1]);
            
            if (width1 != width2) {
                return Integer.compare(width1, width2);
            }
            return Integer.compare(height1, height2);
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Détermine la résolution par défaut à utiliser
    private String determineDefaultResolution() {
        String savedResolution = UserPreferences.getSelectedResolution();
        
        for (String item : resolutionComboBox.getItems()) {
            String cleanItem = item.replace(" (Écran natif)", "");
            String cleanSaved = savedResolution.replace(" (Écran natif)", "");
            
            if (cleanItem.equals(cleanSaved) || item.equals(savedResolution)) {
                return item;
            }
        }
        
        double screenWidth = JeuQuoridor.getScreenWidth();
        double screenHeight = JeuQuoridor.getScreenHeight();
        String nativeResolution = (int)screenWidth + "x" + (int)screenHeight;
        
        for (String item : resolutionComboBox.getItems()) {
            if (item.startsWith(nativeResolution)) {
                System.out.println("Utilisation de la résolution native comme valeur par défaut : " + item);
                return item;
            }
        }
        
        if (!resolutionComboBox.getItems().isEmpty()) {
            return resolutionComboBox.getItems().get(0);
        }
        
        return "1920x1080";
    }
    
    // Applique la résolution sélectionnée
    private void applyResolution(String resolution) {
        System.out.println("Changement de résolution vers : " + resolution);
        
        UserPreferences.setSelectedResolution(resolution);
        
        String cleanResolution = resolution.replace(" (Écran natif)", "");
        String[] parts = cleanResolution.split("x");
        if (parts.length == 2) {
            try {
                double width = Double.parseDouble(parts[0]);
                double height = Double.parseDouble(parts[1]);
                JeuQuoridor.setResolution(width, height, false);
                System.out.println("Résolution fixe appliquée : " + width + "x" + height);
            } catch (NumberFormatException e) {
                System.err.println("Résolution invalide : " + resolution);
            }
        }
    }
    
    // Initialise le système d'arrière-plan
    private void initializeBackgroundSystem() {
        String selectedBackground = UserPreferences.getSelectedBackground();
        currentBackgroundIndex = BackgroundManager.getBackgroundIndex(selectedBackground);
        
        updateBackgroundPreview();
        
        System.out.println("Système d'arrière-plan initialisé avec : " + selectedBackground);
    }
    
    // Met à jour l'aperçu de l'arrière-plan
    private void updateBackgroundPreview() {
        if (BackgroundManager.getBackgroundCount() == 0) return;
        
        BackgroundManager.BackgroundInfo currentBg = BackgroundManager.getBackgroundByIndex(currentBackgroundIndex);
        
        try {
            String imagePath = "/com/dryt/quoridor/styles/background/" + currentBg.getFileName();
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            backgroundPreview.setImage(image);
            
            backgroundPreview.setOnMouseClicked(e -> {
                System.out.println("Aperçu de l'arrière-plan cliqué - changement de mode de taille");
                applyBackgroundChange(currentBg.getFileName());
            });
            
            backgroundNameLabel.setText(currentBg.getDisplayName());
            
            backgroundPrevButton.setDisable(BackgroundManager.getBackgroundCount() <= 1);
            backgroundNextButton.setDisable(BackgroundManager.getBackgroundCount() <= 1);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'aperçu du fond d'écran : " + e.getMessage());
            backgroundNameLabel.setText("Erreur de chargement");
        }
    }
    
    // Change vers l'arrière-plan précédent
    @FXML
    private void onPreviousBackground(ActionEvent event) {
        if (BackgroundManager.getBackgroundCount() <= 1) return;
        
        currentBackgroundIndex--;
        if (currentBackgroundIndex < 0) {
            currentBackgroundIndex = BackgroundManager.getBackgroundCount() - 1;
        }
        
        BackgroundManager.BackgroundInfo selectedBg = BackgroundManager.getBackgroundByIndex(currentBackgroundIndex);
        UserPreferences.setSelectedBackground(selectedBg.getFileName());
        updateBackgroundPreview();
        
        applyBackgroundChange(selectedBg.getFileName());
        
        System.out.println("Fond d'écran changé vers : " + selectedBg.getDisplayName());
    }
    
    // Change vers l'arrière-plan suivant
    @FXML
    private void onNextBackground(ActionEvent event) {
        if (BackgroundManager.getBackgroundCount() <= 1) return;
        
        currentBackgroundIndex++;
        if (currentBackgroundIndex >= BackgroundManager.getBackgroundCount()) {
            currentBackgroundIndex = 0;
        }
        
        BackgroundManager.BackgroundInfo selectedBg = BackgroundManager.getBackgroundByIndex(currentBackgroundIndex);
        UserPreferences.setSelectedBackground(selectedBg.getFileName());
        updateBackgroundPreview();
        
        applyBackgroundChange(selectedBg.getFileName());
        
        System.out.println("Fond d'écran changé vers : " + selectedBg.getDisplayName());
    }
    
    // Applique le changement d'arrière-plan
    private void applyBackgroundChange(String backgroundFileName) {
        try {
            JeuQuoridor.updateGameBackground(backgroundFileName);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'application du fond d'écran : " + e.getMessage());
        }
    }

    // Gère le retour au menu précédent
    @FXML
    private void onRetour(ActionEvent event) {
        String previousContext = JeuQuoridor.getOptionsPreviousContext();
        
        if ("game".equals(previousContext)) {
            System.out.println("Retour au menu de jeu...");
            if (JeuQuoridor.getCurrentGameScene() != null) {
                JeuQuoridor.getPrimaryStage().setScene(JeuQuoridor.getCurrentGameScene());
                applySavedResolutionToCurrentScene();
                triggerGameBoardResizeAfterOptions();
                JeuQuoridor.showGameMenuOverlay();
            } else {
                JeuQuoridor.goMenu();
            }
        } else if ("endgame".equals(previousContext)) {
            System.out.println("Retour à l'écran de fin de partie...");
            if (JeuQuoridor.getCurrentGameScene() != null) {
                JeuQuoridor.getPrimaryStage().setScene(JeuQuoridor.getCurrentGameScene());
                applySavedResolutionToCurrentScene();
                triggerGameBoardResizeAfterOptions();
                JeuQuoridor.showGameVictoryOverlay();
            } else {
                JeuQuoridor.goMenu();
            }
        } else {
            System.out.println("Retour au menu principal...");
            JeuQuoridor.goMenu();
        }
    }
    
    // Applique la résolution sauvegardée à la scène actuelle
    private void applySavedResolutionToCurrentScene() {
        try {
            String savedResolution = UserPreferences.getSelectedResolution();
            
            String cleanResolution = savedResolution.replace(" (Écran natif)", "");
            String[] parts = cleanResolution.split("x");
            if (parts.length == 2) {
                double width = Double.parseDouble(parts[0]);
                double height = Double.parseDouble(parts[1]);
                
                double currentWidth = JeuQuoridor.getPrimaryStage().getWidth();
                double currentHeight = JeuQuoridor.getPrimaryStage().getHeight();
                
                if (Math.abs(currentWidth - width) > 10 || Math.abs(currentHeight - height) > 10) {
                    JeuQuoridor.setResolution(width, height, false);
                    System.out.println("Résolution fixe appliquée : " + width + "x" + height);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'application de la résolution sauvegardée : " + e.getMessage());
        }
    }
    
    // Déclenche le redimensionnement du plateau après les options
    private void triggerGameBoardResizeAfterOptions() {
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
        delay.setOnFinished(e -> {
            try {
                if (JeuQuoridor.getCurrentGameScene() != null) {
                    JeuQuoridor.triggerGameBoardResize();
                    System.out.println("Redimensionnement du plateau déclenché après retour des options");
                }
            } catch (Exception ex) {
                System.err.println("Erreur lors du redimensionnement du plateau après les options : " + ex.getMessage());
            }
        });
        delay.play();
    }
}
