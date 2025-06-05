package com.dryt.quoridor.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurMenu {

    @FXML
    private void onJouer(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }

    @FXML
    private void onOptions(ActionEvent event) {
        JeuQuoridor.goOptions();
    }

    @FXML
    private void onQuitter(ActionEvent event) {
        Platform.exit();
    }
}
