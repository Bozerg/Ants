package Model;

import Controller.GameRunner;

import java.awt.*;
import java.util.*;
import java.util.Queue;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class BoardGenerator {
    private static int height; //height of the map (y direction)
    private static int width; //width of the map (x direction)
    private static boolean globalVision; //true if each player can see the whole map at all times, false otherwise
    private static int visionRadius; //how far the ant can see in all directions
    private final int numPlayers; //number of ant colonies on the map
    private Ant[][] antBoard; //all the ants on the map
    private Hill[][] hillBoard; //all the ant hills on the map
    private Rock[][] rockBoard; //all the rocks on the map
    private Point[] foodSeeds; //array of all possible food spawns on the map
    private HashSet<Point> foodSeedHash = new HashSet<Point>(); //set of locations of all possible food spawns on the map
    private final HashSet<Hill> hillLocations = new HashSet<Hill>(); //set of all ant hills on the map

    //tuning parameters, feel free to mess with them and see what happens
    private final int ROCK_NUM_PASSES = 5; //Number of times to loop over the board creating rocks
    private final double ROCK_RAW_PROBABILITY = 0.01; //probability that a rock will be spawned in on any given square
    private final double ROCK_CLUSTER_PROBABILITY = 0.08; //probability that a rock will be spawned in for each neighbor rock
    private final double MAX_ROCK_DENSITY = 0.15; //max percent of the board that can be rocks
    private final double MIN_ROCK_DENSITY = 0.05; //min percent of the board that can be rocks
    private final int MAX_ROCK_FAILURES = 5; //number of times that the generator will attempt to generate rocks before shifting the number of passes up or down to hit the allowable density zone
    private final double FOOD_SEED_DENSITY = 0.05; //percent of the board that can potentially have food spawned in on it
    private final double HILL_DISTANCE_FACTOR = 0.4; //used to determine how far apart hills need to be, see call to generateHills method for usage
    private final double HILL_DISTANCE_SHIFT = 10;  //used to determine how far apart hills need to be, see call to generateHills method for usage
    private final int STARTING_NUMBER_OF_ANTS = 5; //number of ants each colony starts with
    private final int MAX_HILL_ATTEMPTS = 25; //number of times the generator will attempt to create hills before restarting the whole process.  Note, if your map is too small for all the hills to legally spawn in you will blow your stack.

    public BoardGenerator(int width, int height, int numPlayers, boolean globalVision, int visionRadius) {
        BoardGenerator.width = width;
        BoardGenerator.height = height;
        BoardGenerator.globalVision = globalVision;
        BoardGenerator.visionRadius = visionRadius;
        this.numPlayers = numPlayers;
        generateRocks(ROCK_NUM_PASSES, ROCK_RAW_PROBABILITY, ROCK_CLUSTER_PROBABILITY);
        generateHills((int) Math.floor((Math.pow((height * height + width * width), (HILL_DISTANCE_FACTOR)) / numPlayers) + HILL_DISTANCE_SHIFT));
        generateFoodSeeds(Math.max((int) Math.floor(height * width * FOOD_SEED_DENSITY), numPlayers * 15));
        generateAnts(STARTING_NUMBER_OF_ANTS);
    }

    //creates and returns a new board using all the generated components
    public Board generateBoard() {
        return new Board(width, height, numPlayers, visionRadius, globalVision, antBoard, hillBoard, rockBoard, foodSeeds);
    }

    //generates all the rocks on the map
    private void generateRocks(int numPasses, double rawProb, double clusterProb) {
        System.out.println("Generating Rocks");
        Rock[][] rocks = rockGenerator(numPasses, rawProb, clusterProb);
        double rockDensity = densityCalculator(rocks);
        int failures = 0;
        while (rockDensity < MIN_ROCK_DENSITY || rockDensity > MAX_ROCK_DENSITY) {
            failures++;
            if (rockDensity > MAX_ROCK_DENSITY) {
                if (failures > MAX_ROCK_FAILURES) {
                    System.out.println("Rock Density too high, reducing number of passes");
                    if (numPasses > 1) {
                        numPasses--;
                    }
                    rocks = rockGenerator(numPasses, rawProb, clusterProb);
                    failures = 0;
                } else {
                    rocks = rockGenerator(numPasses, ROCK_RAW_PROBABILITY, ROCK_CLUSTER_PROBABILITY);
                }
            } else if (rockDensity < MIN_ROCK_DENSITY) {
                if (failures > MAX_ROCK_FAILURES) {
                    System.out.println("Rock Density too low, increasing number of passes");
                    numPasses++;
                    rocks = rockGenerator(numPasses, rawProb, clusterProb);
                    failures = 0;
                } else {
                    rocks = rockGenerator(numPasses, rawProb, clusterProb);
                }
            }
            rockDensity = densityCalculator(rocks);
        }
        this.rockBoard = rocks;
    }

    //generates all the rocks on the map, is called recursively
    private Rock[][] rockGenerator(int numPasses, double rawProb, double clusterProb) {
        Rock[][] rocks = new Rock[width][height];
        Random r = new Random();
        while (numPasses > 0) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    double prob = rawProb;
                    if (rocks[(i + 1 + width) % width][j] != null) {
                        prob += clusterProb;
                    }
                    if (rocks[(i - 1 + width) % width][j] != null) {
                        prob += clusterProb;
                    }
                    if (rocks[i][(j + 1 + height) % height] != null) {
                        prob += clusterProb;
                    }
                    if (rocks[i][(j - 1 + height) % height] != null) {
                        prob += clusterProb;
                    }

                    if (r.nextDouble() < prob) {
                        rocks[i][j] = new Rock(new Point(i, j));
                    }
                }
            }
            numPasses--;
        }
        return rocks;
    }

    //calculates the number of rocks on the map, used to check density
    private int numRocks(Rock[][] rocks) {
        int numRocks = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (rocks[i][j] != null) {
                    numRocks++;
                }
            }
        }
        return numRocks;
    }

    private double densityCalculator(Rock[][] rocks) {
        int numRocks = numRocks(rocks);
        return (numRocks) / (double) (height * width);
    }

    private void generateFoodSeeds(int numSeeds) {
        System.out.println("Generating Food Seeds");
        HashSet<Point> seeds = new HashSet<Point>();
        int seedsCalculated = 0;
        Random r = new Random();
        int failures = 0;
        int decrement = 0;
        while (seedsCalculated < numSeeds) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            Point testPoint = new Point(x, y);
            boolean legal = false;
            if (rockBoard[x][y] == null) {
                legal = true;
                for (Hill hill : hillLocations) {
                    if (manhattanDistance(hill.getLocation(), testPoint) <= 5) {
                        legal = false;
                    }
                }
                if (legal) {
                    for (Point seed : seeds) {
                        if (manhattanDistance(seed, testPoint) <= ((height + width) - decrement)) {
                            legal = false;
                            failures++;
                            break;
                        }
                    }
                }
            }
            if (legal) {
                seeds.add(testPoint);
                seedsCalculated++;
                failures = 0;
            }
            if(failures > (height * width)){
                decrement++;
                failures = 0;
            }
        }
        this.foodSeedHash = seeds;
        Point[] returnSeeds = new Point[seeds.size()];
        int count = 0;
        for (Point seed : seeds) {
            returnSeeds[count] = seed;
            count++;
        }
        System.out.println("Food Seeds Generated");
        this.foodSeeds = returnSeeds;
    }

    private void generateHills(int minDistanceToNearestHill) {
        System.out.println("Generating Ant Hills");
        int numResets = 0;
        HashSet<Point> hills = new HashSet<Point>();
        HashSet<Point> tested = new HashSet<Point>();
        int hillsLocated = 0;
        int hillsTested = 0;
        Random r = new Random();
        boolean failed = false;
        while (hillsLocated < numPlayers) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            Point testPoint = new Point(x, y);
            if (!tested.contains(testPoint)) {
                if (hillCheck(testPoint, hills, minDistanceToNearestHill) && allHillsConnected(hills)) {
                    hills.add(testPoint);
                    hillsLocated++;
                }
                tested.add(testPoint);
                hillsTested++;
            }
            if (hillsTested > (width * height * .85)) {
                hills = new HashSet<Point>();
                tested = new HashSet<Point>();
                hillsTested = 0;
                hillsLocated = 0;
                numResets++;
                if (numResets > MAX_HILL_ATTEMPTS) {
                    System.out.println("Couldn't generate Ant Hills after " + MAX_HILL_ATTEMPTS + " attempts.  Regenerating Entire Map");
                    failed = true;
                    generateRocks(ROCK_NUM_PASSES, ROCK_RAW_PROBABILITY, ROCK_CLUSTER_PROBABILITY);
                    generateHills((int) Math.floor((Math.pow((height * height + width * width), (HILL_DISTANCE_FACTOR)) / numPlayers) + HILL_DISTANCE_SHIFT));
                    break;
                }
            }
        }
        if(!failed) {
            Hill[][] returnHills = new Hill[width][height];
            Iterator h = hills.iterator();
            int index = 0;
            ArrayList<Color> colors = GameRunner.getColors();
            while (h.hasNext()) {
                Point p = (Point) h.next();
                Hill hill = new Hill(colors.get(index), p);
                returnHills[((int) p.getX())][((int) p.getY())] = hill;
                hillLocations.add(new Hill(hill));
                index++;
            }
            System.out.println("Hills generated");
            this.hillBoard = returnHills;
        }
    }

    //checks to see that a point is suitable for a new hill
    private boolean hillCheck(Point check, HashSet<Point> hills, int minDistanceAllowed) {
        for (Point p : hills) {
            if (manhattanDistance(p, check) < minDistanceAllowed) {
                return false;
            }
        }
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                int x = (int) ((i + width + check.getX()) % width);
                int y = (int) ((j + height + check.getY()) % height);
                Point testPoint = new Point(x, y);
                if (rockBoard[x][y] != null && manhattanDistance(testPoint, check) <= (2 + Math.ceil(Math.sqrt(numPlayers - 1)))){
                    return false;
                }
            }
        }
        return true;
    }

    //calculates manhattan distance on the surface of a taurus (distance that takes into account wrapping around from E to W and from N to S)
    public static int manhattanDistance(Point a, Point b) {
        int aX = (int) a.getX();
        int aY = (int) a.getY();
        int bX = (int) b.getX();
        int bY = (int) b.getY();
        int xDistance = Math.min(width - Math.abs(aX - bX), Math.abs(aX - bX));
        int yDistance = Math.min(height - Math.abs(aY - bY), Math.abs(aY - bY));
        return xDistance + yDistance;
    }
    
    public static double euclideanDistance(Point a, Point b) {
    	int diffX = Math.abs((int)a.getX() - (int)b.getX());
    	int diffY = Math.abs((int)a.getY() - (int)b.getY());
        return Math.sqrt(
        		Math.pow(Math.min(diffX, width-diffX), 2) + 
        		Math.pow(Math.min(diffY, height-diffY), 2)
        		);
    }
    
    public static double toroidalXdiff(Point a, Point b) {
    	double aX = a.getX();
    	double bX = b.getX();
    	double naive = bX - aX;
    	if (naive > (double)width/2d) return bX - (aX + width); 
    	else if (naive < (double)width/-2d) return (bX + width) - aX;
    	else return naive;
    }
    
    public static double toroidalYdiff(Point a, Point b) {
    	double aY = a.getY();
    	double bY = b.getY();
    	double naive = bY - aY;
    	if (naive > (double)height/2d) return bY - (aY + height); 
    	else if (naive < (double)height/-2d) return (bY + height) - aY;
    	else return naive;
    }

    //checks to see that all hills can be reached from all other hills so the game is winnable/losable
    private boolean allHillsConnected(HashSet<Point> hillLocations) {
        if (hillLocations.size() < 2) {
            return true;
        }
        Iterator i = hillLocations.iterator();
        ArrayList<Point> unconnected = new ArrayList<Point>();
        while (i.hasNext()) {
            unconnected.add((Point) i.next());
        }
        HashSet<Point> explored = new HashSet<Point>();
        Queue<Point> frontier = new LinkedList<Point>();
        if (!unconnected.isEmpty()) {
            frontier.add(unconnected.get(0));
            unconnected.remove(0);
        }
        while (!frontier.isEmpty()) {
            Point curNode = frontier.poll();
            if (unconnected.contains(curNode)) {
                unconnected.remove(curNode);
            }
            if (unconnected.isEmpty()) {
                return true;
            }
            int x = (int) curNode.getX();
            int y = (int) curNode.getY();
            if (rockBoard[(x + 1 + width) % width][y] == null && !frontier.contains(new Point((x + 1 + width) % width, y)) && !explored.contains(new Point((x + 1 + width) % width, y))) {
                frontier.add(new Point((x + 1 + width) % width, y));
            }
            if (rockBoard[(x - 1 + width) % width][y] == null && !frontier.contains(new Point((x - 1 + width) % width, y)) && !explored.contains(new Point((x - 1 + width) % width, y))) {
                frontier.add(new Point((x - 1 + width) % width, y));
            }
            if (rockBoard[x][(y + 1 + height) % height] == null && !frontier.contains(new Point(x, (y + 1 + height) % height)) && !explored.contains(new Point(x, (y + 1 + height) % height))) {
                frontier.add(new Point(x, (y + 1 + height) % height));
            }
            if (rockBoard[x][(y - 1 + height) % height] == null && !frontier.contains(new Point(x, (y - 1 + height) % height)) && !explored.contains(new Point(x, (y - 1 + height) % height))) {
                frontier.add(new Point(x, (y - 1 + height) % height));
            }
            explored.add(curNode);
        }
        return false;
    }

    //generates the ants for each colony
    private void generateAnts(int numAntsPerColony) {
        System.out.println("Generating Ants");
        Random r = new Random();
        Iterator h = hillLocations.iterator();
        HashSet<Point> occupiedPoints = new HashSet<Point>();
        Ant[][] returnAnts = new Ant[width][height];
        while (h.hasNext()) {
            Hill current = (Hill) h.next();
            Point curLocation = current.getLocation();
            Ant ant = new Ant(current.getColor(), curLocation);
            occupiedPoints.add(curLocation);
            returnAnts[((int) curLocation.getX())][((int) curLocation.getY())] = ant;
            int numAntsPlaced = 1;
            while (numAntsPlaced < numAntsPerColony) {
                int distance = (int) Math.ceil(Math.sqrt(numAntsPerColony - 1)) - 1;
                int x = (r.nextInt(2 * distance + 1) - distance + (int) curLocation.getX() + width) % width;
                int y = (r.nextInt(2 * distance + 1) - distance + (int) curLocation.getY() + height) % height;
                Point testPoint = new Point(x, y);
                if (rockBoard[x][y] == null && !foodSeedHash.contains(testPoint) && !occupiedPoints.contains(testPoint)) {
                    ant = new Ant(current.getColor(), testPoint);
                    occupiedPoints.add(testPoint);
                    returnAnts[x][y] = ant;
                    numAntsPlaced++;
                }
            }
        }
        System.out.println("Ants Generated");
        this.antBoard = returnAnts;
    }
}
