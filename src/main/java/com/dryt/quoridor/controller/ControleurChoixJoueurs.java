package com.dryt.quoridor.controller;

import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class ControleurChoixJoueurs {

    @FXML
    private void on1v1Humain(ActionEvent event) {
        // Logique pour le mode 1 VS 1 Humain
        // Naviguer directement vers le choix des skins
        System.out.println("Mode 1 VS 1 Humain sélectionné");
        JeuQuoridor.setNombreJoueurs(2); // 2 joueurs humains
        JeuQuoridor.goChoixSkins(); // Appel à la méthode de navigation (à créer)
    }

    @FXML
    private void on1v1IA(ActionEvent event) {
        // Logique pour le mode 1 VS 1 IA
        // Naviguer vers la page de choix de difficulté de l'IA
        System.out.println("Mode 1 VS 1 IA sélectionné");
        JeuQuoridor.setNombreJoueurs(2); // 1 humain + 1 IA
        JeuQuoridor.goChoixDifficulteIA(); // Appel à la méthode de navigation (à créer)
    }

    @FXML
    private void on4Joueurs(ActionEvent event) {
        // Logique pour le mode 4 Joueurs
        // Naviguer vers la page de choix du nombre d'IA et de leur difficulté
        System.out.println("Mode 4 Joueurs sélectionné");
        JeuQuoridor.setNombreJoueurs(4); // 4 joueurs (humains + IA)
        JeuQuoridor.goChoixNbIADifficulte(); // Appel à la méthode de navigation (à créer)
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
