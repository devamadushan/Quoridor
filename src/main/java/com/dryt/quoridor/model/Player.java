package com.dryt.quoridor.model;

public class Player extends Entity {
    private String nom;
    private int nbWall;
    private int goalRow;
    private GoalDimension goalDim;


    public Player(String name ,int x, int y , int goalRow , GoalDimension goalDim) {
        super(x, y);
        this.nom = name;
        this.nbWall =10;
        this.goalRow = goalRow;
        this.goalDim = goalDim;
    }
    @Override
    public boolean move(int newX, int newY) {
        if (newX < 0 || newX >= 9 || newY < 0 || newY >= 9) {
            return false;
        }
        boolean moved  =  super.move(newX, newY);
        return moved && hasWon();
    }


    public boolean hasWon(){
        if(this.goalDim == GoalDimension.X){
            return goalRow == this.x;
        }
        if(this.goalDim == GoalDimension.Y){
            return goalRow == this.y;
        }
        return false;
    }

    public String getNom() {
        return nom;
    }
    public int getNbWall() {
        return nbWall;
    }
    public void decrementWall() {
        if (hasWall()) {
            nbWall--;
        }
    }
    public String  getGoalRow() {
        return ""+goalDim + goalRow;
    }

    public boolean hasWall(){
        return this.nbWall > 0 ;
    }

}
