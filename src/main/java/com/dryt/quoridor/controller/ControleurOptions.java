package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurOptions {

    @FXML
    private void onRes800x600(ActionEvent event) {
        Stage stage = JeuQuoridor.getPrimaryStage();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setX(0);
        stage.setY(0);
    }

    @FXML
    private void onRes1024x1024(ActionEvent event) {
        Stage stage = JeuQuoridor.getPrimaryStage();
        stage.setWidth(1024);
        stage.setHeight(1024);
        stage.setX(0);
        stage.setY(-500);
    }

    @FXML
    private void onRes1920x1080(ActionEvent event) {
        Stage stage = JeuQuoridor.getPrimaryStage();
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setX(0);
        stage.setY(-500);
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
