package com.dryt.quoridor.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ControleurJeu {

    @FXML
    private Pane boardPane;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final int cellSize = 60;
    private final int wallSize = 8;
    private Rectangle ghostWall;

    @FXML
    private void initialize() {
        plateau = JeuQuoridor.getPlateau();
        cellButtons = new Button[9][9];

        javafx.application.Platform.runLater(() -> {
            int totalWidth = 9 * cellSize + 8 * wallSize;
            int totalHeight = 9 * cellSize + 8 * wallSize;

            double offsetX = 80;
            double offsetY = 80;

            for (int y = 0; y < 17; y++) {
                for (int x = 0; x < 17; x++) {
                    if (x % 2 == 0 && y % 2 == 0) {
                        double posX = offsetX + (x / 2.0) * (cellSize + wallSize);
                        double posY = offsetY + (y / 2.0) * (cellSize + wallSize);
                        Button cell = new Button();
                        cell.setPrefSize(cellSize, cellSize);
                        cell.getStyleClass().add("cell");
                        cell.setLayoutX(posX);
                        cell.setLayoutY(posY);
                        final int cx = x / 2;
                        final int cy = y / 2;
                        cell.setOnAction(event -> onCellClicked(cx, cy));
                        cellButtons[cx][cy] = cell;
                        boardPane.getChildren().add(cell);
                    } else if (x % 2 != y % 2) {
                        final int wx = x / 2;
                        final int wy = y / 2;

                        boolean vertical;
                        if (x % 2 == 1 && y % 2 == 0) vertical = true;
                        else if (x % 2 == 0 && y % 2 == 1) vertical = false;
                        else continue;

                        double posX, posY;
                        if (vertical) {
                            posX = offsetX + (x / 2) * (cellSize + wallSize) + cellSize;
                            posY = offsetY + (y / 2.0) * (cellSize + wallSize);
                        } else {
                            posX = offsetX + (x / 2.0) * (cellSize + wallSize);
                            posY = offsetY + (y / 2) * (cellSize + wallSize) + cellSize;
                        }

                        Button wallButton = new Button();
                        wallButton.setPrefSize(wallSize + 4, wallSize + 4);
                        wallButton.getStyleClass().add("wall-placeholder");
                        wallButton.setLayoutX(posX);
                        wallButton.setLayoutY(posY);
                        wallButton.setFocusTraversable(false);

                        wallButton.setOnAction(event -> {
                            // ✅ Nouveau contrôle de chevauchement ajouté ici
                            if (!plateau.peutPlacerMurSansChevauchement(wx, wy, vertical)) return;
                            if (plateau.placeWallCurrentPlayer(wx, wy, vertical)) {
                                wallButton.setVisible(false);
                                drawWall(wx, wy, vertical, offsetX, offsetY);
                                plateau.switchPlayerTurn();
                                updateBoardState();
                            }
                        });
                        wallButton.setOnMouseEntered(e -> showGhostWall(wx, wy, vertical, offsetX, offsetY));
                        wallButton.setOnMouseExited(e -> hideGhostWall());
                        boardPane.getChildren().add(wallButton);
                    }
                }
            }

            updateBoardState();
        });
    }

    private void drawWall(int wx, int wy, boolean vertical, double offsetX, double offsetY) {
        Rectangle wallSegment = new Rectangle();
        if (vertical) {
            wallSegment.setWidth(wallSize);
            wallSegment.setHeight(cellSize * 2 + wallSize);
            wallSegment.setX(offsetX + wx * (cellSize + wallSize) + cellSize);
            wallSegment.setY(offsetY + wy * (cellSize + wallSize));
        } else {
            wallSegment.setWidth(cellSize * 2 + wallSize);
            wallSegment.setHeight(wallSize);
            wallSegment.setX(offsetX + wx * (cellSize + wallSize));
            wallSegment.setY(offsetY + wy * (cellSize + wallSize) + cellSize);
        }
        wallSegment.getStyleClass().add("wall-placed");
        boardPane.getChildren().add(wallSegment);
    }

    private void showGhostWall(int wx, int wy, boolean vertical, double offsetX, double offsetY) {
        if (!plateau.peutPlacerMurSansChevauchement(wx, wy, vertical)) {
            hideGhostWall();
            return;
        }

        if (ghostWall != null) boardPane.getChildren().remove(ghostWall);
        ghostWall = new Rectangle();
        ghostWall.getStyleClass().add("wall-ghost");
        ghostWall.setMouseTransparent(true);

        if (vertical) {
            ghostWall.setWidth(wallSize);
            ghostWall.setHeight(cellSize * 2 + wallSize);
            ghostWall.setX(offsetX + wx * (cellSize + wallSize) + cellSize);
            ghostWall.setY(offsetY + wy * (cellSize + wallSize));
        } else {
            ghostWall.setWidth(cellSize * 2 + wallSize);
            ghostWall.setHeight(wallSize);
            ghostWall.setX(offsetX + wx * (cellSize + wallSize));
            ghostWall.setY(offsetY + wy * (cellSize + wallSize) + cellSize);
        }

        boardPane.getChildren().add(ghostWall);
    }


    private void hideGhostWall() {
        if (ghostWall != null) {
            boardPane.getChildren().remove(ghostWall);
            ghostWall = null;
        }
    }

    private void onCellClicked(int x, int y) {
        if (!cellButtons[x][y].getStyleClass().contains("highlight")) return;
        if (!plateau.moveCurrentPlayer(x, y)) return;

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
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cellButtons[x][y].getStyleClass().removeAll("highlight", "player1", "player2", "player3", "player4");
            }
        }
        for (Joueur joueur : plateau.getJoueurs()) {
            String styleClass = "player" + joueur.getId();
            cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
        }
        for (int[] move : plateau.getPossibleMoves()) {
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
        }
        labelMursRestants.setText("Joueur " + plateau.getCurrentPlayer().getId() +
                " - murs restants : " + plateau.getCurrentPlayer().getWallsRemaining());
    }
}
