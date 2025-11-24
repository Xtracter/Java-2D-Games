package tetris;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by Fredrik on 2015-11-17.
 */
public class PreviewPanel extends JPanel {

    Block block;
    int size = 10;
    Dimension dim;
    Vector<Block> blocks = new Vector<Block>();
    Tetris tetris;

    public PreviewPanel(Tetris tetris, Dimension dim){
        super();
        this.tetris = tetris;
        this.dim = dim;
        blocks = tetris.getBlocks(10);
        setBackground(Color.BLACK);
    }

    @Override
    public synchronized Dimension getPreferredSize(){
        return dim;
    }

    protected Block getBlockByType(int type){
        for(Block b:blocks){
            if(b.type == type){
                return b;
            }
        }
        return null;
    }

    public void setBlock(Block b){
        this.block = getBlockByType(b.type);
        this.repaint();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(block!=null){
            int y_add = 20;
            int x_add = 16;
            if(block.type==Tetris.IBLOCK){
                y_add = 12;
                x_add = 18;
            }
            if(block.type==Tetris.OBLOCK){
                y_add = 21;
                x_add = 21;
            }
            for (Rectangle r : block) {
                g.setColor(tetris.blackAndWhite?Color.GRAY:block.color);
                g.fillRect(r.x+x_add, r.y+y_add, size, size);
                g.setColor(getBackground());
                g.drawRect(r.x+x_add, r.y+y_add, size, size);
            }

        }
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0,0,getWidth()-1,getWidth()-1);

    }

}
