package com.dryt.quoridor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Label salutTeam ;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        salutTeam.setText("Salut les guys! Tamer , Remi , Yohan");

        Stage newStage = new Stage();
        newStage.setTitle("Partie");
        newStage.show();
    }
}