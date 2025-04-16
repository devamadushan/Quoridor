package com.dryt.quoridor.controller;


import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.dryt.quoridor.app.JeuQuoridor;

public class ControleurFin {
    @FXML private Label labelVictoire;

    /** Définit le message de fin avec le nom du vainqueur à afficher. */
    public void setVainqueur(String nomVainqueur) {
        if (labelVictoire != null) {
            labelVictoire.setText("Victoire de " + nomVainqueur + " !");
        }
    }

    /** Action du bouton "Menu Principal" : retour au menu principal */
    @FXML
    private void retourMenu(ActionEvent event) {
        try {
            Stage stage = JeuQuoridor.getPrimaryStage();
            Parent racineMenu = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/menu.fxml"));
            Scene sceneMenu = new Scene(racineMenu);
            sceneMenu.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            stage.setScene(sceneMenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Action du bouton "Quitter" : quitte l'application */
    @FXML
    private void quitter(ActionEvent event) {
        Platform.exit();
    }
}
