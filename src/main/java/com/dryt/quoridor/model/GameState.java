package com.dryt.quoridor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a snapshot of the game state that can be saved and restored for undo functionality
 */
public class GameState {
    private final int currentPlayerId;
    private final List<PlayerState> playerStates;
    private final List<Mur> placedWalls;
    private final boolean[][] verticalWalls;
    private final boolean[][] horizontalWalls;
    private final boolean[][] blockedRight;
    private final boolean[][] blockedDown;
    
    /**
     * Creates a snapshot of the current game state
     */
    public GameState(Plateau plateau) {
        this.currentPlayerId = plateau.getCurrentPlayer().getId();
        
        // Save player states
        this.playerStates = new ArrayList<>();
        for (Joueur joueur : plateau.getJoueurs()) {
            playerStates.add(new PlayerState(joueur));
        }
        
        // Save walls - create deep copy
        this.placedWalls = new ArrayList<>(plateau.getMurs());
        
        // Save wall positions (deep copy)
        this.verticalWalls = copyBooleanArray(plateau.getVerticalWallPositions());
        this.horizontalWalls = copyBooleanArray(plateau.getHorizontalWallPositions());
        this.blockedRight = copyBooleanArray(plateau.getBlockedRight());
        this.blockedDown = copyBooleanArray(plateau.getBlockedDown());
    }
    
    /**
     * Restores the game state to the plateau
     */
    public void restoreToBoard(Plateau plateau) {
        // Restore player states
        for (PlayerState playerState : playerStates) {
            Joueur joueur = plateau.getJoueurById(playerState.getId());
            if (joueur != null) {
                joueur.setPosition(playerState.getX(), playerState.getY());
                joueur.setWallsRemaining(playerState.getWallsRemaining());
            }
        }
        
        // Restore current player
        plateau.setCurrentPlayer(plateau.getJoueurById(currentPlayerId));
        
        // Restore walls
        plateau.setMurs(new ArrayList<>(placedWalls));
        plateau.setVerticalWallPositions(copyBooleanArray(verticalWalls));
        plateau.setHorizontalWallPositions(copyBooleanArray(horizontalWalls));
        plateau.setBlockedRight(copyBooleanArray(blockedRight));
        plateau.setBlockedDown(copyBooleanArray(blockedDown));
    }
    
    /**
     * Creates a deep copy of a 2D boolean array
     */
    private boolean[][] copyBooleanArray(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
    
    /**
     * Inner class to represent a player's state
     */
    private static class PlayerState {
        private final int id;
        private final int x;
        private final int y;
        private final int wallsRemaining;
        
        public PlayerState(Joueur joueur) {
            this.id = joueur.getId();
            this.x = joueur.getX();
            this.y = joueur.getY();
            this.wallsRemaining = joueur.getWallsRemaining();
        }
        
        public int getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWallsRemaining() { return wallsRemaining; }
    }
} 