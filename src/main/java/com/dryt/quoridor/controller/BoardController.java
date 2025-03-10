package com.dryt.quoridor.controller;

import com.dryt.quoridor.gameLogic.GameInstance;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardController {

    @FXML
    private GridPane boardGrid = new GridPane();

    private static final int CELL_SIZE = 50; // Taille d'une case
    private GameInstance gameInstance;

    public void initialize() {
        // Par défaut, créer une partie 9x9
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

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setStroke(Color.BLACK);

                // Définir la couleur des cases
                cell.setFill((i % 2 == 0 && j % 2 == 0) ? Color.BEIGE : Color.LIGHTGRAY);

                boardGrid.add(cell, j, i);
            }
        }
    }
}