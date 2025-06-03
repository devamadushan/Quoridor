package com.dryt.quoridor.controller;

import com.dryt.quoridor.ai.Action;
import com.dryt.quoridor.ai.MinimaxAI;
import com.dryt.quoridor.ai.MoveType;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.Mur;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;

public class ControleurJeu {

    @FXML
    private Pane boardPane;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final int cellSize = 60;
    private final int wallSize = 8;
    private final double offsetX = 80;
    private final double offsetY = 80;
    private Rectangle ghostWall;
    private MinimaxAI aiStrategy;

    @FXML
    private void initialize() {
        plateau = JeuQuoridor.getPlateau();
        cellButtons = new Button[9][9];

        javafx.application.Platform.runLater(() -> {
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    double baseX = offsetX + x * (cellSize + wallSize);
                    double baseY = offsetY + y * (cellSize + wallSize);

                    Button cell = new Button();
                    cell.setPrefSize(cellSize, cellSize);
                    cell.getStyleClass().add("cell");
                    cell.setLayoutX(baseX);
                    cell.setLayoutY(baseY);
                    final int cx = x;
                    final int cy = y;
                    cell.setOnAction(event -> onCellClicked(cx, cy));
                    cellButtons[cx][cy] = cell;
                    boardPane.getChildren().add(cell);

                    if (x < 8 && y < 9)
                        createWallPlaceholder(baseX + cellSize, baseY + cellSize / 2.0 - wallSize / 2.0, x, y, true);
                    if (y < 8 && x < 9)
                        createWallPlaceholder(baseX + cellSize / 2.0 - wallSize / 2.0, baseY + cellSize, x, y, false);
                    if (x < 8 && y < 8)
                        createWallPlaceholder(baseX + cellSize, baseY + cellSize, x, y, true);
                }
            }
            updateBoardState();
        });

        aiStrategy = new MinimaxAI(5);
    }

    private void createWallPlaceholder(double x, double y, int wx, int wy, boolean vertical) {
        double detectorSize = wallSize * 4;

        Rectangle wallDetector = new Rectangle(detectorSize, detectorSize);
        wallDetector.setLayoutX(x - (detectorSize - wallSize) / 4.0);
        wallDetector.setLayoutY(y - (detectorSize - wallSize) / 4.0);
        wallDetector.setStyle("-fx-fill: transparent; -fx-stroke: transparent;");

        wallDetector.setOnMouseEntered(e -> showGhostWall(wx, wy, vertical));
        wallDetector.setOnMouseExited(e -> hideGhostWall());

        wallDetector.setOnMouseClicked(e -> {
            int effectiveWx = wx;
            int effectiveWy = wy;
            if (!vertical && wx == 8) effectiveWx = 7;
            if (vertical && wy == 8) effectiveWy = 7;

            if (isCrossingWall(effectiveWx, effectiveWy, vertical)) {
                System.out.println("❌ Croisement de mur interdit.");
                return;
            }
            if (!plateau.allPlayersHaveAPathAfterWall(effectiveWx, effectiveWy, vertical)) {
                System.out.println("❌ Ce mur bloquerait un joueur complètement.");
                return;
            }
            if (plateau.isWallOverlapping(effectiveWx, effectiveWy, vertical)) {
                System.out.println("❌ Chevauchement de mur interdit.");
                return;
            }
            if (isWallAlreadyPresent(effectiveWx, effectiveWy, vertical)) {
                System.out.println("❌ Un mur est déjà présent ici.");
                return;
            }

            if (plateau.canPlaceWall(effectiveWx, effectiveWy, vertical)
                    && plateau.placeWallCurrentPlayer(effectiveWx, effectiveWy, vertical)) {
                drawWall(effectiveWx, effectiveWy, vertical);
                switchPlayerTurn();
            }
        });
        boardPane.getChildren().add(wallDetector);
    }

    private boolean isWallAlreadyPresent(int wx, int wy, boolean vertical) {
        for (Mur mur : plateau.getMurs()) {
            if (mur.getX() == wx && mur.getY() == wy && mur.isVertical() == vertical) {
                return true;
            }
        }
        return false;
    }

    private boolean isCrossingWall(int wx, int wy, boolean vertical) {
        if (vertical) {
            return plateau.hasHorizontalWall(wx, wy) && plateau.hasHorizontalWall(wx + 1, wy);
        } else {
            return plateau.hasVerticalWall(wx, wy) && plateau.hasVerticalWall(wx, wy + 1);
        }
    }

    private void showGhostWall(int wx, int wy, boolean vertical) {
        hideGhostWall();
        ghostWall = new Rectangle();
        ghostWall.setMouseTransparent(true);

        int effectiveWx = wx;
        int effectiveWy = wy;
        if (!vertical && wx == 8) effectiveWx = 7;
        if (vertical && wy == 8) effectiveWy = 7;

        boolean noWallsLeft = plateau.getCurrentPlayer().getWallsRemaining() <= 0;
        boolean invalid = isCrossingWall(effectiveWx, effectiveWy, vertical)
                || !plateau.canPlaceWall(effectiveWx, effectiveWy, vertical)
                || noWallsLeft
                || isWallAlreadyPresent(effectiveWx, effectiveWy, vertical);

        ghostWall.setStyle(invalid
                ? "-fx-fill: rgba(255, 0, 0, 0.3); -fx-stroke: red;"
                : "-fx-fill: rgba(0, 0, 0, 0.3); -fx-stroke: green;");

        if (vertical) {
            ghostWall.setWidth(wallSize);
            ghostWall.setHeight(cellSize * 2 + wallSize);
            ghostWall.setX(offsetX + effectiveWx * (cellSize + wallSize) + cellSize);
            ghostWall.setY(offsetY + effectiveWy * (cellSize + wallSize));
        } else {
            ghostWall.setWidth(cellSize * 2 + wallSize);
            ghostWall.setHeight(wallSize);
            ghostWall.setX(offsetX + effectiveWx * (cellSize + wallSize));
            ghostWall.setY(offsetY + effectiveWy * (cellSize + wallSize) + cellSize);
        }

        boardPane.getChildren().add(ghostWall);
    }

    private void hideGhostWall() {
        if (ghostWall != null) {
            boardPane.getChildren().remove(ghostWall);
            ghostWall = null;
        }
    }

    private void drawWall(int wx, int wy, boolean vertical) {
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
        System.out.println("Mur placé : " + (vertical ? "V" : "H") + " à " + wx + ", " + wy);
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

        switchPlayerTurn();
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

        System.out.println("Possible moves: ");
        for (int[] move : plateau.getPossibleMoves()) {
            System.out.println(Arrays.toString(move));
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
        }

        labelMursRestants.setText("Joueur " + plateau.getCurrentPlayer().getId()
                + " - murs restants : " + plateau.getCurrentPlayer().getWallsRemaining());
    }

    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        updateBoardState();

        if (plateau.getCurrentPlayer().isAI()) {
            System.out.println("AI " + plateau.getCurrentPlayer().getId() + " looking for move");
            Action action = aiStrategy.getBestAction(plateau);
            System.out.println("Move found");

            if (action.getType() == MoveType.MOVE) {
                plateau.moveCurrentPlayer(action.getX(), action.getY());
            } else if (action.getType() == MoveType.WALL) {
                if (plateau.canPlaceWall(action.getX(), action.getY(), action.getVertical())
                        && plateau.allPlayersHaveAPathAfterWall(action.getX(), action.getY(), action.getVertical())
                        && !plateau.isWallOverlapping(action.getX(), action.getY(), action.getVertical())
                        && !isWallAlreadyPresent(action.getX(), action.getY(), action.getVertical())) {
                    plateau.placeWallCurrentPlayer(action.getX(), action.getY(), action.getVertical());
                    drawWall(action.getX(), action.getY(), action.getVertical());
                }
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

            switchPlayerTurn();
        }
    }
}
