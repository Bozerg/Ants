package Model;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Move {
    private HashMap<Ant, Ant> moves = new HashMap<Ant, Ant>();
    private final Color color;

    public Move(Color color, HashMap<Ant, Ant> moves) {
        this.color = color;
        this.moves = moves;
    }

    public HashMap<Ant, Ant> getMoves() {
        return this.moves;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;

        Move move = (Move) o;

        if (color != null ? !color.equals(move.color) : move.color != null) return false;
        return !(moves != null ? !moves.equals(move.moves) : move.moves != null);

    }

    @Override
    public int hashCode() {
        int result = moves != null ? moves.hashCode() : 0;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
