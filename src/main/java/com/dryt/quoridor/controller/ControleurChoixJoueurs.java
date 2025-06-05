package com.dryt.quoridor.controller;

import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class ControleurChoixJoueurs {

    @FXML
    private void on1v1Humain(ActionEvent event) {
        System.out.println("Mode 1 VS 1 Humain sélectionné");
        JeuQuoridor.setNombreJoueurs(2);
        JeuQuoridor.setIsVsAI(false);
        JeuQuoridor.goChoixSkins();
    }

    @FXML
    private void on1v1IA(ActionEvent event) {
        System.out.println("Mode 1 VS 1 IA sélectionné");
        JeuQuoridor.setNombreJoueurs(2);
        JeuQuoridor.setIsVsAI(true);
        JeuQuoridor.goChoixDifficulteIA();
    }

    @FXML
    private void on4Joueurs(ActionEvent event) {
        System.out.println("Mode 4 Joueurs sélectionné");
        JeuQuoridor.setNombreJoueurs(4);
        JeuQuoridor.goChoixNbIADifficulte();
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
