package com.dryt.quoridor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import com.dryt.quoridor.gameLogic.GameInstance;
import com.dryt.quoridor.model.Player;

public class GameController {
    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    private GameInstance game;


    @FXML
    public void initialize() {
        game = new GameInstance(9, 2, 0, 10); // grille 9x9, 2 joueurs, 0 IA, 10 murs chacun
        drawBoard();
        drawPawns();
        statusLabel.setText("Partie initialisée !");

    }
    private void drawPawns() {
        for (Player p : game.getPlayers()) {
            Circle pawn = new Circle(15);
            pawn.setFill(p.getNom().contains("0") ? Color.RED : Color.BLUE);

            // Adapter les coordonnées 18x18 à la grille visible 9x9
            int guiRow = p.getX() / 2;
            int guiCol = p.getY() / 2;

            boardGrid.add(pawn, guiCol, guiRow); // GridPane: col, row
        }
    }


    private void moveCurrentPlayerTo(int x, int y) {
        Player current = game.getCurrentPlayer();

        boolean moved = current.move(x, y); // essaie de bouger (vérifie la logique dans Player)

        if (moved) {
            System.out.println(current.getNom() + " s’est déplacé !");
            drawBoard(); // on redessine tout
            drawPawns();
            // À terme : passer au joueur suivant
        } else {
            System.out.println("Déplacement refusé");
        }
    }

    private void drawBoard() {
        int size = 9;
        boardGrid.getChildren().clear();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Rectangle cell = new Rectangle(40, 40);
                cell.setFill(Color.BEIGE);
                cell.setStroke(Color.BLACK);

                int targetX = row * 2;
                int targetY = col * 2;

                cell.setOnMouseClicked(e -> {
                    System.out.println("Clic sur : " + targetX + "," + targetY);
                    moveCurrentPlayerTo(targetX, targetY);
                });

                boardGrid.add(cell, targetY, targetX);
            }
        }
    }

}
