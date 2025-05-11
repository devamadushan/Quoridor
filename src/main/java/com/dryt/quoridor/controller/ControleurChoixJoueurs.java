package com.dryt.quoridor.controller;

import com.dryt.quoridor.model.Plateau;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurChoixJoueurs {

    @FXML private RadioButton radio2Joueurs;
    @FXML private RadioButton radio4Joueurs;

    @FXML
    private void initialize() {
        ToggleGroup group = new ToggleGroup();
        radio2Joueurs.setToggleGroup(group);
        radio4Joueurs.setToggleGroup(group);
        radio2Joueurs.setSelected(true);
    }

    // Lorsqu'on clique sur "Valider et jouer"
    @FXML
    private void onValider(ActionEvent event) {
        // Vérifie si 2 ou 4 joueurs ont été choisis
        int nombreJoueurs = radio4Joueurs.isSelected() ? 4 : 2;

        // Créer le plateau avec le nombre de joueurs choisi
        Plateau plateau = new Plateau(nombreJoueurs,3);  // Crée un nouveau plateau avec le nombre de joueurs

        // Assure-toi que l'application utilise ce plateau
        JeuQuoridor.setPlateau(plateau);  // Mets à jour le plateau global de l'application

        // Lancer le jeu
        try {
            JeuQuoridor.startGame();  // Lance le jeu après la configuration
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
