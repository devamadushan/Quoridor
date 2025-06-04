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
        System.out.println("Mode 1 VS 1 Humain sélectionné");
        JeuQuoridor.setNombreJoueurs(2); // 2 joueurs humains
        JeuQuoridor.setIsVsAI(false); // Assurez-vous que isVsAI est bien à false pour 1v1 humain
        JeuQuoridor.goChoixSkins();
    }

    @FXML
    private void on1v1IA(ActionEvent event) {
        // Logique pour le mode 1 VS 1 IA
        System.out.println("Mode 1 VS 1 IA sélectionné");
        JeuQuoridor.setNombreJoueurs(2); // 1 humain + 1 IA
        JeuQuoridor.setIsVsAI(true); // Indiquer que c'est un match contre IA
        JeuQuoridor.goChoixDifficulteIA(); // Naviguer vers la page de choix de difficulté de l'IA
    }

    @FXML
    private void on4Joueurs(ActionEvent event) {
        // Logique pour le mode 4 Joueurs
        System.out.println("Mode 4 Joueurs sélectionné");
        JeuQuoridor.setNombreJoueurs(4); // 4 joueurs (humains + IA)
        // Pas besoin de setIsVsAI(true) ici car la gestion des IA multiples est différente en mode 4 joueurs
        JeuQuoridor.goChoixNbIADifficulte(); // Naviguer vers la page de choix du nombre d'IA et de leur difficulté
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
