package Players;

import Model.Ant;
import Model.Move;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class PlayerRandom extends Player {

    public PlayerRandom(Color color, int playerNumber, int turnTime, String name){
        super(color, playerNumber, turnTime, name);
    }

    public PlayerRandom(Color color, int playerNumber, int turnTime){
        this(color, playerNumber, turnTime, "Random");
    }

    public void run() {
        while (!turnOver) {
            //generate moves and use a heuristic to score them
            HashSet<Ant> ants = currentState.getMyAnts();
            Random r = new Random();
            HashMap<Ant, Ant> antMoves = new HashMap<Ant, Ant>();
            for (Ant ant : ants) {
                if (ant.getColor().equals(getColor())) {
                    Point[] legalMoves = currentState.getLegalMovesForAnt(ant);
                    if (legalMoves.length > 0) {
                        int index = r.nextInt(legalMoves.length);
                        Point antMove = legalMoves[index];
                        antMoves.put(ant, new Ant(getColor(), antMove));
                    }
                }
            }
            //set better moves as you go along so that you have your best move found stored when GameRunner ends you
            if (move.getMoves() == null) {
                setMove(new Move(getColor(), antMoves));
                endTurn();
            }
        }
    }
}
