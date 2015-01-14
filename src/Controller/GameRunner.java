package Controller;

import Model.*;
import Players.*;
import View.GraphicalView;
import View.ReplayMachine;
import View.TextualView;
import View.View;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Scanner;

import static java.awt.Color.getHSBColor;

/**
 * Created by Bozerg on 11/25/2014.
 */

/*  This is the main engine for things.  If your heap is blowing up on you and you're getting out of memory errors, comment
    the line that adds boards to replay or add more memory to your max heap size using a VM argument like -Xmx2g.
    I've profiled the code reasonably extensively and replays are the only part that increases heap size over time.
 */

 /* Basic rules of the game:
    1. Last ant colony standing is the winner.  Each ant colony starts with one ant hill and some number of ants (the number is the same for all players and current default is 4) and play proceeds from there.
    2. An ant colony and all of its ants are eliminated from the game as soon as that ant colony has no remaining ant hills.
    3. An ant hill switches ownership if an ant of another colony captures it by occupying its square on the board.  The ant hill will belong to the capturing colony until it is captured by another colony.
       Additionally, the newly captured colony will be a valid spawn location for ants of its controlling colony.
    4. Moving an ant onto food creates a new ant on an ant hill belonging to that ant at the next opportunity.  Spawn opportunities occur at the beginning of each turn.  If an ant colony has food stored up at the beginning of the turn,
       ants will spawn onto unoccupied hills of that colony until their food runs out or there are no unoccupied hills remaining. Any ants still waiting to spawn in will be carried over to the next turn.
    5. The amount of food on the map is constant, each time a food is eaten by an ant a new food will be spawned somewhere on the map at the beginning of the next turn.
    6. All players submit their moves for a turn and then the game engine resolves all those moves at once, updates the game, and informs the players of the new game state.  Players do not take turns in order, they merely submit their moves in order.
    7. Rocks obstruct vision.  The rock will be visible but squares behind it will not be.  Ants, food, ant hills, and regular terrain do not obstruct vision.
    8. If global vision is not enabled an ant or ant hill can see anything that can be reached by drawing a straight line in any direction from the center of the square without encountering a rock or exceeding the vision radius.
    9. If global vision is enabled all ants and ant hills can see everything else in the game.
    10. The map wraps around, namely an ant can walk left from the left side to end up on the right side and vice versa and walk up on the top side to end up on the bottom side and vice versa.
    11. Each turn each ant can move to any of the four tiles immediately adjacent to it, or stay on the tile it already occupies.
    12. If two ants of opposite colors switch places with each other, they will both be destroyed.
    13. If two ants of the same color attempt to switch places with each other they will remain where they are.
    14. If two or more ants of opposite colors move to the same square they will all die.
    15. If two or more ants of the same color attempt to move to the same square, one of them will move and the other(s) will remain where they are.
    16. If two or more ants of opposite colors move to the same square and that square is an ant hill or food, they will destroy each other and the food or ant hill will remain unscathed.
 */

public class GameRunner extends Observable implements Runnable {

    private ArrayList<Player> players = new ArrayList<Player>();
    private static final ArrayList<Color> colors = new ArrayList<Color>();
    private static final int MAX_PLAYERS = 32;
    private volatile boolean turnDone = false;
    private final ArrayList<Player> beginningPlayers = new ArrayList<Player>();
    private int numPlayers;
    private int turnTime;
    private int turnLimit;
    private int width;
    private int height;
    private boolean globalVision = false;
    private int visionRadius = 2;
    private Boolean playing = true;
    private View view;
    private static final Scanner sc = new Scanner(System.in);
    private boolean replayEnabled = false;
    private HashMap<Integer, Board> replay;
    private Board board;
    private Board startingBoard;
    private static GameRunner theInstance;

    public GameRunner() {
        if (theInstance == null) {
            setup();
            theInstance = this;
        }
    }

    public GameRunner(int width, int height, ArrayList<Player> players, int turnTime, boolean globalVision, int visionRadius, View view, boolean replayEnabled, int turnLimit) {
        this.width = width;
        this.height = height;
        this.players = players;
        this.turnLimit = turnLimit;
        for (Player player : players) {
            beginningPlayers.add(player);
        }
        this.numPlayers = players.size();
        this.turnTime = turnTime;
        this.globalVision = globalVision;
        this.visionRadius = visionRadius;
        this.view = view;
        this.replayEnabled = replayEnabled;
        if (numPlayers > MAX_PLAYERS) {
            numPlayers = MAX_PLAYERS;
        }
        setupColors();
        BoardGenerator generator = new BoardGenerator(width, height, numPlayers, globalVision, visionRadius);
        board = generator.generateBoard();
        if (replayEnabled) {
            replay = new HashMap<Integer, Board>();
            replay.put(0, new Board(board));
        }
        theInstance = this;
    }

