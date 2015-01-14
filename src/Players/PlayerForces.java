package Players;

import Model.Ant;
import Model.Board;
import Model.BoardGenerator;
import Model.Food;
import Model.Hill;
import Model.Move;
import Model.Rock;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class PlayerForces extends Player {

	public static final int num_params = 4;
	private int width;
	private int height;
	private double[] weights;
	private double[] dist_scale;
	private double[] hilldist_scale;
	private final int max_turns_since_explored = 10;
	private boolean notSetUp = true;
	private HashMap<Point, Integer> turns_since_explored;
	private HashMap<Point, Rock> rocks;
	private HashSet<Point> enemy_hills_seen;
	
    public PlayerForces(Color color, int playerNumber, int turnTime, String name){
        super(color, playerNumber, turnTime, name);
        initWeights();
    }

    public PlayerForces(Color color, int playerNumber, int turnTime) {
        super(color, playerNumber, turnTime, "Forces");
        initWeights();
    }
    
    public PlayerForces(Color color, int playerNumber, int turnTime, double[] weights) {
    	super(color, playerNumber, turnTime, "Forces");
    	initWeights(weights);
    }
    
    private void setUp(Board visible) {
    	notSetUp = false;
    	width = visible.getWidth();
    	height = visible.getHeight();
    	turns_since_explored = new HashMap<Point, Integer>();
    	rocks = new HashMap<Point, Rock>();
    	enemy_hills_seen = new HashSet<Point>();
    	for (int i=0; i<width; i++) {
    		for (int j=0; j<height; j++) {
    			this.turns_since_explored.put(new Point(i, j), 2);
    		}
    	}
    }
    
    private void initWeights() {
    	this.weights = new double[num_params];
    	this.dist_scale = new double[num_params];
    	this.hilldist_scale = new double[num_params];
    	// other own ants
//    	this.attract_weights[0] = 0;
//    	this.repel_weights[0] = 50;
//    	this.attract_scales[0] = 0.5;
//    	this.repel_scales[0] = 1;
    	// food
    	this.weights[0] = -16.5; // the constant for this particular item
    	this.dist_scale[0] = 1.576313; // higher values means force decays over distance more quickly
    	this.hilldist_scale[0] = -0.5231548; // higher positive values means force decays with hill distance more quickly; negative values mean force increases with hill distance
    	// enemy ants
    	this.weights[1] = 44;
    	this.dist_scale[1] = 0.964;
    	this.hilldist_scale[1] = 2.5377;
    	// own hill
    	this.weights[2] = 33;
    	this.dist_scale[2] = -0.07490;
    	this.hilldist_scale[2] = 1.97021;
    	// enemy hill
    	this.weights[3] = 2;
    	this.dist_scale[3] = -0.0624486;
    	this.hilldist_scale[3] = 0.967443;
    }
    
	private void initWeights(double[] inweights) {
		assert(inweights.length == num_params);
		this.weights = new double[num_params];
		this.dist_scale = new double[num_params];
		this.hilldist_scale = new double[num_params];
		for (int i=0; i<inweights.length/3; i++) {
			for (int j=0; j<4; j++) {
				this.weights[i] = inweights[i*3];
				this.dist_scale[i] = inweights[i*3 + 1];
				this.hilldist_scale[i] = inweights[i*3 + 2];
			}
		}
		this.hilldist_scale[2] = 0;
	}
    
    private double attractionX(int what, Point here, Point there, int dist_to_hill) {
    	double tXd = BoardGenerator.toroidalXdiff(here, there);
    	if (tXd == 0) return 0;
    	double attractX = this.weights[what]/(Math.pow(Math.abs(tXd), this.dist_scale[what]) * (Math.pow(dist_to_hill, this.hilldist_scale[what])));
    	double sign = tXd/Math.abs(tXd);
    	return sign*attractX + Math.random();
    }
    
    private double attractionY(int what, Point here, Point there, int dist_to_hill) {
    	double tYd = BoardGenerator.toroidalYdiff(here, there);
    	if (tYd == 0) return 0;
    	double attractY = this.weights[what]/(Math.pow(Math.abs(tYd), this.dist_scale[what]) * (Math.pow(dist_to_hill, this.hilldist_scale[what])));
    	double sign = tYd/Math.abs(tYd);
    	return sign*attractY + Math.random();
    }
    
    private double yen_for_travelX(Point here, Point there, int t) {
    	double tXd = BoardGenerator.toroidalXdiff(here, there);
    	if (tXd == 0) return 0;
    	double sign = tXd/Math.abs(tXd);
    	return (double)t*sign/Math.pow(0.5, -Math.abs(tXd));
    }
    
    private double yen_for_travelY(Point here, Point there, int t) {
    	double tYd = BoardGenerator.toroidalYdiff(here, there);
    	if (tYd == 0) return 0;
    	double sign = tYd/Math.abs(tYd);
    	return (double)t*sign/Math.pow(0.5, -Math.abs(tYd));
    }
    
    public void run() {
    	Board visibleBoard = currentState.getVisibleBoard();
    	if (notSetUp) setUp(visibleBoard);
    	HashMap<Point, Rock> newrocks = currentState.getRockLocation();
    	for (Point p : visibleBoard.getVisible(getColor())) {
    		if (newrocks.containsKey(p)) {
    			rocks.put(p, newrocks.get(p));
    			this.turns_since_explored.put(p, 0);
    		}
    	}
        Random r = new Random();
    	HashSet<Ant> ants = currentState.getMyAnts();
    	HashMap<Point, Ant> myAntLocs = currentState.getMyAntLocation();
    	HashMap<Point, Hill> enemyHills = currentState.getEnemyHillLocation();
    	HashMap<Point, Food> food = currentState.getFoodLocation();
        HashMap<Point, Hill> myHillMap = currentState.getMyHillLocation();
        HashMap<Point, Ant> enemyAntLocs = currentState.getEnemyAntLocation();
        HashMap<Point, HashSet<Point>> visible = visibleBoard.getVisibleInfoForPlayer(getColor());
        for (int i=0; i<width; i++) {
    		for (int j=0; j<height; j++) {
    			Point p = new Point(i, j);
	        	if (rocks.containsKey(p)) continue; 
	        	if (enemyHills.containsKey(p)) this.enemy_hills_seen.add(p);
	        	if (this.enemy_hills_seen.contains(p) && myHillMap.containsKey(p)) this.enemy_hills_seen.remove(p);
	        	if (visible.containsKey(p)) {
	        		this.turns_since_explored.put(p, 0);
	        	} else {
	        		this.turns_since_explored.put(p, Math.min(this.turns_since_explored.get(p)+1, this.max_turns_since_explored));
	        	}
    		}
        }
        
        
        while(!turnOver) {
        	HashMap<Ant, Ant> antMoves = new HashMap<Ant, Ant>();
        	for (Ant ant : ants) {
        		Point[] legalMoves = currentState.getLegalMovesForAnt(ant);
                Point curLocation = ant.getLocation();
                int dist_to_closest_hill = width+height;
                for (Point p : myHillMap.keySet()) {
                	int dist = BoardGenerator.manhattanDistance(p, curLocation);
                	if (dist < dist_to_closest_hill) dist_to_closest_hill = dist;
                }
                double sumXForces = 0;
                double sumYForces = 0;
                double unexplored_vector[] = {0, 0};
                boolean found_a_food = false;
                //for (Point p : visible.keySet()) {
                for (Point p : visible.get(curLocation)) {
                	//boolean in_my_vision = visible.get(ant.getLocation()).contains(p);
                	if (p.equals(curLocation)) {
						//                	} else if (rocks.containsKey(p)) {
//                		sumXForces += attractionX(rock, curLocation, p);
//                		sumYForces += attractionY(rock, curLocation, p);
//                	} else if (myAntLocs.containsKey(p)) {
//                		sumXForces += attractionX(0, curLocation, p, dist_to_closest_hill);
//                		sumYForces += attractionY(0, curLocation, p, dist_to_closest_hill);
                	} else if (!found_a_food && food.containsKey(p)) {
                		found_a_food = true;
                		sumXForces += attractionX(0, curLocation, p, dist_to_closest_hill);
                		sumYForces += attractionY(0, curLocation, p, dist_to_closest_hill);
                	} else if (enemyAntLocs.containsKey(p)) {
                		sumXForces += attractionX(1, curLocation, p, dist_to_closest_hill);
                		sumYForces += attractionY(1, curLocation, p, dist_to_closest_hill);
                	} else if (myHillMap.containsKey(p)) {
                		sumXForces += attractionX(2, curLocation, p, dist_to_closest_hill);
                		sumYForces += attractionY(2, curLocation, p, dist_to_closest_hill);
                	}
                }
                for (int i=0; i<width; i++) {
            		for (int j=0; j<height; j++) {
            			Point p = new Point(i, j);
            			if (visible.get(curLocation).contains(p)) turns_since_explored.put(p, 0);
            			if (!found_a_food && enemy_hills_seen.contains(p)) {
            				sumXForces += attractionX(3, curLocation, p, dist_to_closest_hill);
            				sumYForces += attractionY(3, curLocation, p, dist_to_closest_hill);
            			} else {
            				unexplored_vector[0] += yen_for_travelX(curLocation, p, turns_since_explored.get(p));
            				unexplored_vector[1] += yen_for_travelY(curLocation, p, turns_since_explored.get(p));
            			}
                	}
                }
                sumXForces += unexplored_vector[0];
                sumYForces += unexplored_vector[1];
                int prefX = (int)(sumXForces/Math.abs(sumXForces));
                int prefY = (int)(sumYForces/Math.abs(sumYForces));
                Point pref1;
                Point pref2;
                int x = (int)curLocation.getX();
                int y = (int)curLocation.getY();
                if (prefX == 0 && prefY == 0) {
                	pref1 = legalMoves[r.nextInt(legalMoves.length)];
                	pref2 = new Point(x, y);
                } else if (Math.abs(sumXForces) >= Math.abs(sumYForces)) {
                	pref1 = new Point((x + prefX + width) % width, y);
                	pref2 = new Point(x, (y + prefY + height) % height);
                } else {
                	pref2 = new Point((x + prefX + width) % width, y);
                	pref1 = new Point(x, (y + prefY + height) % height);
                }
                boolean move_made = false;
                Point actualMove = new Point(x, y);
            	for (Point legal : legalMoves) if (legal.equals(pref1)) {
            		move_made = true;
            		actualMove = pref1;
            	}
            	if (!move_made) {
                	for (Point legal : legalMoves) if (legal.equals(pref2)) {
                		move_made = true;
                		actualMove = pref2;
                	}
                	if (!move_made) actualMove = legalMoves[r.nextInt(legalMoves.length)];
            	}
                antMoves.put(ant, new Ant(ant.getColor(), actualMove));
                setMove(new Move(getColor(), antMoves));
        	}
			endTurn();
        }
    }
}

