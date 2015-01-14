package Players;

import Model.Ant;
import Model.Board;
import Model.BoardGenerator;
import Model.Move;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Bozerg on 11/28/2014.
 */
public class PlayerHunter extends Player {

    public PlayerHunter(Color color, int playerNumber, int turnTime, String name) {
        super(color, playerNumber, turnTime, name);
    }

    public PlayerHunter(Color color, int playerNumber, int turnTime) {
        super(color, playerNumber, turnTime, "Hunter");
    }

    public void run() {
        while (!turnOver) {
            //generate moves and use a heuristic to score them
            HashSet<Ant> ants = currentState.getMyAnts();
            HashMap<Ant, Ant> antMoves = new HashMap<Ant, Ant>();
            Board visibleBoard = currentState.getVisibleBoard();
            HashMap<Point, HashSet<Point>> visible = visibleBoard.getVisibleInfoForPlayer(getColor());
            Random r = new Random();
            for (Ant ant : ants) {
                Point[] legalMoves = currentState.getLegalMovesForAnt(ant);
                HashSet<Point> antVision = visible.get(ant.getLocation());
                for (Point p : antVision) {
                    if(BoardGenerator.manhattanDistance(p, ant.getLocation()) < 7) {
                        if (currentState.getEnemyHillLocation().get(p) != null && !antMoves.containsKey(ant)) {
                            Point bestLocation = new Point(ant.getLocation());
                            for (Point m : legalMoves) {
                                if (BoardGenerator.manhattanDistance(m, p) < BoardGenerator.manhattanDistance(bestLocation, p)) {
                                    bestLocation = m;
                                }
                            }
                            antMoves.put(ant, new Ant(getColor(), bestLocation));
                        } else if (currentState.getEnemyAntLocation().get(p) != null && !antMoves.containsKey(ant)) {
                            Point bestLocation = new Point(ant.getLocation());
                            for (Point m : legalMoves) {
                                if (BoardGenerator.manhattanDistance(m, p) < BoardGenerator.manhattanDistance(bestLocation, p)) {
                                    bestLocation = m;
                                }
                            }
                            antMoves.put(ant, new Ant(getColor(), bestLocation));
                        } else if (currentState.getFoodLocation().get(p) != null && !antMoves.containsKey(ant)) {
                            Point bestLocation = new Point(ant.getLocation());
                            for (Point m : legalMoves) {
                                if (BoardGenerator.manhattanDistance(m, p) < BoardGenerator.manhattanDistance(bestLocation, p)) {
                                    bestLocation = m;
                                }
                            }
                            antMoves.put(ant, new Ant(getColor(), bestLocation));
                        }
                    }
                }

                if (!antMoves.containsKey(ant) && legalMoves.length > 0) {
                    int index = r.nextInt(legalMoves.length);
                    Point antMove = legalMoves[index];
                    antMoves.put(ant, new Ant(getColor(), antMove));
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

