package com.dryt.quoridor.model;

public class Mur {
    // Orientation: true for vertical, false for horizontal
    private boolean vertical;
    private int x;
    private int y;

    public Mur(boolean vertical, int x, int y) {
        this.vertical = vertical;
        this.x = x;
        this.y = y;
    }

    public boolean isVertical() {
        return vertical;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
