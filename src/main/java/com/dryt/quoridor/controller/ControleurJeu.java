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
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import com.dryt.quoridor.model.Plateau;
import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.Mur;
import com.dryt.quoridor.app.JeuQuoridor;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

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
    private Pane boardContainer;

    @FXML
    private javafx.scene.control.Label labelMursRestants;

    @FXML
    private Button volumeButton;
    
    @FXML 
    private Slider volumeSlider;

    private Plateau plateau;
    private Button[][] cellButtons;
    private final double cellSize = GameConstants.CELL_SIZE;
    private final double wallSize = GameConstants.WALL_SIZE;
    private final double offsetX = GameConstants.OFFSET_X;
    private final double offsetY = GameConstants.OFFSET_Y;
    private Rectangle ghostWall;
    private Map<Integer, MinimaxAI> aiStrategies;
    
    // Audio management
    private MediaPlayer backgroundMusic;
    private boolean isMusicMuted = false;
    private double savedVolume = 0.3; // Default volume at 30%

    @FXML
    private void initialize() {
        System.out.println("🎮 ControleurJeu.initialize() called");
        cellButtons = new Button[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        aiStrategies = new HashMap<>();
        
        // Initialize audio immediately
        System.out.println("🎵 About to initialize audio...");
        initializeAudio();
        
        // Wait for plateau to be set up via setupPlateauAndDisplay
        javafx.application.Platform.runLater(() -> {
            System.out.println("🎮 Platform.runLater executing...");
            loadCSS();
            setBoardContainerSize();
            createGameBoard();
            setupKeyboardShortcuts();
            setupVolumeControls();
        });
    }
    
    private void initializeAudio() {
        System.out.println("🎵 initializeAudio() method called");
        try {
            // Load the background music - properly encode the file name with spaces
            String musicPath = getClass().getResource("/com/dryt/quoridor/sounds/Highland Hymn Bonnie Grace.mp3").toExternalForm();
            System.out.println("🎵 Music path: " + musicPath);
            Media music = new Media(musicPath);
            backgroundMusic = new MediaPlayer(music);
            
            // Set music properties
            backgroundMusic.setVolume(savedVolume);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
            backgroundMusic.setAutoPlay(false); // Don't auto-play immediately
            
            System.out.println("🎵 Background music loaded successfully: " + musicPath);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load background music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupVolumeControls() {
        System.out.println("🎵 setupVolumeControls() called");
        System.out.println("🎵 volumeButton: " + volumeButton);
        System.out.println("🎵 volumeSlider: " + volumeSlider);
        
        if (volumeSlider != null) {
            // Initialize volume slider
            volumeSlider.setMin(0.0);
            volumeSlider.setMax(1.0);
            volumeSlider.setValue(savedVolume);
            
            // Add listener for volume changes
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (backgroundMusic != null) {
                    savedVolume = newValue.doubleValue();
                    if (!isMusicMuted) {
                        backgroundMusic.setVolume(savedVolume);
                    }
                    updateVolumeButtonIcon(); // Update icon when volume changes
                }
            });
            System.out.println("🎵 Volume slider initialized");
        } else {
            System.out.println("❌ Volume slider is null!");
        }
        
        if (volumeButton != null) {
            // Force initial icon update
            System.out.println("🎵 Setting up volume button with initial icon...");
            updateVolumeButtonIcon();
        } else {
            System.out.println("❌ Volume button is null!");
        }
    }
    
    @FXML
    private void onVolumeToggle() {
        if (backgroundMusic != null) {
            isMusicMuted = !isMusicMuted;
            
            if (isMusicMuted) {
                backgroundMusic.setVolume(0.0);
            } else {
                backgroundMusic.setVolume(savedVolume);
            }
            
            updateVolumeButtonIcon();
            System.out.println("🎵 Volume toggled - Muted: " + isMusicMuted);
        }
    }
    
    private void updateVolumeButtonIcon() {
        if (volumeButton != null) {
            // Clear existing style classes
            volumeButton.getStyleClass().removeAll("volume-button-sound", "volume-button-mute");
            
            if (isMusicMuted || savedVolume == 0.0) {
                volumeButton.getStyleClass().add("volume-button-mute");
                System.out.println("🎵 Applied mute icon");
            } else {
                volumeButton.getStyleClass().add("volume-button-sound");
                System.out.println("🎵 Applied sound icon");
            }
        }
    }
    
    private void startBackgroundMusic() {
        if (backgroundMusic != null && !isMusicMuted) {
            try {
                backgroundMusic.play();
                System.out.println("🎵 Background music started");
            } catch (Exception e) {
                System.err.println("❌ Failed to start background music: " + e.getMessage());
            }
        }
    }
    
    private void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.stop();
                System.out.println("🎵 Background music stopped");
            } catch (Exception e) {
                System.err.println("❌ Failed to stop background music: " + e.getMessage());
            }
        }
    }
    
    private void setBoardContainerSize() {
        if (boardContainer != null) {
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
        // Clear existing board
        boardPane.getChildren().clear();

        for (int y = 0; y < GameConstants.BOARD_SIZE; y++) {
            for (int x = 0; x < GameConstants.BOARD_SIZE; x++) {
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

    public void setupPlateauAndDisplay(Plateau plateau) {
        this.plateau = plateau;
        
        // Start background music when game starts
        startBackgroundMusic();
        
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
            alert.setHeaderText(GameConstants.MSG_GAME_OVER);
            alert.setContentText(String.format(GameConstants.MSG_PLAYER_WINS, winner.getId()));
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

        // Get selected skins
        int[] selectedSkins = JeuQuoridor.getSelectedSkins();

        // Add player styles with appropriate skins
        for (Joueur joueur : plateau.getJoueurs()) {
            int playerIndex = joueur.getId() - 1;
            if (playerIndex >= 0 && playerIndex < selectedSkins.length) {
                int skinId = selectedSkins[playerIndex];
                String styleClass = "player" + skinId;
                cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
                System.out.println("🎭 Added " + styleClass + " to cell [" + joueur.getX() + "," + joueur.getY() + "]");
            } else {
                // Fallback to default player style
                String styleClass = "player" + joueur.getId();
                cellButtons[joueur.getX()][joueur.getY()].getStyleClass().add(styleClass);
                System.out.println("🎭 Added fallback " + styleClass + " to cell [" + joueur.getX() + "," + joueur.getY() + "]");
            }
        }

        // Add highlight styles for possible moves
        System.out.println("Possible moves: ");
        for (int[] move : plateau.getPossibleMoves()) {
            System.out.println(Arrays.toString(move));
            cellButtons[move[0]][move[1]].getStyleClass().add("highlight");
            System.out.println("✨ Added highlight to cell [" + move[0] + "," + move[1] + "]");
        }

        // Update walls remaining display for all players (IMPROVED FROM INTERFACE VERSION)
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

    private void switchPlayerTurn() {
        plateau.switchPlayerTurn();
        Joueur currentPlayer = plateau.getCurrentPlayer();
        updateBoardState();

        if (currentPlayer.isAI()) {
            // Use appropriate AI delay
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> runIA());
            pause.play();
        }
    }
    
    // FXML methods that were missing (restored from interface version)
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
            
            // Make sure the scene can receive key events
            boardPane.getScene().getRoot().setFocusTraversable(true);
            boardPane.getScene().getRoot().requestFocus();
        }
    }
    
    private void toggleFullscreen() {
        Stage stage = JeuQuoridor.getPrimaryStage();
        stage.setFullScreen(!stage.isFullScreen());
        System.out.println("🖥️ Fullscreen toggled: " + stage.isFullScreen());
    }
}
