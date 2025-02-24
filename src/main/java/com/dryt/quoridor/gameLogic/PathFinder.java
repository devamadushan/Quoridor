package com.dryt.quoridor.gameLogic;

import com.dryt.quoridor.model.Entity;
import com.dryt.quoridor.model.GoalDimension;
import com.dryt.quoridor.model.Player;
import com.dryt.quoridor.model.Wall;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {
    private final Entity[][] board;
    private final int rows, cols;

    public PathFinder(Entity[][] board) {
        this.board = board;
        this.rows = board.length;
        this.cols = board[0].length;
    }

    public boolean canReachDestination(int startX, int startY, int dest, GoalDimension dim) {
        boolean[][] visited = new boolean[rows][cols];
        return dfs(startX, startY, dest, dim, visited);
    }

    public boolean canPlaceWallWithoutBlocking(int wallX, int wallY, Player[] players) {

        if (wallX % 2 == 0 || wallY % 2 == 0 || board[wallX][wallY] != null) {
            return false; // Invalid wall placement
        }

        // Temporarily place the wall
        Entity tempWall = board[wallX][wallY];
        board[wallX][wallY] = new Wall(wallX, wallY, false);

        // Check if all players still have a path to their goal
        for (Player player : players) {
            if (!canReachDestination(player.getX(), player.getY(), player.getGoalRow(), player.getGoalDim())) {
                board[wallX][wallY] = tempWall; // Revert wall
                return false; // This wall would block a player
            }
        }

        board[wallX][wallY] = tempWall;
        return true;
    }

    public List<Boolean> getValidMoves(int x, int y) {
        List<Boolean> validMoves = new ArrayList<>(List.of(false, false, false, false));

        int[][] directions = {
                {-2, 0},  // Up
                {2, 0},   // Down
                {0, -2},  // Left
                {0, 2}    // Right
        };

        for (int i = 0; i < directions.length; i++) {
            int newX = x + directions[i][0];
            int newY = y + directions[i][1];
            int wallX = x + directions[i][0] / 2;
            int wallY = y + directions[i][1] / 2;

            if (isValidMove(newX, newY, wallX, wallY)) {
                validMoves.set(i, true);
            }
        }

        return validMoves;
    }


    private boolean dfs(int x, int y, int dest, GoalDimension dim, boolean[][] visited) {
        if (x < 0 || y < 0 || x >= rows || y >= cols || board[x][y] != null || visited[x][y]) {
            return false;
        }

        if (x == dest && dim == GoalDimension.X || y == dest && dim == GoalDimension.Y) return true;

        visited[x][y] = true;

        int[][] directions = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            int wallX = x + dir[0] / 2;
            int wallY = y + dir[1] / 2;

            // Check for normal movements
            if (isValidMove(newX, newY, wallX, wallY, visited)) {
                if (dfs(newX, newY, dest, dim, visited)) return true;
            }

            // Check for jumps over players
            if (isPlayer(x + dir[0], y + dir[1])) {
                int jumpX = x + dir[0] * 2;
                int jumpY = y + dir[1] * 2;
                int jumpWallX = x + dir[0] + dir[0] / 2;
                int jumpWallY = y + dir[1] + dir[1] / 2;

                if (isValidMove(jumpX, jumpY, jumpWallX, jumpWallY, visited)) {
                    if (dfs(jumpX, jumpY, dest, dim, visited)) return true;
                }


                int[][] sideJumps = (dir[0] == 0) ? new int[][]{{2, 0}, {-2, 0}} : new int[][]{{0, 2}, {0, -2}};
                for (int[] side : sideJumps) {
                    int sideX = x + dir[0] + side[0];
                    int sideY = y + dir[1] + side[1];
                    int sideWallX = x + dir[0] + side[0] / 2;
                    int sideWallY = y + dir[1] + side[1] / 2;

                    if (isValidMove(sideX, sideY, sideWallX, sideWallY, visited)) {
                        if (dfs(sideX, sideY, dest, dim, visited)) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isValidMove(int x, int y, int wallX, int wallY) {
        return x >= 0 && y >= 0 && x < rows && y < cols &&
                board[x][y] == null && board[wallX][wallY] == null;
    }

    private boolean isValidMove(int x, int y, int wallX, int wallY, boolean[][] visited) {
        return x >= 0 && y >= 0 && x < rows && y < cols &&
                board[x][y] == null && board[wallX][wallY] == null && !visited[x][y];
    }

    private boolean isPlayer(int x, int y) {
        return x >= 0 && y >= 0 && x < rows && y < cols && board[x][y] instanceof Player;
    }


}

