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

            Joueur maximizing = maximizingPlayer ? plateau.getCurrentPlayer() : cloned.getCurrentPlayer();
            if (estimateDistance(cloned, maximizing) == 0) {
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

        Joueur current = plateau.getCurrentPlayer();
        int currentDist = estimateDistance(plateau, current);

        // Only include pawn moves that reduce the shortest path to goal
        for (int[] move : plateau.getPossibleMoves()) {
            Plateau cloned = plateau.clone();
            cloned.moveCurrentPlayer(move[0], move[1]);
            Joueur clonedPlayer = cloned.getCurrentPlayer();
            int newDist = estimateDistance(cloned, clonedPlayer);

            if (newDist < currentDist && newDist < 100) { // 100 = unreachable
                pawnMoves.add(Action.move(move[0], move[1]));
            }
        }

        // Wall moves as before
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

        List<Action> actions = new ArrayList<>(pawnMoves);
        actions.addAll(wallMoves);
        return actions;
    }

    private int evaluateBoard(Plateau plateau) {
        int score = 0;
        for (Joueur j : plateau.getJoueurs()) {
            int dist = estimateDistance(plateau, j);

            if (j == plateau.getCurrentPlayer()) {
                score -= dist;
            } else {
                score += dist;
            }
        }
        return score;
    }

    // Use Plateau.getShortestPathToGoal to get the true shortest path length
    private int estimateDistance(Plateau plateau, Joueur joueur) {
        List<int[]> path = plateau.getShortestPathToGoal(joueur);
        return path.isEmpty() ? 100 : path.size() - 1;
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
