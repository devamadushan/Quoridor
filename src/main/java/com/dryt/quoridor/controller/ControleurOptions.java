package com.dryt.quoridor.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurOptions {

    @FXML
    private void onRes800x600(ActionEvent event) {
        Stage stage = JeuQuoridor.primaryStage;
        stage.setWidth(800);
        stage.setHeight(600);
        stage.centerOnScreen();
    }

    @FXML
    private void onRes1920x1080(ActionEvent event) {
        Stage stage = JeuQuoridor.primaryStage;
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.centerOnScreen();
    }
    @FXML
    private void onRes1024x024(ActionEvent event) {
        Stage stage = JeuQuoridor.primaryStage;
        stage.setWidth(1024);
        stage.setHeight(1024);
        stage.centerOnScreen();
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
