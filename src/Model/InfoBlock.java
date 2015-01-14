package Model;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Bozerg on 11/25/2014.
 */

//Used to make life easier on AI players by giving them a bunch of information ready packaged.
public class InfoBlock {
    private final int height;
    private final int width;
    private final Board visibleBoard; //censored version of the board that contains only the information that is visible for the player
    private final Color playerColor;
    private final HashSet<Ant> myAnts;
    private final HashMap<Point, Ant> myAntLocation = new HashMap<Point, Ant>();
    private final HashSet<Ant> enemyAnts;
    private final HashMap<Point, Ant> enemyAntLocation = new HashMap<Point, Ant>();
    private final HashSet<Rock> rocks;
    private final HashMap<Point, Rock> rockLocation = new HashMap<Point, Rock>();
    private final HashSet<Food> food;
    private final HashMap<Point, Food> foodLocation = new HashMap<Point, Food>();
    private final HashSet<Hill> myHills;
    private final HashMap<Point, Hill> myHillLocation = new HashMap<Point, Hill>();
    private final HashSet<Hill> enemyHills;
    private final HashMap<Point, Hill> enemyHillLocation = new HashMap<Point, Hill>();
    private final HashSet<Point> visible;

    public InfoBlock(int height, int width, Color playerColor, HashSet<Ant> myAnts, HashSet<Ant> enemyAnts, HashSet<Rock> rocks, HashSet<Food> food, HashSet<Hill> myHills, HashSet<Hill> enemyHills, HashSet<Point> visible, Board visibleBoard) {
        this.height = height;
        this.width = width;
        this.playerColor = playerColor;
        this.myAnts = myAnts;
        for(Ant a : myAnts){
            myAntLocation.put(a.getLocation(), new Ant(a));
        }
        this.enemyAnts = enemyAnts;
        for(Ant a : enemyAnts){
            enemyAntLocation.put(a.getLocation(), new Ant(a));
        }
        this.rocks = rocks;
        for(Rock r : rocks){
            rockLocation.put(r.getLocation(), new Rock(r));
        }
        this.food = food;
        for(Food f : food){
            foodLocation.put(f.getLocation(), new Food(f));
        }
        this.myHills = myHills;
        for(Hill h : myHills){
            myHillLocation.put(h.getLocation(), new Hill(h));
        }
        this.enemyHills = enemyHills;
        for(Hill h : enemyHills){
            enemyHillLocation.put(h.getLocation(), new Hill(h));
        }
        this.visible = visible;
        this.visibleBoard = visibleBoard;
    }

    public HashSet<Ant> getMyAnts() {
        return this.myAnts;
    }

    public HashMap<Point, Ant> getMyAntLocation(){
        return this.myAntLocation;
    }

    public HashSet<Ant> getEnemyAnts(){
        return this.enemyAnts;
    }

    public HashMap<Point, Ant> getEnemyAntLocation(){
        return this.enemyAntLocation;
    }

    public HashSet<Rock> getRocks(){
        return this.rocks;
    }

    public HashMap<Point, Rock> getRockLocation(){
        return this.rockLocation;
    }

    public HashSet<Food> getFood(){
        return this.food;
    }

    public HashMap<Point, Food> getFoodLocation(){
        return this.foodLocation;
    }

    public HashSet<Hill> getEnemyHills(){
        return this.enemyHills;
    }

    public HashMap<Point, Hill> getEnemyHillLocation(){
        return this.enemyHillLocation;
    }

    public HashSet<Hill> getMyHills(){
        return this.myHills;
    }

    public HashMap<Point, Hill> getMyHillLocation(){
        return this.myHillLocation;
    }

    public HashSet<Point> getVisiblePoints(){
        return this.visible;
    }

    public Board getVisibleBoard(){
        return this.visibleBoard;
    }

