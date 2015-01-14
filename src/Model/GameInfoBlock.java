package Model;

import Players.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bozerg on 12/9/2014.
 */
public class GameInfoBlock {

	public final int width; //board width
    public final int height; //board height
    public final ArrayList<Player> players; //ArrayList of all the players in the game
    public final HashMap<Color, Integer> whenEliminated; //map from players in the game to the turn on which they were eliminated, winner will not be in this map
    public final int turnCount; //number of turns the game took
    public final Player[] placing; //array with index 0 being the winning player and index[numPlayers - 1] the first player eliminated
    public final int turnTime; //how long in milliseconds each player has to make their move
    public final boolean globalVision; //whether global vision is enabled
    public final int visionRadius; //vision radius, note that if globalVision is enabled this doesn't mean anything
    public final Board startingBoard; //the layout of the starting board;
    public final int numPlayers; //number of players in the game
    public final boolean earlyTermination; //whether or not the game was terminated as a result of turn limit being reached

    public GameInfoBlock(int width, int height, ArrayList<Player> players, HashMap<Color, Integer> whenEliminated, int turnCount, Player[] placing, int turnTime, boolean globalVision, int visionRadius, Board startingBoard, boolean earlyTermination) {
        this.width = width;
        this.height = height;
        this.players = players;
        this.whenEliminated = whenEliminated;
        this.turnCount = turnCount;
        this.placing = placing;
        this.turnTime = turnTime;
        this.globalVision = globalVision;
        this.visionRadius = visionRadius;
        this.startingBoard = startingBoard;
        this.numPlayers = placing.length;
        this.earlyTermination = earlyTermination;
    }
}
