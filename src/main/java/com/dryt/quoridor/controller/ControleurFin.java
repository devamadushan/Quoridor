package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurFin {
    @FXML private Label labelVictoire;

    // Affiche le message de victoire du joueur
    public void setVainqueur(String nomVainqueur) {
        if (labelVictoire != null) {
            labelVictoire.setText("Victoire de " + nomVainqueur + " !");
        }
    }

    // Retourne au menu principal
    @FXML
    private void retourMenu(ActionEvent event) {
        JeuQuoridor.goMenu();
    }

    // Ferme l'application
    @FXML
    private void quitter(ActionEvent event) {
        Platform.exit();
    }
}
