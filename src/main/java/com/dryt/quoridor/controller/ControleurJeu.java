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
    
    // Dynamic scaling
    private double scaleFactor = 1.0;
    private static final double BASE_WINDOW_WIDTH = 1400.0;
    private static final double BASE_WINDOW_HEIGHT = 900.0;

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
            setupDynamicScaling();
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

        // Use scaled dimensions
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
                
                // Appliquer le style de la cellule
                if ((x + y) % 2 == 0) {
                    cell.getStyleClass().add("cell");        // Light cells
                } else {
                    cell.getStyleClass().add("cell-dark");   // Dark cells
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
        
        System.out.println("🎯 Board creation completed with scale factor: " + scaleFactor);
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

        // Use scaled dimensions
        double scaledCellSize = GameConstants.CELL_SIZE * scaleFactor;
        double scaledWallSize = GameConstants.WALL_SIZE * scaleFactor;
        double scaledOffsetX = GameConstants.OFFSET_X * scaleFactor;
        double scaledOffsetY = GameConstants.OFFSET_Y * scaleFactor;

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

    private void hideGhostWall() {
        if (ghostWall != null) {
            boardPane.getChildren().remove(ghostWall);
            ghostWall = null;
        }
    }

    private void drawWall(int wx, int wy, boolean vertical) {
        // Use scaled dimensions
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
        System.out.println("Mur placé : " + (vertical ? "V" : "H") + " à " + wx + ", " + wy + " (scale: " + scaleFactor + ")");
    }

    private void onCellClicked(int x, int y) {
        if (!cellButtons[x][y].getStyleClass().contains("highlight")) return;
        if (!plateau.moveCurrentPlayer(x, y)) return;

        Joueur winner = plateau.getWinner();
        if (winner != null) {
            // Stop background music when game ends
            stopBackgroundMusic();
            
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
                Button cell = cellButtons[joueur.getX()][joueur.getY()];
                cell.getStyleClass().add(styleClass);
                
                // Ajuster la taille de l'icône en fonction de la taille de la cellule
                double cellSize = GameConstants.CELL_SIZE * scaleFactor;
                double iconSize = cellSize * 0.8; // 80% de la taille de la cellule
                cell.setStyle(cell.getStyle() + 
                    String.format("-fx-background-size: %fpx %fpx;", iconSize, iconSize) +
                    "-fx-background-position: center;" +
                    "-fx-background-repeat: no-repeat;");
                
                System.out.println("🎭 Added " + styleClass + " to cell [" + joueur.getX() + "," + joueur.getY() + "]");
            } else {
                // Fallback to default player style
                String styleClass = "player" + joueur.getId();
                Button cell = cellButtons[joueur.getX()][joueur.getY()];
                cell.getStyleClass().add(styleClass);
                
                // Ajuster la taille de l'icône en fonction de la taille de la cellule
                double cellSize = GameConstants.CELL_SIZE * scaleFactor;
                double iconSize = cellSize * 0.8; // 80% de la taille de la cellule
                cell.setStyle(cell.getStyle() + 
                    String.format("-fx-background-size: %fpx %fpx;", iconSize, iconSize) +
                    "-fx-background-position: center;" +
                    "-fx-background-repeat: no-repeat;");
                
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

        // Update walls remaining display for all players
        StringBuilder wallInfo = new StringBuilder();
        
        // Sort players by ID to ensure consistent order (J1, J2, J3, J4)
        java.util.List<Joueur> sortedPlayers = new java.util.ArrayList<>(plateau.getJoueurs());
        sortedPlayers.sort((j1, j2) -> Integer.compare(j1.getId(), j2.getId()));
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Joueur joueur = sortedPlayers.get(i);
            
            if (i > 0) {
                wallInfo.append("\n");
            }
            
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
            // Stop background music when game ends
            stopBackgroundMusic();
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText(GameConstants.MSG_GAME_OVER);
            alert.setContentText(String.format(GameConstants.MSG_PLAYER_WINS, winner.getId()));
            alert.showAndWait();
            JeuQuoridor.goMenu();
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
        
        // Stop current background music before starting new game
        stopBackgroundMusic();
        
        JeuQuoridor.goChoixJoueurs();
    }
    
    @FXML
    private void onOpenMenu() {
        System.out.println("📋 Opening menu overlay...");
        
        // Stop background music when returning to menu
        stopBackgroundMusic();
        
        // Return to main menu
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

    private void setupDynamicScaling() {
        // Add window resize listener
        if (boardPane.getScene() != null && boardPane.getScene().getWindow() != null) {
            Stage stage = (Stage) boardPane.getScene().getWindow();
            
            // Listen for width changes
            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateScaling();
            });
            
            // Listen for height changes  
            stage.heightProperty().addListener((obs, oldVal, newVal) -> {
                updateScaling();
            });
            
            // Initial scaling update
            updateScaling();
            System.out.println("🔄 Dynamic scaling initialized");
        }
    }
    
    private void updateScaling() {
        if (boardPane.getScene() != null && boardPane.getScene().getWindow() != null) {
            Stage stage = (Stage) boardPane.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            // Calculer le ratio d'aspect de la fenêtre
            double aspectRatio = currentWidth / currentHeight;
            
            // Calculer le facteur d'échelle en fonction de la taille de la fenêtre
            double availableWidth = currentWidth * 0.75; // 75% de la largeur de la fenêtre
            double availableHeight = currentHeight * 0.75; // 75% de la hauteur de la fenêtre
            
            // Calculer la taille de base du plateau
            double baseBoardWidth = 2 * GameConstants.OFFSET_X + 9 * GameConstants.CELL_SIZE + 8 * GameConstants.WALL_SIZE;
            double baseBoardHeight = 2 * GameConstants.OFFSET_Y + 9 * GameConstants.CELL_SIZE + 8 * GameConstants.WALL_SIZE;
            
            // Calculer les facteurs d'échelle pour la largeur et la hauteur
            double widthRatio = availableWidth / baseBoardWidth;
            double heightRatio = availableHeight / baseBoardHeight;
            
            // Utiliser le plus petit des deux ratios pour maintenir les proportions
            scaleFactor = Math.min(widthRatio, heightRatio);
            
            // Ajuster les limites en fonction de la résolution
            if (currentWidth <= 800 || currentHeight <= 600) {
                // Pour les petites résolutions (800x600 et moins)
                scaleFactor = Math.max(scaleFactor, 0.2);
                scaleFactor = Math.min(scaleFactor, 0.5);
            } else if (currentWidth >= 1920) {
                // Pour les grandes résolutions (1920x1080)
                scaleFactor = Math.max(scaleFactor, 0.5);
                scaleFactor = Math.min(scaleFactor, 0.8);
            } else if (currentWidth >= 1280) {
                // Pour les résolutions moyennes (1280x800)
                scaleFactor = Math.max(scaleFactor, 0.4);
                scaleFactor = Math.min(scaleFactor, 0.7);
            } else {
                // Pour les autres résolutions
                scaleFactor = Math.max(scaleFactor, 0.3);
                scaleFactor = Math.min(scaleFactor, 0.6);
            }
            
            System.out.println("🔄 Facteur d'échelle mis à jour: " + scaleFactor + 
                             " (Fenêtre: " + currentWidth + "x" + currentHeight + 
                             ", Ratio: " + aspectRatio + ")");
            
            // Appliquer la mise à l'échelle aux éléments du jeu
            applyScaling();
        }
    }
    
    private void applyScaling() {
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
            double containerPadding = Math.max(4 * scaleFactor, 3);
            double containerSize = Math.max(boardWidth, boardHeight) + (2 * containerPadding);
            
            boardContainer.setPrefSize(containerSize, containerSize);
            boardContainer.setMinSize(containerSize, containerSize);
            boardContainer.setMaxSize(containerSize, containerSize);
            
            // Recréer le plateau de jeu avec la nouvelle échelle
            if (cellButtons != null && cellButtons[0] != null && cellButtons[0][0] != null) {
                createGameBoard();
                if (plateau != null) {
                    updateBoardState();
                }
            }
            
            System.out.println("🔄 Mise à l'échelle " + scaleFactor + " appliquée au plateau (taille: " + containerSize + ")");
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
                labelMursRestants.setStyle("-fx-font-size: " + fontSize + "px;");
            }
            
            // Mise à l'échelle générale de la police
            if (boardPane.getScene().getRoot() != null) {
                double generalFontSize = Math.max(baseFontSize * scaleFactor, baseFontSize * 0.7);
                boardPane.getScene().getRoot().setStyle("-fx-font-size: " + generalFontSize + "px;");
            }
            
            System.out.println("🔄 Contrôles mis à l'échelle - Bouton: " + baseButtonSize + 
                             "px, Slider: " + baseSliderWidth + "px, Police: " + baseFontSize + "px");
        }
    }
}
