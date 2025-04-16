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
        plateau = new Plateau();
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
                    // Attempt to place a wall at (pwx, pwy) with selected orientation
                    boolean success = plateau.placeWallCurrentPlayer(pwx, pwy, radVertical.isSelected());
                    if (success) {
                        // Remove the "+" button (wall placed here)
                        boardPane.getChildren().remove(plus);
                        // Draw a wall segment as a visual representation
                        if (radVertical.isSelected()) {
                            // Vertical wall segment (spanning two cells vertically)
                            Button wallSegment = new Button();
                            wallSegment.setDisable(true);
                            wallSegment.setPrefSize(wallSize, cellSize * 2);
                            wallSegment.setLayoutX((pwx + 1) * cellSize - wallSize / 2.0);
                            wallSegment.setLayoutY(pwy * cellSize);
                            wallSegment.getStyleClass().add("wall-placed");
                            boardPane.getChildren().add(wallSegment);
                        } else {
                            // Horizontal wall segment (spanning two cells horizontally)
                            Button wallSegment = new Button();
                            wallSegment.setDisable(true);
                            wallSegment.setPrefSize(cellSize * 2, wallSize);
                            wallSegment.setLayoutX(pwx * cellSize);
                            wallSegment.setLayoutY((pwy + 1) * cellSize - wallSize / 2.0);
                            wallSegment.getStyleClass().add("wall-placed");
                            boardPane.getChildren().add(wallSegment);
                        }
                        // Switch turn after placing a wall
                        plateau.switchPlayerTurn();
                        updateBoardState();
                    }
                });
                boardPane.getChildren().add(plus);
            }
        }
        // Set up toggle group for orientation radios
        ToggleGroup orientationGroup = new ToggleGroup();
        radHorizontal.setToggleGroup(orientationGroup);
        radVertical.setToggleGroup(orientationGroup);
        radVertical.setSelected(true);

        // Initialize board state (place pawns and highlight moves for first player)
        updateBoardState();
    }

    // Handle clicking on a board cell (to move a pawn)
    private void onCellClicked(int x, int y) {
        // Only allow move if this cell is highlighted as a valid move
        if (!cellButtons[x][y].getStyleClass().contains("highlight")) {
            return;
        }
        // Move the current player's pawn
        boolean moved = plateau.moveCurrentPlayer(x, y);
        if (!moved) {
            return;
        }
        // Check if this move wins the game
        Joueur winner = plateau.getWinner();
        if (winner != null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Partie terminée");
            alert.setContentText("Le joueur " + winner.getId() + " a gagné !");
            alert.showAndWait();
            // Return to main menu after game ends
            JeuQuoridor.goMenu();
            return;
        }
        // No winner, switch turn and update board
        plateau.switchPlayerTurn();
        updateBoardState();
    }

    // Update the UI to reflect the current game state (pawn positions and possible moves)
    private void updateBoardState() {
        // Clear highlights and player markers from all cells
        for (int yy = 0; yy < 9; yy++) {
            for (int xx = 0; xx < 9; xx++) {
                cellButtons[xx][yy].getStyleClass().remove("highlight");
                cellButtons[xx][yy].getStyleClass().remove("player1");
                cellButtons[xx][yy].getStyleClass().remove("player2");
            }
        }
        // Mark the players' current positions
        Joueur j1 = plateau.getJoueur1();
        Joueur j2 = plateau.getJoueur2();
        cellButtons[j1.getX()][j1.getY()].getStyleClass().add("player1");
        cellButtons[j2.getX()][j2.getY()].getStyleClass().add("player2");
        // Highlight possible moves for the current player's turn
        ArrayList<int[]> possibleMoves = plateau.getPossibleMoves();
        for (int[] move : possibleMoves) {
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
        }
    }
}
