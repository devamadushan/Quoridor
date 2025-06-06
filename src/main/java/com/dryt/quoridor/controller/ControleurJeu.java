package com.dryt.quoridor.controller;

import com.dryt.quoridor.ai.Action;
import com.dryt.quoridor.ai.MinimaxAI;
import com.dryt.quoridor.ai.MoveType;
import com.dryt.quoridor.ai.DifficulteIA;
import com.dryt.quoridor.utils.GameConstants;
import com.dryt.quoridor.utils.UserPreferences;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.GameState;
import com.dryt.quoridor.model.Mur;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class ControleurJeu {

    @FXML
    private Pane boardPane;
    
    @FXML
    private Pane boardContainer;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    @FXML
    private Button volumeButton;
    
    @FXML 
    private Slider volumeSlider;

    // Victory overlay controls
    @FXML
    private StackPane victoryOverlay;
    
    @FXML
    private Label victoryTitleLabel;
    
    @FXML
    private Label victoryMessageLabel;
    
    @FXML
    private Button victoryRedoButton;
    
    @FXML
    private Button victoryHomeButton;
    
    @FXML
    private Button victorySettingsButton;

    // Menu overlay elements
    @FXML
    private StackPane menuOverlay;

    @FXML
    private Button menuResumeButton;

    @FXML
    private Button menuNewGameButton;

    @FXML
    private Button menuHomeButton;

    @FXML
    private Button menuSettingsButton;

    @FXML
    private Label errorMessageLabel;
    
    @FXML
    private Button undoButton;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final double cellSize = GameConstants.CELL_SIZE;
    private final double wallSize = GameConstants.WALL_SIZE;
    private final double offsetX = GameConstants.OFFSET_X;
    private final double offsetY = GameConstants.OFFSET_Y;
    private Rectangle ghostWall;
    private Map<Integer, MinimaxAI> aiStrategies;
    
    private double scaleFactor = 1.0;

    // Optimisation du redimensionnement
    private PauseTransition resizeDebounceTimer;
    private boolean isResizing = false;
    
    private GameState previousGameState = null;
    private boolean undoAvailable = false;

    // Initialise l'interface du jeu et ses composants
    @FXML
    private void initialize() {
        System.out.println("Initialisation du contrôleur de jeu");
        cellButtons = new Button[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        aiStrategies = new HashMap<>();

        javafx.application.Platform.runLater(() -> {
            System.out.println("Exécution des initialisations différées");
            loadCSS();
            setBoardContainerSize();
            createGameBoard();
            setupKeyboardShortcuts();
            setupVolumeControls();
            setupDynamicScaling();

        });
    }
    
    // Configure les contrôles de volume
    private void setupVolumeControls() {
        System.out.println("Configuration des contrôles de volume");
        System.out.println("Bouton volume : " + volumeButton);
        System.out.println("Curseur volume : " + volumeSlider);
        
        if (volumeSlider != null) {
            volumeSlider.setMin(0.0);
            volumeSlider.setMax(1.0);
            volumeSlider.setValue(JeuQuoridor.getGlobalMusicVolume());
            
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                JeuQuoridor.setGlobalMusicVolume(newValue.doubleValue());
                updateVolumeButtonIcon(); 
            });
            System.out.println("Curseur de volume initialisé avec le volume global");
        } else {
            System.out.println("Erreur : Curseur de volume non initialisé");
        }
        
        if (volumeButton != null) {
            System.out.println("Configuration de l'icône du bouton volume");
            updateVolumeButtonIcon();
        } else {
            System.out.println("Erreur : Bouton de volume non initialisé");
        }
    }
    
    // Bascule l'état du volume
    @FXML
    private void onVolumeToggle() {
        JeuQuoridor.toggleGlobalMusicMute();
        updateVolumeButtonIcon();
        System.out.println("Volume basculé - Muet : " + JeuQuoridor.isGlobalMusicMuted());
    }
    
    // Met à jour l'icône du bouton volume
    private void updateVolumeButtonIcon() {
        if (volumeButton != null) {
            volumeButton.getStyleClass().removeAll("volume-button-sound", "volume-button-mute");
            
            if (JeuQuoridor.isGlobalMusicMuted() || JeuQuoridor.getGlobalMusicVolume() == 0.0) {
                volumeButton.getStyleClass().add("volume-button-mute");
                System.out.println("Icône muet appliquée");
            } else {
                volumeButton.getStyleClass().add("volume-button-sound");
                System.out.println("Icône son appliquée");
            }
        }
    }
    
    // Définit la taille du conteneur du plateau
    private void setBoardContainerSize() {
        if (boardContainer != null) {
            double boardWidth = 2 * offsetX + 9 * cellSize + 8 * wallSize; 
            double boardHeight = 2 * offsetY + 9 * cellSize + 8 * wallSize; 
        
            boardPane.setPrefSize(boardWidth, boardHeight);
            boardPane.setMinSize(boardWidth, boardHeight);
            boardPane.setMaxSize(boardWidth, boardHeight);
            
            double containerPadding = 4; 
            double containerSize = boardWidth + (2 * containerPadding);
            
            boardContainer.setPrefSize(containerSize, containerSize);
            boardContainer.setMinSize(containerSize, containerSize);
            boardContainer.setMaxSize(containerSize, containerSize);
            
            System.out.println("Taille du plateau : " + boardWidth + "x" + boardHeight);
            System.out.println("Taille du conteneur : " + containerSize + "x" + containerSize);
        }
    }
    
    // Charge les styles CSS
    private void loadCSS() {
        try {
            String cssPath = getClass().getResource("/com/dryt/quoridor/styles/style_jeu.css").toExternalForm();
            boardPane.getScene().getStylesheets().add(cssPath);
            System.out.println("CSS chargé : " + cssPath);
        } catch (Exception e) {
            System.out.println("Échec du chargement CSS : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Crée le plateau de jeu
    private void createGameBoard() {
        boardPane.getChildren().clear();

        double scaledCellSize = GameConstants.CELL_SIZE * scaleFactor;
        double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
        double scaledOffsetX = GameConstants.OFFSET_X * scaleFactor;
        double scaledOffsetY = GameConstants.OFFSET_Y * scaleFactor;

        for (int y = 0; y < GameConstants.BOARD_SIZE; y++) {
            for (int x = 0; x < GameConstants.BOARD_SIZE; x++) {
                double baseX = scaledOffsetX + x * (scaledCellSize + scaledWallSize);
                double baseY = scaledOffsetY + y * (scaledCellSize + scaledWallSize);

                    Button cell = new Button();
                cell.setPrefSize(scaledCellSize, scaledCellSize);
                cell.setMinSize(scaledCellSize, scaledCellSize);
                cell.setMaxSize(scaledCellSize, scaledCellSize);
                
                if ((x + y) % 2 == 0) {
                    cell.getStyleClass().add("cell");
                } else {
                    cell.getStyleClass().add("cell-dark");   
                }
                
                // Centrer le contenu de la cellule
                cell.setStyle(cell.getStyle() + 
                    "-fx-padding: 0;" +
                    "-fx-content-display: center;" +
                    "-fx-alignment: center;");
                
                    cell.setLayoutX(baseX);
                    cell.setLayoutY(baseY);
                    final int cx = x;
                    final int cy = y;
                    cell.setOnAction(event -> onCellClicked(cx, cy));
                    cellButtons[cx][cy] = cell;
                    boardPane.getChildren().add(cell);

                    if (x < 8 && y < 9)
                    createWallPlaceholder(baseX + scaledCellSize, baseY + scaledCellSize / 2.0 - scaledWallSize / 2.0, x, y, true);
                    if (y < 8 && x < 9)
                    createWallPlaceholder(baseX + scaledCellSize / 2.0 - scaledWallSize / 2.0, baseY + scaledCellSize, x, y, false);
                    if (x < 8 && y < 8)
                    createWallPlaceholder(baseX + scaledCellSize, baseY + scaledCellSize, x, y, true);
            }
        }
        
        System.out.println("Création du plateau terminée avec facteur d'échelle : " + scaleFactor);
    }

    // Configure le plateau et affiche l'interface
    public void setupPlateauAndDisplay(Plateau plateau) {
        this.plateau = plateau;

        // Réinitialiser l'état d'annulation pour une nouvelle partie
        previousGameState = null;
        undoAvailable = false;
        updateUndoButtonState();

        // Réinitialiser les stratégies IA
        aiStrategies = new HashMap<>();

        // Réinitialiser complètement le plateau graphique et les boutons
        if (boardPane != null) {
            boardPane.getChildren().clear();
        }
        cellButtons = new Button[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        createGameBoard();

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

    // Crée un emplacement pour un mur
    private void createWallPlaceholder(double x, double y, int wx, int wy, boolean vertical) {
        double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
        double detectorSize = scaledWallSize * 4;

        Rectangle wallDetector = new Rectangle(detectorSize, detectorSize);
        wallDetector.setLayoutX(x - (detectorSize - scaledWallSize) / 4.0);
        wallDetector.setLayoutY(y - (detectorSize - scaledWallSize) / 4.0);
        wallDetector.setStyle("-fx-fill: transparent; -fx-stroke: transparent;");

        wallDetector.setOnMouseEntered(e -> showGhostWall(wx, wy, vertical));
        wallDetector.setOnMouseExited(e -> hideGhostWall());

        wallDetector.setOnMouseClicked(e -> {
            int effectiveWx = wx;
            int effectiveWy = wy;
            if (!vertical && wx == 8) effectiveWx = 7;
            if (vertical && wy == 8) effectiveWy = 7;

            if (!plateau.canPlaceWall(effectiveWx, effectiveWy, vertical)) {
                if (plateau.getCurrentPlayer().getWallsRemaining() <= 0) {
                    showErrorMessage("Plus de murs disponibles");
                } else if (isCrossingWall(effectiveWx, effectiveWy, vertical)) {
                    showErrorMessage("Croisement de mur interdit");
                } else if (!plateau.allPlayersHaveAPathAfterWall(effectiveWx, effectiveWy, vertical)) {
                    showErrorMessage("Ce mur bloquerait un joueur complètement");
                } else if (plateau.isWallOverlapping(effectiveWx, effectiveWy, vertical)) {
                    showErrorMessage("Chevauchement de mur interdit");
                } else if (isWallAlreadyPresent(effectiveWx, effectiveWy, vertical)) {
                    showErrorMessage("Un mur est déjà présent ici");
                } else {
                    showErrorMessage("Placement de mur invalide");
                }
                return;
            }

            // Sauvegarder l'état avant le placement de mur
            saveGameStateForUndo();
            
            if (plateau.placeWallCurrentPlayer(effectiveWx, effectiveWy, vertical)) {
                drawWall(effectiveWx, effectiveWy, vertical);
                switchPlayerTurn();
            }
        });
        boardPane.getChildren().add(wallDetector);
    }

    // Vérifie si un mur est déjà présent
    private boolean isWallAlreadyPresent(int wx, int wy, boolean vertical) {
        for (Mur mur : plateau.getMurs()) {
            if (mur.getX() == wx && mur.getY() == wy && mur.isVertical() == vertical) {
                return true;
            }
        }
        return false;
    }

    // Vérifie si un mur croise un autre mur
    private boolean isCrossingWall(int wx, int wy, boolean vertical) {
        if (vertical) {
            // Pour un mur vertical, vérifier s'il y a un mur horizontal qui le croise
            // Un mur vertical à (wx, wy) croise avec un mur horizontal à (wx, wy)
            return plateau.hasHorizontalWall(wx, wy);
        } else {
            // Pour un mur horizontal, vérifier s'il y a un mur vertical qui le croise
            // Un mur horizontal à (wx, wy) croise avec un mur vertical à (wx, wy)
            return plateau.hasVerticalWall(wx, wy);
        }
    }

    // Affiche le mur fantôme lors du survol
    private void showGhostWall(int wx, int wy, boolean vertical) {
        hideGhostWall();
        ghostWall = new Rectangle();
        ghostWall.setMouseTransparent(true);

        double scaledCellSize = GameConstants.CELL_SIZE * scaleFactor;
        double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
        double scaledOffsetX = GameConstants.OFFSET_X * scaleFactor;
        double scaledOffsetY = GameConstants.OFFSET_Y * scaleFactor;

        int effectiveWx = wx;
        int effectiveWy = wy;
        if (!vertical && wx == 8) effectiveWx = 7;
        if (vertical && wy == 8) effectiveWy = 7;

        boolean canPlace = plateau.canPlaceWall(effectiveWx, effectiveWy, vertical);
        boolean invalid = !canPlace;

        ghostWall.setStyle(invalid
                ? "-fx-fill: rgba(255, 0, 0, 0.4); -fx-stroke: red; -fx-stroke-width: 3;"
                : "-fx-fill: rgba(218, 165, 32, 0.6); -fx-stroke: #FFD700; -fx-stroke-width: 3; -fx-effect: dropshadow(gaussian, rgba(255,215,0,0.8), 6, 0.7, 0, 0);");

        if (vertical) {
            ghostWall.setWidth(scaledWallSize);
            ghostWall.setHeight(scaledCellSize * 2 + scaledWallSize);
            ghostWall.setX(scaledOffsetX + effectiveWx * (scaledCellSize + scaledWallSize) + scaledCellSize);
            ghostWall.setY(scaledOffsetY + effectiveWy * (scaledCellSize + scaledWallSize));
        } else {
            ghostWall.setWidth(scaledCellSize * 2 + scaledWallSize);
            ghostWall.setHeight(scaledWallSize);
            ghostWall.setX(scaledOffsetX + effectiveWx * (scaledCellSize + scaledWallSize));
            ghostWall.setY(scaledOffsetY + effectiveWy * (scaledCellSize + scaledWallSize) + scaledCellSize);
        }

        boardPane.getChildren().add(ghostWall);
    }

    // Cache le mur fantôme
    private void hideGhostWall() {
        if (ghostWall != null) {
            boardPane.getChildren().remove(ghostWall);
            ghostWall = null;
        }
    }

    // Dessine un mur sur le plateau
    private void drawWall(int wx, int wy, boolean vertical) {
        double scaledCellSize = GameConstants.CELL_SIZE * scaleFactor;
        double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
        double scaledOffsetX = GameConstants.OFFSET_X * scaleFactor;
        double scaledOffsetY = GameConstants.OFFSET_Y * scaleFactor;

        Rectangle wallSegment = new Rectangle();
        if (vertical) {
            wallSegment.setWidth(scaledWallSize);
            wallSegment.setHeight(scaledCellSize * 2 + scaledWallSize);
            wallSegment.setX(scaledOffsetX + wx * (scaledCellSize + scaledWallSize) + scaledCellSize);
            wallSegment.setY(scaledOffsetY + wy * (scaledCellSize + scaledWallSize));
        } else {
            wallSegment.setWidth(scaledCellSize * 2 + scaledWallSize);
            wallSegment.setHeight(scaledWallSize);
            wallSegment.setX(scaledOffsetX + wx * (scaledCellSize + scaledWallSize));
            wallSegment.setY(scaledOffsetY + wy * (scaledCellSize + scaledWallSize) + scaledCellSize);
        }
        wallSegment.getStyleClass().add("wall-placed");
        boardPane.getChildren().add(wallSegment);
        System.out.println("Mur placé : " + (vertical ? "V" : "H") + " à " + wx + ", " + wy + " (échelle : " + scaleFactor + ")");
    }

    //Redessine tous les murs existants avec le nouveau facteur d'échelle
    private void redrawAllWalls() {
        if (plateau != null) {
            // Supprimer tous les murs visuels existants
            clearAllWallsFromUI();
            
            // Redessiner uniquement les murs qui existent dans le modèle
            for (Mur mur : plateau.getMurs()) {
                drawWall(mur.getX(), mur.getY(), mur.isVertical());
            }
            System.out.println(plateau.getMurs().size() + " mur(s) redessiné(s) avec l'échelle " + scaleFactor);
        }
    }
    
    // Supprime tous les éléments visuels de murs du plateau
    private void clearAllWallsFromUI() {
        if (boardPane != null) {
            // Supprimer tous les rectangles qui ont la classe "wall-placed"
            boardPane.getChildren().removeIf(node -> 
                node instanceof Rectangle && 
                node.getStyleClass().contains("wall-placed")
            );
            System.out.println("Tous les murs visuels supprimés de l'interface");
        }
    }

    // Gère le clic sur une cellule
    private void onCellClicked(int x, int y) {
        System.out.println("Cellule cliquée à [" + x + "," + y + "] par le joueur " + plateau.getCurrentPlayer().getId());
        
        if (!cellButtons[x][y].getStyleClass().contains("highlight")) {
            System.out.println("Cellule [" + x + "," + y + "] non surlignée - clic ignoré");
            return;
        }
        
        System.out.println("Déplacement valide vers [" + x + "," + y + "] - exécution du mouvement");
        
        // Sauvegarder l'état avant le mouvement
        saveGameStateForUndo();
        
        if (!plateau.moveCurrentPlayer(x, y)) {
            System.out.println("Échec du déplacement pour le joueur " + plateau.getCurrentPlayer().getId());
            return;
        }

        Joueur winner = plateau.getWinner();
        if (winner != null) {
            System.out.println("Partie terminée - Vainqueur : Joueur " + winner.getId());
            
            showVictoryPopup(winner.getId());
            return;
        }

        switchPlayerTurn();
    }

    // Met à jour l'état du plateau
    private void updateBoardState() {
        System.out.println("Mise à jour de l'état du plateau");
        
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cellButtons[x][y].getStyleClass().removeAll("highlight", "player1", "player2", "player3", "player4", "current-player");
            }
        }

        int[] selectedSkins = JeuQuoridor.getSelectedSkins();

        for (Joueur joueur : plateau.getJoueurs()) {
            int playerIndex = joueur.getId() - 1;
            if (playerIndex >= 0 && playerIndex < selectedSkins.length) {
                int skinId = selectedSkins[playerIndex];
                String styleClass = "player" + skinId;
                Button cell = cellButtons[joueur.getX()][joueur.getY()];
                cell.getStyleClass().add(styleClass);
                
                // Ajuster la taille de l'icône en fonction de la taille de la cellule
                double cellSize = GameConstants.CELL_SIZE * scaleFactor;
                double iconSize = cellSize * 0.8; 
                cell.setStyle(cell.getStyle() + 
                    String.format("-fx-background-size: %fpx %fpx;", iconSize, iconSize) +
                    "-fx-background-position: center;" +
                    "-fx-background-repeat: no-repeat;");
                
                System.out.println("Style " + styleClass + " ajouté à la cellule [" + joueur.getX() + "," + joueur.getY() + "]");
                
                if (joueur.getId() == plateau.getCurrentPlayer().getId()) {
                    cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add("current-player");
                    System.out.println("Indicateur de joueur actuel ajouté au joueur " + joueur.getId());
                }
            } else {
            String styleClass = "player" + joueur.getId();
                Button cell = cellButtons[joueur.getX()][joueur.getY()];
                cell.getStyleClass().add(styleClass);
                
                double cellSize = GameConstants.CELL_SIZE * scaleFactor;
                double iconSize = cellSize * 0.8;
                cell.setStyle(cell.getStyle() + 
                    String.format("-fx-background-size: %fpx %fpx;", iconSize, iconSize) +
                    "-fx-background-position: center;" +
                    "-fx-background-repeat: no-repeat;");
                
                System.out.println("Style par défaut " + styleClass + " ajouté à la cellule [" + joueur.getX() + "," + joueur.getY() + "]");
                
                if (joueur.getId() == plateau.getCurrentPlayer().getId()) {
                    cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add("current-player");
                    System.out.println("Indicateur de joueur actuel ajouté au joueur " + joueur.getId());
                }
            }
        }

        System.out.println("Mouvements possibles : ");
        for (int[] move : plateau.getPossibleMoves()) {
            System.out.println(Arrays.toString(move));
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
            System.out.println("Surlignage ajouté à la cellule [" + move[0] + "," + move[1] + "]");
        }

        updateWallCountDisplay();
    }

    // Met à jour l'affichage du nombre de murs restants
    private void updateWallCountDisplay() {
        StringBuilder wallInfo = new StringBuilder();
        
        java.util.List<Joueur> sortedPlayers = new java.util.ArrayList<>(plateau.getJoueurs());
        sortedPlayers.sort((j1, j2) -> Integer.compare(j1.getId(), j2.getId()));
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Joueur joueur = sortedPlayers.get(i);
            
            if (i > 0) {
                wallInfo.append("\n");
            }
            
            String prefix = (joueur.getId() == plateau.getCurrentPlayer().getId()) ? "► " : "  ";
            String suffix = (joueur.getId() == plateau.getCurrentPlayer().getId()) ? " ◄" : "  ";
            
            wallInfo.append(String.format("%s J%d: %2d murs%s", 
                prefix, 
                joueur.getId(), 
                joueur.getWallsRemaining(), 
                suffix));
        }
        
        labelMursRestants.setText(wallInfo.toString());
    }

    // Exécute le tour de l'IA
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
                return;
            }
        } else if (action.getType() == MoveType.WALL) {
             if (!plateau.canPlaceWall(action.getX(), action.getY(), action.getVertical())
                    || !plateau.placeWallCurrentPlayer(action.getX(), action.getY(), action.getVertical())) {
                 System.err.println(GameConstants.ERROR_INVALID_WALL);
                 return;
             } else {
                 drawWall(action.getX(), action.getY(), action.getVertical());
             }
        }

        Joueur winner = plateau.getWinner();
        if (winner != null) {
            System.out.println("Partie terminée - Vainqueur : Joueur " + winner.getId());
            
            showVictoryPopup(winner.getId());
            return;
        }

        switchPlayerTurn();
    }

    // Vérifie s'il y a un vainqueur
    private void checkForWinner() {
        Joueur winner = plateau.getWinner();
        if (winner != null) {
            System.out.println("Partie terminée - Vainqueur : Joueur " + winner.getId());
            
            showVictoryPopup(winner.getId());
        }
    }

    // Change le tour du joueur
    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        Joueur currentPlayer = plateau.getCurrentPlayer();
        updateBoardState();

        if (currentPlayer.isAI()) {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                runIA();
                // Ne PAS désactiver l'annulation après le tour de l'IA
                // L'humain doit pouvoir annuler sa séquence (son coup + réponse IA)
                System.out.println("Tour de l'IA terminé - annulation reste disponible pour l'humain");
            });
            pause.play();
        }
    }
    
    // Ouvre le menu de pause
    @FXML
    private void onOpenMenu() {
        System.out.println("Ouverture du menu de pause");
        
        if (menuOverlay != null) {
            menuOverlay.setVisible(true);
            menuOverlay.setManaged(true);
            
            menuOverlay.toFront();
            
            System.out.println("Menu de pause affiché avec succès");
        } else {
            System.err.println("Menu de pause non initialisé, retour au menu principal");
            JeuQuoridor.goMenu();
        }
    }

    public void showMenuOverlay() {
        System.out.println("Ouverture du menu de pause (appel externe)");
        
        // Show the menu overlay
        if (menuOverlay != null) {
            menuOverlay.setVisible(true);
            menuOverlay.setManaged(true);
            
            menuOverlay.toFront();
            
            System.out.println("Menu de pause affiché avec succès");
        } else {
            System.err.println("Menu de pause non initialisé, retour au menu principal");
            JeuQuoridor.goMenu();
        }
    }
    
    // Affiche l'écran de victoire
    public void showVictoryOverlay() {
        System.out.println("Affichage de l'écran de victoire (appel externe)");
        
        if (victoryOverlay != null) {
            victoryOverlay.setVisible(true);
            victoryOverlay.setManaged(true);
            
            victoryOverlay.toFront();
            
            System.out.println("Écran de victoire affiché avec succès");
        } else {
            System.err.println("Écran de victoire non initialisé");
        }
    }

    // Configure les raccourcis clavier
    private void setupKeyboardShortcuts() {
        if (boardPane.getScene() != null) {
            boardPane.getScene().setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case F11:
                        toggleFullscreen();
                        break;
                    case ESCAPE:
                        if (JeuQuoridor.getPrimaryStage().isFullScreen()) {
                            JeuQuoridor.getPrimaryStage().setFullScreen(false);
                        }
                        break;
                }
            });
            
            boardPane.getScene().getRoot().setFocusTraversable(true);
            boardPane.getScene().getRoot().requestFocus();
        }
    }
    
    // Bascule le mode plein écran
    private void toggleFullscreen() {
        Stage stage = JeuQuoridor.getPrimaryStage();
        stage.setFullScreen(!stage.isFullScreen());
        System.out.println("Plein écran basculé : " + stage.isFullScreen());
    }

    // Configure la mise à l'échelle dynamique
    private void setupDynamicScaling() {
        if (boardPane.getScene() != null && boardPane.getScene().getWindow() != null) {
            Stage stage = (Stage) boardPane.getScene().getWindow();
            
            resizeDebounceTimer = new PauseTransition(Duration.millis(100)); // 100ms de délai
            resizeDebounceTimer.setOnFinished(e -> {
                isResizing = false;
                updateScaling();
                System.out.println("Redimensionnement terminé - application de l'échelle finale");
            });
            
            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                handleResize();
            });
            
            stage.heightProperty().addListener((obs, oldVal, newVal) -> {
                handleResize();
            });
            
            updateScaling();
            System.out.println("Mise à l'échelle dynamique initialisée avec optimisation");
        }
    }
    
    // Gère le redimensionnement
    private void handleResize() {
        if (!isResizing) {
            isResizing = true;
            System.out.println("Redimensionnement détecté - démarrage du timer");
        }
        
        // Redémarrer le timer de débounce à chaque redimensionnement
        resizeDebounceTimer.stop();
        resizeDebounceTimer.play();
    }
    
    // Met à jour l'échelle
    private void updateScaling() {
        if (boardPane.getScene() != null && boardPane.getScene().getWindow() != null) {
            Stage stage = (Stage) boardPane.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            double aspectRatio = currentWidth / currentHeight;
            
            double availableWidth = currentWidth * 0.75; 
            double availableHeight = currentHeight * 0.75; 
            
            double baseBoardWidth = 2 * GameConstants.OFFSET_X + 9 * GameConstants.CELL_SIZE + 8 * GameConstants.WALL_SIZE;
            double baseBoardHeight = 2 * GameConstants.OFFSET_Y + 9 * GameConstants.CELL_SIZE + 8 * GameConstants.WALL_SIZE;
            
            double widthRatio = availableWidth / baseBoardWidth;
            double heightRatio = availableHeight / baseBoardHeight;
            
            scaleFactor = Math.min(widthRatio, heightRatio);
            
            if (currentWidth <= 800 || currentHeight <= 600) {
                scaleFactor = Math.max(scaleFactor, 0.2);
                scaleFactor = Math.min(scaleFactor, 0.5);
            } else if (currentWidth >= 1920) {
                scaleFactor = Math.max(scaleFactor, 0.5);
                scaleFactor = Math.min(scaleFactor, 0.8);
            } else if (currentWidth >= 1280) {
                scaleFactor = Math.max(scaleFactor, 0.4);
                scaleFactor = Math.min(scaleFactor, 0.7);
            } else {
                scaleFactor = Math.max(scaleFactor, 0.3);
                scaleFactor = Math.min(scaleFactor, 0.6);
            }
            
            System.out.println("Facteur d'échelle mis à jour : " + scaleFactor + 
                             " (Fenêtre : " + currentWidth + "x" + currentHeight + 
                             ", Ratio : " + aspectRatio + ")");
            
            // Appliquer la mise à l'échelle aux éléments du jeu
            applyScaling();
        }
    }
    
    // Applique la mise à l'échelle
    private void applyScaling() {
        System.out.println("Application de l'échelle avec facteur : " + scaleFactor);
        
        // Mise à l'échelle du conteneur du plateau
        if (boardContainer != null) {
            // Calculer la taille du plateau mise à l'échelle
            double scaledCellSize = GameConstants.CELL_SIZE * scaleFactor;
            double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
            double scaledOffsetX = GameConstants.OFFSET_X * scaleFactor;
            double scaledOffsetY = GameConstants.OFFSET_Y * scaleFactor;
            
            double boardWidth = 2 * scaledOffsetX + 9 * scaledCellSize + 8 * scaledWallSize;
            double boardHeight = 2 * scaledOffsetY + 9 * scaledCellSize + 8 * scaledWallSize;
            
            // Mettre à jour la taille du plateau
            boardPane.setPrefSize(boardWidth, boardHeight);
            boardPane.setMinSize(boardWidth, boardHeight);
            boardPane.setMaxSize(boardWidth, boardHeight);
            
            // Calculer le padding en fonction de la taille de la fenêtre
            double containerPadding = Math.max(2 * scaleFactor, 2);
            double containerSize = Math.max(boardWidth, boardHeight) + (2 * containerPadding);
            
            boardContainer.setPrefSize(containerSize, containerSize);
            boardContainer.setMinSize(containerSize, containerSize);
            boardContainer.setMaxSize(containerSize, containerSize);
            
            // Toujours recréer le plateau de jeu avec la nouvelle échelle
            System.out.println("Recréation du plateau avec facteur d'échelle : " + scaleFactor);
            createGameBoard();
            
            if (plateau != null) {
                updateBoardState();
                // Redessiner tous les murs existants avec la nouvelle échelle
                redrawAllWalls();
                System.out.println("État du plateau et murs mis à jour avec nouvelle échelle");
            }
            
            System.out.println("Mise à l'échelle du plateau appliquée : " + scaleFactor + " (conteneur : " + containerSize + "px)");
        }
        
        // Mise à l'échelle des contrôles audio et du texte
        if (boardPane.getScene() != null && boardPane.getScene().getWindow() != null) {
            Stage stage = (Stage) boardPane.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            // Calculer les tailles de base en fonction de la résolution
            double baseButtonSize, baseSliderWidth, baseFontSize;
            
            // Calculer un facteur de résolution basé sur la surface de l'écran
            double screenArea = currentWidth * currentHeight;
            double resolutionFactor = Math.sqrt(screenArea) / Math.sqrt(1920 * 1080);
            
            // Tailles de base pour 1920x1080
            double base1920ButtonSize = 45;
            double base1920SliderWidth = 150;
            double base1920FontSize = 24;
            
            // Ajuster les tailles en fonction du facteur de résolution
            baseButtonSize = base1920ButtonSize * resolutionFactor;
            baseSliderWidth = base1920SliderWidth * resolutionFactor;
            baseFontSize = base1920FontSize * resolutionFactor;
            
            // Appliquer des limites minimales et maximales
            baseButtonSize = Math.min(Math.max(baseButtonSize, 30), 60);
            baseSliderWidth = Math.min(Math.max(baseSliderWidth, 100), 200);
            baseFontSize = Math.min(Math.max(baseFontSize, 16), 32);
            
            // Appliquer la mise à l'échelle aux contrôles audio
            if (volumeButton != null) {
                double buttonSize = Math.max(baseButtonSize * scaleFactor, baseButtonSize * 0.7);
                volumeButton.setPrefSize(buttonSize, buttonSize);
                volumeButton.setMinSize(buttonSize, buttonSize);
            }
            
            if (volumeSlider != null) {
                double sliderWidth = Math.max(baseSliderWidth * scaleFactor, baseSliderWidth * 0.7);
                volumeSlider.setPrefWidth(sliderWidth);
            }
            
            // Mise à l'échelle du texte
            if (labelMursRestants != null) {
                double fontSize = Math.max(baseFontSize * scaleFactor, baseFontSize * 0.7);
                labelMursRestants.setStyle(
                    labelMursRestants.getStyle().replaceAll("-fx-font-size: [^;]+;", "") + 
                    " -fx-font-size: " + fontSize + "px;"
                );
            }
            
            System.out.println("Contrôles mis à l'échelle - Bouton : " + baseButtonSize + 
                             "px, Curseur : " + baseSliderWidth + "px, Police : " + baseFontSize + "px");
        }
        
        if (!isResizing) {
            if (!JeuQuoridor.wasBackgroundPreserved()) {
                applySavedBackground();
            } else {
                System.out.println("Arrière-plan préservé, application de l'arrière-plan sauvegardé ignorée");
            }
        }
    }

    // Affiche la popup de victoire
    private void showVictoryPopup(int winnerId) {
        System.out.println("Affichage de l'écran de victoire pour le joueur " + winnerId);
        
        if (victoryMessageLabel != null) {
            victoryMessageLabel.setText("Le joueur " + winnerId + " a gagné !");
        }
        
        if (victoryOverlay != null) {
            victoryOverlay.setVisible(true);
            victoryOverlay.setManaged(true);
            
            victoryOverlay.toFront();
            
            System.out.println("Écran de victoire affiché avec succès");
        } else {
            System.err.println("Écran de victoire non initialisé, retour au menu principal");
            JeuQuoridor.goMenu();
        }
    }
    
    // Cache l'écran de victoire
    private void hideVictoryOverlay() {
        if (victoryOverlay != null) {
            victoryOverlay.setVisible(false);
            victoryOverlay.setManaged(false);
        }
    }
    
    // Relance la partie
    @FXML
    private void onVictoryReplay() {
        System.out.println("Bouton de relance cliqué");
        
        hideVictoryOverlay();
        
        JeuQuoridor.restartCurrentGame();
    }
    
    // Retourne au menu principal
    @FXML
    private void onVictoryMenu() {
        System.out.println("Bouton menu principal cliqué");
        
        hideVictoryOverlay();
        
        JeuQuoridor.stopGlobalMusic();
        
        JeuQuoridor.goMenu();
    }
    
    // Ouvre les paramètres
    @FXML
    private void onVictorySettings() {
        System.out.println("Bouton paramètres cliqué");
        
        hideVictoryOverlay();
        
        JeuQuoridor.goOptionsFromEndGame();
    }
    
    // Cache le menu de pause
    private void hideMenuOverlay() {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
        }
    }
    
    // Reprend la partie
    @FXML
    private void onMenuResume() {
        System.out.println("Bouton reprise cliqué");
        
        hideMenuOverlay();
    }
    
    // Lance une nouvelle partie
    @FXML
    private void onMenuNewGame() {
        System.out.println("Bouton nouvelle partie cliqué");
        
        hideMenuOverlay();
        
        JeuQuoridor.restartCurrentGame();
    }
    
    // Retourne au menu principal
    @FXML
    private void onMenuHome() {
        System.out.println("Bouton menu principal cliqué");
        
        hideMenuOverlay();
        
        JeuQuoridor.stopGlobalMusic();
        
        JeuQuoridor.goMenu();
    }
    
    // Ouvre les paramètres
    @FXML
    private void onMenuSettings() {
        System.out.println("Bouton paramètres cliqué");
        
        hideMenuOverlay();
        
        JeuQuoridor.goOptionsFromGame();
    }

    // Affiche un message d'erreur
    private void showErrorMessage(String message) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
            errorMessageLabel.setVisible(true);
            errorMessageLabel.setManaged(true);
            errorMessageLabel.setOpacity(1.0);
            
            // Créer une transition de fondu
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), errorMessageLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Créer une transition pour le délai
            PauseTransition delay = new PauseTransition(Duration.seconds(0.7));
            
            // Créer une séquence de transitions
            SequentialTransition sequence = new SequentialTransition(delay, fadeOut);
            sequence.setOnFinished(event -> {
                errorMessageLabel.setVisible(false);
                errorMessageLabel.setManaged(false);
            });
            sequence.play();
        }
    }

    // Applique l'arrière-plan sauvegardé
    private void applySavedBackground() {
        if (JeuQuoridor.wasBackgroundPreserved()) {
            System.out.println("Arrière-plan préservé, application ignorée");
            return;
        }
        
        try {
            String selectedBackground = UserPreferences.getSelectedBackground();
            JeuQuoridor.updateGameBackground(selectedBackground);
            System.out.println("Arrière-plan appliqué : " + selectedBackground);
        } catch (Exception e) {
            System.err.println("Échec de l'application de l'arrière-plan : " + e.getMessage());
        }
    }

    // Déclenche le redimensionnement du plateau
    public void triggerBoardResize() {
        System.out.println("Redimensionnement du plateau déclenché - recalcul complet");
        
        javafx.application.Platform.runLater(() -> {
            try {
                isResizing = false;
                
                updateScaling();
                
                if (boardPane != null) {
                    boardPane.requestLayout();
                }
                if (boardContainer != null) {
                    boardContainer.requestLayout();
                }
                
                System.out.println("Recalcul complet du plateau terminé");
            } catch (Exception e) {
                System.err.println("Erreur lors du redimensionnement : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    // Sauvegarde l'état du jeu pour permettre l'annulation (uniquement pour les joueurs humains)
    private void saveGameStateForUndo() {
        // Seuls les coups des joueurs humains peuvent être annulés
        if (plateau.getCurrentPlayer().isAI()) {
            System.out.println("Pas de sauvegarde pour l'IA");
            return;
        }
        
        previousGameState = new GameState(plateau);
        undoAvailable = true;
        updateUndoButtonState();
        System.out.println("État du jeu sauvegardé pour annulation");
    }
    
    // Met à jour l'état du bouton d'annulation
    private void updateUndoButtonState() {
        if (undoButton != null) {
            undoButton.setDisable(!undoAvailable);
        }
    }
    
    // Annule le dernier coup
    @FXML
    private void onUndo() {
        if (!undoAvailable || previousGameState == null) {
            System.out.println("Aucune action à annuler");
            return;
        }
        
        System.out.println("Annulation du dernier coup");
        
        // Restaurer l'état précédent
        previousGameState.restoreToBoard(plateau);
        
        // Mettre à jour l'affichage
        updateBoardState();
        redrawAllWalls();
        updateWallCountDisplay();
        
        // TOUJOURS désactiver l'annulation après une restauration
        // Cela empêche l'annulation en chaîne et assure qu'on ne peut annuler qu'un coup à la fois
        undoAvailable = false;
        previousGameState = null;
        updateUndoButtonState();
        
        System.out.println("État du jeu restauré avec succès - annulation désactivée");
    }
}
