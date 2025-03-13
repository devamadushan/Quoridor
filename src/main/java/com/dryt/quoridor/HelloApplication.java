package com.dryt.quoridor;

import com.dryt.quoridor.gameLogic.GameInstance;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
       FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
          Color black = Color.BLACK;
        Scene scene = new Scene(fxmlLoader.load(), 720, 440 );

        stage.setTitle("Quoridor");
        stage.setScene(scene);
        stage.show();





    }

    public static void main(String[] args) {
        //launch();
        GameInstance j1 = new GameInstance(9,2,0,10);
        j1.startGame();

    }
}