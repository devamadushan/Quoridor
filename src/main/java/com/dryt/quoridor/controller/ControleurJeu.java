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
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;

public class ControleurJeu {

    @FXML
    private Pane boardPane;
    
    @FXML
    private Pane boardContainer;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final double cellSize = 50;
    private final double wallSize = 6;
    private final double offsetX = 20;
    private final double offsetY = 20;
    private Rectangle ghostWall;
    private MinimaxAI aiStrategy;

    @FXML
    private void initialize() {
        plateau = JeuQuoridor.getPlateau();
        cellButtons = new Button[9][9];
        
        javafx.application.Platform.runLater(() -> {
            // Ensure CSS is loaded
            loadCSS();
            // Set proper container size based on board dimensions
            setBoardContainerSize();
            createGameBoard();
            updateBoardState();
        });

        aiStrategy = new MinimaxAI(4);
    }
    
    private void setBoardContainerSize() {
        // Calculate the actual board size
        double boardWidth = 2 * offsetX + 9 * cellSize + 8 * wallSize;  // 538px
        double boardHeight = 2 * offsetY + 9 * cellSize + 8 * wallSize; // 538px
        
        // Set board pane to exact content size
        boardPane.setPrefSize(boardWidth, boardHeight);
        boardPane.setMinSize(boardWidth, boardHeight);
        boardPane.setMaxSize(boardWidth, boardHeight);
        
        // Set container size closer to game size (40px padding on each side)
        double containerPadding = 10;
        double containerSize = boardWidth + (2 * containerPadding); // 618px
        
        boardContainer.setPrefSize(containerSize, containerSize);
        boardContainer.setMinSize(containerSize, containerSize);
        boardContainer.setMaxSize(containerSize, containerSize);
        
        
        System.out.println("🎯 Board pane sized to: " + boardWidth + "x" + boardHeight);
        System.out.println("🎯 Board container sized to: " + containerSize + "x" + containerSize);
    }
    
    private void loadCSS() {
        try {
            // Add CSS programmatically as backup
            String cssPath = getClass().getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm();
            boardPane.getScene().getStylesheets().add(cssPath);
            System.out.println("✅ CSS loaded: " + cssPath);
        } catch (Exception e) {
            System.out.println("❌ Failed to load CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createGameBoard() {
        // Nettoyer le plateau existant
        boardPane.getChildren().clear();
        
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                double baseX = offsetX + x * (cellSize + wallSize);
                double baseY = offsetY + y * (cellSize + wallSize);

                Button cell = new Button();
                cell.setPrefSize(cellSize, cellSize);
                
                // Apply checkerboard pattern: alternating colors based on position
                if ((x + y) % 2 == 0) {
                    cell.getStyleClass().add("cell");        // Light cells
                } else {
                    cell.getStyleClass().add("cell-dark");   // Dark cells
                }
                
                // Debug: Log which class was applied
                String cellClass = (x + y) % 2 == 0 ? "cell" : "cell-dark";
                System.out.println("🎨 Cell [" + x + "," + y + "] -> " + cellClass + " (sum=" + (x+y) + ")");
                
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
        
        System.out.println("🎯 Board creation completed - CSS should now show alternating cell colors!");
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

            if (plateau.canPlaceWall(effectiveWx, effectiveWy, vertical)
                    && plateau.placeWallCurrentPlayer(effectiveWx, effectiveWy, vertical)) {
                drawWall(effectiveWx, effectiveWy, vertical);
                switchPlayerTurn();
            }
        });
        boardPane.getChildren().add(wallDetector);
    }

