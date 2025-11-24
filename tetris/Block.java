package tetris;

import java.awt.*;
import java.io.Serializable;
import java.util.Vector;

/**
 * Created by Fredrik on 2015-11-17.
 */
public class Block extends Vector<TRect> implements Serializable {
    public Block(int x, int y, int type, Color c){
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = c;
        this.angle = 0;
    }
    double angle;
    int type;
    Color color;
    int x,y;
    Point center;
}
