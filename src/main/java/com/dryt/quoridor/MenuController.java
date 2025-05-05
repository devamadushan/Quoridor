package com.dryt.quoridor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController {
    @FXML
    private Button playButton;

    @FXML
    private Button optionsButton;

    @FXML
    private Button quitButton;

    @FXML
    private void handlePlay(ActionEvent event) {
        // TODO: Implémenter la logique pour démarrer une nouvelle partie
        com.dryt.quoridor.app.JeuQuoridor.goChoixJoueurs();
    }

    @FXML
    private void handleOptions(ActionEvent event) {
        // TODO: Implémenter la logique pour afficher les options
        System.out.println("Afficher les options");
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Stage stage = (Stage) quitButton.getScene().getWindow();
        stage.close();
    }
} 