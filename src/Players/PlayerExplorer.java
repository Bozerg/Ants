package Players;

/**
 * Created by Bozerg on 11/30/2014.
 */

import Model.*;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


public class PlayerExplorer extends Player {

    public PlayerExplorer(Color color, int playerNumber, int turnTime, String name) {
        super(color, playerNumber, turnTime, name);
    }

    public PlayerExplorer(Color color, int playerNumber, int turnTime) {
        super(color, playerNumber, turnTime, "Explorer");
    }

    @Override
    public void run() {
        Random r = new Random();
        HashSet<Ant> ants = currentState.getMyAnts();
        Board visibleBoard = currentState.getVisibleBoard();
        HashMap<Point, HashSet<Point>> visible = visibleBoard.getVisibleInfoForPlayer(getColor());
        HashMap<Point, Food> food = currentState.getFoodLocation();
        HashMap<Point, Hill> enemyHills = currentState.getEnemyHillLocation();
        HashMap<Point, Hill> myHills = currentState.getMyHillLocation();
        int width = visibleBoard.getWidth();
        int height = visibleBoard.getHeight();
        while (!turnOver) {
            HashMap<Ant, Ant> antMoves = new HashMap<Ant, Ant>();
            for (Ant ant : ants) {
                boolean splitFlag = false;
                Point curLocation = ant.getLocation();
                Point bestMove = ant.getLocation();
                Point[] legalMoves = currentState.getLegalMovesForAnt(ant);
                HashSet<Point> antVision = visible.get(ant.getLocation());
                boolean foodFlag = false;
                boolean hillFlag = false;
                double minManhattanDistance = width + height;
                double minEuclidianDistance = width + height;
                for (Point p : antVision) {
                    if (enemyHills.containsKey(p)) {
                        hillFlag = true;
                        for (Point destination : legalMoves) {
                            if (BoardGenerator.manhattanDistance(destination, p) < minManhattanDistance) {
                                bestMove = destination;
                                minManhattanDistance = BoardGenerator.manhattanDistance(destination, p);
                                minEuclidianDistance = BoardGenerator.euclideanDistance(destination, p);
                            } else if (BoardGenerator.manhattanDistance(destination, p) == minManhattanDistance) {
                                if (BoardGenerator.euclideanDistance(destination, p) < minEuclidianDistance) {
                                    bestMove = destination;
                                    minEuclidianDistance = BoardGenerator.euclideanDistance(destination, p);
                                }
                            }
                        }
                    }
                    if (!hillFlag && food.containsKey(p)) {
                        foodFlag = true;
                        for (Point destination : legalMoves) {
                            if (BoardGenerator.manhattanDistance(destination, p) < minManhattanDistance) {
                                bestMove = destination;
                                minManhattanDistance = BoardGenerator.manhattanDistance(destination, p);
                                minEuclidianDistance = BoardGenerator.euclideanDistance(destination, p);
                            } else if (BoardGenerator.manhattanDistance(destination, p) == minManhattanDistance) {
                                if (BoardGenerator.euclideanDistance(destination, p) < minEuclidianDistance) {
                                    bestMove = destination;
                                    minEuclidianDistance = BoardGenerator.euclideanDistance(destination, p);
                                }
                            }
                        }
                    }
                }
                if (bestMove.equals(curLocation) && !foodFlag) {
                    for (Ant a : ants) {
                        Point other = a.getLocation();
                        if (BoardGenerator.manhattanDistance(curLocation, other) == 1) {
                            for (Point destination : legalMoves) {
                                if (Math.abs(destination.getX() - other.getX()) > 1 || Math.abs(destination.getY() - other.getY()) > 1) {
                                    if (!myHills.containsKey(destination)) {
                                        bestMove = destination;
                                        splitFlag = true;
                                    }
                                }
                            }
                        }
                    }

                }
                if (bestMove.equals(curLocation) && !foodFlag && !splitFlag) {
                    double globalMaximizedMinDistance = 0;
                    if (r.nextInt(4) == 0) {
                        bestMove = legalMoves[r.nextInt(legalMoves.length)];
                    } else {
                        for (Point destination : legalMoves) {
                            if (!curLocation.equals(destination) && !myHills.containsKey(destination)) {
                                double distanceToNearest = visibleBoard.getHeight() * visibleBoard.getWidth();
                                for (Ant a : ants) {
                                    Point other = a.getLocation();
                                    if (!other.equals(curLocation)) {
                                        int distanceToOther = BoardGenerator.manhattanDistance(other, destination);
                                        if (distanceToOther < distanceToNearest) {
                                            distanceToNearest = distanceToOther;
                                        }
                                    }
                                }
                                if (distanceToNearest > globalMaximizedMinDistance) {
                                    bestMove = destination;
                                    globalMaximizedMinDistance = distanceToNearest;
                                }
                                //helps to break some 3 ant move cycles
                                else if (distanceToNearest == globalMaximizedMinDistance) {
                                    if (r.nextBoolean()) {
                                        bestMove = destination;
                                    }
                                }
                            }
                        }
                    }
                }
                antMoves.put(ant, new Ant(ant.getColor(), bestMove));
                setMove(new Move(getColor(), antMoves));
            }
            endTurn();
        }
    }
}
