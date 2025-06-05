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
        resolutionComboBox.getItems().addAll("800x600", "1024x1024", "1920x1080","1280x800");
        resolutionComboBox.setValue("1920x1080"); // Valeur par défaut
        resolutionComboBox.setStyle("-fx-text-fill: #c1a57b; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        resolutionComboBox.setOnAction(e -> {
            String resolution = resolutionComboBox.getValue();
            switch (resolution) {
                case "800x600" -> onRes800x600(e);
                case "1024x1024" -> onRes1024x1024(e);
                case "1920x1080" -> onRes1920x1080(e);
                case "1280x800" -> onRes1280x800(e);
            }
        });
        
        initializeBackgroundSystem();
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
    private void onRes800x600(ActionEvent event) {
        JeuQuoridor.setResolution(800, 600);
    }
    
    @FXML
    private void onRes1024x1024(ActionEvent event) {
        JeuQuoridor.setResolution(1024, 1024);
    }
    
    @FXML
    private void onRes1920x1080(ActionEvent event) {
        JeuQuoridor.setResolution(1920, 1080);
    }
    @FXML
    private void onRes1280x800(ActionEvent event) {
        JeuQuoridor.setResolution(1280 , 800);
    }

    @FXML
    private void onRetour(ActionEvent event) {
        String previousContext = JeuQuoridor.getOptionsPreviousContext();
        
        if ("game".equals(previousContext)) {
            System.out.println("🔙 Returning to game menu overlay...");
            if (JeuQuoridor.getCurrentGameScene() != null) {
                JeuQuoridor.getPrimaryStage().setScene(JeuQuoridor.getCurrentGameScene());
                JeuQuoridor.getPrimaryStage().setMaximized(true);
                JeuQuoridor.showGameMenuOverlay();
            } else {
                JeuQuoridor.goMenu();
            }
        } else {
            System.out.println("🔙 Returning to main menu...");
            JeuQuoridor.goMenu();
        }
    }
}
