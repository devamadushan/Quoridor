package com.dryt.quoridor.model;

import java.util.*;

public class Plateau {
    private final int size = 9;
    private List<Joueur> joueurs;
    private Joueur currentPlayer;
    private boolean[][] verticalWallPositions;
    private boolean[][] horizontalWallPositions;
    private boolean[][] blockedRight;
    private boolean[][] blockedDown;

    public Plateau(int nombreJoueurs) {
        joueurs = new ArrayList<>();
        switch (nombreJoueurs) {
            case 2 -> {
                // Bleu (haut), Rouge (bas) → ici on commence par Rouge
                joueurs.add(new Joueur(2, 4, 8, 10)); // Rouge commence
                joueurs.add(new Joueur(1, 4, 0, 10));
            }
            case 4 -> {
                // Ordre souhaité : Orange (bas), Vert (droite), Rouge (gauche), Bleu (haut)

                joueurs.add(new Joueur(1, 4, 0, 5)); // Bleu
                joueurs.add(new Joueur(2, 4, 8, 5)); // Orange
                joueurs.add(new Joueur(3, 0, 4, 5)); // Rouge
                joueurs.add(new Joueur(4, 8, 4, 5)); // Vert


            }
            default -> throw new IllegalArgumentException("Nombre de joueurs non supporté: " + nombreJoueurs);
        }
        currentPlayer = joueurs.get(0); // le premier joueur dans la liste commence

        verticalWallPositions = new boolean[8][8];
        horizontalWallPositions = new boolean[8][8];
        blockedRight = new boolean[9][9];
        blockedDown = new boolean[9][9];
    }


    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public Joueur getCurrentPlayer() {
        return currentPlayer;
    }
    public boolean canPlaceWall(int wx, int wy, boolean vertical) {
        // Vérifie les limites du plateau
        if (vertical && wy >= 8) return false;
        if (!vertical && wx >= 8) return false;

        // Vérifie si un mur du même type est déjà présent
        if (vertical && hasVerticalWall(wx, wy)) return false;
        if (!vertical && hasHorizontalWall(wx, wy)) return false;

        // Vérifie le croisement avec un mur opposé
        if (vertical && hasHorizontalWall(wx, wy)) return false;
        if (!vertical && hasVerticalWall(wx, wy)) return false;

        // Optionnel : bloquer les placements si une des deux cases est déjà bloquée (superposition)
        if (vertical && (blockedRight[wx][wy] || blockedRight[wx][wy + 1])) return false;
        if (!vertical && (blockedDown[wx][wy] || blockedDown[wx + 1][wy])) return false;

        return true;
    }

    public void switchPlayerTurn() {
        int index = joueurs.indexOf(currentPlayer);
        currentPlayer = joueurs.get((index + 1) % joueurs.size());
    }

    public boolean peutPlacerMurSansChevauchement(int wx, int wy, boolean vertical) {
        if (wx < 0 || wy < 0 || wx >= 8 || wy >= 8) return false;

        if (vertical) {
            if (wy >= 7) return false; // dépasse en bas
            return !verticalWallPositions[wx][wy] && !verticalWallPositions[wx][wy + 1];
        } else {
            if (wx >= 7) return false; // dépasse à droite
            return !horizontalWallPositions[wx][wy] && !horizontalWallPositions[wx + 1][wy];
        }
    }

    public boolean isVerticalWallAt(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8 && verticalWallPositions[x][y];
    }

    public boolean isHorizontalWallAt(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8 && horizontalWallPositions[x][y];
    }


    public boolean peutPlacerMurSansCroix(int x, int y, boolean vertical) {
        if (vertical) {
            return !(murExiste(x - 1, y, false) || murExiste(x, y, false));
        } else {
            return !(murExiste(x, y - 1, true) || murExiste(x, y, true));
        }
    }

    private boolean murExiste(int x, int y, boolean vertical) {
        if (x < 0 || y < 0 || x >= 8 || y >= 8) return false;
        return vertical ? verticalWallPositions[x][y] : horizontalWallPositions[x][y];
    }

    public List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();
        Joueur jp = currentPlayer;
        int x = jp.getX();
        int y = jp.getY();

        int[][] directions = switch (jp.getId()) {
            case 1 -> new int[][]{{0, 1}, {-1, 0}, {1, 0}};
            case 2 -> new int[][]{{0, -1}, {-1, 0}, {1, 0}};
            case 3 -> new int[][]{{1, 0}, {0, -1}, {0, 1}};
            case 4 -> new int[][]{{-1, 0}, {0, -1}, {0, 1}};
            default -> new int[0][0];
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx < 0 || nx >= size || ny < 0 || ny >= size) continue;

