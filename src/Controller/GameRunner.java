package Controller;

import View.GameRunnerWindow;

import java.util.Scanner;

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
public class GameRunner{
    private static GameRunner theInstance;
    private GameRunner() { }
    public static void clearTheInstance() {
        theInstance = null;
    }
    public static GameRunner getTheInstance() {
        if(theInstance==null){
            new GameRunner();
        }
        return theInstance;
    }
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String playAgain = "y";
        GameRunnerWindow window = new GameRunnerWindow();
        while (playAgain.equals("y")) {
            Game game = new Game();
            window.getGameConfiguration(game);
            Thread t = new Thread(game);
            t.start();

            //See if they want to play a new game
            //System.out.println("Would you like to play a game?");
            playAgain = "";//sc.next().toLowerCase().trim();
        }
    }
}
