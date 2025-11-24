package tetris;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Fredrik on 2015-11-17.
 */
public class TRect extends Rectangle implements Serializable {

    public Color color;
    String letter;

    TRect(int x, int y, int width, int height){
        this(new Rectangle(x,y,width,height), Color.WHITE);
    }

    TRect(Rectangle r, Color c){
        super(r.x,r.y,r.width,r.height);
        this.color=c;
    }

    TRect(Rectangle r, Color c, String letter){
        super(r.x,r.y,r.width,r.height);
        this.color=c;
        this.letter = letter;
    }
}
