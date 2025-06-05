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
    
    @FXML
    public void initialize() {
        // Ajouter les résolutions dans l'ordre croissant, plus l'option dynamique
        resolutionComboBox.getItems().addAll(
            "800x600", 
            "1024x768", 
            "1280x720", 
            "1280x800", 
            "1366x768", 
            "1440x900", 
            "1680x1050", 
            "1920x1080", 
            "2560x1440", 
            "Dynamique"
        );
        
        // Déterminer la résolution par défaut (celle de l'écran ou la sauvegardée)
        String defaultResolution = determineDefaultResolution();
        resolutionComboBox.setValue(defaultResolution);
        resolutionComboBox.setStyle("-fx-text-fill: #c1a57b; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        resolutionComboBox.setOnAction(e -> {
            String resolution = resolutionComboBox.getValue();
            applyResolution(resolution);
        });
        
        initializeBackgroundSystem();
    }
    
    private String determineDefaultResolution() {
        // Récupérer la résolution sauvegardée
        String savedResolution = UserPreferences.getSelectedResolution();
        
        // Vérifier si la résolution sauvegardée est valide
        if (resolutionComboBox.getItems().contains(savedResolution)) {
            return savedResolution;
        }
        
        // Sinon, utiliser "Dynamique" par défaut
        return "Dynamique";
    }
    
    private void applyResolution(String resolution) {
        System.out.println("🖥️ Changing resolution to: " + resolution);
        
        // Sauvegarder la préférence
        UserPreferences.setSelectedResolution(resolution);
        
        if ("Dynamique".equals(resolution)) {
            // Mode dynamique : utiliser la taille de l'écran
            double screenWidth = JeuQuoridor.getScreenWidth();
            double screenHeight = JeuQuoridor.getScreenHeight();
            JeuQuoridor.setResolution(screenWidth, screenHeight, true); // true pour mode maximisé
        } else {
            // Mode résolution fixe
            String[] parts = resolution.split("x");
            if (parts.length == 2) {
                try {
                    double width = Double.parseDouble(parts[0]);
                    double height = Double.parseDouble(parts[1]);
                    JeuQuoridor.setResolution(width, height, false); // false pour mode fenêtré
                } catch (NumberFormatException e) {
                    System.err.println("❌ Résolution invalide: " + resolution);
                }
            }
        }
    }
    
    private void initializeBackgroundSystem() {
        String selectedBackground = UserPreferences.getSelectedBackground();
        currentBackgroundIndex = BackgroundManager.getBackgroundIndex(selectedBackground);
        
        updateBackgroundPreview();
        
        System.out.println("��️ Background system initialized with: " + selectedBackground);
    }
    
    private void updateBackgroundPreview() {
        if (BackgroundManager.getBackgroundCount() == 0) return;
        
        BackgroundManager.BackgroundInfo currentBg = BackgroundManager.getBackgroundByIndex(currentBackgroundIndex);
        
        try {
            String imagePath = "/com/dryt/quoridor/styles/background/" + currentBg.getFileName();
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            backgroundPreview.setImage(image);
            
            backgroundPreview.setOnMouseClicked(e -> {
                System.out.println("🖼️ Background preview clicked - cycling size mode");
                applyBackgroundChange(currentBg.getFileName());
            });
            
            backgroundNameLabel.setText(currentBg.getDisplayName());
            
            backgroundPrevButton.setDisable(BackgroundManager.getBackgroundCount() <= 1);
            backgroundNextButton.setDisable(BackgroundManager.getBackgroundCount() <= 1);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de l'aperçu du fond d'écran: " + e.getMessage());
            backgroundNameLabel.setText("Erreur de chargement");
        }
    }
    
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
        
        System.out.println("🖼️ Fond d'écran changé vers: " + selectedBg.getDisplayName());
    }
    
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
        
        System.out.println("🖼️ Fond d'écran changé vers: " + selectedBg.getDisplayName());
    }
    
    private void applyBackgroundChange(String backgroundFileName) {
        try {
            JeuQuoridor.updateGameBackground(backgroundFileName);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'application du fond d'écran: " + e.getMessage());
        }
    }

    @FXML
    private void onRetour(ActionEvent event) {
        String previousContext = JeuQuoridor.getOptionsPreviousContext();
        
        if ("game".equals(previousContext)) {
            System.out.println("🔙 Returning to game menu overlay...");
            if (JeuQuoridor.getCurrentGameScene() != null) {
                JeuQuoridor.getPrimaryStage().setScene(JeuQuoridor.getCurrentGameScene());
                applySavedResolutionToCurrentScene();
                JeuQuoridor.showGameMenuOverlay();
            } else {
                JeuQuoridor.goMenu();
            }
        } else {
            System.out.println("🔙 Returning to main menu...");
            JeuQuoridor.goMenu();
        }
    }
    
    private void applySavedResolutionToCurrentScene() {
        try {
            String savedResolution = UserPreferences.getSelectedResolution();
            
            if ("Dynamique".equals(savedResolution)) {
                // Mode dynamique : juste s'assurer que c'est maximisé
                if (!JeuQuoridor.getPrimaryStage().isMaximized()) {
                    JeuQuoridor.getPrimaryStage().setMaximized(true);
                }
            } else {
                // Mode résolution fixe : appliquer seulement si nécessaire
                String[] parts = savedResolution.split("x");
                if (parts.length == 2) {
                    double width = Double.parseDouble(parts[0]);
                    double height = Double.parseDouble(parts[1]);
                    
                    // Vérifier si la résolution est déjà correcte pour éviter les animations inutiles
                    double currentWidth = JeuQuoridor.getPrimaryStage().getWidth();
                    double currentHeight = JeuQuoridor.getPrimaryStage().getHeight();
                    
                    if (Math.abs(currentWidth - width) > 10 || Math.abs(currentHeight - height) > 10) {
                        // Appliquer seulement si la différence est significative
                        JeuQuoridor.setResolution(width, height, false);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error applying saved resolution: " + e.getMessage());
            // Fallback silencieux sans forcer
        }
    }
}
