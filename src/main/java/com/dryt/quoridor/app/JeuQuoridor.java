package com.dryt.quoridor.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class JeuQuoridor extends Application {
    public static Stage primaryStage;
    public static Scene sceneMenu;
    public static Scene sceneOptions;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Parent menuRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/menu.fxml"));
        sceneMenu = new Scene(menuRoot,1024, 1024);
        Parent optionsRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/options.fxml"));
        sceneOptions = new Scene(optionsRoot, 1024, 1024);
        sceneMenu.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
        sceneOptions.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());

        stage.setTitle("Jeu Quoridor");
        stage.setScene(sceneMenu);
        stage.setResizable(false);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void startGame() throws Exception {
        Parent gameRoot = FXMLLoader.load(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml"));
        Scene sceneJeu = new Scene(gameRoot, primaryStage.getWidth(), primaryStage.getHeight());
        sceneJeu.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
        primaryStage.setScene(sceneJeu);
    }

    public static void goMenu() {
        primaryStage.setScene(sceneMenu);
    }

    public static void goOptions() {
        primaryStage.setScene(sceneOptions);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
