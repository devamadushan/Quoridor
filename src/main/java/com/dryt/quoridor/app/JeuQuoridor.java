package com.dryt.quoridor.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import com.dryt.quoridor.model.Plateau;
import java.net.URL;

public class JeuQuoridor extends Application {
    public static Stage primaryStage;
    public static Scene sceneMenu;
    public static Scene sceneOptions;
    public static Scene sceneChoixJoueurs;

    private static int nombreJoueurs = 2;
    private static Plateau plateau;
    private static double windowX;
    private static double windowY;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Parent menuRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/menu.fxml"));
        sceneMenu = new Scene(menuRoot, 1920, 1080);

        Parent optionsRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/options.fxml"));
        sceneOptions = new Scene(optionsRoot, 1920, 1080);

        Parent choixRoot = FXMLLoader.load(getClass().getResource("/com/dryt/quoridor/views/choix_joueurs.fxml"));
        sceneChoixJoueurs = new Scene(choixRoot, 1920, 1080);

        sceneMenu.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
        sceneOptions.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
        sceneChoixJoueurs.getStylesheets().add(getClass().getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());

        stage.setTitle("Jeu Quoridor");
        stage.setScene(sceneMenu);
        stage.setResizable(false);
        stage.show();

        // Sauvegarder la position initiale
       // windowX = stage.getX();
        //windowY = stage.getY();
    }

    private static void updateWindowPosition() {
        if (primaryStage != null) {
            primaryStage.setX(windowX);
            primaryStage.setY(windowY);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setNombreJoueurs(int nb) {
        nombreJoueurs = nb;
    }

    public static int getNombreJoueurs() {
        return nombreJoueurs;
    }

    public static void setPlateau(Plateau p) {
        plateau = p;
    }

    public static Plateau getPlateau() {
        return plateau;
    }

    public static void startGame() throws Exception {
        windowX = primaryStage.getX(); // Sauvegarder la position avant de changer de scène
        windowY = primaryStage.getY();
        Parent gameRoot = FXMLLoader.load(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml"));
        Scene sceneJeu = new Scene(gameRoot, primaryStage.getWidth(), primaryStage.getHeight());
        sceneJeu.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm());
        primaryStage.setScene(sceneJeu);
        updateWindowPosition(); // Appliquer la position après le changement de scène
    }

    public static void goMenu() {
        windowX = primaryStage.getX(); // Sauvegarder la position avant de changer de scène
        windowY = primaryStage.getY();
        primaryStage.setScene(sceneMenu);
        updateWindowPosition(); // Appliquer la position après le changement de scène
    }

    public static void goOptions() {
        windowX = primaryStage.getX(); // Sauvegarder la position avant de changer de scène
        windowY = primaryStage.getY();
        primaryStage.setScene(sceneOptions);
        updateWindowPosition(); // Appliquer la position après le changement de scène
    }

    public static void goChoixJoueurs() {
        windowX = primaryStage.getX(); // Sauvegarder la position avant de changer de scène
        windowY = primaryStage.getY();
        primaryStage.setScene(sceneChoixJoueurs);
        updateWindowPosition(); // Appliquer la position après le changement de scène
    }

    public static void main(String[] args) {
        launch(args);
    }
}
