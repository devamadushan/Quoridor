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
        if (depth == 0 || winner != null) {
            return new ActionScore(null, evaluateBoard(plateau));
        }


        List<Action> actions = generateAllActions(plateau);

        if (depth == maxDepth) {
            System.out.println("Evaluating " + actions.size() + " actions for player " + plateau.getCurrentPlayer().getId());
        }


        Action bestAction = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Action action : actions) {
            Plateau cloned = plateau.clone();
            boolean valid = action.apply(cloned);
            if (!valid) continue;

            cloned.switchPlayerTurn();
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
