package com.dryt.quoridor.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Menu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charge le fichier FXML dans le dossier views
        URL fxmlLocation = getClass().getResource("/com/dryt/quoridor/views/menu.fxml");
        if (fxmlLocation == null) {
            throw new RuntimeException("Fichier menu.fxml introuvable !");
        }
        Parent root = FXMLLoader.load(fxmlLocation);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Menu Quoridor");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}