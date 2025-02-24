package com.dryt.quoridor.gameLogic;

import com.dryt.quoridor.model.Entity;
import com.dryt.quoridor.model.GoalDimension;
import com.dryt.quoridor.model.Player;
import eu.hansolo.toolbox.tuples.Tuple;

public class GameInstance {

    private final Entity[][] board;
    private final Entity[] players;
    private final int maxWalls;
    private final int size;
    private final int playerCount;
    private int turn=0;

    public GameInstance(int size,int nbPlayers, int AIPlayers, int walls){
        this.board=new Entity[size*2][size*2];
        this.maxWalls=walls;
        this.players=new Entity[nbPlayers];
        this.size=size;
        this.playerCount=nbPlayers;

        int side=0;

        for (int i=0;i<AIPlayers;i++){
            Integer[] pos=calculateStarting(side,size);
            int GoalR=0;
            GoalDimension GoalD=GoalDimension.Y;
            if (pos[2]==1){GoalD=GoalDimension.X;}
            if (side % 2 ==0){GoalR=size*2;}
            side++;
            players[nbPlayers-i]= new Player("AI"+(AIPlayers+i),pos[1],pos[0],GoalR,GoalD,true);
        }

        for (int i=0;i<nbPlayers-AIPlayers;i++){
            Integer[] pos=calculateStarting(side,size);
            int GoalR=0;
            GoalDimension GoalD=GoalDimension.Y;
            if (pos[2]==1){GoalD=GoalDimension.X;}
            if (side % 2 ==0){GoalR=size*2;}
            side++;
            players[nbPlayers-AIPlayers-i]= new Player("AI"+i,pos[1],pos[0],GoalR,GoalD,false);
        }


    }

    public boolean turn(){
        return true;
    }

    private Integer[] calculateStarting(int side, int size) {

        return switch (side) {
            case 0 -> new Integer[]{0, size,0};
            case 1 -> new Integer[]{size, 0,1};
            case 2 -> new Integer[]{size, size*2,1};
            case 3 -> new Integer[]{size*2, size,0};
            default -> throw new IllegalArgumentException("Invalid side: " + side);
        };
    }
}
