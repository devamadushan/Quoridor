package com.dryt.quoridor.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.ai.DifficulteIA;
import com.dryt.quoridor.controller.ControleurJeu;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; // Importation pour Arrays.copyOf

public class JeuQuoridor extends Application {
    public static Stage primaryStage;
    public static Scene sceneMenu;
    public static Scene sceneOptions;
    public static Scene sceneChoixJoueurs;

    private static int nombreJoueurs = 2; // 2 pour 1v1, 4 pour 4 joueurs
    private static boolean isVsAI = false; // Pour le mode 1v1: true si contre IA, false si 1v1 humain
    private static int nombreIA4Joueurs = 0; // Variable pour stocker le nombre d'IA en mode 4 joueurs
    private static Plateau plateau;
    private static double windowX;
    private static double windowY;
    private static DifficulteIA difficulteIA = DifficulteIA.MOYEN; // Difficulté par défaut pour 1v1 IA
    private static List<DifficulteIA> difficultesIA = new ArrayList<>(); // Difficultés pour les IA en mode 4 joueurs
    private static int[] selectedSkins = new int[4]; // Tableau pour stocker les skins sélectionnés par chaque joueur (index 0 pour joueur 1, etc.)

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
        if (nb == 2) { // En mode 1v1, on initialise isVsAI à false par défaut
             isVsAI = false;
        }
    }

    public static int getNombreJoueurs() {
        return nombreJoueurs;
    }

    // Méthode pour définir si le mode 1v1 est contre une IA
    public static void setIsVsAI(boolean vsAI) {
        isVsAI = vsAI;
    }

    public static boolean getIsVsAI() {
        return isVsAI;
    }

    public static void setNombreIA4Joueurs(int nb) {
        nombreIA4Joueurs = nb;
    }

    public static int getNombreIA4Joueurs() {
        return nombreIA4Joueurs;
    }

    // Méthode pour définir les skins sélectionnés
    public static void setSelectedSkins(int[] skins) {
        // Copier le tableau pour éviter les modifications externes directes
        selectedSkins = Arrays.copyOf(skins, skins.length);
    }

    // Méthode pour obtenir les skins sélectionnés
    public static int[] getSelectedSkins() {
        // Retourner une copie du tableau pour éviter les modifications externes directes
        return Arrays.copyOf(selectedSkins, selectedSkins.length);
    }

    public static Plateau getPlateau() {
        // Le contrôleur du jeu doit obtenir le plateau via cette méthode après qu'il soit créé dans startGame
        return plateau;
    }

    public static void startGame() throws Exception {
        windowX = primaryStage.getX(); // Sauvegarder la position avant de changer de scène
        windowY = primaryStage.getY();

        // Créer le plateau avec le bon nombre de joueurs et d'IA en fonction de la sélection
        if (nombreJoueurs == 2) {
            if (isVsAI) {
                // Mode 1v1 Humain vs IA (nombreJoueurs=2, 1 IA)
                plateau = new Plateau(22, 1); // Utilise le constructeur 22 pour 1v1 IA
            } else {
                // Mode 1v1 Humain vs Humain (nombreJoueurs=2, 0 IA)
                plateau = new Plateau(21, 0); // Utilise le constructeur 21 pour 1v1 humain
            }
        } else if (nombreJoueurs == 4) {
            // Mode 4 joueurs (Humains vs IA). Le nombreIA4Joueurs a été défini dans ControleurChoixNbIADifficulte
            plateau = new Plateau(4, nombreIA4Joueurs); // Utilise le constructeur 4 pour 4 joueurs avec X IA
        } else {
            throw new IllegalStateException("Nombre de joueurs non géré pour le démarrage du jeu : " + nombreJoueurs);
        }

        FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/jeu.fxml"));
        Parent gameRoot = loader.load();
        // Obtenir le contrôleur après le chargement du FXML
        ControleurJeu gameController = loader.getController();
        // Initialiser le plateau et l'affichage dans le contrôleur
        if (gameController != null) {
            gameController.setupPlateauAndDisplay(plateau);
        }

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

    // Nouvelle méthode pour naviguer vers le choix des skins
    public static void goChoixSkins() {
        windowX = primaryStage.getX();
        windowY = primaryStage.getY();
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_skins.fxml"));
            Parent skinsRoot = loader.load();
            // Futurement : passer des données au contrôleur de choix de skins si nécessaire
            // ControleurChoixSkins skinsController = loader.getController();

            Scene sceneSkins = new Scene(skinsRoot, primaryStage.getWidth(), primaryStage.getHeight());
            sceneSkins.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            primaryStage.setScene(sceneSkins);
            updateWindowPosition();
        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel: Afficher une alerte à l'utilisateur en cas d'erreur
        }
    }

    // Nouvelle méthode pour naviguer vers le choix de difficulté de l'IA (mode 1v1 IA)
    public static void goChoixDifficulteIA() {
        windowX = primaryStage.getX();
        windowY = primaryStage.getY();
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_difficulte_ia.fxml"));
            Parent difficulteRoot = loader.load();
            // Futurement : passer des données au contrôleur de difficulté IA si nécessaire
            // ControleurChoixDifficulteIA difficulteController = loader.getController();

            Scene sceneDifficulte = new Scene(difficulteRoot, primaryStage.getWidth(), primaryStage.getHeight());
            sceneDifficulte.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            primaryStage.setScene(sceneDifficulte);
            updateWindowPosition();
        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel: Afficher une alerte à l'utilisateur en cas d'erreur
        }
    }

    // Nouvelle méthode pour naviguer vers le choix du nombre et de la difficulté des IA (mode 4 joueurs)
    public static void goChoixNbIADifficulte() {
        windowX = primaryStage.getX();
        windowY = primaryStage. getY();
        try {
            FXMLLoader loader = new FXMLLoader(JeuQuoridor.class.getResource("/com/dryt/quoridor/views/choix_nb_ia_difficulte.fxml"));
            Parent nbDifficulteRoot = loader.load();
            // Futurement : passer des données au contrôleur de choix nb IA et difficulté si nécessaire
            // ControleurChoixNbIADifficulte nbDifficulteController = loader.getController();

            Scene sceneNbDifficulte = new Scene(nbDifficulteRoot, primaryStage.getWidth(), primaryStage.getHeight());
            sceneNbDifficulte.getStylesheets().add(JeuQuoridor.class.getResource("/com/dryt/quoridor/styles/style_menu.css").toExternalForm());
            primaryStage.setScene(sceneNbDifficulte);
            updateWindowPosition();
        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel: Afficher une alerte à l'utilisateur en cas d'erreur
        }
    }

    public static void setDifficulteIA(DifficulteIA difficulte) {
        difficulteIA = difficulte;
    }

    public static DifficulteIA getDifficulteIA() {
        return difficulteIA;
    }

    public static void setDifficultesIA(List<DifficulteIA> difficultes) {
        difficultesIA = new ArrayList<>(difficultes);
    }

    public static List<DifficulteIA> getDifficultesIA() {
        return difficultesIA;
    }

    public static void setResolution(int width, int height) {
        Stage stage = getPrimaryStage();
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(0);
        stage.setY(height == 600 ? 0 : -500);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
