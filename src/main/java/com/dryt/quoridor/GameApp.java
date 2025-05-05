package com.dryt.quoridor;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dryt/quoridor/views/game.fxml"));
        Scene scene = new Scene(loader.load(), 800, 800);
        stage.setTitle("Quoridor - Interface Seule");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

