package com.dryt.quoridor.controller;

import com.dryt.quoridor.ai.Action;
import com.dryt.quoridor.ai.MinimaxAI;
import com.dryt.quoridor.ai.MoveType;
import com.dryt.quoridor.ai.DifficulteIA;
import com.dryt.quoridor.utils.GameConstants;
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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ControleurJeu {

    @FXML
    private Pane boardPane;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    private Plateau plateau;
    private Button[][] cellButtons;
    private Rectangle ghostWall;
    private Map<Integer, MinimaxAI> aiStrategies;

    @FXML
    private void initialize() {
        cellButtons = new Button[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        aiStrategies = new HashMap<>();

        for (int y = 0; y < GameConstants.BOARD_SIZE; y++) {
            for (int x = 0; x < GameConstants.BOARD_SIZE; x++) {
                double baseX = GameConstants.OFFSET_X + x * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE);
                double baseY = GameConstants.OFFSET_Y + y * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE);

                Button cell = new Button();
                cell.setPrefSize(GameConstants.CELL_SIZE, GameConstants.CELL_SIZE);
                cell.getStyleClass().add("cell");
                cell.setLayoutX(baseX);
                cell.setLayoutY(baseY);
                final int cx = x;
                final int cy = y;
                cell.setOnAction(event -> onCellClicked(cx, cy));
                cellButtons[cx][cy] = cell;
                boardPane.getChildren().add(cell);

                if (x < 8 && y < 9)
                    createWallPlaceholder(baseX + GameConstants.CELL_SIZE, baseY + GameConstants.CELL_SIZE / 2.0 - GameConstants.WALL_SIZE / 2.0, x, y, true);
                if (y < 8 && x < 9)
                    createWallPlaceholder(baseX + GameConstants.CELL_SIZE / 2.0 - GameConstants.WALL_SIZE / 2.0, baseY + GameConstants.CELL_SIZE, x, y, false);
                if (x < 8 && y < 8)
                    createWallPlaceholder(baseX + GameConstants.CELL_SIZE, baseY + GameConstants.CELL_SIZE, x, y, true);
            }
        }
    }

    public void setupPlateauAndDisplay(Plateau plateau) {
        this.plateau = plateau;
        
        for (Joueur joueur : plateau.getJoueurs()) {
            if (joueur.isAI()) {
                if (JeuQuoridor.getNombreJoueurs() == 4) {
                    List<DifficulteIA> difficultes = JeuQuoridor.getDifficultesIA();
                    int indexIA = 0;
                    for (Joueur j : plateau.getJoueurs()) {
                        if (j.isAI() && j.getId() < joueur.getId()) {
                            indexIA++;
                        }
                    }
                    if (indexIA < difficultes.size()) {
                        aiStrategies.put(joueur.getId(), new MinimaxAI(difficultes.get(indexIA).getProfondeur()));
                    } else {
                        System.err.println(String.format(GameConstants.ERROR_NO_DIFFICULTE, joueur.getId()));
                    }
                } else {
                    aiStrategies.put(joueur.getId(), new MinimaxAI(JeuQuoridor.getDifficulteIA().getProfondeur()));
                }
            }
        }

        javafx.application.Platform.runLater(() -> {
            updateBoardState();
            PauseTransition pause = new PauseTransition(Duration.millis(GameConstants.RENDER_DELAY));
            pause.setOnFinished(event -> {
                boardPane.requestLayout();
            });
            pause.play();
        });
    }

    private void createWallPlaceholder(double x, double y, int wx, int wy, boolean vertical) {
        double detectorSize = GameConstants.WALL_SIZE * 4;

        Rectangle wallDetector = new Rectangle(detectorSize, detectorSize);
        wallDetector.setLayoutX(x - (detectorSize - GameConstants.WALL_SIZE) / 4.0);
        wallDetector.setLayoutY(y - (detectorSize - GameConstants.WALL_SIZE) / 4.0);
        wallDetector.setStyle("-fx-fill: transparent; -fx-stroke: transparent;");

        wallDetector.setOnMouseEntered(e -> showGhostWall(wx, wy, vertical));
        wallDetector.setOnMouseExited(e -> hideGhostWall());

        wallDetector.setOnMouseClicked(e -> {
            int effectiveWx = wx;
            int effectiveWy = wy;
            if (!vertical && wx == 8) effectiveWx = 7;
            if (vertical && wy == 8) effectiveWy = 7;

            if (isCrossingWall(effectiveWx, effectiveWy, vertical)) {
                System.out.println(GameConstants.MSG_WALL_CROSSING);
                return;
            }
            if (!plateau.allPlayersHaveAPathAfterWall(effectiveWx, effectiveWy, vertical)) {
                System.out.println(GameConstants.MSG_WALL_BLOCKING);
                return;
            }
            if (plateau.isWallOverlapping(effectiveWx, effectiveWy, vertical)) {
                System.out.println(GameConstants.MSG_WALL_OVERLAP);
                return;
            }
            if (isWallAlreadyPresent(effectiveWx, effectiveWy, vertical)) {
                System.out.println(GameConstants.MSG_WALL_ALREADY);
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

    private void runIA() {
        Joueur currentPlayer = plateau.getCurrentPlayer();
        
        MinimaxAI aiStrategy = aiStrategies.get(currentPlayer.getId());
        if (aiStrategy == null) {
            System.err.println(String.format(GameConstants.ERROR_NO_IA, currentPlayer.getId()));
            return;
        }

        Action action = aiStrategy.getBestAction(plateau);

        if (action.getType() == MoveType.MOVE) {
            if (!plateau.moveCurrentPlayer(action.getX(), action.getY())) {
                System.err.println(GameConstants.ERROR_INVALID_MOVE);
            }
        } else if (action.getType() == MoveType.WALL) {
             if (!plateau.canPlaceWall(action.getX(), action.getY(), action.getVertical())
                    || !plateau.placeWallCurrentPlayer(action.getX(), action.getY(), action.getVertical())) {
                 System.err.println(GameConstants.ERROR_INVALID_WALL);
             } else {
                 drawWall(action.getX(), action.getY(), action.getVertical());
             }
        }

        Joueur winner = plateau.getWinner();
        if (winner != null) {
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText(GameConstants.MSG_GAME_OVER);
                alert.setContentText(String.format(GameConstants.MSG_PLAYER_WINS, winner.getId()));
                alert.showAndWait();
                JeuQuoridor.goMenu();
            });
        } else {
            switchPlayerTurn();
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
            ghostWall.setWidth(GameConstants.WALL_SIZE);
            ghostWall.setHeight(GameConstants.CELL_SIZE * 2 + GameConstants.WALL_SIZE);
            ghostWall.setX(GameConstants.OFFSET_X + effectiveWx * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE) + GameConstants.CELL_SIZE);
            ghostWall.setY(GameConstants.OFFSET_Y + effectiveWy * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE));
        } else {
            ghostWall.setWidth(GameConstants.CELL_SIZE * 2 + GameConstants.WALL_SIZE);
            ghostWall.setHeight(GameConstants.WALL_SIZE);
            ghostWall.setX(GameConstants.OFFSET_X + effectiveWx * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE));
            ghostWall.setY(GameConstants.OFFSET_Y + effectiveWy * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE) + GameConstants.CELL_SIZE);
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
            wallSegment.setWidth(GameConstants.WALL_SIZE);
            wallSegment.setHeight(GameConstants.CELL_SIZE * 2 + GameConstants.WALL_SIZE);
            wallSegment.setX(GameConstants.OFFSET_X + wx * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE) + GameConstants.CELL_SIZE);
            wallSegment.setY(GameConstants.OFFSET_Y + wy * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE));
        } else {
            wallSegment.setWidth(GameConstants.CELL_SIZE * 2 + GameConstants.WALL_SIZE);
            wallSegment.setHeight(GameConstants.WALL_SIZE);
            wallSegment.setX(GameConstants.OFFSET_X + wx * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE));
            wallSegment.setY(GameConstants.OFFSET_Y + wy * (GameConstants.CELL_SIZE + GameConstants.WALL_SIZE) + GameConstants.CELL_SIZE);
        }
        wallSegment.getStyleClass().add("wall-placed");
        boardPane.getChildren().add(wallSegment);
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
        // Réinitialiser toutes les cases (enlève highlight et les anciens skins)
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                // Conserver les classes de base comme 'cell' si elles existent
                boolean hasCellClass = cellButtons[x][y].getStyleClass().contains("cell");
                cellButtons[x][y].getStyleClass().clear();
                if (hasCellClass) {
                    cellButtons[x][y].getStyleClass().add("cell");
                }
            }
        }

        // Récupérer les skins sélectionnés
        int[] selectedSkins = JeuQuoridor.getSelectedSkins();

        // Mettre à jour les positions des joueurs avec les skins appropriés
        for (Joueur joueur : plateau.getJoueurs()) {
            // Assurez-vous que l'ID du joueur est valide pour l'index du tableau de skins (1-basé vers 0-basé)
            int playerIndex = joueur.getId() - 1;
            if (playerIndex >= 0 && playerIndex < selectedSkins.length) {
                int skinId = selectedSkins[playerIndex];
                // Appliquer le style CSS basé sur le skin sélectionné
                String styleClass = "player" + skinId;
                cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
            } else {
                 System.err.println("Erreur: Skin non sélectionné pour le joueur ID: " + joueur.getId());
                 // Appliquer un style par défaut ou le style basé sur l'ID du joueur si la sélection échoue
                 String styleClass = "player" + joueur.getId();
                 cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
            }
        }

        // Mettre à jour les cases valides pour le joueur courant
        for (int[] move : plateau.getPossibleMoves()) {
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
        }

        // Mettre à jour le label des murs restants
        Joueur currentPlayer = plateau.getCurrentPlayer();
        labelMursRestants.setText("Murs restants : " + currentPlayer.getWallsRemaining());
    }

    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        Joueur currentPlayer = plateau.getCurrentPlayer();
        updateBoardState();

        if (currentPlayer.isAI()) {
            // Utiliser la difficulté de l'IA appropriée si nécessaire
            PauseTransition pause = new PauseTransition(Duration.millis(500)); // Délai pour l'IA
            pause.setOnFinished(e -> runIA());
            pause.play();
        }
    }

}
