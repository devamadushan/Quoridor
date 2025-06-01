package com.dryt.quoridor.controller;

import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class ControleurChoixJoueurs {

    @FXML private RadioButton radio2Humains;
    @FXML private RadioButton radio1v1Bot;
    @FXML private RadioButton radio4Joueurs;
    @FXML private Spinner<Integer> spinnerIA;

    @FXML
    private void initialize() {
        // Crée un groupe unique pour que les radios soient exclusives
        ToggleGroup group = new ToggleGroup();
        radio2Humains.setToggleGroup(group);
        radio1v1Bot.setToggleGroup(group);
        radio4Joueurs.setToggleGroup(group);
        radio2Humains.setSelected(true); // Par défaut

        // Config spinner (pour le mode 4 joueurs)
        spinnerIA.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1));
        spinnerIA.setEditable(true);
        spinnerIA.setDisable(true); // désactivé sauf si on coche 4 joueurs

        // Activation du spinner uniquement si 4 joueurs est sélectionné
        radio4Joueurs.setOnAction(e -> spinnerIA.setDisable(false));
        radio2Humains.setOnAction(e -> spinnerIA.setDisable(true));
        radio1v1Bot.setOnAction(e -> spinnerIA.setDisable(true));
    }

    @FXML
    private void onValider(ActionEvent event) {
        Plateau plateau;

        if (radio2Humains.isSelected()) {
            plateau = new Plateau(21, 0); // 2 humains
        } else if (radio1v1Bot.isSelected()) {
            plateau = new Plateau(22, 1); // 1 humain + 1 IA
        } else {
            int nbIA = spinnerIA.getValue(); // 1 à 3
            plateau = new Plateau(4, nbIA); // 4 joueurs avec nbIA IA
        }

        JeuQuoridor.setPlateau(plateau);

        try {
            JeuQuoridor.startGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goMenu();
    }
}
