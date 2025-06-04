package com.dryt.quoridor.controller;

import com.dryt.quoridor.ai.Action;
import com.dryt.quoridor.ai.MinimaxAI;
import com.dryt.quoridor.ai.MoveType;
import com.dryt.quoridor.ai.DifficulteIA;
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
    private final int cellSize = 60;
    private final int wallSize = 8;
    private final double offsetX = 80;
    private final double offsetY = 80;
    private Rectangle ghostWall;
    private Map<Integer, MinimaxAI> aiStrategies; // Map des IA par ID de joueur

    @FXML
    private void initialize() {
        cellButtons = new Button[9][9];
        aiStrategies = new HashMap<>();

        // La création des cellules et des détecteurs de murs se fait ici
        // La mise à jour initiale du plateau se fera via setupPlateauAndDisplay
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
    }

    // Méthode appelée depuis JeuQuoridor pour initialiser le plateau et l'affichage
    public void setupPlateauAndDisplay(Plateau plateau) {
        this.plateau = plateau;
        System.out.println("🎮 Configuration du jeu :");
        System.out.println("Nombre de joueurs : " + JeuQuoridor.getNombreJoueurs());
        System.out.println("Nombre d'IA : " + JeuQuoridor.getNombreIA4Joueurs());
        System.out.println("Difficultés des IA : " + JeuQuoridor.getDifficultesIA());
        
        // Créer une IA pour chaque joueur IA
        for (Joueur joueur : plateau.getJoueurs()) {
            System.out.println("Vérification du joueur " + joueur.getId() + " : " + (joueur.isAI() ? "IA" : "Humain"));
            if (joueur.isAI()) {
                // En mode 4 joueurs, utiliser la difficulté correspondante
                if (JeuQuoridor.getNombreJoueurs() == 4) {
                    List<DifficulteIA> difficultes = JeuQuoridor.getDifficultesIA();
                    int indexIA = 0;
                    for (Joueur j : plateau.getJoueurs()) {
                        if (j.isAI() && j.getId() < joueur.getId()) {
                            indexIA++;
                        }
                    }
                    if (indexIA < difficultes.size()) {
                        System.out.println("Création de l'IA " + joueur.getId() + " avec difficulté " + difficultes.get(indexIA));
                        aiStrategies.put(joueur.getId(), new MinimaxAI(difficultes.get(indexIA).getProfondeur()));
                    } else {
                        System.err.println("Erreur : Pas de difficulté trouvée pour l'IA " + joueur.getId());
                    }
                } else {
                    // En mode 1v1 IA, utiliser la difficulté unique
                    System.out.println("Création de l'IA " + joueur.getId() + " avec difficulté " + JeuQuoridor.getDifficulteIA());
                    aiStrategies.put(joueur.getId(), new MinimaxAI(JeuQuoridor.getDifficulteIA().getProfondeur()));
                }
            }
        }

        // S'assurer que la mise à jour se fait sur le thread JavaFX
        javafx.application.Platform.runLater(() -> {
            updateBoardState();
            // Ajouter une petite pause et une requête de mise en page pour forcer le rendu
            PauseTransition pause = new PauseTransition(Duration.millis(50)); // Petite pause de 50 ms
            pause.setOnFinished(event -> {
                boardPane.requestLayout(); // Demander une nouvelle mise en page
            });
            pause.play();
        });
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
    private void runIA() {
        Joueur currentPlayer = plateau.getCurrentPlayer();
        System.out.println("IA " + currentPlayer.getId() + " réfléchit...");
        
        // Récupérer l'IA correspondante au joueur courant
        MinimaxAI aiStrategy = aiStrategies.get(currentPlayer.getId());
        if (aiStrategy == null) {
            System.err.println("Erreur: Pas d'IA trouvée pour le joueur " + currentPlayer.getId());
            return;
        }

        Action action = aiStrategy.getBestAction(plateau);
        System.out.println("Action trouvée.");

        if (action.getType() == MoveType.MOVE) {
            if (plateau.moveCurrentPlayer(action.getX(), action.getY())) {
                // Mouvement réussi
            } else {
                System.err.println("Erreur: L'IA a proposé un mouvement invalide!");
            }
        } else if (action.getType() == MoveType.WALL) {
             if (plateau.canPlaceWall(action.getX(), action.getY(), action.getVertical())
                    && plateau.placeWallCurrentPlayer(action.getX(), action.getY(), action.getVertical())) {
                 drawWall(action.getX(), action.getY(), action.getVertical());
             } else {
                 System.err.println("Erreur: L'IA a proposé un placement de mur invalide!");
             }
        }

        Joueur winner = plateau.getWinner();
        if (winner != null) {
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Partie terminée");
                alert.setContentText("Le joueur " + winner.getId() + " a gagné !");
                alert.showAndWait();
                JeuQuoridor.goMenu();
            });
        } else {
            switchPlayerTurn(); // continuer le tour suivant
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
        System.out.println("Entering updateBoardState...");
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
        System.out.println("Updating player positions with selected skins...");
        for (Joueur joueur : plateau.getJoueurs()) {
            // Assurez-vous que l'ID du joueur est valide pour l'index du tableau de skins (1-basé vers 0-basé)
            int playerIndex = joueur.getId() - 1;
            if (playerIndex >= 0 && playerIndex < selectedSkins.length) {
                int skinId = selectedSkins[playerIndex];
                // Appliquer le style CSS basé sur le skin sélectionné
                String styleClass = "player" + skinId;
                cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
                System.out.println("Player " + joueur.getId() + " at " + joueur.getX() + "," + joueur.getY() + " using skin " + skinId + " with style " + styleClass);
            } else {
                 System.err.println("Erreur: Skin non sélectionné pour le joueur ID: " + joueur.getId());
                 // Appliquer un style par défaut ou le style basé sur l'ID du joueur si la sélection échoue
                 String styleClass = "player" + joueur.getId();
                 cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
            }
        }

        // Mettre à jour les cases valides pour le joueur courant
        System.out.println("Highlighting valid moves...");
        for (int[] move : plateau.getPossibleMoves()) {
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
            System.out.println("Highlighting cell at " + move[0] + "," + move[1]);
        }

        // Mettre à jour le label des murs restants
        Joueur currentPlayer = plateau.getCurrentPlayer();
        labelMursRestants.setText("Murs restants : " + currentPlayer.getWallsRemaining());
        System.out.println("Murs restants pour le joueur " + currentPlayer.getId() + ": " + currentPlayer.getWallsRemaining());
        System.out.println("Exiting updateBoardState.");
    }

    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        Joueur currentPlayer = plateau.getCurrentPlayer();
        System.out.println("Switched to player ID: " + currentPlayer.getId() + ", is AI: " + currentPlayer.isAI());
        updateBoardState();

        if (currentPlayer.isAI()) {
            System.out.println("It is an AI player's turn, running IA...");
            // Utiliser la difficulté de l'IA appropriée si nécessaire
            // Pour l'instant, on utilise aiStrategy qui est la MinimaxAI globale
            PauseTransition pause = new PauseTransition(Duration.millis(500)); // Délai pour l'IA
            pause.setOnFinished(e -> runIA());
            pause.play();
        } else {
             System.out.println("It is a human player's turn.");
        }
    }

}
