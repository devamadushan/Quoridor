package com.dryt.quoridor.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Plateau {
    private final int size = 9;  // 9x9 board
    private Joueur joueur1;
    private Joueur joueur2;
    private Joueur currentPlayer;
    // Wall positions
    private boolean[][] verticalWallPositions;   // true if a vertical wall is placed at (x,y)
    private boolean[][] horizontalWallPositions; // true if a horizontal wall is placed at (x,y)
    // Blocked moves due to walls
    private boolean[][] blockedRight; // blockedRight[x][y] = true if a wall blocks move between (x,y) and (x+1,y)
    private boolean[][] blockedDown;  // blockedDown[x][y] = true if a wall blocks move between (x,y) and (x,y+1)

    public Plateau() {
        // Initialize two players
        joueur1 = new Joueur(1, 4, 0, 10);  // Player 1 starts at top middle (x=4, y=0)
        joueur2 = new Joueur(2, 4, 8, 10);  // Player 2 starts at bottom middle (x=4, y=8)
        currentPlayer = joueur1;
        // Allocate wall and blocked-move arrays
        verticalWallPositions = new boolean[8][8];
        horizontalWallPositions = new boolean[8][8];
        blockedRight = new boolean[9][9];
        blockedDown = new boolean[9][9];
    }

    public Joueur getCurrentPlayer() {
        return currentPlayer;
    }
    public Joueur getJoueur1() {
        return joueur1;
    }
    public Joueur getJoueur2() {
        return joueur2;
    }

    public void switchPlayerTurn() {
        currentPlayer = (currentPlayer == joueur1 ? joueur2 : joueur1);
    }

    /**
     * Returns a list of possible moves for the current player's pawn, respecting Quoridor movement rules.
     */
    public ArrayList<int[]> getPossibleMoves() {
        ArrayList<int[]> moves = new ArrayList<>();
        Joueur jp = currentPlayer;
        Joueur jo = (jp == joueur1 ? joueur2 : joueur1);  // the opponent
        int x = jp.getX();
        int y = jp.getY();
        // Up move
        if (y > 0 && !blockedDown[x][y-1]) {
            if (!(jo.getX() == x && jo.getY() == y-1)) {
                moves.add(new int[]{x, y-1});
            } else {  // opponent is directly above
                if (y > 1 && !blockedDown[x][y-2]) {
                    moves.add(new int[]{x, y-2});  // jump over opponent
                } else {
                    // Diagonal moves around the opponent
                    if (x > 0 && !blockedRight[x-1][y-1]) {
                        moves.add(new int[]{x-1, y-1});
                    }
                    if (x < size-1 && !blockedRight[x][y-1]) {
                        moves.add(new int[]{x+1, y-1});
                    }
                }
            }
        }
        // Down move
        if (y < size-1 && !blockedDown[x][y]) {
            if (!(jo.getX() == x && jo.getY() == y+1)) {
                moves.add(new int[]{x, y+1});
            } else {
                if (y < size-2 && !blockedDown[x][y+1]) {
                    moves.add(new int[]{x, y+2});
                } else {
                    if (x > 0 && !blockedRight[x-1][y+1]) {
                        moves.add(new int[]{x-1, y+1});
                    }
                    if (x < size-1 && !blockedRight[x][y+1]) {
                        moves.add(new int[]{x+1, y+1});
                    }
                }
            }
        }
        // Left move
        if (x > 0 && !blockedRight[x-1][y]) {
            if (!(jo.getX() == x-1 && jo.getY() == y)) {
                moves.add(new int[]{x-1, y});
            } else {
                if (x > 1 && !blockedRight[x-2][y]) {
                    moves.add(new int[]{x-2, y});
                } else {
                    if (y > 0 && !blockedDown[x-1][y-1]) {
                        moves.add(new int[]{x-1, y-1});
                    }
                    if (y < size-1 && !blockedDown[x-1][y]) {
                        moves.add(new int[]{x-1, y+1});
                    }
                }
            }
        }
        // Right move
        if (x < size-1 && !blockedRight[x][y]) {
            if (!(jo.getX() == x+1 && jo.getY() == y)) {
                moves.add(new int[]{x+1, y});
            } else {
                if (x < size-2 && !blockedRight[x+1][y]) {
                    moves.add(new int[]{x+2, y});
                } else {
                    if (y > 0 && !blockedDown[x+1][y-1]) {
                        moves.add(new int[]{x+1, y-1});
                    }
                    if (y < size-1 && !blockedDown[x+1][y]) {
                        moves.add(new int[]{x+1, y+1});
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Moves the current player's pawn to the target position if it is a valid move.
     * Returns true if the move is successful.
     */
    public boolean moveCurrentPlayer(int targetX, int targetY) {
        ArrayList<int[]> possible = getPossibleMoves();
        boolean valid = false;
        for (int[] pos : possible) {
            if (pos[0] == targetX && pos[1] == targetY) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            return false;
        }
        currentPlayer.setPosition(targetX, targetY);
        return true;
    }

    /**
     * Attempts to place a wall for the current player at (wx, wy) with the given orientation.
     * @param vertical true for vertical wall, false for horizontal wall.
     * @return true if the wall is placed successfully.
     */
    public boolean placeWallCurrentPlayer(int wx, int wy, boolean vertical) {
        // Bounds and availability checks
        if (wx < 0 || wx > 7 || wy < 0 || wy > 7) return false;
        if (currentPlayer.getWallsRemaining() <= 0) return false;
        if (vertical) {
            if (verticalWallPositions[wx][wy]) {
                return false;  // wall already present here
            }
            // Check crossing with existing horizontal walls
            if ((wx <= 7 && wy <= 7 && horizontalWallPositions[wx][wy]) ||
                    (wx <= 7 && wy > 0 && horizontalWallPositions[wx][wy-1]) ||
                    (wx <= 7 && wy <= 6 && horizontalWallPositions[wx][wy+1]) ||
                    (wx > 0 && wy <= 7 && horizontalWallPositions[wx-1][wy]) ||
                    (wx < 7 && wy <= 7 && horizontalWallPositions[wx+1][wy])) {
                return false;
            }
            // Place wall temporarily
            boolean edge1Before = blockedRight[wx][wy];
            boolean edge2Before = blockedRight[wx][wy+1];
            verticalWallPositions[wx][wy] = true;
            blockedRight[wx][wy] = true;
            blockedRight[wx][wy+1] = true;
            // Verify that each player still has a path to goal
            if (!isPathToGoal(joueur1.getX(), joueur1.getY(), 8) || !isPathToGoal(joueur2.getX(), joueur2.getY(), 0)) {
                // Invalid placement, revert
                verticalWallPositions[wx][wy] = false;
                if (!edge1Before) blockedRight[wx][wy] = false;
                if (!edge2Before) blockedRight[wx][wy+1] = false;
                return false;
            }
            // Commit placement
            currentPlayer.decrementWalls();
            return true;
        } else {
            if (horizontalWallPositions[wx][wy]) {
                return false;
            }
            // Check crossing with existing vertical walls
            if ((wx <= 7 && wy <= 7 && verticalWallPositions[wx][wy]) ||
                    (wx > 0 && wy <= 7 && verticalWallPositions[wx-1][wy]) ||
                    (wx <= 7 && wy > 0 && verticalWallPositions[wx][wy-1]) ||
                    (wx <= 7 && wy < 7 && verticalWallPositions[wx][wy+1]) ||
                    (wx > 0 && wy < 7 && verticalWallPositions[wx-1][wy+1])) {
                return false;
            }
            boolean edge1Before = blockedDown[wx][wy];
            boolean edge2Before = blockedDown[wx+1][wy];
            horizontalWallPositions[wx][wy] = true;
            blockedDown[wx][wy] = true;
            blockedDown[wx+1][wy] = true;
            if (!isPathToGoal(joueur1.getX(), joueur1.getY(), 8) || !isPathToGoal(joueur2.getX(), joueur2.getY(), 0)) {
                horizontalWallPositions[wx][wy] = false;
                if (!edge1Before) blockedDown[wx][wy] = false;
                if (!edge2Before) blockedDown[wx+1][wy] = false;
                return false;
            }
            currentPlayer.decrementWalls();
            return true;
        }
    }

    // Breadth-first search to check if there's a path from (sx, sy) to any cell with y == goalY
    private boolean isPathToGoal(int sx, int sy, int goalY) {
        boolean[][] visited = new boolean[size][size];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{sx, sy});
        visited[sx][sy] = true;
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0];
            int y = pos[1];
            if (y == goalY) {
                return true;
            }
            // Explore neighbors (up, down, left, right) if not blocked by walls
            if (y > 0 && !blockedDown[x][y-1] && !visited[x][y-1]) {
                visited[x][y-1] = true;
                queue.add(new int[]{x, y-1});
            }
            if (y < size-1 && !blockedDown[x][y] && !visited[x][y+1]) {
                visited[x][y+1] = true;
                queue.add(new int[]{x, y+1});
            }
            if (x > 0 && !blockedRight[x-1][y] && !visited[x-1][y]) {
                visited[x-1][y] = true;
                queue.add(new int[]{x-1, y});
            }
            if (x < size-1 && !blockedRight[x][y] && !visited[x+1][y]) {
                visited[x+1][y] = true;
                queue.add(new int[]{x+1, y});
            }
        }
        return false;
    }

    /**
     * Checks if a player has reached the opposite side.
     * @return the winning Joueur if there's a winner, otherwise null.
     */
    public Joueur getWinner() {
        if (joueur1.getY() == 8) {
            return joueur1;
        }
        if (joueur2.getY() == 0) {
            return joueur2;
        }
        return null;
    }
}
