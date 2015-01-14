package Model;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public class Hill extends Tile {
    private final Color color;

    public Hill(Color color, Point location) {
        super(location);
        this.color = color;
    }

    public Hill(Hill hill) {
        this(hill.getColor(), hill.getLocation());
    }

    public Color getColor() {
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hill)) return false;
        if (!super.equals(o)) return false;

        Hill hill = (Hill) o;

        return !(color != null ? !color.equals(hill.color) : hill.color != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}

