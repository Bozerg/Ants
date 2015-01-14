package View;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Richard on 12/9/2014.
 */
public class TilePanel extends JPanel {

    private Color color;
    private Color borderColor;

    public TilePanel(Color color) {
        this.color = color;
        this.borderColor = color;

    }

    public void setInteriorColor(Color color) {
        this.color = color;
    }

    public void setBorderColor(Color color){
        this.borderColor = color;
    }

    public Color getInteriorColor() {
        return this.color;
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    public void setColor(Color color) {
        this.color = color;
        this.borderColor = color;
        this.setBackground(color);
    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(color);
        int width = (int) getSize().getWidth();
        int height = (int) getSize().getHeight();
        g.fillRect(0, 0, width, height);
        g.setColor(borderColor);
        int offSet = Math.min(height, width) / 4;
        if(offSet < 1){
            offSet = 1;
        }
        ((Graphics2D) g).setStroke(new BasicStroke(offSet));
        g.drawRect(offSet/2, offSet/2, width - offSet, height - offSet);
    }
}
