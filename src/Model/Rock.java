package Model;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Rock extends Tile {
    public Rock(Point location) {
        super(location);
    }

    public Rock(Rock rock) {
        this(rock.getLocation());
    }


    @Override
    public String toString() {
        return "Rock: (" + location.getX() + ", " + location.getY() + ")";
    }
}
