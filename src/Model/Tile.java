package Model;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public abstract class Tile {
    protected final Point location;

    public Tile(Point location) {
        this.location = location;
    }

    public Point getLocation() {
        return new Point(this.location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;

        Tile tile = (Tile) o;

        return !(location != null ? !location.equals(tile.location) : tile.location != null);

    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }
}
