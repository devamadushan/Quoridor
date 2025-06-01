package com.dryt.quoridor.ai;

import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.Plateau;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinimaxAI {

    private final int maxDepth;

    public MinimaxAI(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Action getBestAction(Plateau plateau) {
        return minimax(plateau, maxDepth, true,Integer.MIN_VALUE, Integer.MAX_VALUE).action;
    }

    private ActionScore minimax(Plateau plateau, int depth, boolean maximizingPlayer, int alpha, int beta) {
        Joueur winner = plateau.getWinner();
        Joueur aiPlayer = maximizingPlayer ? plateau.getCurrentPlayer() : plateau.getPreviousPlayer();
        if (depth == 0 || winner != null) {
            return new ActionScore(null, evaluateBoard(plateau, aiPlayer));
        }

        List<Action> actions = generateAllActions(plateau);
        Action bestAction = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Action action : actions) {
            Plateau cloned = plateau.clone();
            if (!action.apply(cloned)) continue;
            cloned.switchPlayerTurn();
            int score = minimax(cloned, depth - 1, !maximizingPlayer, alpha, beta).score;

            if (maximizingPlayer) {
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = action;
                }
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) break; // Beta cut-off
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestAction = action;
                }
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) break; // Alpha cut-off
            }
        }
        return new ActionScore(bestAction, bestScore);
    }

    private List<Action> generateAllActions(Plateau plateau) {
        List<Action> actions = new ArrayList<>();

        // 1. All pawn moves
        for (int[] move : plateau.getPossibleMoves()) {
            actions.add(Action.move(move[0], move[1]));
        }

        // 2. All valid wall placements
        Joueur current = plateau.getCurrentPlayer();
        if (current.getWallsRemaining() > 0) {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (boolean vertical : new boolean[]{true, false}) {
                        if (plateau.canPlaceWall(x, y, vertical) &&
                                plateau.allPlayersHaveAPathAfterWall(x, y, vertical)) {
                            actions.add(Action.wall(x, y, vertical));
                        }
                    }
                }
            }
        }

        return actions;
    }

    private int evaluateBoard(Plateau plateau, Joueur aiPlayer) {
        int score = 100;
        int currentDist = estimateDistance(aiPlayer);

        // Strongly reward forward progress
        score -= 4 * currentDist;

        // Penalize walls behind opponents
        for (Joueur j : plateau.getJoueurs()) {
            if (j != aiPlayer) {
                score -= countWallsBehindPlayer(plateau, j) * 10;
                score -= (int) Math.round(0.25 * estimateDistance(j));
            }
        }
        return score;
    }
    private int countWallsBehindPlayer(Plateau plateau, Joueur player) {
        int count = 0;
        // Check vertical walls
        for (int wx = 0; wx < 8; wx++) {
            for (int wy = 0; wy < 8; wy++) {
                if (plateau.hasVerticalWall(wx, wy) && isWallBehindPlayer(wx, wy, true, player)) {
                    count++;
                }
            }
        }
        // Check horizontal walls
        for (int wx = 0; wx < 8; wx++) {
            for (int wy = 0; wy < 8; wy++) {
                if (plateau.hasHorizontalWall(wx, wy) && isWallBehindPlayer(wx, wy, false, player)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isWallBehindPlayer(int wx, int wy, boolean vertical, Joueur player) {
        switch (player.getId()) {
            case 1: // Goal is y == 8, so behind is wy < player.getY()
                return (!vertical && wy < player.getY());
            case 2: // Goal is y == 0, so behind is wy > player.getY()
                return (!vertical && wy > player.getY());
            case 3: // Goal is x == 8, so behind is wx < player.getX()
                return (vertical && wx < player.getX());
            case 4: // Goal is x == 0, so behind is wx > player.getX()
                return (vertical && wx > player.getX());
            default:
                return false;
        }
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
