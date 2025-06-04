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

        if (maxDepth % 2 != 0 ) {
            maxDepth++;
        }
        this.maxDepth = maxDepth;
    }

    public Action getBestAction(Plateau plateau) {
        ActionScore result = minimax(plateau, maxDepth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (result.action != null) {
            return result.action;
        }

        List<int[]> path = plateau.getShortestPathToGoal(plateau.getCurrentPlayer());
        if (path.size() >= 2) { // path[0] is current position, path[1] is next move
            int[] next = path.get(1);
            return Action.move(next[0], next[1]);
        }
        throw new IllegalStateException("No valid actions or path to goal for AI");
    }

    private ActionScore minimax(Plateau plateau, int depth, boolean maximizingPlayer, int alpha, int beta) {
        Joueur winner = plateau.getWinner();
        if (winner != null) {
            int score = (maximizingPlayer == (winner == plateau.getCurrentPlayer())) ? 10000 : -10000;
            return new ActionScore(null, score);
        }
        if (depth == 0) {
            return new ActionScore(null, evaluateBoard(plateau));
        }

        List<Action> actions = generateAllActions(plateau);

        ;

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
                if (beta <= alpha) break;
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestAction = action;
                }
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) break;
            }
        }
        return new ActionScore(bestAction, bestScore);
    }

    List<Action> generateAllActions(Plateau plateau) {
        List<Action> pawnMoves = new ArrayList<>();
        List<Action> wallMoves = new ArrayList<>();

        Joueur current = plateau.getCurrentPlayer();
        int currentDist = estimateDistance(plateau, current);

        // Pawn moves as before
        for (int[] move : plateau.getPossibleMoves()) {
            Plateau cloned = plateau.clone();
            cloned.moveCurrentPlayer(move[0], move[1]);
            Joueur clonedPlayer = cloned.getCurrentPlayer();
            int newDist = estimateDistance(cloned, clonedPlayer);

            if (newDist < currentDist && newDist < 100) {
                pawnMoves.add(Action.move(move[0], move[1]));
            }
        }

        // Wall moves: only allow if it doesn't increase AI's own shortest path
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
                                Plateau cloned = plateau.clone();
                                cloned.placeWallCurrentPlayer(wx, wy, vertical);
                                int newDist = estimateDistance(cloned, current);
                                if (newDist == currentDist) { // Only allow if path length is unchanged
                                    wallMoves.add(Action.wall(wx, wy, vertical));
                                }
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
