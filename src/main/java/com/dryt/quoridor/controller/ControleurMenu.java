package com.dryt.quoridor.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurMenu {

    // Lance l'écran de choix des joueurs
    @FXML
    private void onJouer(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }

    // Ouvre les options du jeu
    @FXML
    private void onOptions(ActionEvent event) {
        JeuQuoridor.goOptions();
    }

    // Ferme l'application
    @FXML
    private void onQuitter(ActionEvent event) {
        Platform.exit();
    }
}
