package View;

import Model.*;
import Players.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class GraphicalView extends View {

    private final ArrayList<Player> players = new ArrayList<Player>();
    private final JFrame window;
    private final JPanel boardPanel;
    private final int SQUARE_SIZE;
    private final TilePanel[][] tiles;

    public GraphicalView(Board board, ArrayList<Player> players) {
        super(board);
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        int width = board.getWidth();
        int height = board.getHeight();
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenHeight = (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.9);
        SQUARE_SIZE = Math.min(screenWidth / width, screenHeight / height);
        tiles = new TilePanel[width][height];
        for (Player p : players) {
            this.players.add(p);
        }
        window = new JFrame("Ants");
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        boardPanel = new JPanel(new GridLayout(height, width));
        Hill[][] hills = board.getHills();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TilePanel tile = new TilePanel(Color.BLACK);
                if (hills[i][j] != null) {
                    tile = new TilePanel(hills[i][j].getColor());
                }
                tiles[i][j] = tile;
            }
        }
        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                boardPanel.add(tiles[i][j]);
            }
        }
        boardPanel.setPreferredSize(new Dimension(board.getWidth() * SQUARE_SIZE, board.getHeight() * SQUARE_SIZE));
        window.add(boardPanel);
        window.pack();
        foregroundWindow();
    }

    private void resetView() {
        Hill[][] hills = board.getHills();
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                tiles[i][j].setColor(Color.BLACK);
            }
        }
    }

    public void closeWindow() {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }

    public void foregroundWindow() {
        window.setVisible(true);
        window.setAlwaysOnTop(true);
        window.toFront();
        window.requestFocus();
        window.setAlwaysOnTop(false);
    }

    @Override
    public void display() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run(){
                    resetView();
                    HashSet<Point> visibleTiles = new HashSet<Point>();
                    Ant[][] ants = board.getAnts();
                    Hill[][] hills = board.getHills();
                    Food[][] food = board.getFood();
                    Rock[][] rocks = board.getRocks();
                    HashSet<Point> antLocs = new HashSet<Point>();
                    for (int i = 0; i < board.getWidth(); i++) {
                        for (int j = 0; j < board.getHeight(); j++) {
                            if (ants[i][j] != null) {
                                Color curColor = ants[i][j].getColor();
                                antLocs.add(new Point(i, j));
                                if (hills[i][j] != null) {
                                    tiles[i][j].setColor(curColor);
                                } else {
                                    tiles[i][j].setColor(curColor);
                                }
                                visibleTiles.addAll(board.getVisibleFromPoint(new Point(i, j)));

                            } else if (food[i][j] != null) {
                                tiles[i][j].setBorderColor(Color.BLACK);
                                tiles[i][j].setInteriorColor(Color.GREEN);

                            } else if (hills[i][j] != null) {
                                tiles[i][j].setInteriorColor(hills[i][j].getColor());
                                tiles[i][j].setBorderColor(Color.WHITE);
                                tiles[i][j].setBackground(hills[i][j].getColor());
                                visibleTiles.addAll(board.getVisibleFromPoint(new Point(i, j)));

                            } else if (rocks[i][j] != null) {
                                Color rockColor = new Color(128, 128, 128);
                                tiles[i][j].setColor(rockColor);

                            } else {
                                tiles[i][j].setColor(Color.BLACK);
                            }
                        }
                    }
                    for (Point p : visibleTiles) {
                        if (tiles[(int) p.getX()][(int) p.getY()].getBorderColor().equals(Color.BLACK)) {
                            tiles[(int) p.getX()][(int) p.getY()].setBorderColor(new Color(25, 25, 25));
                            if (tiles[(int) p.getX()][(int) p.getY()].getInteriorColor().equals(Color.BLACK)) {
                                tiles[(int) p.getX()][(int) p.getY()].setInteriorColor(new Color(25, 25, 25));
                            }
                        }
                    }
                    window.repaint();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
