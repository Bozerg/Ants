package Players;

import Model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Bozerg on 11/30/2014.
 */
public class PlayerKamikaze extends Player {
    private final HashMap<Point, Hill> knownEnemyHills = new HashMap<Point, Hill>();
    private HashMap<Point, Hill> myPreviousHills;

    public PlayerKamikaze(Color color, int playerNumber, int turnTime, String name) {
        super(color, playerNumber, turnTime, name);
    }

    public PlayerKamikaze(Color color, int playerNumber, int turnTime) {
        super(color, playerNumber, turnTime, "Kamikaze");
    }

    @Override
    public void run() {
        Random r = new Random();
        HashSet<Ant> ants = currentState.getMyAnts();
        HashSet<Ant> enemyAnts = currentState.getEnemyAnts();
        Board visibleBoard = currentState.getVisibleBoard();
        HashMap<Point, HashSet<Point>> visible = visibleBoard.getVisibleInfoForPlayer(getColor());
        HashMap<Point, Food> food = currentState.getFoodLocation();
        HashMap<Point, Hill> enemyHills = currentState.getEnemyHillLocation();
        HashSet<Hill> enemyHillSet = currentState.getEnemyHills();
        HashSet<Hill> myHills = currentState.getMyHills();
        for(Hill h: enemyHillSet){
            knownEnemyHills.put(h.getLocation(), h);
        }
        if(myPreviousHills != null) {
            for (Point p : myPreviousHills.keySet()){
                if(!myHills.contains(myPreviousHills.get(p))){
                    knownEnemyHills.put(myPreviousHills.get(p).getLocation(), new Hill(Color.BLACK, myPreviousHills.get(p).getLocation()));
                }
            }
        }
        for(Hill h: myHills){
            if(knownEnemyHills.containsKey(h.getLocation())){
                knownEnemyHills.remove(h.getLocation());
            }
        }
        HashMap<Point, Hill> myHillMap = currentState.getMyHillLocation();
        myPreviousHills = currentState.getMyHillLocation();
        int width = visibleBoard.getWidth();
        int height = visibleBoard.getHeight();
        while (!turnOver) {
            HashMap<Ant, Ant> antMoves = new HashMap<Ant, Ant>();
            HashSet<Ant> defending = new HashSet<Ant>();
            HashMap<Ant, Hill> guardians = new HashMap<Ant, Hill>();
            if(visibleBoard.getTurnCount() > 30 && ants.size() > 3 * myHills.size()) {
                for (Hill hill : myHills) {
                    Ant closestAnt = null;
                    for (Ant ant : ants) {
                        if (closestAnt == null) {
                            closestAnt = ant;
                        } else if (BoardGenerator.manhattanDistance(closestAnt.getLocation(), hill.getLocation()) > BoardGenerator.manhattanDistance(ant.getLocation(), hill.getLocation())) {
                            closestAnt = ant;
                        }
                    }
                    guardians.put(closestAnt, hill);
                }
            }

            //defend the bases
            for (Ant enemyAnt : enemyAnts) {
                Point enemyAntLocation = enemyAnt.getLocation();
                for (Hill hill : myHills) {
                    if (BoardGenerator.manhattanDistance(enemyAnt.getLocation(), hill.getLocation()) < 5) {
                        for (Ant myAnt : ants) {
                            Point myAntLocation = myAnt.getLocation();
                            if (BoardGenerator.manhattanDistance(myAntLocation, enemyAntLocation) + BoardGenerator.manhattanDistance(myAntLocation, hill.getLocation()) < 2 * BoardGenerator.manhattanDistance(enemyAntLocation, hill.getLocation())) {
                                Point bestMove = myAnt.getLocation();
                                Point[] legalMoves = currentState.getLegalMovesForAnt(myAnt);
                                for (Point destination : legalMoves) {
                                    if (BoardGenerator.manhattanDistance(destination, enemyAntLocation) < BoardGenerator.manhattanDistance(bestMove, enemyAntLocation)) {
                                        bestMove = destination;
                                    } else if(BoardGenerator.manhattanDistance(destination, enemyAntLocation) == BoardGenerator.manhattanDistance(bestMove, enemyAntLocation)){
                                        if(BoardGenerator.euclideanDistance(destination, enemyAntLocation) < BoardGenerator.euclideanDistance(bestMove, enemyAntLocation)) {
                                            bestMove = destination;
                                        }
                                    }
                                }
                                defending.add(myAnt);
                                antMoves.put(myAnt, new Ant(myAnt.getColor(), bestMove));
                                setMove(new Move(getColor(), antMoves));
                            }
                        }
                    }
                }
            }
            for (Ant ant : ants) {
                Point[] legalMoves = currentState.getLegalMovesForAnt(ant);
                Point bestMove = ant.getLocation();
                Point curLocation = ant.getLocation();
                HashSet<Point> antVision = visible.get(ant.getLocation());

                //ant hill guardian ant can't stray too far from the base
                if (guardians.containsKey(ant) && !defending.contains(ant)) {
                    Point hillDefending = guardians.get(ant).getLocation();
                    ArrayList<Point> possibleMoves = new ArrayList<Point>();
                    if (BoardGenerator.manhattanDistance(curLocation, hillDefending) > 2) {
                        bestMove = curLocation;
                        for (Point destination : legalMoves) {
                            if (BoardGenerator.manhattanDistance(destination, hillDefending) < BoardGenerator.manhattanDistance(bestMove, hillDefending)) {
                                bestMove = destination;
                            }
                            else if(BoardGenerator.manhattanDistance(destination, hillDefending) == BoardGenerator.manhattanDistance(bestMove, hillDefending)) {
                                if (BoardGenerator.euclideanDistance(destination, hillDefending) < BoardGenerator.euclideanDistance(bestMove, hillDefending)) {
                                    bestMove = destination;
                                }
                            }
                        }
                    } else {
                        boolean proximityAlert = false;
                        for (Ant enemyAnt : enemyAnts) {
                            if (BoardGenerator.manhattanDistance(enemyAnt.getLocation(), hillDefending) <= 5) {
                                proximityAlert = true;
                                bestMove = curLocation;
                                for (Point destination : legalMoves) {
                                    if (BoardGenerator.manhattanDistance(destination, hillDefending) < BoardGenerator.manhattanDistance(bestMove, hillDefending)) {
                                        bestMove = destination;
                                    }
                                    else if(BoardGenerator.manhattanDistance(destination, hillDefending) == BoardGenerator.manhattanDistance(bestMove, hillDefending)) {
                                        if (BoardGenerator.euclideanDistance(destination, hillDefending) < BoardGenerator.euclideanDistance(bestMove, hillDefending)) {
                                            bestMove = destination;
                                        }
                                    }
                                }
                            }
                        }
                        if (!proximityAlert) {
                            for (Point destination : legalMoves) {
                                int xDistance = (int) Math.min(width - Math.abs(destination.getX() - hillDefending.getX()), Math.abs(destination.getX() - hillDefending.getX()));
                                int yDistance = (int) Math.min(height - Math.abs(destination.getY() - hillDefending.getY()), Math.abs(destination.getY() - hillDefending.getY()));
                                if (xDistance + yDistance <= 2 && xDistance + yDistance > 0) {
                                    possibleMoves.add(destination);
                                }
                            }
                        }
                        if (!possibleMoves.isEmpty() && !proximityAlert) {
                            bestMove = possibleMoves.get(r.nextInt(possibleMoves.size()));
                        } else if (!proximityAlert) {
                            bestMove = curLocation;
                        }
                    }
                }

                //bases don't need defending by this ant
                else if (!defending.contains(ant)) {
                    boolean splitFlag = false;
                    boolean foodFlag = false;
                    boolean hillFlag = false;
                    int minManhattanDistance = height + width;
                    double minEuclidianDistance = height + width;
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
                    }
                    if(!hillFlag) {
                        for (Point p : antVision) {
                            if (food.containsKey(p)) {
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
                    }
                    if (bestMove.equals(curLocation) && !foodFlag && !hillFlag) {
                        for (Ant a : ants) {
                            Point other = a.getLocation();
                            if (BoardGenerator.manhattanDistance(curLocation, other) == 1) {
                                for (Point destination : legalMoves) {
                                    if (Math.abs(destination.getX() - other.getX()) > 1 || Math.abs(destination.getY() - other.getY()) > 1) {
                                        if (!myHillMap.containsKey(destination)) {
                                            bestMove = destination;
                                            splitFlag = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (bestMove.equals(curLocation) && !foodFlag && !splitFlag && !hillFlag) {
                        double globalMaximizedMinDistance = 0;
                        if (r.nextInt(4) == 0) {
                            bestMove = legalMoves[r.nextInt(legalMoves.length)];
                        } else {
                            if(knownEnemyHills.keySet().size() == 0 || ants.size() < myHills.size() * 10) {
                                for (Point destination : legalMoves) {
                                    if (!curLocation.equals(destination) && !myHillMap.containsKey(destination)) {
                                        double distanceToNearest = height * width;
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
                            else{
                                minManhattanDistance = height + width;
                                minEuclidianDistance = height + width;
                                for(Point p: knownEnemyHills.keySet()){
                                    for(Point destination: legalMoves) {
                                        if (BoardGenerator.manhattanDistance(p, destination) < minManhattanDistance) {
                                            bestMove = destination;
                                            minManhattanDistance = BoardGenerator.manhattanDistance(p, destination);
                                            minEuclidianDistance = BoardGenerator.euclideanDistance(p, destination);
                                        }
                                        if(BoardGenerator.manhattanDistance(p,  destination) == minManhattanDistance){
                                            if(BoardGenerator.euclideanDistance(p, destination) < minEuclidianDistance){
                                                bestMove = destination;
                                                minEuclidianDistance = BoardGenerator.euclideanDistance(p, destination);
                                            }
                                        }
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