    public GameRunner(Board board, View view, ArrayList<Player> players, int turnTime, boolean replayEnabled, int turnLimit) {
        this.width = board.getWidth();
        this.height = board.getHeight();
        this.players = players;
        this.board = board;
        this.turnLimit = turnLimit;
        for (Player player : players) {
            beginningPlayers.add(player);
        }
        this.numPlayers = players.size();
        this.turnTime = turnTime;
        this.globalVision = board.getGlobalVision();
        this.view = view;
        this.replayEnabled = replayEnabled;
        if (numPlayers > MAX_PLAYERS) {
            numPlayers = MAX_PLAYERS;
        }
        setupColors();
        if (replayEnabled) {
            replay = new HashMap<Integer, Board>();
            replay.put(0, new Board(board));
        }
        theInstance = this;
    }

    //For training of AI's only, not to be used otherwise, you stinkin' cheaters.
    public static void clearTheInstance() {
        theInstance = null;
    }

    //For training of AI's only, not to be used otherwise, you stinkin' cheaters.
    public static GameRunner getTheInstance() {
        return theInstance;
    }

    public static void main(String[] args) {
        String playAgain = "y";
        while (playAgain.equals("y")) {
            clearTheInstance();
            GameRunner game = new GameRunner();
            Thread t = new Thread(game);
            t.start();
            System.out.println(theInstance.playing);
            while (theInstance.playing) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //See if they want to play a new game
            System.out.println("Would you like to play again?");
            playAgain = sc.next().toLowerCase().trim();
            if (theInstance.view != null && theInstance.view.getClass().equals(GraphicalView.class)) {
                ((GraphicalView) theInstance.view).closeWindow();
            }
        }
    }

