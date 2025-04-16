package com.dryt.quoridor.model;

public class Joueur {
    private int id;
    private int x;
    private int y;
    private int wallsRemaining;

    public Joueur(int id, int startX, int startY, int walls) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.wallsRemaining = walls;
    }

    public int getId() {
        return id;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
    public int getWallsRemaining() {
        return wallsRemaining;
    }
    public void decrementWalls() {
        if (wallsRemaining > 0) {
            wallsRemaining--;
        }
    }
}
