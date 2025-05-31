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

    public Plateau(){}

    public Plateau(int nombreJoueurs, int nbAI) {
        joueurs = new ArrayList<>();
        switch (nombreJoueurs) {
            case 2 -> {
                // Bleu (haut), Rouge (bas) → ici on commence par Rouge
                joueurs.add(new Joueur(2, 4, 8, 10,false)); // Rouge commence
                joueurs.add(new Joueur(1, 4, 0, 10,true));
            }
            case 4 -> {
                joueurs.add(new Joueur(4, 8, 4, 5,true)); // Vert
                joueurs.add(new Joueur(2, 4, 8, 5,false)); // Orange
                joueurs.add(new Joueur(3, 0, 4, 5,true)); // Rouge.
                joueurs.add(new Joueur(1, 4, 0, 5,false)); // Bleu
            }
            default -> throw new IllegalArgumentException("Nombre de joueurs non supporté: " + nombreJoueurs);
        }
        currentPlayer = getJoueurById(2);; // le premier joueur dans la liste commence

        verticalWallPositions = new boolean[8][8];
        horizontalWallPositions = new boolean[8][8];
        blockedRight = new boolean[9][9];
        blockedDown = new boolean[9][9];
    }

    public Joueur getJoueurById(int id) {
        for (Joueur j : joueurs) {
            if (j.getId() == id) return j;
        }
        return null;
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
//        System.out.println("Searching for currentPlayer ID=" + currentPlayer.getId() + ", index=" + index);
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

        int[][] directions = new int[][]{
                {0, 1},    // bas
                {0, -1},   // haut
                {-1, 0},   // gauche
                {1, 0}     // droite
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
        System.out.println("Move Fails");
        return false;
    }

    public boolean placeWallCurrentPlayer(int wx, int wy, boolean vertical) {
        if (currentPlayer.getWallsRemaining() <= 0) return false; // ✅ bloque si plus de murs

        if (vertical) {
            if (wy >= 8){ System.out.println("Wall Fails"); return false;}
            verticalWallPositions[wx][wy] = true;
            blockedRight[wx][wy] = true;
            blockedRight[wx][wy + 1] = true;
        } else {
            if (wx >= 8){ System.out.println("Wall Fails"); return false;}
            horizontalWallPositions[wx][wy] = true;
            blockedDown[wx][wy] = true;
            blockedDown[wx + 1][wy] = true;
        }

        currentPlayer.decrementWalls(); // ✅ décrémente le compteur
        return true;
    }


    public boolean allPlayersHaveAPathAfterWall(int wx, int wy, boolean vertical) {
        // Simuler l'ajout du mur
        if (vertical) {
            verticalWallPositions[wx][wy] = true;
            blockedRight[wx][wy] = true;
            blockedRight[wx][wy + 1] = true;
        } else {
            horizontalWallPositions[wx][wy] = true;
            blockedDown[wx][wy] = true;
            blockedDown[wx + 1][wy] = true;
        }

        boolean allHavePath = true;
        for (Joueur j : joueurs) {
            if (!hasPathToGoal(j)) {
                allHavePath = false;
                break;
            }
        }

        // Annuler le mur simulé
        if (vertical) {
            verticalWallPositions[wx][wy] = false;
            blockedRight[wx][wy] = false;
            blockedRight[wx][wy + 1] = false;
        } else {
            horizontalWallPositions[wx][wy] = false;
            blockedDown[wx][wy] = false;
            blockedDown[wx + 1][wy] = false;
        }

        return allHavePath;
    }
    public boolean isWallOverlapping(int wx, int wy, boolean vertical) {
        if (vertical) {
            if (wx >= 0 && wx < 8 && wy >= 0 && wy < 7) {
                return verticalWallPositions[wx][wy] || verticalWallPositions[wx][wy - 1];
            }
        } else {
            if (wx >= 0 && wx < 7 && wy >= 0 && wy < 8) {
                return horizontalWallPositions[wx][wy] || horizontalWallPositions[wx + 1][wy];
            }
        }

        return false;
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

    public void removeVerticalWall(int x, int y) {
        verticalWallPositions[x][y] = false;
        blockedRight[x][y] = false;
        blockedRight[x][y + 1] = false;
    }

    public void removeHorizontalWall(int x, int y) {
        horizontalWallPositions[x][y] = false;
        blockedDown[x][y] = false;
        blockedDown[x + 1][y] = false;
    }

    @Override
    public Plateau clone() {
        Plateau copy = new Plateau(); // bypass constructor logic

        // Copy wall positions
        copy.verticalWallPositions = deepCopy(this.verticalWallPositions);
        copy.horizontalWallPositions = deepCopy(this.horizontalWallPositions);
        copy.blockedRight = deepCopy(this.blockedRight);
        copy.blockedDown = deepCopy(this.blockedDown);

        // Copy players
        copy.joueurs = new ArrayList<>();
        for (Joueur j : this.joueurs) {
            copy.joueurs.add(j.clone());
        }


        // Copy current player
        copy.currentPlayer = copy.getJoueurById(this.currentPlayer.getId());

//        System.out.println("Original Joueur IDs: ");
//        for (Joueur j : this.joueurs) System.out.print(j.getId() + " ");
//        System.out.println("\nCloned Joueur IDs: ");
//        for (Joueur j : copy.joueurs) System.out.print(j.getId() + " ");
//
//        System.out.println("Set currentPlayer to Original: ID=" + this.currentPlayer.getId());
//        System.out.println("Set currentPlayer to clone: ID=" + copy.getJoueurById(this.currentPlayer.getId()).getId());


        return copy;
    }

    private boolean[][] deepCopy(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

}