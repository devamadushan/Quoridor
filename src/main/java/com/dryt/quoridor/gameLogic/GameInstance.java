package com.dryt.quoridor.gameLogic;

import com.dryt.quoridor.model.Entity;
import com.dryt.quoridor.model.GoalDimension;
import com.dryt.quoridor.model.Player;
import com.dryt.quoridor.model.Wall;

import java.util.Scanner;

public class GameInstance {
    private final Entity[][] board;
    private final Player[] players;
    private final int maxWalls;
    private final int size;
    private final int playerCount;
    private int turn = 0;
    private final PathFinder pathFinder;
    private final Scanner scanner;

    public GameInstance(int size, int nbPlayers, int AIPlayers, int walls) {
        this.board = new Entity[size * 2][size * 2];
        this.maxWalls = walls;
        this.players = new Player[nbPlayers];
        this.size = size;
        this.playerCount = nbPlayers;
        this.pathFinder = new PathFinder(board);
        this.scanner = new Scanner(System.in);

        int side = 0;
        if (AIPlayers > nbPlayers) {
            throw new IllegalArgumentException("Le nombre d'IA ne peut pas dépasser le nombre total de joueurs !");
        }

        System.out.println("Création du jeu : Taille=" + size + ", Joueurs=" + nbPlayers + ", IA=" + AIPlayers + ", Murs=" + walls);

        for (int i = 0; i < AIPlayers; i++) {
            Integer[] pos = calculateStarting(side, size);
            int goalR = (side % 2 == 0) ? size * 2 : 0;
            GoalDimension goalD = (pos[2] == 1) ? GoalDimension.X : GoalDimension.Y;
            side++;

            players[nbPlayers - AIPlayers + i] = new Player("AI" + i, pos[0], pos[1], goalR, goalD, true); // x, y
            System.out.println("IA placée : " + players[nbPlayers - AIPlayers + i].getNom());
        }

        for (int i = 0; i < nbPlayers - AIPlayers; i++) {
            Integer[] pos = calculateStarting(side, size);
            int goalR = (side % 2 == 0) ? size * 2 : 0;
            GoalDimension goalD = (pos[2] == 1) ? GoalDimension.X : GoalDimension.Y;
            side++;

            players[i] = new Player("Player" + i, pos[0], pos[1], goalR, goalD, false); // x, y
            System.out.println("Joueur placé : " + players[i].getNom());
        }
    }

    public boolean playTurn() {
        Player currentPlayer = getCurrentPlayer();
        System.out.println("Tour du joueur : " + currentPlayer.getNom());

        if (currentPlayer.hasWon()) {
            System.out.println("🏆 " + currentPlayer.getNom() + " a gagné !");
            scanner.close();
            return false;
        }

        if (currentPlayer.isAI) {
            playAITurn(currentPlayer);
        } else {
            playHumanTurn(currentPlayer);
        }

        turn = (turn + 1) % playerCount;
        return true;
    }

    private void playHumanTurn(Player player) {
        System.out.println(player.getNom() + ", c'est votre tour !");
        System.out.println("Voulez-vous (1) vous déplacer ou (2) poser un mur ?");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Nettoyer le buffer

        if (choice == 1) {
            System.out.println("Déplacez-vous avec : Z (haut), S (bas), Q (gauche), D (droite)");
            String input = scanner.nextLine().toUpperCase();

            int dx = 0, dy = 0;
            switch (input) {
                case "Z" -> dy = -1;
                case "S" -> dy = 1;
                case "Q" -> dx = -1;
                case "D" -> dx = 1;
                default -> {
                    System.out.println("Mouvement invalide !");
                    return;
                }
            }

            int newX = player.getX() + dx;
            int newY = player.getY() + dy;

            if (newX < 0 || newX >= size * 2 || newY < 0 || newY >= size * 2) {
                System.out.println("Déplacement impossible : hors limites !");
                return;
            }

            if (!player.move(newX, newY)) {
                System.out.println("Déplacement bloqué !");
            } else {
                System.out.println("Déplacement réussi !");
            }
        } else if (choice == 2) {
            System.out.println("Entrez la position du mur (x y) et orientation (H pour horizontal, V pour vertical) : ");
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            char orientation = Character.toUpperCase(scanner.next().charAt(0));

            boolean isHorizontal = (orientation == 'H');

            if (player.hasWall()) {
                Wall newWall = new Wall(x, y, isHorizontal);
                if (pathFinder.canPlaceWallWithoutBlocking(x, y, players)) {
                    board[x][y] = newWall;
                    player.decrementWall();
                    System.out.println(" Mur placé !");
                } else {
                    System.out.println("Ce mur bloque un joueur !");
                }
            } else {
                System.out.println("Vous n'avez plus de murs !");
            }
        }
    }

    private void playAITurn(Player aiPlayer) {
        if (aiPlayer.getGoalDim() == GoalDimension.Y) {
            aiPlayer.move(aiPlayer.getX(), aiPlayer.getY() + 2);
        } else {
            aiPlayer.move(aiPlayer.getX() + 2, aiPlayer.getY());
        }
    }

    public void startGame() {
        while (playTurn()) {}
        System.out.println("Partie terminée !");
        scanner.close();
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players[turn];
    }

    public Player getPlayer(int index) {
        if (index < 0 || index >= players.length) {
            throw new IndexOutOfBoundsException("Index invalide : " + index);
        }
        return players[index];
    }

    private Integer[] calculateStarting(int side, int size) {
        int midCol = size * 2 / 2 - 1; // milieu de la grille (0-indexé) → 8 pour une grille 18x18
        return switch (side) {
            case 0 -> new Integer[]{0, midCol, 0};                // 🔴 haut-centre (ligne 0, col 8)
            case 1 -> new Integer[]{size * 2 - 1, midCol, 0};     // 🔵 bas-centre (ligne 17, col 8)
            case 2 -> new Integer[]{midCol, 0, 1};                // gauche (facultatif)
            case 3 -> new Integer[]{midCol, size * 2 - 1, 1};     // droite (facultatif)
            default -> throw new IllegalArgumentException("Invalid side: " + side);
        };
    }


}
