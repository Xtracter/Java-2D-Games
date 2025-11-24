package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by Fredrik on 2015-11-16.
 */
public class Tetris extends JPanel {

    int size = 20;
    int _size = size - (size*2);
    int cols = 10;
    int rows = 20;
    long speed = 350;
    long drop_speed = 50;
    long speed_increase = 20;
    boolean gameOn = false;
    boolean dropping = false;
    int score = 0;
    Stack<Block> blockQue = new Stack<Block>();
    Vector<PreviewPanel> previewPanels = new Vector<PreviewPanel>();
    final String title = "T E T R I S";
    final String adde[] = {"A","D","R","I","E","N"};

    public static final int TBLOCK = 0;
    public static final int LBLOCK = 1;
    public static final int JBLOCK = 2;
    public static final int OBLOCK = 3;
    public static final int SBLOCK = 4;
    public static final int ZBLOCK = 5;
    public static final int IBLOCK = 6;

    int killIter = 0;
    Color[] killColor = new Color[]{Color.BLACK,Color.RED,Color.GREEN,Color.BLUE};
    Color[] killColorBW = new Color[]{Color.BLACK,Color.GRAY,Color.LIGHT_GRAY,Color.GRAY};

    final int LEFT = 0;
    final int RIGHT = 1;
    final int UP = 2;
    final int DOWN = 3;

    int removedRows,totalRows;
    int dropTick = 0;
    boolean gameOver;
    Block currentBlock;
    Vector<Block> blocks = new Vector<Block>();
    Rectangle bounds;
    Thread runner;
    boolean blackAndWhite; //= true;

    Vector<TRect> deadRects = new Vector<TRect>();

    public Tetris() {
        init();
    }

    public Tetris(int size) {
        this(20,10,size);
    }

    public Tetris(int rows, int cols, int size) {
        this.rows = rows;
        this.cols = cols;
        this.size = size;
        this._size = size - (size*2);
        init();
    }

    public void addPreviewPanel(PreviewPanel preview){
        this.previewPanels.add(preview);
    }

    public void removePreviewPanel(PreviewPanel preview){
        this.previewPanels.remove(preview);
    }

    public void init(){
        setBackground(Color.BLACK);
        this.blocks = getBlocks(size);
    }

