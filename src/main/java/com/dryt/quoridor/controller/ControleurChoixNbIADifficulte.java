package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import com.dryt.quoridor.app.JeuQuoridor;
import com.dryt.quoridor.ai.DifficulteIA;
import java.util.ArrayList;
import java.util.List;

public class ControleurChoixNbIADifficulte {
    
    @FXML
    private Spinner<Integer> nbIASpinner;
    
    @FXML
    private VBox difficulteContainer;
    
    @FXML
    private VBox difficulteButtons;
    
    @FXML
    private Button validerButton;

    @FXML
    private Text messageMode;
    
    private List<DifficulteIA> difficultesSelectionnees = new ArrayList<>();
    
    // Initialise les composants de l'interface
    @FXML
    public void initialize() {
        nbIASpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        
        nbIASpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateDifficulteButtons(newVal);
        });

        messageMode.setText("Mode 4 joueurs");
    }
    
    // Met à jour l'interface selon le nombre d'IA
    private void updateDifficulteButtons(int nbIA) {
        difficulteButtons.getChildren().clear();
        difficultesSelectionnees.clear();
        
        if (nbIA > 0) {
            difficulteContainer.setVisible(true);
            messageMode.setText("Mode " + (4 - nbIA) + " joueur(s) humain(s) et " + nbIA + " IA");
            
            for (int i = 0; i < nbIA; i++) {
                difficultesSelectionnees.add(DifficulteIA.MOYEN);
            }
            
            for (int i = 0; i < nbIA; i++) {
                final int indexIA = i;
                HBox hbox = new HBox(10);
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                
                Label label = new Label("IA " + (i + 1) + " :");
                label.setStyle("-fx-text-fill: #c1a57b; -fx-font-size: 20px; -fx-font-weight: bold;");
                label.getStyleClass().add("menu-label");
                
                ComboBox<DifficulteIA> comboBox = new ComboBox<>();
                comboBox.getItems().addAll(DifficulteIA.values());
                comboBox.setValue(difficultesSelectionnees.get(i));
                comboBox.setStyle("-fx-text-fill: #c1a57b; -fx-font-size: 20px; -fx-font-weight: bold;");
                comboBox.getStyleClass().add("menu-spinner");
                
                comboBox.setOnAction(e -> {
                    difficultesSelectionnees.set(indexIA, comboBox.getValue());
                });
                
                hbox.getChildren().addAll(label, comboBox);
                difficulteButtons.getChildren().add(hbox);
            }
        } else {
            difficulteContainer.setVisible(false);
            messageMode.setText("Mode 4 joueurs");
        }
        
        validerButton.setVisible(true);
    }
    
    // Valide les choix et passe à l'écran suivant
    @FXML
    private void onValider(ActionEvent event) {
        int nbIA = nbIASpinner.getValue();
        JeuQuoridor.setNombreIA4Joueurs(nbIA);
        JeuQuoridor.setDifficultesIA(difficultesSelectionnees);
        JeuQuoridor.goChoixSkins();
    }
    
    // Retourne à l'écran précédent
    @FXML
    private void onRetour(ActionEvent event) {
        JeuQuoridor.goChoixJoueurs();
    }
} 