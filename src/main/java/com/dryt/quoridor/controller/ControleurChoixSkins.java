package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurChoixSkins {
    private int joueurActuel = 1;
    private int nombreJoueurs;
    private int[] skinsSelectionnes = new int[4]; // Pour stocker les skins choisis par chaque joueur

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

    @FXML
    public void initialize() {
        nombreJoueurs = JeuQuoridor.getNombreJoueurs();
        updateMessageJoueur();
         // Initialiser skinsSelectionnes avec une valeur par défaut ou 0
        for (int i = 0; i < skinsSelectionnes.length; i++) {
            skinsSelectionnes[i] = 0; // Ou une autre valeur par défaut si nécessaire
        }
    }

    private void updateMessageJoueur() {
        messageJoueur.setText("Joueur " + joueurActuel + ", veuillez choisir un skin");
    }

    private void selectionnerSkin(int numeroSkin) {
        skinsSelectionnes[joueurActuel - 1] = numeroSkin;
        
        // Désactiver le skin sélectionné pour les autres joueurs
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
            // Tous les joueurs ont choisi leur skin, enregistrer et lancer le jeu
            JeuQuoridor.setSelectedSkins(skinsSelectionnes); // Enregistrer les skins sélectionnés
            try {
                JeuQuoridor.startGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onSkin1(ActionEvent event) {
        selectionnerSkin(1);
    }

    @FXML
    private void onSkin2(ActionEvent event) {
        selectionnerSkin(2);
    }

    @FXML
    private void onSkin3(ActionEvent event) {
        selectionnerSkin(3);
    }

    @FXML
    private void onSkin4(ActionEvent event) {
        selectionnerSkin(4);
    }

    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }

    // Méthodes pour gérer la sélection des skins et la validation seront ajoutées ici
} 