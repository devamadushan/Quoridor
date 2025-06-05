package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurFin {
    @FXML private Label labelVictoire;

    /** Définit le message de fin avec le nom du vainqueur à afficher. */
    public void setVainqueur(String nomVainqueur) {
        if (labelVictoire != null) {
            labelVictoire.setText("Victoire de " + nomVainqueur + " !");
        }
    }

    /** Action du bouton "Menu Principal" : retour au menu principal */
    @FXML
    private void retourMenu(ActionEvent event) {
        JeuQuoridor.goMenu();
    }

    /** Action du bouton "Quitter" : quitte l'application */
    @FXML
    private void quitter(ActionEvent event) {
        Platform.exit();
    }
}
