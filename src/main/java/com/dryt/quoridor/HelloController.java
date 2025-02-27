package com.dryt.quoridor;

import com.dryt.quoridor.controller.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.io.IOException;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private Label salutTeam ;

    @FXML
    private Button btnOption;

    @FXML
    private Button btnJouer;

    @FXML
    private Button btnQuit;


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        salutTeam.setText("Salut les guys! Tamer , Remi , Yohan");

        Stage newStage = new Stage();
        newStage.setTitle("Partie");
        newStage.show();
    }

    @FXML
    public void lancerOptions() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("options-view.fxml"));
            Parent optionsRoot = loader.load();
            OptionsController optionsCtrl = loader.getController();

            Scene mainScene = btnOption.getScene();
            optionsCtrl.setMainScene(mainScene);

            Scene optionsScene = new Scene(optionsRoot, 720, 440);

            Stage currentStage = (Stage) mainScene.getWindow();
            currentStage.setScene(optionsScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void lancerPartie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("partie-view.fxml"));
            Parent partieRoot = loader.load();
            GameController gameCtrl = loader.getController();

            Scene mainScene = btnJouer.getScene();
            gameCtrl.setMainScene(mainScene);

            Scene partieScene = new Scene(partieRoot, 720, 440);

            Stage currentStage = (Stage) mainScene.getWindow();
            currentStage.setScene(partieScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void quitJeu() {

        Stage stage = (Stage) btnQuit.getScene().getWindow();
        stage.close();

        System.exit(0);
    }


}