    private boolean isCrossingWall(int wx, int wy, boolean vertical) {
        if (vertical) {
            return plateau.hasHorizontalWall(wx, wy) || plateau.hasHorizontalWall(wx - 1, wy);
        } else {
            return plateau.hasVerticalWall(wx, wy) || plateau.hasVerticalWall(wx, wy - 1);
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
                || noWallsLeft;

        ghostWall.setStyle(invalid
                ? "-fx-fill: rgba(255, 0, 0, 0.4); -fx-stroke: red; -fx-stroke-width: 3;"
                : "-fx-fill: rgba(218, 165, 32, 0.6); -fx-stroke: #FFD700; -fx-stroke-width: 3; -fx-effect: dropshadow(gaussian, rgba(255,215,0,0.8), 6, 0.7, 0, 0);");

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
        System.out.println("🎮 Updating board state...");
        
        // Clear only game-specific styles, preserve base cell styling
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cellButtons[x][y].getStyleClass().removeAll("highlight", "player1", "player2", "player3", "player4");
                // Preserve "cell" or "cell-dark" classes for checkerboard pattern
            }
        }

        // Add player styles
        for (Joueur joueur : plateau.getJoueurs()) {
            String styleClass = "player" + joueur.getId();
            cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
            System.out.println("🎭 Added " + styleClass + " to cell [" + joueur.getX() + "," + joueur.getY() + "]");
            System.out.println("   Cell classes: " + cellButtons[joueur.getX()][joueur.getY()].getStyleClass());
        }

        // Add highlight styles for possible moves
        System.out.println("Possible moves: ");
        for (int[] move : plateau.getPossibleMoves()) {
            System.out.println(Arrays.toString(move));
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
            System.out.println("✨ Added highlight to cell [" + move[0] + "," + move[1] + "]");
        }

        // Update walls remaining display for all players
        StringBuilder wallInfo = new StringBuilder();
        
        // Sort players by ID to ensure consistent order (J1, J2, J3, J4)
        java.util.List<Joueur> sortedPlayers = new java.util.ArrayList<>(plateau.getJoueurs());
        sortedPlayers.sort((j1, j2) -> Integer.compare(j1.getId(), j2.getId()));
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Joueur joueur = sortedPlayers.get(i);
            
            if (i > 0) {
                wallInfo.append("    ");  // Space separator for same line
            }
            
            // Highlight current player with arrows
            if (joueur.getId() == plateau.getCurrentPlayer().getId()) {
                wallInfo.append("► ");
            }
            
            wallInfo.append("J").append(joueur.getId())
                   .append(": ").append(joueur.getWallsRemaining()).append(" murs");
            
            if (joueur.getId() == plateau.getCurrentPlayer().getId()) {
                wallInfo.append(" ◄");
            }
        }
        
        labelMursRestants.setText(wallInfo.toString());
    }

    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        updateBoardState();

        if (plateau.getCurrentPlayer().isAI()) {
            System.out.println("AI looking for move");
            Action action = aiStrategy.getBestAction(plateau);
            System.out.println("Move found");

            if (action.getType() == MoveType.MOVE) {
                plateau.moveCurrentPlayer(action.getX(), action.getY());
                switchPlayerTurn();
            } else if (action.getType() == MoveType.WALL) {
                if (plateau.canPlaceWall(action.getX(), action.getY(), action.getVertical())
                        && plateau.allPlayersHaveAPathAfterWall(action.getX(), action.getY(), action.getVertical())
                        && !plateau.isWallOverlapping(action.getX(), action.getY(), action.getVertical())) {
                    plateau.placeWallCurrentPlayer(action.getX(), action.getY(), action.getVertical());
                    drawWall(action.getX(), action.getY(), action.getVertical());
                }
                switchPlayerTurn();
            }
        }
    }
    
    @FXML
    private void onNouvellePartie() {
        System.out.println("🎮 Starting new game...");
        JeuQuoridor.goChoixJoueurs();
    }
    
    @FXML
    private void onOpenMenu() {
        System.out.println("📋 Opening menu overlay...");
        // TODO: Add menu overlay functionality
        // For now, just return to main menu
        JeuQuoridor.goMenu();
    }
}
