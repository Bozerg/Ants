package Model;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Food extends Tile {
    public Food(Point location) {
        super(location);
    }

    public Food(Food food) {
        this(food.getLocation());
    }
}
