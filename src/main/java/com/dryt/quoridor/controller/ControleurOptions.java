package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurOptions {
    
    @FXML
    private ComboBox<String> resolutionComboBox;
    
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
        JeuQuoridor.goMenu();
    }
}
