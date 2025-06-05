package com.dryt.quoridor.controller;


import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurFin {

    @FXML
    private void retourMenu(ActionEvent event) {
        try {
            Stage stage = JeuQuoridor.getPrimaryStage();
            Parent racineMenu = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/menu.fxml"));
            Scene sceneMenu = new Scene(racineMenu);
            sceneMenu.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            stage.setScene(sceneMenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void quitter(ActionEvent event) {
        Platform.exit();
    }
}
