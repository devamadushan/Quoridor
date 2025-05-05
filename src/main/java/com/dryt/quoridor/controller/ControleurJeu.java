package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.app.JeuQuoridor;

import java.util.ArrayList;
import java.util.List;

public class ControleurJeu {

    @FXML
    private Pane boardPane;
    @FXML
    private RadioButton radHorizontal;
    @FXML
    private RadioButton radVertical;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final int cellSize = 75;
    private final int wallSize = 5;

    @FXML
    private void initialize() {
        plateau = JeuQuoridor.getPlateau();
        cellButtons = new Button[9][9];
        // Create cell buttons for the 9x9 board
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Button cell = new Button();
                cell.setPrefSize(cellSize, cellSize);
                cell.getStyleClass().add("cell");
                // Position the cell in the pane
                cell.setLayoutX(x * cellSize);
                cell.setLayoutY(y * cellSize);
                // Set click event for movement
                final int cx = x;
                final int cy = y;
                cell.setOnAction(event -> onCellClicked(cx, cy));
                cellButtons[x][y] = cell;
                boardPane.getChildren().add(cell);
            }
        }
        // Create "+" buttons for possible wall placements at intersections
        for (int wy = 0; wy < 8; wy++) {
            for (int wx = 0; wx < 8; wx++) {
                Button plus = new Button("+");
                plus.setPrefSize(wallSize, wallSize);
                plus.setLayoutX((wx + 1) * cellSize - wallSize / 2.0);
                plus.setLayoutY((wy + 1) * cellSize - wallSize / 2.0);
                plus.getStyleClass().add("wall-placeholder");
                plus.setFocusTraversable(false);
                final int pwx = wx;
                final int pwy = wy;
                plus.setOnAction(event -> {
                    boolean success = plateau.placeWallCurrentPlayer(pwx, pwy, radVertical.isSelected());
                    if (success) {
                        boardPane.getChildren().remove(plus);
                        if (radVertical.isSelected()) {
                            Button wallSegment = new Button();
                            wallSegment.setDisable(true);
                            wallSegment.setPrefSize(wallSize, cellSize * 2);
                            wallSegment.setLayoutX((pwx + 1) * cellSize - wallSize / 2.0);
                            wallSegment.setLayoutY(pwy * cellSize);
                            wallSegment.getStyleClass().add("wall-placed");
                            boardPane.getChildren().add(wallSegment);
                        } else {
                            Button wallSegment = new Button();
                            wallSegment.setDisable(true);
                            wallSegment.setPrefSize(cellSize * 2, wallSize);
                            wallSegment.setLayoutX(pwx * cellSize);
                            wallSegment.setLayoutY((pwy + 1) * cellSize - wallSize / 2.0);
                            wallSegment.getStyleClass().add("wall-placed");
                            boardPane.getChildren().add(wallSegment);
                        }
                        plateau.switchPlayerTurn();
                        updateBoardState();
                    }
                });
                boardPane.getChildren().add(plus);
            }
        }
        ToggleGroup orientationGroup = new ToggleGroup();
        radHorizontal.setToggleGroup(orientationGroup);
        radVertical.setToggleGroup(orientationGroup);
        radVertical.setSelected(true);

        updateBoardState();
    }

    private void onCellClicked(int x, int y) {
        if (!cellButtons[x][y].getStyleClass().contains("highlight")) {
            return;
        }
        boolean moved = plateau.moveCurrentPlayer(x, y);
        if (!moved) {
            return;
        }
        Joueur winner = plateau.getWinner();
        if (winner != null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Partie terminée");
            alert.setContentText("Le joueur " + winner.getId() + " a gagné !");
            alert.showAndWait();
            JeuQuoridor.goMenu();
            return;
        }
        plateau.switchPlayerTurn();
        updateBoardState();
    }

    private void updateBoardState() {
        for (int yy = 0; yy < 9; yy++) {
            for (int xx = 0; xx < 9; xx++) {
                cellButtons[xx][yy].getStyleClass().remove("highlight");
                cellButtons[xx][yy].getStyleClass().remove("player1");
                cellButtons[xx][yy].getStyleClass().remove("player2");
                cellButtons[xx][yy].getStyleClass().remove("player3");
                cellButtons[xx][yy].getStyleClass().remove("player4");
            }
        }
        for (Joueur joueur : plateau.getJoueurs()) {
            String styleClass = "player" + joueur.getId();
            cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
        }
        List<int[]> possibleMoves = plateau.getPossibleMoves();
        for (int[] move : possibleMoves) {
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
        }
        System.out.println("===== Positions des joueurs =====");
        for (Joueur j : plateau.getJoueurs()) {
            System.out.println("Joueur " + j.getId() + " : (" + j.getX() + ", " + j.getY() + ")");
        }

    }
}