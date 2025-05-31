package com.dryt.quoridor.ai;

import com.dryt.quoridor.model.Joueur;
import com.dryt.quoridor.model.Plateau;

public class

Action {


    private final MoveType type;
    private final int x, y;
    private final boolean vertical;

    private Action(MoveType type, int x, int y, boolean vertical) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vertical = vertical;
    }

    public static Action move(int x, int y) {
        return new Action(MoveType.MOVE, x, y, false);
    }

    public static Action wall(int x, int y, boolean vertical) {
        return new Action(MoveType.WALL, x, y, vertical);
    }

    public MoveType getType() {return type;}
    public int getX() {return x;}
    public int getY() {return y;}
    public boolean getVertical() {return vertical;}

    public void undo(Plateau plateau, int oldX, int oldY, int oldWalls) {
        Joueur current = plateau.getCurrentPlayer();
        current.setPosition(oldX, oldY);
        while (current.getWallsRemaining() < oldWalls) {
            current.incrementWalls(); // Assumes you have this method; if not, reset directly
        }

        if (type == MoveType.WALL) {
            if (vertical) {
                plateau.removeVerticalWall(x, y); // Add these helper methods if not present
            } else {
                plateau.removeHorizontalWall(x, y);
            }
        }
    }

    public boolean apply(Plateau plateau) {
        return switch (type) {
            case MOVE -> plateau.moveCurrentPlayer(x, y);
            case WALL -> plateau.placeWallCurrentPlayer(x, y, vertical);
        };
    }

    @Override
    public String toString() {
        return (type == MoveType.MOVE ? "Move to (" + x + "," + y + ")" :
                "Place " + (vertical ? "vertical" : "horizontal") + " wall at (" + x + "," + y + ")");
    }
}