            boolean blocked = false;
            if (dir[0] == 1 && blockedRight[x][y]) blocked = true;
            if (dir[0] == -1 && blockedRight[x - 1][y]) blocked = true;
            if (dir[1] == 1 && blockedDown[x][y]) blocked = true;
            if (dir[1] == -1 && blockedDown[x][y - 1]) blocked = true;

            if (!blocked) {
                if (isPlayerAt(nx, ny)) {
                    int nnx = nx + dir[0];
                    int nny = ny + dir[1];
                    if (nnx >= 0 && nnx < size && nny >= 0 && nny < size) {
                        boolean blockedJump = false;
                        if (dir[0] == 1 && blockedRight[nx][ny]) blockedJump = true;
                        if (dir[0] == -1 && blockedRight[nx - 1][ny]) blockedJump = true;
                        if (dir[1] == 1 && blockedDown[nx][ny]) blockedJump = true;
                        if (dir[1] == -1 && blockedDown[nx][ny - 1]) blockedJump = true;

                        if (!blockedJump && !isPlayerAt(nnx, nny)) {
                            moves.add(new int[]{nnx, nny});
                        } else {
                            if (dir[0] == 0) {
                                if (nx > 0 && !blockedRight[nx - 1][ny] && !isPlayerAt(nx - 1, ny))
                                    moves.add(new int[]{nx - 1, ny});
                                if (nx < size - 1 && !blockedRight[nx][ny] && !isPlayerAt(nx + 1, ny))
                                    moves.add(new int[]{nx + 1, ny});
                            } else if (dir[1] == 0) {
                                if (ny > 0 && !blockedDown[nx][ny - 1] && !isPlayerAt(nx, ny - 1))
                                    moves.add(new int[]{nx, ny - 1});
                                if (ny < size - 1 && !blockedDown[nx][ny] && !isPlayerAt(nx, ny + 1))
                                    moves.add(new int[]{nx, ny + 1});
                            }
                        }
                    }
                } else {
                    moves.add(new int[]{nx, ny});
                }
            }
        }
        return moves;
    }

    private boolean isPlayerAt(int x, int y) {
        for (Joueur j : joueurs) {
            if (j.getX() == x && j.getY() == y) return true;
        }
        return false;
    }



    public boolean moveCurrentPlayer(int x, int y) {
        for (int[] m : getPossibleMoves()) {
            if (m[0] == x && m[1] == y) {
                currentPlayer.setPosition(x, y);
                return true;
            }
        }
        return false;
    }

    public boolean placeWallCurrentPlayer(int wx, int wy, boolean vertical) {
        if (currentPlayer.getWallsRemaining() <= 0) return false; // ✅ bloque si plus de murs

        if (vertical) {
            if (wy >= 8) return false;
            verticalWallPositions[wx][wy] = true;
            blockedRight[wx][wy] = true;
            blockedRight[wx][wy + 1] = true;
        } else {
            if (wx >= 8) return false;
            horizontalWallPositions[wx][wy] = true;
            blockedDown[wx][wy] = true;
            blockedDown[wx + 1][wy] = true;
        }

        currentPlayer.decrementWalls(); // ✅ décrémente le compteur
        return true;
    }






    public boolean hasVerticalWall(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8 && verticalWallPositions[x][y];
    }

    public boolean hasHorizontalWall(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8 && horizontalWallPositions[x][y];
    }


    public Joueur getWinner() {
        for (Joueur j : joueurs) {
            switch (j.getId()) {
                case 1 -> { if (j.getY() == 8) return j; }
                case 2 -> { if (j.getY() == 0) return j; }
                case 3 -> { if (j.getX() == 8) return j; }
                case 4 -> { if (j.getX() == 0) return j; }
            }
        }
        return null;
    }

    private boolean hasPathToGoal(Joueur j) {
        boolean[][] visited = new boolean[size][size];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{j.getX(), j.getY()});
        visited[j.getX()][j.getY()] = true;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0];
            int y = pos[1];

            switch (j.getId()) {
                case 1 -> { if (y == 8) return true; }
                case 2 -> { if (y == 0) return true; }
                case 3 -> { if (x == 8) return true; }
                case 4 -> { if (x == 0) return true; }
            }

            for (int[] m : getNeighbors(x, y)) {
                if (!visited[m[0]][m[1]]) {
                    visited[m[0]][m[1]] = true;
                    queue.add(m);
                }
            }
        }
        return false;
    }

    private List<int[]> getNeighbors(int x, int y) {
        List<int[]> neighbors = new ArrayList<>();
        if (y > 0 && !blockedDown[x][y - 1]) neighbors.add(new int[]{x, y - 1});
        if (y < size - 1 && !blockedDown[x][y]) neighbors.add(new int[]{x, y + 1});
        if (x > 0 && !blockedRight[x - 1][y]) neighbors.add(new int[]{x - 1, y});
        if (x < size - 1 && !blockedRight[x][y]) neighbors.add(new int[]{x + 1, y});
        return neighbors;
    }
}