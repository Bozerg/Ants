package Model;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Ant extends Tile {
    private final Color color;

    public Ant(Color color, Point location) {
        super(location);
        this.color = color;
    }

    public Ant(Ant ant) {
        this(ant.getColor(), ant.getLocation());
    }

    public Color getColor() {
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
    }

    public String toString() {
        return color.toString() + " Ant: (" + location.getX() + ", " + location.getY() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ant)) return false;
        if (!super.equals(o)) return false;

        Ant ant = (Ant) o;

        return !(color != null ? !color.equals(ant.color) : ant.color != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
