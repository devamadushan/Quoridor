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

        ToggleGroup group = new ToggleGroup();
        radio2Humains.setToggleGroup(group);
        radio1v1Bot.setToggleGroup(group);
        radio4Joueurs.setToggleGroup(group);
        radio2Humains.setSelected(true);


        spinnerIA.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        spinnerIA.setEditable(true);
        spinnerIA.setDisable(true);

        radio4Joueurs.setOnAction(e -> spinnerIA.setDisable(false));
        radio2Humains.setOnAction(e -> spinnerIA.setDisable(true));
        radio1v1Bot.setOnAction(e -> spinnerIA.setDisable(true));
    }

    @FXML
    private void onValider(ActionEvent event) {
        Plateau plateau;

        if (radio2Humains.isSelected()) {
            plateau = new Plateau(21, 0); // 1 vs 1
        } else if (radio1v1Bot.isSelected()) {
            plateau = new Plateau(22, 1); // 1 vs AI
        } else {
            int nbIA = spinnerIA.getValue();    // nb IA
            plateau = new Plateau(4, nbIA); // 4 joueurs avec nb IA
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