    void start(){
        score=0;
        totalRows=0;
        setScoreCount(score);
        setRowCount(totalRows);
        blockQue.clear();
        blockQue.push(blocks.get(new Random().nextInt(blocks.size())));
        blockQue.push(blocks.get(new Random().nextInt(blocks.size())));
        deadRects.clear();
        kill.clear();
        dropping=false;
        currentBlock = getRandBlock();
        score = 0;
        setScoreCount(score);
        gameOn = true;
        gameOver = false;
        accelerate = 0;
        runner = new Thread(new Runnable() {
            @Override
            public void run() {
                while(Thread.currentThread()==runner && runner!=null){
                    try{
                        Thread.sleep(!dropping?(speed-(accelerate*speed_increase)):drop_speed);
                        if(bounds!=null){
                            tick();
                        }
                        repaint();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        runner.start();
    }

    synchronized void tick(){
        if(gameOver) return;
        dropTick++;
        if(isMoveAllowed(currentBlock,DOWN)) {
            currentBlock.y += size;
        }else {
            killBlock(currentBlock);
            removedRows = removeDeadRows();
            totalRows+=removedRows;
            for(int i = 0; i < removedRows; i++){
                score += 100 + (50*i);
            }
            setRowCount(totalRows);
            dropping=false;
            currentBlock = getRandBlock();
            if(dropTick<2){
                gameOver = true;
                gameOn = false;
            }
            dropTick = 0;
        }
        for(PreviewPanel p:previewPanels){
            p.setBlock(blockQue.get(1));
        }
    }

    void killBlock(Block b){
        Vector<TRect> rects = getScreenRects(b);
        for(TRect r:rects){
            deadRects.add(new TRect(r,b.color,r.letter));
        }
        b.x = size*2;
        b.y=0;
    }

    Vector<TRect> kill = new Vector<TRect>();
    int removeDeadRows(){
        Vector<TRect> row = new Vector<TRect>();
        kill.clear();
        int removed = 0;
        for(int i = 0; i < rows; i++) {
            for(TRect dr:deadRects){
                if(dr.y == i*size){
                    row.add(dr);
                    if(row.size()==cols){
                        removed++;
                        kill.addAll(row);
                        if(textPuzzleMode){
                            for(TRect r:row){
                                System.out.print((r.letter!=null?r.letter:" "));
                            }
                            System.out.println();
                        }
                        row.clear();
                    }
                }
            }
            row.clear();
        }
        deadRects.removeAll(kill);
        return removed;
    }

    int accumRows=0;
    int accelerate = 1;
    int numRowsForNextSpeed = 10;
    void packRows() {
        int offset = 0;
        Vector<Integer> rInts = new Vector<Integer>();
        for(int i = rows; i > 0; i--){
            Vector<TRect> row = getRowAt(i*size, kill);
            if(!row.isEmpty()){
                pushRow(row.get(0).y-size+(offset*size));
                offset++;
            }
        }
        accumRows+=removedRows;
        if(accumRows>=numRowsForNextSpeed){
            accelerate++;
            accumRows=0;
        }
        removedRows=0;
    }

    void pushRow(int y){
        for(int i = rows; y > 0; y--){
            if(i>=y) continue;
            Vector<TRect> row = getRowAt(y,deadRects);
            for(TRect r:row){
                r.y+=size;
            }
        }
    }

    Vector<TRect> getRowAt(int y, Vector<TRect> list){
        Vector<TRect> row = new Vector<TRect>();
        for(TRect r:list){
            if(r.y==y) row.add(r);
        }
        return row;
    }

    TRect getRectAt(int x, int y, Vector<TRect> rects){
        for(TRect r:rects){
            if(r.x==x && r.y==y){
                return r;
            }
        }
        return null;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        bounds = new Rectangle(0, 0, getWidth(), getHeight());
        if(gameOn) {
            g.setColor(currentBlock.color);
            Vector<TRect> rs = getScreenRects(currentBlock);
            for(TRect r:rs){
                g.setColor(blackAndWhite ? Color.GRAY : currentBlock.color);
                g.fill3DRect(r.x, r.y, r.width, r.height, true);
                g.fill3DRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, true);
                g.setColor(Color.BLACK);
                g.drawRect(r.x+4, r.y+4, r.width-8, r.height-8);
                g.drawRect(r.x + 8, r.y + 8, r.width - 16, r.height - 16);
                g.setColor(getBackground());
                g.drawRect(r.x, r.y, r.width, r.height);
                if(r.letter!=null && textPuzzleMode){
                    g.setColor(Color.BLACK);
                    g.fillRect(r.x+6, r.y+6, r.width-12, r.height-12);
                    g.setColor(Color.WHITE);
                    int lw = g.getFontMetrics().stringWidth( ((TRect)r).letter );
                    int ld = g.getFontMetrics().getDescent();
                    g.drawString(r.letter,(r.x+(size/2)-(lw/2)),r.y+size/2+ld);
                }
            }

            for(TRect r:deadRects){
                g.setColor(blackAndWhite ? Color.GRAY : r.color);
                g.fill3DRect(r.x, r.y, r.width, r.height, true);
                g.fill3DRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, true);
                g.setColor(Color.BLACK);
                g.drawRect(r.x+4, r.y+4, r.width-8, r.height-8);
                g.drawRect(r.x + 8, r.y + 8, r.width - 16, r.height - 16);
                g.setColor(getBackground());
                g.drawRect(r.x, r.y, r.width, r.height);
                if(r.letter!=null && textPuzzleMode){
                    g.setColor(Color.BLACK);
                    g.fillRect(r.x + 6, r.y + 6, r.width - 12, r.height - 12);
                    g.setColor(Color.WHITE);
                    int lw = g.getFontMetrics().stringWidth( ((TRect)r).letter );
                    int ld = g.getFontMetrics().getDescent();
                    g.drawString(((TRect)r).letter,(r.x+(size/2)-(lw/2)),r.y+size/2+ld);
                }
             }
            if(!kill.isEmpty() && killIter<killColor.length-1) {
                for (TRect r : kill) {
                    g.setColor(blackAndWhite?Color.GRAY:r.color);
                    g.fill3DRect(r.x, r.y, r.width, r.height, true);
                    g.fill3DRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, true);
                    g.setColor(Color.BLACK);
                    g.drawRect(r.x+4, r.y+4, r.width-8, r.height-8);
                    if(!blackAndWhite) g.setColor(killColor[killIter]);
                    else g.setColor(killColorBW[killIter]);
                    g.fillRect(r.x+8, r.y+8, r.width-16, r.height-16);
                    g.setColor(Color.BLACK);
                    g.drawRect(r.x+8, r.y+8, r.width-16, r.height-16);
                }
                killIter++;
                if(killIter==killColor.length-1){
                    packRows();
                    setScoreCount(score);
                }
            }else{
                kill.clear();
                killIter=0;
            }
        }else{
            drawMarquee(g);
        }

        g.setColor(Color.WHITE);
        g.drawRect(0,0,getWidth(),getHeight());
        //drawGrid(g);
    }
    String font = "Lucida Console";

    void drawMarquee(Graphics g){
        for(TRect r:deadRects){
            g.setColor(blackAndWhite?Color.GRAY:r.color);
            g.fill3DRect(r.x, r.y, r.width, r.height, true);
            g.fill3DRect(r.x+1, r.y+1, r.width-2, r.height-2, true);
            g.setColor(Color.BLACK);
            g.drawRect(r.x+4, r.y+4, r.width-8, r.height-8);
            g.drawRect(r.x+8, r.y+8, r.width-16, r.height-16);
            g.setColor(getBackground());
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        g.setColor(Color.GRAY);
        g.setFont(new Font(font, Font.BOLD,24));
        drawCenteredString(title, getWidth(), getHeight() - 100, g);
        g.setFont(new Font(font, Font.BOLD, 12));
        drawCenteredString("F 1  =  S T A R T", getWidth(), getHeight() + 10, g);
        drawCenteredString("CrazedoutSoft 2015", getWidth(), getHeight()+getHeight()-20,g);
    }

    boolean textPuzzleMode = false;
    Block getRandBlock(){
        Block b = blockQue.pop();
        blockQue.push(blocks.get(new Random().nextInt(blocks.size())));
        if(textPuzzleMode){
            int i = new Random().nextInt(b.size());
            for(int j=0;j<b.size();j++){
                if(j==i){
                    b.get(j).letter = adde[new Random().nextInt(adde.length)];
                }else{
                    b.get(j).letter=null;
                }
            }
        }
        return b;
    }

    void drawGrid(Graphics g){
        g.setColor(Color.GRAY);
        for(int i = 0; i < rows*size; i++){
            g.drawLine(0,i*size,cols*size,i*size);
        }
        for(int i = 0; i < rows*size; i++){
            g.drawLine(i*size,0,i*size,rows*size);
        }
    }

    Vector<TRect> getScreenRects(Block b){
        return getScreenRects(b,b.angle);
    }

    Vector<TRect> getScreenRects(Block b, double angle){
        Vector<TRect> list = new Vector<TRect>();
        for(TRect rt: b){
            Rectangle r = rotateRect(rt,angle);
            TRect rect = new TRect(r.x + b.x, r.y + b.y, r.width, r.height);
            rect.letter = rt.letter;
            list.add(rect);
        }
        return list;
    }

    Rectangle rotateRect(Rectangle r, double angle){
        Rectangle rect = new Rectangle();
        Point p = rotatePoint(new Point(r.x, r.y), angle);
        rect.x = p.x;
        rect.y = p.y;
        rect.width = size;
        rect.height = size;
        return rect;
    }

    boolean isMoveAllowed(Block b, int move){
        boolean ok = true;
        Vector<TRect> rects = this.getScreenRects(b);
        for(Rectangle r:rects){
            switch(move){
                case LEFT:
                    if(r.x-size<bounds.x){
                        return false;
                    }
                    if(getRectAt(r.x-size,r.y,deadRects)!=null){
                        return false;
                    }
                    break;
                case RIGHT:
                    if(r.x+ r.width + size > bounds.width-bounds.x){
                        return false;
                    }
                    if(getRectAt(r.x+size,r.y,deadRects)!=null){
                        return false;
                    }
                    break;
                case DOWN:
                    if(r.y + r.height + size > bounds.height+bounds.y){
                        return false;
                    }
                    for(TRect dr:deadRects){
                        if(r.x == dr.x && r.y + r.height + (size*2) > dr.height+dr.y){
                            return false;
                        }
                    }
                    break;
            }
        }
        return ok;
    }

    boolean isRotationAllowed(Block b, double requestedAngle){

        if(b.type==OBLOCK) {
            return false;
        }

        boolean ok = true;
        Vector<TRect> rects = this.getScreenRects(b,requestedAngle);
        for(Rectangle r:rects){
            if(r.x<bounds.x || r.x + r.width > bounds.width-bounds.x){
                return false;
            }
            if(getRectAt(r.x,r.y,deadRects)!=null){
                return false;
            }
        }
        return ok;
    }

    private int round(double d){
        double dAbs = Math.abs(d);
        int i = (int) dAbs;
        double result = dAbs - (double) i;
        if(result<0.5){
            return d<0 ? -i : i;
        }else{
            return d<0 ? -(i+1) : i+1;
        }
    }

    Point rotatePoint(Point p, double angle){

        double sinAng = Math.sin((angle / 180) * Math.PI);
        double cosAng = Math.cos((angle / 180) * Math.PI);
        double dx = p.x;
        double dy = p.y;
        Point pr = new Point();
        pr.x = round((dx * cosAng - dy * sinAng));
        pr.y = round((dx*sinAng+dy*cosAng));

        return pr;
    }

    void rotate(final int dir){
        if(dropping) return;
        switch(dir){
            case UP:
                if(isRotationAllowed(currentBlock,shift(currentBlock.angle-90))) {
                    currentBlock.angle -= 90;
                }
                break;
            case DOWN:
                if(isRotationAllowed(currentBlock,shift(currentBlock.angle+90))) {
                    currentBlock.angle += 90;
                }
                break;
        }
        currentBlock.angle = shift(currentBlock.angle);
        repaint();
    }

    void move(final int dir){
        if(dropping) return;
        switch(dir){
            case 0:
                if(isMoveAllowed(currentBlock, LEFT)) {
                    currentBlock.x -= size;
                }
                break;
            case 1:
                if(isMoveAllowed(currentBlock, RIGHT)) {
                    currentBlock.x += size;
                }
                break;
        }
        repaint();
    }

    void drop(){
        dropping = true;
    }

    double shift(double angle){
        if(angle < 0){
            angle += 360;
        }else if(angle>360){
            angle-=360;
        }
        return angle;
    }

    public static void drawCenteredString(String s, int w, int h, Graphics g) {
        try {
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(s)) / 2 ;
            int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(s, x, y);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized Dimension getPreferredSize(){
        return new Dimension(size*cols,size*rows);
    }

    void setRowCount(int rows){
        try{
        String s = ""+rows;
        if(s.length()==1) {
            r3.setDigit(Integer.parseInt("" + s.charAt(0)));
        }else
        if(s.length()==2) {
            r2.setDigit(Integer.parseInt("" + s.charAt(0)));
            r3.setDigit(Integer.parseInt("" + s.charAt(1)));
        }
        else if(s.length()==3) {
            d1.setDigit(Integer.parseInt("" + s.charAt(0)));
            d2.setDigit(Integer.parseInt("" + s.charAt(1)));
            d3.setDigit(Integer.parseInt("" + s.charAt(2)));
        }
        }catch(Exception ex){
            // Do NOTHING.
        }
    }

    void setScoreCount(int score){
        try{
        String s = ""+score;
        if(s.length()==1) {
            d5.setDigit(Integer.parseInt("" + s.charAt(0)));
        }else
        if(s.length()==2) {
            d4.setDigit(Integer.parseInt("" + s.charAt(0)));
            d5.setDigit(Integer.parseInt("" + s.charAt(1)));
        }
        else if(s.length()==3) {
            d3.setDigit(Integer.parseInt("" + s.charAt(0)));
            d4.setDigit(Integer.parseInt("" + s.charAt(1)));
            d5.setDigit(Integer.parseInt("" + s.charAt(2)));
        }else if(s.length()==4) {
            d5.setDigit(Integer.parseInt("" + s.charAt(3)));
            d4.setDigit(Integer.parseInt("" + s.charAt(2)));
            d3.setDigit(Integer.parseInt("" + s.charAt(1)));
            d2.setDigit(Integer.parseInt("" + s.charAt(0)));
        }else if(s.length()==5) {
            d5.setDigit(Integer.parseInt("" + s.charAt(4)));
            d4.setDigit(Integer.parseInt("" + s.charAt(3)));
            d3.setDigit(Integer.parseInt("" + s.charAt(2)));
            d2.setDigit(Integer.parseInt("" + s.charAt(1)));
            d1.setDigit(Integer.parseInt("" + s.charAt(0)));
        }
        }catch(Exception ex){
            // DO NOTHING.
        }
    }

    Image getIconImage(){
        Image img=new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Block tBlock = new Block(0,0,TBLOCK,Color.RED);
        int size = 8;
        int _size = -8;
        tBlock.add(new TRect(_size, _size, size, size));
        tBlock.add(new TRect(0, _size, size, size));
        tBlock.add(new TRect(size, _size, size, size));
        tBlock.add(new TRect(0, 0, size, size));
        img.getGraphics().setColor(Color.RED);
        for(Rectangle r:tBlock){
            img.getGraphics().fillRect(r.x+12,r.y+16,r.width,r.height);
        }
        return img;
    }

    DigitalDigit d1,d2,d3,d4,d5;
    DigitalDigit r1,r2,r3;

    Vector<Block> getBlocks(int size){
        int _size = size - (size*2);
        Vector<Block> blocks = new Vector<Block>();
        Block tBlock = new Block(3*size,0, TBLOCK, Color.RED);
        tBlock.add(new TRect(_size, _size, size, size));
        tBlock.add(new TRect(0, _size, size, size));
        tBlock.add(new TRect(size, _size, size, size));
        tBlock.add(new TRect(0, 0, size, size));
        blocks.add(tBlock);

        Block lBlock = new Block(3*size,0, LBLOCK, Color.YELLOW);
        lBlock.add(new TRect(_size, _size, size, size));
        lBlock.add(new TRect(0, _size, size, size));
        lBlock.add(new TRect(size, _size, size, size));
        lBlock.add(new TRect(size, 0, size, size));
        blocks.add(lBlock);

        Block jBlock = new Block(3*size,0, JBLOCK, Color.BLUE);
        jBlock.add(new TRect(_size, _size, size, size));
        jBlock.add(new TRect(0, _size, size, size));
        jBlock.add(new TRect(size, _size, size, size));
        jBlock.add(new TRect(_size, 0, size, size));
        blocks.add(jBlock);

        Block oBlock = new Block(2*size,0, OBLOCK, Color.RED);
        oBlock.add(new TRect(_size, _size, size, size));
        oBlock.add(new TRect(0, _size, size, size));
        oBlock.add(new TRect(_size, 0, size, size));
        oBlock.add(new TRect(0, 0, size, size));
        blocks.add(oBlock);

        Block sBlock = new Block(2*size,0, SBLOCK, Color.GREEN);
        sBlock.add(new TRect(_size, _size, size, size));
        sBlock.add(new TRect(0, _size, size, size));
        sBlock.add(new TRect(0, 0, size, size));
        sBlock.add(new TRect(size, 0, size, size));
        blocks.add(sBlock);

        Block zBlock = new Block(2*size,0, ZBLOCK, Color.CYAN);
        zBlock.add(new TRect(0, _size, size, size));
        zBlock.add(new TRect(size, _size, size, size));
        zBlock.add(new TRect(_size, 0, size, size));
        zBlock.add(new TRect(0, 0, size, size));
        blocks.add(zBlock);

        Block iBlock = new Block(2*size,0, IBLOCK, Color.RED);
        iBlock.add(new TRect(0,_size,size,size));
        iBlock.add(new TRect(0,0,size,size));
        iBlock.add(new TRect(0,size,size,size));
        iBlock.add(new TRect(0,size*2,size,size));
        blocks.add(iBlock);
        return blocks;
    }

    public static void main(String argv[]){

        final Tetris t = new Tetris(30);
        JFrame frame = new JFrame(t.title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setIconImage(t.getIconImage());
        frame.setResizable(false);
        JPanel scoreBoard = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scoreBoard.setBackground(Color.GRAY);
        scoreBoard.add((t.r1 = new DigitalDigit(0, t.size)));
        scoreBoard.add((t.r2=new DigitalDigit(0,t.size)));
        scoreBoard.add(t.r3=new DigitalDigit(0,t.size));
        JPanel spacer = new JPanel(){
            public synchronized Dimension getPreferredSize(){
                return new Dimension(20,20);
            }
        };
        scoreBoard.add(spacer);
        scoreBoard.add((t.d1 = new DigitalDigit(0, t.size + t.size / 2)), false);
        scoreBoard.add(t.d2=new DigitalDigit(0,t.size+t.size/2),false);
        scoreBoard.add(t.d3=new DigitalDigit(0,t.size+t.size/2));
        scoreBoard.add(t.d4=new DigitalDigit(0,t.size+t.size/2));
        scoreBoard.add(t.d5=new DigitalDigit(0,t.size+t.size/2));
        spacer.setBackground(spacer.getParent().getBackground());


        PreviewPanel previewPanel = new PreviewPanel(t,new Dimension(45,45));
        t.addPreviewPanel(previewPanel);
        scoreBoard.add(previewPanel);
        frame.add("North", scoreBoard);
        frame.add("Center",t);
        frame.pack();
        frame.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){
                    case KeyEvent.VK_F1:
                        t.start();
                        break;
                    case KeyEvent.VK_P:
                        t.textPuzzleMode = t.textPuzzleMode?false:true;
                        System.out.println(t.textPuzzleMode);
                        break;
                    case KeyEvent.VK_LEFT:
                        t.move(t.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        t.move(t.RIGHT);
                        break;
                    case KeyEvent.VK_UP:
                        t.rotate(t.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        t.rotate(t.DOWN);
                        break;
                    case KeyEvent.VK_SPACE:
                        t.drop();
                        break;
                }
            }
        });
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sdim = t.getPreferredSize();
        frame.setLocation(dim.width/2-sdim.width/2,(dim.height/2-sdim.height/2)-45);
    }

}
