package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurChoixSkins {
    private int joueurActuel = 1;
    private int nombreJoueurs;
    private int[] skinsSelectionnes = new int[4]; 

    @FXML
    private Text messageJoueur;

    @FXML
    private Button skin1;
    @FXML
    private Button skin2;
    @FXML
    private Button skin3;
    @FXML
    private Button skin4;

    // Initialise l'interface de sélection des skins
    @FXML
    public void initialize() {
        nombreJoueurs = JeuQuoridor.getNombreJoueurs();
        updateMessageJoueur();
        for (int i = 0; i < skinsSelectionnes.length; i++) {
            skinsSelectionnes[i] = 0; 
        }
    }

    // Met à jour le message pour le joueur actuel
    private void updateMessageJoueur() {
        messageJoueur.setText("Joueur " + joueurActuel + ", veuillez choisir un skin");
    }

    private void selectionnerSkin(int numeroSkin) {
        skinsSelectionnes[joueurActuel - 1] = numeroSkin;
        
        switch(numeroSkin) {
            case 1: skin1.setDisable(true); break;
            case 2: skin2.setDisable(true); break;
            case 3: skin3.setDisable(true); break;
            case 4: skin4.setDisable(true); break;
        }

        joueurActuel++;
        
        if (joueurActuel <= nombreJoueurs) {
            updateMessageJoueur();
        } else {
            JeuQuoridor.setSelectedSkins(skinsSelectionnes); 
            try {
                JeuQuoridor.startGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Sélectionne le skin 1
    @FXML
    private void onSkin1(ActionEvent event) {
        selectionnerSkin(1);
    }

    // Sélectionne le skin 2
    @FXML
    private void onSkin2(ActionEvent event) {
        selectionnerSkin(2);
    }

    // Sélectionne le skin 3
    @FXML
    private void onSkin3(ActionEvent event) {
        selectionnerSkin(3);
    }

    // Sélectionne le skin 4
    @FXML
    private void onSkin4(ActionEvent event) {
        selectionnerSkin(4);
    }

    // Retourne à l'écran précédent
    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }

   
} 