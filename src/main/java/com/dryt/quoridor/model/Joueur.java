package com.dryt.quoridor.model;

public class Joueur {
    private int id;
    private int x;
    private int y;
    private int wallsRemaining;
    private boolean isAI;

    public Joueur(int id, int startX, int startY, int walls,boolean isAI) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.wallsRemaining = walls;
        this.isAI = isAI;
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

    public void incrementWalls(){
        wallsRemaining++;
    }
    public boolean isAI() {
        return isAI;
    }

    @Override
    public Joueur clone() {
        return new Joueur(this.id, this.x, this.y, this.wallsRemaining, this.isAI);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Joueur joueur = (Joueur) o;
        return id == joueur.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
