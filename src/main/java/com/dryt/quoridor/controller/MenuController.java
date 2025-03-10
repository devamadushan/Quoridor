package com.dryt.quoridor.controller;

import com.dryt.quoridor.gameLogic.GameInstance;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    @FXML
    private Button newGameButton;

    @FXML
    public void initialize() {
        newGameButton.setOnAction(event -> openGameWindow());
    }

    private void openGameWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dryt/quoridor/views/board.fxml"));
            GridPane root = loader.load();

            // Récupérer le contrôleur et lui passer une GameInstance
            BoardController controller = loader.getController();
            controller.setGameInstance(new GameInstance(9, 2, 0, 20));

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 600));
            stage.setTitle("Plateau de Quoridor");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}