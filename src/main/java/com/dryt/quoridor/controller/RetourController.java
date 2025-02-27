package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class RetourController {
    @FXML
    private Button btnRetour;

    private Scene mainScene;

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    @FXML
    private void pageRetour() {
        Stage currentStage = (Stage) btnRetour.getScene().getWindow();
        currentStage.setScene(mainScene);
    }
}
