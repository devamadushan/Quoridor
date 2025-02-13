package com.dryt.quoridor.model;

public class Wall extends Entity {
    private boolean isHorizontal;

    public Wall(int x, int y , boolean isHorizontal) {
        super(x, y);
        this.isHorizontal = isHorizontal;
    }
    public boolean isHorizontal() {
        return isHorizontal;
    }


    @Override
    public String toString() {
        return "Wall{" +
                "isHorizontal=" + isHorizontal +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
