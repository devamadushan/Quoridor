package com.dryt.quoridor.controller;

import com.dryt.quoridor.gameLogic.GameInstance;
import com.dryt.quoridor.model.Entity;
import com.dryt.quoridor.model.Player;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class BoardController {

    @FXML
    private GridPane boardGrid;  // Assurez-vous que l'attribut fx:id="boardGrid" est défini dans board.fxml

    private static final int CELL_SIZE = 50; // Taille d'une case en pixels
    private GameInstance gameInstance;

    @FXML
    public void initialize() {
        // Création d'une instance de jeu par défaut : 9, 2 joueurs, 0 IA, 20 murs
        gameInstance = new GameInstance(9, 2, 0, 20);
        drawBoard();
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        drawBoard();
    }

    private void drawBoard() {
        boardGrid.getChildren().clear(); // Nettoyer l'ancien affichage

        int rows = gameInstance.getBoard().length;
        int cols = gameInstance.getBoard()[0].length;

        // Dessiner la grille (chaque case est un rectangle)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setStroke(Color.BLACK);
                // Effet damier : alterner deux couleurs
                if ((i + j) % 2 == 0) {
                    cell.setFill(Color.BEIGE);
                } else {
                    cell.setFill(Color.LIGHTGRAY);
                }
                boardGrid.add(cell, j, i);
            }
        }

        // Dessiner les pions (joueurs) sur le plateau
        for (Entity e : gameInstance.getPlayers()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                // Création d'un cercle pour représenter le pion
                Circle piece = new Circle(CELL_SIZE * 0.4); // rayon légèrement inférieur à la moitié de la case
                // Choisir une couleur en fonction du joueur
                if (p.getNom().startsWith("AI")) {
                    piece.setFill(Color.RED);
                } else {
                    piece.setFill(Color.BLUE);
                }
                // Ajouter le pion à la grille aux coordonnées du joueur
                // Les coordonnées du joueur sont supposées être en fonction des cellules (ex : 9 pour une cellule sur 18 si board dimension est size*2)
                boardGrid.add(piece, p.getX(), p.getY());
            }
        }
    }
}