package View;

import Model.*;
import Players.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bozerg on 11/26/2014.
 */
public class TextualView extends View{

    public TextualView(Board board, ArrayList<Player> players){
        super(board,players);
    }

    public void display() {
            HashMap<Color, Integer> playerTracker = new HashMap<Color, Integer>();
            for (Player p : players) {
                playerTracker.put(p.getColor(), p.getPlayerNumber());
            }
            if (board != null) {
                Hill[][] hills = board.getHills();
                Ant[][] ants = board.getAnts();
                Rock[][] rocks = board.getRocks();
                Food[][] food = board.getFood();
                System.out.println("\nTurn: " + board.getTurnCount());
                for (int j = 0; j < board.getHeight(); j++) {
                    for (int i = 0; i < board.getWidth(); i++) {
                        if (hills[i][j] != null) {
                            System.out.print("X");
                        } else if (ants[i][j] != null && hills[i][j] == null) {
                            System.out.print(playerTracker.get(ants[i][j].getColor()));
                        } else if (food[i][j] != null && ants[i][j] == null) {
                            System.out.print("F");
                        } else if (rocks[i][j] != null) {
                            System.out.print("R");
                        } else {
                            System.out.print(" ");
                        }
                    }
                    System.out.println();
                }
                System.out.println();
            }

    }
}
