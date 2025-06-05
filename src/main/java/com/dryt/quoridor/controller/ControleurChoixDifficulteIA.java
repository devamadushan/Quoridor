package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import com.dryt.quoridor.app.JeuQuoridor;
import com.dryt.quoridor.ai.DifficulteIA;

public class ControleurChoixDifficulteIA {
    
    // Définit la difficulté facile et change d'écran
    @FXML
    private void onFacile(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.FACILE);
        JeuQuoridor.goChoixSkins();
    }

    // Définit la difficulté moyenne et change d'écran
    @FXML
    private void onMoyen(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.MOYEN);
        JeuQuoridor.goChoixSkins();
    }

    // Définit la difficulté difficile et change d'écran
    @FXML
    private void onDifficile(ActionEvent event) {
        JeuQuoridor.setDifficulteIA(DifficulteIA.DIFFICILE);
        JeuQuoridor.goChoixSkins();
    }

    // Retourne à l'écran de choix des joueurs
    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }
} 