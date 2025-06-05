package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import com.dryt.quoridor.app.JeuQuoridor;
import com.dryt.quoridor.ai.DifficulteIA;

public class ControleurChoixDifficulteIA {
    
    @FXML
    private void onFacile(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.FACILE);
        JeuQuoridor.goChoixSkins();
    }

    @FXML
    private void onMoyen(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.MOYEN);
        JeuQuoridor.goChoixSkins();
    }

    @FXML
    private void onDifficile(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.DIFFICILE);
        JeuQuoridor.goChoixSkins();
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }
} 