    //returns an array of legalMoves for the given ant
    public Point[] getLegalMovesForAnt(Ant ant) {
        ArrayList<Point> legalMoves = new ArrayList<Point>();
        Point location = ant.getLocation();
        int x = (int) location.getX();
        int y = (int) location.getY();
        Point left = new Point((x + 1 + width) % width, y);
        Point right = new Point((x - 1 + width) % width, y);
        Point down = new Point(x, (y + 1 + height) % height);
        Point up = new Point(x, (y - 1 + height) % height);
        legalMoves.add(ant.getLocation());
        if (myAnts.contains(ant)) {
            if (!myAnts.contains(new Ant(playerColor, left)) && !rocks.contains(new Rock(left))) {
                legalMoves.add(left);
            }
            if (!myAnts.contains(new Ant(playerColor, right)) && !rocks.contains(new Rock(right))) {
                legalMoves.add(right);
            }
            if (!myAnts.contains(new Ant(playerColor, down)) && !rocks.contains(new Rock(down))) {
                legalMoves.add(down);
            }
            if (!myAnts.contains(new Ant(playerColor, up)) && !rocks.contains(new Rock(up))) {
                legalMoves.add(up);
            }
        } else if (enemyAnts.contains(ant)) {
            if (!enemyAnts.contains(new Ant(ant.getColor(), left)) && !rocks.contains(new Rock(left))) {
                legalMoves.add(left);
            }
            if (!enemyAnts.contains(new Ant(ant.getColor(), right)) && !rocks.contains(new Rock(right))) {
                legalMoves.add(right);
            }
            if (!enemyAnts.contains(new Ant(ant.getColor(), down)) && !rocks.contains(new Rock(down))) {
                legalMoves.add(down);
            }
            if (!enemyAnts.contains(new Ant(ant.getColor(), up)) && !rocks.contains(new Rock(up))) {
                legalMoves.add(up);
            }
        }
        Point[] returnMoves = new Point[legalMoves.size()];
        int count = 0;
        for (Point p : legalMoves) {
            returnMoves[count] = p;
            count++;
        }
        return returnMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InfoBlock)) return false;

        InfoBlock infoBlock = (InfoBlock) o;

        if (height != infoBlock.height) return false;
        if (width != infoBlock.width) return false;
        if (enemyAntLocation != null ? !enemyAntLocation.equals(infoBlock.enemyAntLocation) : infoBlock.enemyAntLocation != null)
            return false;
        if (!enemyAnts.equals(infoBlock.enemyAnts)) return false;
        if (enemyHillLocation != null ? !enemyHillLocation.equals(infoBlock.enemyHillLocation) : infoBlock.enemyHillLocation != null)
            return false;
        if (!enemyHills.equals(infoBlock.enemyHills)) return false;
        if (!food.equals(infoBlock.food)) return false;
        if (foodLocation != null ? !foodLocation.equals(infoBlock.foodLocation) : infoBlock.foodLocation != null)
            return false;
        if (myAntLocation != null ? !myAntLocation.equals(infoBlock.myAntLocation) : infoBlock.myAntLocation != null)
            return false;
        if (!myAnts.equals(infoBlock.myAnts)) return false;
        if (myHillLocation != null ? !myHillLocation.equals(infoBlock.myHillLocation) : infoBlock.myHillLocation != null)
            return false;
        if (!myHills.equals(infoBlock.myHills)) return false;
        if (playerColor != null ? !playerColor.equals(infoBlock.playerColor) : infoBlock.playerColor != null)
            return false;
        if (rockLocation != null ? !rockLocation.equals(infoBlock.rockLocation) : infoBlock.rockLocation != null)
            return false;
        if (!rocks.equals(infoBlock.rocks)) return false;
        if (visible != null ? !visible.equals(infoBlock.visible) : infoBlock.visible != null) return false;
        return !(visibleBoard != null ? !visibleBoard.equals(infoBlock.visibleBoard) : infoBlock.visibleBoard != null);

    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + (visibleBoard != null ? visibleBoard.hashCode() : 0);
        result = 31 * result + (playerColor != null ? playerColor.hashCode() : 0);
        result = 31 * result + (myAnts.hashCode());
        result = 31 * result + (myAntLocation != null ? myAntLocation.hashCode() : 0);
        result = 31 * result + (enemyAnts.hashCode());
        result = 31 * result + (enemyAntLocation != null ? enemyAntLocation.hashCode() : 0);
        result = 31 * result + (rocks.hashCode());
        result = 31 * result + (rockLocation != null ? rockLocation.hashCode() : 0);
        result = 31 * result + (food.hashCode());
        result = 31 * result + (foodLocation != null ? foodLocation.hashCode() : 0);
        result = 31 * result + (myHills.hashCode());
        result = 31 * result + (myHillLocation != null ? myHillLocation.hashCode() : 0);
        result = 31 * result + (enemyHills.hashCode());
        result = 31 * result + (enemyHillLocation != null ? enemyHillLocation.hashCode() : 0);
        result = 31 * result + (visible != null ? visible.hashCode() : 0);
        return result;
    }
}
