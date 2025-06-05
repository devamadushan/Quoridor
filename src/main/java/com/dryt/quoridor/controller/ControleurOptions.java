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
        // Détecter la résolution d'écran de l'utilisateur
        double screenWidth = JeuQuoridor.getScreenWidth();
        double screenHeight = JeuQuoridor.getScreenHeight();
        String detectedResolution = (int)screenWidth + "x" + (int)screenHeight;
        
        System.out.println("🖥️ Detected screen resolution: " + detectedResolution);
        
        // Ajouter les résolutions dans l'ordre croissant, plus la résolution détectée
        resolutionComboBox.getItems().addAll(
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
        
        // Ajouter la résolution détectée si elle n'est pas déjà dans la liste
        if (!resolutionComboBox.getItems().contains(detectedResolution)) {
            // Trouver la position correcte pour l'insérer (ordre croissant)
            boolean inserted = false;
            for (int i = 0; i < resolutionComboBox.getItems().size(); i++) {
                String existingRes = resolutionComboBox.getItems().get(i);
                if (compareResolutions(detectedResolution, existingRes) < 0) {
                    resolutionComboBox.getItems().add(i, detectedResolution + " (Écran natif)");
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                resolutionComboBox.getItems().add(detectedResolution + " (Écran natif)");
            }
        } else {
            // Si la résolution détectée existe déjà, la marquer comme native
            int index = resolutionComboBox.getItems().indexOf(detectedResolution);
            resolutionComboBox.getItems().set(index, detectedResolution + " (Écran natif)");
        }
        
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
    
    // Méthode pour comparer deux résolutions (format "WIDTHxHEIGHT")
    private int compareResolutions(String res1, String res2) {
        try {
            String[] parts1 = res1.split("x");
            String[] parts2 = res2.split("x");
            
            int width1 = Integer.parseInt(parts1[0]);
            int height1 = Integer.parseInt(parts1[1]);
            int width2 = Integer.parseInt(parts2[0]);
            int height2 = Integer.parseInt(parts2[1]);
            
            // Comparer d'abord par largeur, puis par hauteur
            if (width1 != width2) {
                return Integer.compare(width1, width2);
            }
            return Integer.compare(height1, height2);
        } catch (Exception e) {
            return 0; // En cas d'erreur, considérer comme égales
        }
    }
    
    private String determineDefaultResolution() {
        // Récupérer la résolution sauvegardée
        String savedResolution = UserPreferences.getSelectedResolution();
        
        // Vérifier si la résolution sauvegardée est valide (en tenant compte des nouvelles options)
        for (String item : resolutionComboBox.getItems()) {
            // Comparer en ignorant les suffixes comme "(Écran natif)"
            String cleanItem = item.replace(" (Écran natif)", "");
            String cleanSaved = savedResolution.replace(" (Écran natif)", "");
            
            if (cleanItem.equals(cleanSaved) || item.equals(savedResolution)) {
                return item; // Retourner la version avec le bon suffixe
            }
        }
        
        // Si aucune correspondance, utiliser la résolution native de l'écran par défaut
        double screenWidth = JeuQuoridor.getScreenWidth();
        double screenHeight = JeuQuoridor.getScreenHeight();
        String nativeResolution = (int)screenWidth + "x" + (int)screenHeight;
        
        // Chercher la résolution native dans la liste
        for (String item : resolutionComboBox.getItems()) {
            if (item.startsWith(nativeResolution)) {
                System.out.println("🖥️ Using native screen resolution as default: " + item);
                return item;
            }
        }
        
        // Sinon, utiliser la première résolution de la liste
        if (!resolutionComboBox.getItems().isEmpty()) {
            return resolutionComboBox.getItems().get(0);
        }
        
        return "1920x1080"; // Fallback ultime
    }
    
    private void applyResolution(String resolution) {
        System.out.println("🖥️ Changing resolution to: " + resolution);
        
        // Sauvegarder la préférence
        UserPreferences.setSelectedResolution(resolution);
        
        // Toutes les résolutions sont maintenant traitées comme des résolutions fixes
        // Nettoyer les suffixes comme "(Écran natif)"
        String cleanResolution = resolution.replace(" (Écran natif)", "");
        String[] parts = cleanResolution.split("x");
        if (parts.length == 2) {
            try {
                double width = Double.parseDouble(parts[0]);
                double height = Double.parseDouble(parts[1]);
                JeuQuoridor.setResolution(width, height, false); // false pour mode fenêtré
                System.out.println("🖥️ Fixed resolution applied: " + width + "x" + height);
            } catch (NumberFormatException e) {
                System.err.println("❌ Résolution invalide: " + resolution);
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
                
                // Déclencher le redimensionnement du plateau après le retour au jeu
                triggerGameBoardResizeAfterOptions();
                
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
            
            // Toutes les résolutions sont maintenant traitées comme des résolutions fixes
            // Nettoyer les suffixes comme "(Écran natif)"
            String cleanResolution = savedResolution.replace(" (Écran natif)", "");
            String[] parts = cleanResolution.split("x");
            if (parts.length == 2) {
                double width = Double.parseDouble(parts[0]);
                double height = Double.parseDouble(parts[1]);
                
                // Vérifier si la résolution est déjà correcte pour éviter les animations inutiles
                double currentWidth = JeuQuoridor.getPrimaryStage().getWidth();
                double currentHeight = JeuQuoridor.getPrimaryStage().getHeight();
                
                if (Math.abs(currentWidth - width) > 10 || Math.abs(currentHeight - height) > 10) {
                    // Appliquer seulement si la différence est significative
                    JeuQuoridor.setResolution(width, height, false);
                    System.out.println("🖥️ Applied fixed resolution: " + width + "x" + height);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error applying saved resolution: " + e.getMessage());
            // Fallback silencieux sans forcer
        }
    }
    
    private void triggerGameBoardResizeAfterOptions() {
        // Déclencher le redimensionnement du plateau après un petit délai
        // pour s'assurer que la scène et la résolution sont bien appliquées
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
        delay.setOnFinished(e -> {
            try {
                // Récupérer le contrôleur de jeu via JeuQuoridor
                if (JeuQuoridor.getCurrentGameScene() != null) {
                    // Appeler la méthode statique de redimensionnement dans JeuQuoridor
                    JeuQuoridor.triggerGameBoardResize();
                    System.out.println("🎯 Game board resize triggered after returning from options");
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error triggering game board resize after options: " + ex.getMessage());
            }
        });
        delay.play();
    }
}
