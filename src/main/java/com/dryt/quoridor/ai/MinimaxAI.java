package com.dryt.quoridor.ai;

import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.Plateau;

import java.util.ArrayList;
import java.util.List;

public class MinimaxAI {

    private final int maxDepth;

    public MinimaxAI(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Action getBestAction(Plateau plateau) {
        return minimax(plateau, maxDepth, true).action;
    }

    private ActionScore minimax(Plateau plateau, int depth, boolean maximizingPlayer) {
        Joueur winner = plateau.getWinner();
        if (winner != null) {
            int score = (maximizingPlayer == (winner == plateau.getCurrentPlayer())) ? 10000 : -10000;
            return new ActionScore(null, score);
        }
        if (depth == 0) {
            return new ActionScore(null, evaluateBoard(plateau));
        }

        List<Action> actions = generateAllActions(plateau);
        Action bestAction = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Action action : actions) {
            Plateau cloned = plateau.clone();
            boolean valid = action.apply(cloned);
            if (!valid) continue;

            cloned.switchPlayerTurn();

            // Check if maximizing player's distance is zero after this move
            Joueur maximizing = maximizingPlayer ? plateau.getCurrentPlayer() : cloned.getCurrentPlayer();
            if (estimateDistance(maximizing) == 0) {
                int winScore = maximizingPlayer ? 10000 : -10000;
                return new ActionScore(action, winScore);
            }

            int score = minimax(cloned, depth - 1, !maximizingPlayer).score;

            if (maximizingPlayer && score > bestScore) {
                bestScore = score;
                bestAction = action;
            } else if (!maximizingPlayer && score < bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }
        return new ActionScore(bestAction, bestScore);
    }

    private List<Action> generateAllActions(Plateau plateau) {
        List<Action> pawnMoves = new ArrayList<>();
        List<Action> wallMoves = new ArrayList<>();

        // 1. All pawn moves (always include)
        for (int[] move : plateau.getPossibleMoves()) {
            pawnMoves.add(Action.move(move[0], move[1]));
        }

        // 2. Pruned wall placements (only near players)
        Joueur current = plateau.getCurrentPlayer();
        if (current.getWallsRemaining() > 0) {
            for (Joueur j : plateau.getJoueurs()) {
                int px = j.getX(), py = j.getY();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int wx = px + dx, wy = py + dy;
                        for (boolean vertical : new boolean[]{true, false}) {
                            if (wx >= 0 && wx < 8 && wy >= 0 && wy < 8 &&
                                    plateau.canPlaceWall(wx, wy, vertical) &&
                                    plateau.allPlayersHaveAPathAfterWall(wx, wy, vertical)) {
                                wallMoves.add(Action.wall(wx, wy, vertical));
                            }
                        }
                    }
                }
            }
        }

        // Move ordering: pawn moves first
        List<Action> actions = new ArrayList<>(pawnMoves);
        actions.addAll(wallMoves);
        return actions;
    }

    private int evaluateBoard(Plateau plateau) {
        int score = 0;
        for (Joueur j : plateau.getJoueurs()) {
            int dist = estimateDistance(j);

            if (j == plateau.getCurrentPlayer()) {

                score -= dist;
            } else {
                score += dist;
            }
        }
        return score;
    }

    private int estimateDistance(Joueur joueur) {
        return switch (joueur.getId()) {
            case 1 -> 8 - joueur.getY();
            case 2 -> joueur.getY();
            case 3 -> 8 - joueur.getX();
            case 4 -> joueur.getX();
            default -> 100;
        };
    }

    private static class ActionScore {
        Action action;
        int score;

        public ActionScore(Action action, int score) {
            this.action = action;
            this.score = score;
        }


    }
}
