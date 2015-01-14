package Model;

import Controller.GameRunner;
import Players.Player;

import java.awt.*;
import java.util.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Board {
    private final int width; //width of the board (x direction)
    private final int height; //height of the board (y direction)
    private int turnCount = 0; //what turn it is, starts with 0
    private int numPlayersLeft; //number of players who have not been eliminated from the game
    private final boolean globalVision; //if true all ants can see the entire board
    private Ant[][] antBoard; //contains all the ants on the board
    private Hill[][] hillBoard; //contains all the ant hills on the board
    private Rock[][] rockBoard; //contains all rocks on the board
    private Food[][] foodBoard; //contains all food on the board
    private Point[] foodSeeds; //contains all the possible points that food can spawn in over the course of the game
    private ArrayList<Integer> remainingSeeds = new ArrayList<Integer>();  //contains all the seeds that still can have food spawned in. resets to the full seed list when emptied.
    private HashMap<Point, HashSet<Point>> visibility = new HashMap<Point, HashSet<Point>>(); //a map from a point on the board to a set of all the points on the board that can be seen from that point
    private final HashMap<Color, Integer> antsQueued = new HashMap<Color, Integer>(); //a map that keeps track of the number of ants that are queued to be spawned in for each player
    private int numFood; //the actual amount of food present on the board at any given time
    private final double foodDensity; //used to control how much food is present on the board at the beginning of any turn
    private final int visionRadius; //vision radius, used to calculate vision
    private final Player[] placing; //keeps track of the order that players are eliminated in, index 0 is the winner, index[numplayers - 1] is the first player eliminated
    private final HashMap<Color, Integer> whenEliminated; //maps players to the turn they were eliminated on, winner is not included in the hashmap

    public Board(int width, int height, int numPlayers, int visionRadius, boolean globalVision, Ant[][] antBoard, Hill[][] hillBoard, Rock[][] rockBoard, Point[] foodSeeds) {
        this.width = width;
        this.height = height;
        this.numPlayersLeft = numPlayers;
        this.placing = new Player[numPlayers];
        whenEliminated = new HashMap<Color, Integer>();
        this.globalVision = globalVision;
        this.visionRadius = visionRadius;
        this.antBoard = antBoard;
        this.hillBoard = hillBoard;
        this.rockBoard = rockBoard;
        this.foodBoard = new Food[width][height];
        this.foodSeeds = foodSeeds;
        for (int i = 0; i < foodSeeds.length; i++) {
            remainingSeeds.add(i);
        }
        this.foodDensity = Math.ceil(0.55 * Math.pow(height * width, (1.0 / 3)));
        generateFood();
        setVisibility();
    }

    public Board(Board board) {
        this.width = board.width;
        this.height = board.height;
        this.turnCount = board.turnCount;
        this.numPlayersLeft = board.numPlayersLeft;
        this.placing = board.placing;
        this.whenEliminated = board.whenEliminated;
        this.antBoard = new Ant[width][height];
        this.hillBoard = new Hill[width][height];
        this.rockBoard = new Rock[width][height];
        this.foodBoard = new Food[width][height];
        this.foodSeeds = board.foodSeeds;
        this.numFood = board.numFood;
        this.globalVision = board.globalVision;
        this.visionRadius = board.visionRadius;
        this.foodDensity = board.foodDensity;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (board.antBoard[i][j] != null) {
                    this.antBoard[i][j] = new Ant(board.antBoard[i][j]);
                }
                if (board.rockBoard[i][j] != null) {
                    this.rockBoard[i][j] = new Rock(board.rockBoard[i][j]);
                }
                if (board.foodBoard[i][j] != null) {
                    this.foodBoard[i][j] = new Food(board.foodBoard[i][j]);
                }
                if (board.hillBoard[i][j] != null) {
                    this.hillBoard[i][j] = new Hill(board.hillBoard[i][j]);
                }
            }
        }
        if (board.remainingSeeds != null) {
            for (int i = 0; i < board.remainingSeeds.size(); i++) {
                this.remainingSeeds.add(board.remainingSeeds.get(i));
            }
        }
        this.visibility = board.visibility;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getVisionRadius(){
        return this.visionRadius;
    }

    public boolean getGlobalVision() {
        return this.globalVision;
    }

    public Rock[][] getRocks() {
        Rock[][] returnRockBoard = new Rock[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (rockBoard[i][j] != null) {
                    returnRockBoard[i][j] = new Rock(rockBoard[i][j]);
                }
            }
        }
        return returnRockBoard;
    }

    public Food[][] getFood() {
        Food[][] returnFoodBoard = new Food[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (foodBoard[i][j] != null) {
                    returnFoodBoard[i][j] = new Food(foodBoard[i][j]);
                }
            }
        }
        return returnFoodBoard;
    }

    public Ant[][] getAnts() {
        Ant[][] returnAntBoard = new Ant[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (antBoard[i][j] != null) {
                    returnAntBoard[i][j] = new Ant(antBoard[i][j]);
                }
            }
        }
        return returnAntBoard;
    }

    public Hill[][] getHills() {
        Hill[][] returnHillBoard = new Hill[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (hillBoard[i][j] != null) {
                    returnHillBoard[i][j] = new Hill(hillBoard[i][j]);
                }
            }
        }
        return returnHillBoard;
    }

    public int getTurnCount() {
        return this.turnCount;
    }

    //Generates food on the map using food seeds, called at the end of each turn in creating the board for the next turn
    private void generateFood() {
        Random r = new Random();
        int attempts = 0;
        int bound;
        while (numFood < (int) Math.ceil(numPlayersLeft/Math.log(2 * numPlayersLeft) * foodDensity)) {
            if (remainingSeeds.size() == 0) {
                for (int i = 0; i < foodSeeds.length; i++) {
                    remainingSeeds.add(i);
                }
            }
            bound = remainingSeeds.size();
            int randomIndex = r.nextInt(bound);
            int index = remainingSeeds.get(randomIndex);
            Point foodLocation = new Point(foodSeeds[index]);
            if (foodBoard[(int) foodLocation.getX()][(int) foodLocation.getY()] == null && antBoard[(int) foodLocation.getX()][(int) foodLocation.getY()] == null) {
                foodBoard[(int) foodLocation.getX()][(int) foodLocation.getY()] = new Food(foodLocation);
                remainingSeeds.remove(randomIndex);
                numFood++;
            } else {
                attempts++;
            }
            if (attempts > 4 * remainingSeeds.size() + 10) {
                if (remainingSeeds.size() < foodSeeds.length) {
                    remainingSeeds.clear();
                    for (int i = 0; i < foodSeeds.length; i++) {
                        remainingSeeds.add(i);
                    }
                    generateFood();
                } else {
                    break;
                }
            }
        }
    }

    //Crude visibility calculation, sets visibility for each square of the map
    private void setVisibility() {
        if (globalVision) {
            HashSet<Point> visible = getVisibleFromPoint(new Point(0, 0));
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    this.visibility.put(new Point(i, j), visible);
                }
            }
        } else {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Point p = new Point(i, j);
                    if (getVisibleFromPoint(p).isEmpty()) {
                        System.out.println("Nothing visible from (" + i + ", " + j + ")");
                    }
                    this.visibility.put(p, getVisibleFromPoint(p));
                }
            }
        }
    }

    //returns a set of all points that are visible from the given point
    public HashSet<Point> getVisibleFromPoint(Point p) {
        HashSet<Point> visible = new HashSet<Point>();
        if(visibility.containsKey(p)){
            return visibility.get(p);
        }
        else {
            if (globalVision) {
                for (int k = 0; k < width; k++) {
                    for (int l = 0; l < height; l++) {
                        visible.add(new Point(k, l));
                    }
                }
            } else {
                double centerX = p.getX() + 0.5;
                double centerY = p.getY() + 0.5;
                for (int degree = 0; degree < 360; degree += 1) {
                    double xMult = Math.cos(degree);
                    double yMult = Math.sin(degree);
                    for (double i = 0.5; i < visionRadius; i += 0.02) {
                        int pointXCoord = (int) Math.floor(centerX + xMult * i + width) % width;
                        int pointYCoord = (int) Math.floor(centerY + yMult * i + height) % height;
                        visible.add(new Point(pointXCoord, pointYCoord));
                        if (rockBoard[pointXCoord][pointYCoord] != null) {
                            break;
                        }
                    }
                    degree++;
                }
            }
            return visible;
        }
    }

    //Returns set of points representing all visible tiles on the map that the given player can see
    public HashSet<Point> getVisible(Color player) {
        HashSet<Point> visible = new HashSet<Point>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (globalVision) {
                    visible.add(new Point(i, j));
                } else if (antBoard[i][j] != null && antBoard[i][j].getColor().equals(player)) {
                    HashSet<Point> visibleTiles = getVisibleFromPoint(new Point(i, j));
                    for (Point p : visibleTiles) {
                        visible.add(new Point(p));
                    }
                } else if (hillBoard[i][j] != null && hillBoard[i][j].getColor().equals(player)) {
                    HashSet<Point> visibleTiles = getVisibleFromPoint(new Point(i, j));
                    for (Point p : visibleTiles) {
                        visible.add(new Point(p));
                    }
                }
            }
        }
        return visible;
    }

    //a map from points that the player can see to a set of points that the player can also already see and are visible from the key point
    public HashMap<Point, HashSet<Point>> getVisibleInfoForPlayer(Color player) {
        HashSet<Point> playerVision = getVisible(player);
        HashMap<Point, HashSet<Point>> returnVisibility = new HashMap<Point, HashSet<Point>>();
        for (Point p : playerVision) {
            HashSet<Point> viewableFromPoint = getVisibleFromPoint(p);
            HashSet<Point> visible = new HashSet<Point>();
            for (Point v : viewableFromPoint) {
                if (playerVision.contains(v)) {
                    visible.add(v);
                }
            }
            returnVisibility.put(p, visible);
        }
        return returnVisibility;
    }

    //The Object that Players AIs will be given and use to make their moves, allows control for vision and gives prebuilt data to make AI writing easier.
    public InfoBlock getInfoForPlayer(Color player) {
        HashSet<Ant> myAnts = new HashSet<Ant>();
        HashSet<Ant> enemyAnts = new HashSet<Ant>();
        HashSet<Rock> rocks = new HashSet<Rock>();
        HashSet<Food> food = new HashSet<Food>();
        HashSet<Hill> enemyHills = new HashSet<Hill>();
        HashSet<Hill> myHills = new HashSet<Hill>();
        HashSet<Color> playersLeft = new HashSet<Color>();
        Board visibleBoard = new Board(this);
        visibleBoard.foodSeeds = null;
        visibleBoard.remainingSeeds = null;
        visibleBoard.numFood = 0;
        HashSet<Point> visible = getVisible(player);
        visibleBoard.visibility = getVisibleInfoForPlayer(player);
        visibleBoard.antBoard = new Ant[width][height];
        visibleBoard.rockBoard = new Rock[width][height];
        visibleBoard.foodBoard = new Food[width][height];
        visibleBoard.hillBoard = new Hill[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (visible.contains(new Point(i, j))) {
                    if (antBoard[i][j] != null) {
                        playersLeft.add(antBoard[i][j].getColor());
                        visibleBoard.antBoard[i][j] = new Ant(antBoard[i][j]);
                        if (antBoard[i][j].getColor().equals(player)) {
                            myAnts.add(new Ant(antBoard[i][j]));
                        } else {
                            enemyAnts.add(new Ant(antBoard[i][j]));
                        }
                    }
                    if (rockBoard[i][j] != null) {
                        visibleBoard.rockBoard[i][j] = new Rock(rockBoard[i][j]);
                        rocks.add(new Rock(rockBoard[i][j]));
                    }
                    if (foodBoard[i][j] != null) {
                        visibleBoard.foodBoard[i][j] = new Food(foodBoard[i][j]);
                        food.add(new Food(foodBoard[i][j]));
                    }
                    if (hillBoard[i][j] != null) {
                        playersLeft.add(hillBoard[i][j].getColor());
                        visibleBoard.hillBoard[i][j] = new Hill(hillBoard[i][j]);
                        if (hillBoard[i][j].getColor().equals(player)) {
                            myHills.add(new Hill(hillBoard[i][j]));
                        } else {
                            enemyHills.add(new Hill(hillBoard[i][j]));
                        }
                    }
                }
            }
            visibleBoard.numPlayersLeft = playersLeft.size();
        }
        return new InfoBlock(height, width, player, myAnts, enemyAnts, rocks, food, myHills, enemyHills, visible, visibleBoard);
    }

    //checks if a the ant can move to the point on the given board
    private boolean isLegalMoveForAnt(Ant ant, Point destination, Ant[][] antBoard) {
        if (ant == null || destination == null) {
            return false;
        }

        Point initial = ant.getLocation();
        int destinationX = (int) destination.getX();
        int destinationY = (int) destination.getY();

        if (destinationX >= width || destinationX < 0 || destinationY >= height || destinationY < 0) {
            System.out.println("Out of bounds " + destination);
            return false;
        }

        if (destination.equals(initial)) {
            return true;
        }

        if (BoardGenerator.manhattanDistance(initial, destination) <= 1) {
            if (rockBoard[destinationX][destinationY] == null && (antBoard[destinationX][destinationY] == null || !antBoard[destinationX][destinationY].getColor().equals(ant.getColor()))) {
                return true;
            }
        }
        if (BoardGenerator.manhattanDistance(initial, destination) > 1) {
            return false;
        }
        if (rockBoard[destinationX][destinationY] != null) {
            return false;
        }
        if (antBoard[destinationX][destinationY] != null && antBoard[destinationX][destinationY].getColor().equals(ant.getColor())) {
            return false;
        }
        return false;
    }

    //returns true if the Move given contains moves for all of the ants of the color and no illegal moves
    private boolean isLegalMoveList(Move m) {
        Color player = m.getColor();
        if (m.getMoves() != null) {
            HashMap<Ant, Ant> moves = m.getMoves();
            Set<Ant> moveSetKey = moves.keySet();
            int numMoves = moveSetKey.size();
            HashSet<Point> targets = new HashSet<Point>();
            for (Ant start : moveSetKey) {
                if (targets.contains(moves.get(start).getLocation())) {
                    return false;
                } else {
                    targets.add(moves.get(start).getLocation());
                }
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (antBoard[i][j] != null && antBoard[i][j].getColor().equals(player)) {
                        numMoves--;
                    }
                }
            }
            if (numMoves == 0) {
                return true;
            }
            System.out.println("submitted the wrong number of moves");
        }
        return false;
    }

    //purges the given move of all illegal moves
    private Move removeIllegalMoves(Move m) {
        HashMap<Ant, Ant> legalMoves = new HashMap<Ant, Ant>();
        Color legalColor = m.getColor();
        if (m.getMoves() != null) {
            HashMap<Ant, Ant> moves = m.getMoves();
            Set<Ant> moveSetKey = moves.keySet();
            for (Ant start : moveSetKey) {
                Ant destination = moves.get(start);
                if (start.getColor().equals(destination.getColor()) && start.getColor().equals(m.getColor())) {
                    if (start.getLocation().equals(destination.getLocation())) {
                        if (antBoard[(int) start.getLocation().getX()][(int) start.getLocation().getY()] != null && antBoard[(int) start.getLocation().getX()][((int) start.getLocation().getY())].getColor().equals(start.getColor())) {
                            legalMoves.put(start, destination);
                        }
                    }
                }
            }
            for (Ant start : moveSetKey) {
                Ant destination = moves.get(start);
                if (start.getColor().equals(destination.getColor()) && start.getColor().equals(m.getColor())) {
                    if (antBoard[(int) start.getLocation().getX()][(int) start.getLocation().getY()] != null && antBoard[(int) start.getLocation().getX()][((int) start.getLocation().getY())].getColor().equals(start.getColor())) {
                        if (rockBoard[((int) destination.getLocation().getX())][((int) destination.getLocation().getY())] == null) {
                            if (BoardGenerator.manhattanDistance(start.getLocation(), destination.getLocation()) <= 1) {
                                if (!legalMoves.containsKey(start) && !legalMoves.containsValue(destination)) {
                                    legalMoves.put(start, destination);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Move(legalColor, legalMoves);
    }

    //adds to the given move a stationary move for all unmoved ants
    private Move addUnmovedAntsToMove(Move m) {
        HashMap<Ant, Ant> moves = m.getMoves();
        Color legalColor = m.getColor();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (antBoard[i][j] != null && antBoard[i][j].getColor().equals(legalColor)) {
                    if (!moves.containsKey(new Ant(legalColor, new Point(i, j)))) {
                        Ant stationary = new Ant(legalColor, new Point(i, j));
                        moves.put(stationary, stationary);
                    }
                }
            }
        }
        return new Move(legalColor, moves);
    }

    //My attempt to save us all from ourselves.
    private Move fixTheFuckingMove(Move m) {
        while (!isLegalMoveList(m)) {
            m = removeIllegalMoves(m);
            m = addUnmovedAntsToMove(m);
        }
        return m;
    }

    //Destroy all ants and ant hills of a given color on the map
    private void wipeAntsOfColor(Color color, Ant[][] antBoard) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (antBoard[i][j] != null && antBoard[i][j].getColor().equals(color)) {
                    antBoard[i][j] = null;
                }
            }
        }
    }

    //pretty print for an ant move
    public void printMove(Point startLoc, Point goalLoc) {
        System.out.println("(" + startLoc.getX() + ", " + startLoc.getY() + ") -> (" + goalLoc.getX() + ", " + goalLoc.getY() + ")");
    }

    //Move the game to the next turn
    public void nextTurn(ArrayList<Move> moveList) {
        Ant[][] resultAntBoard = new Ant[width][height];
        Ant[][] tempAntBoard = new Ant[width][height];
        HashMap<Point, Point> reversed = new HashMap<Point, Point>();
        HashSet<Color> potentiallyEliminatedPlayers = new HashSet<Color>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (antBoard[i][j] != null) {
                    tempAntBoard[i][j] = new Ant(antBoard[i][j]);
                }
            }
        }
        for (Move m : moveList) {
            m = fixTheFuckingMove(m);
            if (m != null) {
                HashMap<Ant, Ant> moveSet = m.getMoves();
                Set<Ant> moveSetKey = moveSet.keySet();
                Color curPlayer = m.getColor();
                for (Ant initial : moveSetKey) {
                    Ant destination = moveSet.get(initial);
                    Point destinationPoint = destination.getLocation();
                    int destinationX = (int) destinationPoint.getX();
                    int destinationY = (int) destinationPoint.getY();
                    Point initialPoint = initial.getLocation();
                    int initialX = (int) initialPoint.getX();
                    int initialY = (int) initialPoint.getY();
                    //in case the player submitted an ant move for the other player
                    if (initial.getColor().equals(destination.getColor()) && initial.getColor().equals(curPlayer)) {
                        //makes sure the move is consistent with board state and legal
                        if (tempAntBoard[initialX][initialY] != null && tempAntBoard[initialX][initialY].equals(initial) && isLegalMoveForAnt(initial, destination.getLocation(), resultAntBoard)) {
                            //makes sure another ant hasn't already moved to the square
                            if (resultAntBoard[destinationX][destinationY] != null) {// && (reversed.get(destinationPoint) == null || !reversed.get(destinationPoint).equals(initialPoint))) {
                                //ant of another color was there already, wipe each other out.
                                if (!resultAntBoard[destinationX][destinationY].getColor().equals(tempAntBoard[initialX][initialY].getColor())) {
                                    //BLACK is default value to denote battle fought on square, please do not use for any ant colony.
                                    resultAntBoard[destinationX][destinationY] = new Ant(Color.BLACK, new Point(destinationPoint));
                                    tempAntBoard[initialX][initialY] = null;
                                    tempAntBoard[destinationX][destinationY] = new Ant(Color.BLACK, new Point(destinationPoint));
                                }
                                //ant of the same color was already there, should maintain position
                                else {
                                    resultAntBoard[initialX][initialY] = antBoard[initialX][initialY];
                                }
                            } else if (reversed.get(initialPoint) != null && reversed.get(initialPoint).equals(destinationPoint)) {
                                //if those two ants are the same color, neither moves
                                if (resultAntBoard[initialX][initialY].getColor().equals(tempAntBoard[initialX][initialY].getColor())) {
                                    resultAntBoard[destinationX][destinationY] = new Ant(resultAntBoard[initialX][initialY].getColor(), new Point(destinationX, destinationY));
                                    resultAntBoard[initialX][initialY] = new Ant(tempAntBoard[initialX][initialY]);
                                    tempAntBoard[initialX][initialY] = null;
                                }
                                //if those two ants are different colors, they kill each other but no combat square is created
                                else {
                                    resultAntBoard[destinationX][destinationY] = null;
                                    resultAntBoard[initialX][initialY] = null;
                                    tempAntBoard[initialX][initialY] = null;
                                }
                            }
                            //make the move and add it to reversed so ants swapping places can be detected
                            else {
                                resultAntBoard[destinationX][destinationY] = destination;
                                reversed.put(new Point(destinationPoint), new Point(initialPoint));
                                tempAntBoard[initialX][initialY] = null;
                            }
                            //ant stays stationary
                        } else {
                            System.out.println(" false (" + initialX + ", " + initialY + ")");
                            resultAntBoard[initialX][initialY] = initial;
                            tempAntBoard[initialX][initialY] = null;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (resultAntBoard[i][j] != null) {
                    //ant fight
                    if (resultAntBoard[i][j].getColor().equals(Color.BLACK)) {
                        resultAntBoard[i][j] = null;
                    } else if (tempAntBoard[i][j] != null) {
                        resultAntBoard[i][j] = new Ant(tempAntBoard[i][j]);
                    }
                    //ant found food
                    if (foodBoard[i][j] != null && resultAntBoard[i][j] != null) {
                        Color c = resultAntBoard[i][j].getColor();
                        if (antsQueued.get(c) != null) {
                            //int numAnts = antsQueued.get(c);
                            antsQueued.put(c, antsQueued.get(c) + 1);
                        } else {
                            antsQueued.put(c, 1);
                        }
                        foodBoard[i][j] = null;
                        numFood--;
                    }
                    //checking if a colony should die
                    if (hillBoard[i][j] != null && resultAntBoard[i][j] != null && !resultAntBoard[i][j].getColor().equals(hillBoard[i][j].getColor())) {
                        Color hillColor = hillBoard[i][j].getColor();
                        potentiallyEliminatedPlayers.add(hillColor);
                        hillBoard[i][j] = new Hill(resultAntBoard[i][j].getColor(), new Point(i, j));
                        ArrayList<Color> toBeRemoved = new ArrayList<Color>();
                        for (Color c : potentiallyEliminatedPlayers) {
                            for (int k = 0; k < width; k++) {
                                for (int l = 0; l < height; l++) {
                                    if (hillBoard[k][l] != null && hillBoard[k][l].getColor().equals(c)) {
                                    	toBeRemoved.add(c);                  
                                    }
                                }
                            }
                        }
                        for(Color c : toBeRemoved){
                        	potentiallyEliminatedPlayers.remove(c);
                        }
                    }
                }
            }
        }

        //eliminating colonies that no longer have a hill
        if (!potentiallyEliminatedPlayers.isEmpty()) {
            for (Color c : potentiallyEliminatedPlayers) {
                String eliminatedPlayer = "";
                Player p = GameRunner.getPlayerByColor(c);
                if (p != null) {
                    eliminatedPlayer = p.toString();
                }
                whenEliminated.put(p != null ? p.getColor() : null, turnCount);
                placing[numPlayersLeft - 1] = p;
                System.out.println(eliminatedPlayer + " ant colony eliminated");
                GameRunner.removePlayer(c);
                wipeAntsOfColor(c, resultAntBoard);
                numPlayersLeft--;
                if(numPlayersLeft == 1){
                    for(int i = 0; i < width; i++){
                        for(int j = 0; j < height; j++){
                            if(hillBoard[i][j] != null){
                                placing[0] = GameRunner.getPlayerByColor(hillBoard[i][j].getColor());
                            }
                        }
                    }
                }
            }
        }

        //trying to spawn in new ants
        HashMap<Color, ArrayList<Hill>> antSpawnMap = new HashMap<Color, ArrayList<Hill>>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (hillBoard[i][j] != null && resultAntBoard[i][j] == null) {
                    ArrayList<Hill> hillList = antSpawnMap.get(hillBoard[i][j].getColor());
                    if(hillList == null) {
                        hillList = new ArrayList<Hill>();
                    }
                    hillList.add(hillBoard[i][j]);
                    antSpawnMap.put(hillBoard[i][j].getColor(), hillList);
                }
            }
        }
        for(Color c: antSpawnMap.keySet()){
            ArrayList<Hill> hillList = antSpawnMap.get(c);
            Random r = new Random();
            while(antsQueued.get(c) != null && antsQueued.get(c) > 0 && hillList != null) {
                int index = r.nextInt(hillList.size());
                antsQueued.put(c, antsQueued.get(c) - 1);
                Hill spawnHill = hillList.remove(index);
                Point spawnLocation = spawnHill.getLocation();
                resultAntBoard[((int) spawnLocation.getX())][((int) spawnLocation.getY())] = new Ant(c, spawnLocation);
                if(hillList.size() == 0){
                    break;
                }
            }
        }
        //resetting everything to the updated values and calculating core information
        antBoard = resultAntBoard;
        generateFood();
        turnCount++;
    }

    public GameInfoBlock getGameInfo(int turnTime, Board startingBoard, ArrayList<Player> startingPlayers, boolean earlyTermination) {
        return new GameInfoBlock(width, height, startingPlayers, whenEliminated, turnCount, placing, turnTime, globalVision, visionRadius, startingBoard, earlyTermination);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;

        Board board = (Board) o;

        if (Double.compare(board.foodDensity, foodDensity) != 0) return false;
        if (globalVision != board.globalVision) return false;
        if (height != board.height) return false;
        if (numFood != board.numFood) return false;
        if (numPlayersLeft != board.numPlayersLeft) return false;
        if (turnCount != board.turnCount) return false;
        if (width != board.width) return false;
        if (antsQueued != null ? !antsQueued.equals(board.antsQueued) : board.antsQueued != null) return false;
        if (!Arrays.equals(foodSeeds, board.foodSeeds)) return false;
        if (remainingSeeds != null ? !remainingSeeds.equals(board.remainingSeeds) : board.remainingSeeds != null)
            return false;
        return !(visibility != null ? !visibility.equals(board.visibility) : board.visibility != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = width;
        result = 31 * result + height;
        result = 31 * result + turnCount;
        result = 31 * result + numPlayersLeft;
        result = 31 * result + (globalVision ? 1 : 0);
        result = 31 * result + (foodSeeds != null ? Arrays.hashCode(foodSeeds) : 0);
        result = 31 * result + (remainingSeeds != null ? remainingSeeds.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + (antsQueued != null ? antsQueued.hashCode() : 0);
        result = 31 * result + numFood;
        temp = Double.doubleToLongBits(foodDensity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}