    private void setup() {
        //Setup the game, main loop
        replayEnabled = false;
        turnLimit = 0;
        try {
            replay = new HashMap<Integer, Board>();
            System.out.println("Enter number of players: ");
            numPlayers = Math.abs(Integer.parseInt(sc.next()));
            System.out.println("Enter number of seconds per player per turn");
            turnTime = (int) Math.floor(Math.abs(Double.parseDouble(sc.next())) * 1000 + 50);
            System.out.println("Enter width of the board");
            width = Math.abs(Integer.parseInt(sc.next()));
            System.out.println("Enter height of the board");
            height = Math.abs(Integer.parseInt(sc.next()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            System.exit(1);
        }
        System.out.println("Enable global vision?");
        String globVis = sc.next().toLowerCase().trim();
        if (globVis.equals("yes") || globVis.equals("y")) {
            globalVision = true;
        } else {
            System.out.println("Global vision disabled, please enter desired vision radius as an integer");
            try {
                visionRadius = Math.abs(Integer.parseInt(sc.next()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, defaulting to vision radius of 2");
            }
        }
        System.out.println("Would you like to enable replay capability?");
        System.out.println("(Note: This may cause the program to crash if run on longer games, with larger boards, or with small maximum heap sizes)");
        String replayEn = sc.next().toLowerCase().trim();
        if (replayEn.equals("yes") || replayEn.equals("y")) {
            replayEnabled = true;
        }
        if (numPlayers > MAX_PLAYERS) {
            numPlayers = MAX_PLAYERS;
        }
        setupColors();

        //Generate the players
        for (int i = 0; i < numPlayers; i++) {
            System.out.println("Name for player " + i);
            String name = sc.next().trim();
            if (name.equals("")) {
                name = "" + i;
            }
            System.out.println("Type of player AI for player " + i);

            //Update this section as you add more player types
            System.out.println("(Current player types are: hunter, random, forager, explorer, homeguard, forces, kamikaze, pathfinder)");
            String player = sc.next().toLowerCase();
            if (player.equals("random")) {
                Player random = new PlayerRandom(colors.get(i), i, turnTime, name);
                players.add(random);
                beginningPlayers.add(random);
            } else if (player.equals("hunter")) {
                Player hunter = new PlayerHunter(colors.get(i), i, turnTime, name);
                players.add(hunter);
                beginningPlayers.add(hunter);
            } else if (player.equals("forager")) {
                Player forager = new PlayerForager(colors.get(i), i, turnTime, name);
                players.add(forager);
                beginningPlayers.add(forager);
            } else if (player.equals("explorer")) {
                Player explorer = new PlayerExplorer(colors.get(i), i, turnTime, name);
                players.add(explorer);
                beginningPlayers.add(explorer);
            } else if (player.equals("homeguard") || player.equals("home guard")) {
                Player homeguard = new PlayerHomeguard(colors.get(i), i, turnTime, name);
                players.add(homeguard);
                beginningPlayers.add(homeguard);
            } else if (player.equals("forces")) {
                Player forces = new PlayerForces(colors.get(i), i, turnTime, name);
                players.add(forces);
                beginningPlayers.add(forces);
            } else if (player.equals("kamikaze")) {
                Player kamikaze = new PlayerKamikaze(colors.get(i), i, turnTime, name);
                players.add(kamikaze);
                beginningPlayers.add(kamikaze);
            } else if (player.equals("pathfinder")) {
                Player pathfinder = new PlayerPathfinder(colors.get(i), i, turnTime, name);
                players.add(pathfinder);
                beginningPlayers.add(pathfinder);
            } else {
                System.out.println("Given player type was not recognized, defaulting to random");
                Player elseCase = new PlayerRandom(colors.get(i), i, turnTime, name);
                players.add(elseCase);
                beginningPlayers.add(elseCase);
            }
        }
        System.out.println("Players added\n");

        //Select view
        System.out.println("Would you like to see the game with a graphical view (g), a textual view (t), or no view (n)?");
        String response = sc.next().toLowerCase().trim();

        //Generate the Board
        System.out.println("Generating Board:\n");
        double generateTime = System.currentTimeMillis();
        BoardGenerator generator = new BoardGenerator(width, height, numPlayers, globalVision, visionRadius);
        System.out.println("Board generator created");
        board = generator.generateBoard();
        startingBoard = new Board(board);
        System.out.println("Board generated");
        System.out.println("\nBoard generation took " + (System.currentTimeMillis() - generateTime) / 1000.0 + " seconds\n\n");
        replay.put(0, new Board(board));

        //Actually make the view now that the board is built
        if (response.equals("g")) {
            view = new GraphicalView(board, players);
            System.out.println("Selected graphical view");
        } else if (response.equals("t")) {
            view = new TextualView(board, players);
            System.out.println("Selected textual view");
        } else {
            System.out.println("Selected no view");
        }

        //Display first board if view selected
        if (view != null) {
            System.out.println("Starting board: ");
            view.display();
        }
    }

    public void run() {
        if (theInstance.view != null && theInstance.view.getClass().equals(GraphicalView.class)) {
            ((GraphicalView) theInstance.view).foregroundWindow();
        }
        synchronized (theInstance.playing) {
            theInstance.playing = true;
            while (players.size() > 1 && (turnLimit == 0 || board.getTurnCount() <= turnLimit)) {
                System.out.println("turn Limit: " + turnLimit);
                System.out.println("turn number: " + board.getTurnCount());
                double turnStartTime = System.currentTimeMillis();
                System.out.println(players.size() + " players left");
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            ArrayList<Move> moves = new ArrayList<Move>();
                            for (Player player : players) {
                                turnDone = false;
                                int numAnts = 0;
                                int numHills = 0;
                                Ant[][] ants = board.getAnts();
                                Hill[][] hills = board.getHills();
                                for (int j = 0; j < board.getWidth(); j++) {
                                    for (int k = 0; k < board.getHeight(); k++) {
                                        if (ants[j][k] != null && ants[j][k].getColor().equals(player.getColor())) {
                                            numAnts++;
                                        }
                                        if (hills[j][k] != null && hills[j][k].getColor().equals(player.getColor())) {
                                            numHills++;
                                        }
                                    }
                                }
                                System.out.println("Turn: " + board.getTurnCount() + ", Player " + player.getPlayerNumber() + ", " + player.getName() + ", deciding their move with " + numAnts + " ant(s) and " + numHills + " ant hill(s)");
                                boolean toFinish = false;
                                InfoBlock playerInfo = board.getInfoForPlayer(player.getColor());
                                long startTime = System.currentTimeMillis();
                                player.nextTurn(playerInfo);
                                double elapsedTime = 0;
                                while (!toFinish && !turnDone) {
                                    toFinish = (System.currentTimeMillis() - startTime > turnTime);
                                    elapsedTime = (System.currentTimeMillis() - startTime);
                                }
                                System.out.println("Turn: " + board.getTurnCount() + " took " + player.getName() + " " + elapsedTime / 1000.0 + " seconds");
                                moves.add(player.getMove());
                            }
                            board.nextTurn(moves);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (replayEnabled) {
                    replay.put(board.getTurnCount(), new Board(board));
                }
                if (view != null) {
                    view.display();
                }
                System.out.println("Turn for all players took " + (System.currentTimeMillis() - turnStartTime) / 1000.0 + " seconds\n\n\n");
            }

            //Game is over
            if (board.getTurnCount() > turnLimit && turnLimit != 0) {
                System.out.println("turn limit reached");
                theInstance.setChanged();
                theInstance.notifyObservers(getGameInfo(true));
            } else {
                System.out.println("Winner is " + theInstance.players.get(0));
                theInstance.setChanged();
                theInstance.notifyObservers(getGameInfo(false));
            }

            runReplay();
            theInstance.playing = false;
        }
    }

    private void runReplay() {
        if (replayEnabled && view != null) {
            String response = "yes";
            while (response.equals("y") || response.equals("yes")) {
                System.out.println("Would you like to view the replay?");
                response = sc.next().toLowerCase().trim();
                if (response.equals("y") || response.equals("yes")) {
                    System.out.println("From what perspective would you like the replay? (g for global, player number for that player)");
                    String secondResponse = sc.next().toLowerCase().trim();
                    Player replayPlayer = null;
                    if (!secondResponse.equals("g")) {
                        try {
                            int playerNumber = Math.abs(Integer.parseInt(secondResponse));
                            if (playerNumber < beginningPlayers.size()) {
                                replayPlayer = beginningPlayers.get(playerNumber);
                            } else {
                                System.out.println("No such player number, defaulting to global");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Input is not valid");
                        }
                    }
                    try {
                        System.out.println("How many milliseconds between turns?");
                        int milliseconds = Math.abs(sc.nextInt());
                        ReplayMachine replayMachine = new ReplayMachine(replayPlayer, replay, view);
                        replayMachine.runReplay(milliseconds);
                    } catch (NumberFormatException e) {
                        System.out.println("Input is not valid");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        System.gc();
                    }
                }
            }
        }
    }

    public GameInfoBlock getGameInfo(boolean earlyTermination) {
        return theInstance.board.getGameInfo(theInstance.turnTime, theInstance.startingBoard, theInstance.beginningPlayers, earlyTermination);
    }

    //need to have at least MAX_PLAYER colors here, DO NOT INCLUDE Color.BLACK (used for ant fights), or the colors you use for rocks, ground, and food in the GraphicalView!
    private static void setupColors() {
        colors.clear();
        for (int i = 0; i < MAX_PLAYERS; i++) {
            colors.add(getHSBColor((float) ((i * 0.618033988749895) % 1.0), (float) 0.5, (float) 1.0));
        }
    }

    public static void endTurn() {
        theInstance.turnDone = true;
    }

    //removes a player from the game
    public static void removePlayer(Color color) {
        for (int i = 0; i < theInstance.players.size(); i++) {
            if (theInstance.players.get(i).getColor().equals(color)) {
                Player deadPlayer = theInstance.players.get(i);
                theInstance.players.remove(deadPlayer);
            }
        }
    }

    //get a list of all the colors that players can use
    public static ArrayList<Color> getColors() {
        if (colors.size() < MAX_PLAYERS) {
            setupColors();
        }
        return colors;
    }

    //THE FOLLOWING TWO METHODS ARE BAD INSOFAR AS THEY HAVE THE POTENTIAL TO LET PLAYERS MESS WITH EACH OTHER
    //returns the player of the given color or null if there is no such player
    public static Player getPlayerByColor(Color c) {
        for (Player p : theInstance.players) {
            if (p.getColor().equals(c)) {
                return p;
            }
        }
        return null;
    }

    //returns the player with the given number or null if there is no such player
    public static Player getPlayerByNumber(int number) {
        for (Player p : theInstance.players) {
            if (p.getPlayerNumber() == number) {
                return p;
            }
        }
        return null;
    }
}
