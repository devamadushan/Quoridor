package com.dryt.quoridor.model;

public abstract class Entity {
    protected int x;
    protected int y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {return x;}
    public int getY() {return y;}

    public void setPosition(int x , int y) {
        this.x = x;
        this.y = y;
    }
    public boolean move(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        return true;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
