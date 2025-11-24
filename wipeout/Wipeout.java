package wipeout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Vector;

/**
 * Created by NRKFRR on 2015-11-25.
 */
public class Wipeout extends JPanel {

    public static String TITLE = "W I P E O U T";
    final String desc = "P R E S S  S P A C E  T O  B E G I N";

    long speed = 13;
    Thread runner;
    int origPaddleWith = 120;
    int paddleWidth = origPaddleWith;
    int paddleHeight = 20;
    Rectangle paddle;
    int xAdd,yAdd;
    int paddleSpeed = 8;
    boolean paddleLeft = false;
    boolean paddleRight = false;
    int barWidth = 70;
    int barHeight = 24;
    int barSpace = 6;
    Vector<Bar> bars;
    Color barColors[] = {Color.RED,Color.RED,Color.ORANGE,Color.ORANGE,Color.GREEN,Color.GREEN,Color.YELLOW,Color.YELLOW,};
    Integer points[] = {7,7,5,5,3,3,1,1};
    int score=0;
    boolean easyMode=false;
    int totalBarsHit = 0;
    int orangeHit = 0;
    int redHit = 0;
    int lifes = 3;
    boolean gameOver=true;
    boolean ballReleased=false;
    Ball ball;
    Font font = new Font("Lucida Console", Font.BOLD, 60);
    Font smallfont = new Font("Lucida Console", Font.BOLD, 20);
    int barDrawIter=0;
    int marqueeIter=0;
    int nollBounceIter=0;
    int level = 1;

    class Bar extends Rectangle {
        int arc = 10;
        int points = 0;
        private boolean visible = true;
        boolean wall=false;
        Color blink[];
        Bar(int x, int y, int width, int height){
            this(x,y, width, height, Color.GRAY,0);
            wall=true;
        }
        Bar(int x, int y, int width, int height, Color c, int p){
            super(x, y, width, height);
            this.c = c;
            this.points=p;
            blink = new Color[]{Color.RED,c};
        }

        void setVisible(boolean v){
            if(!wall && !v){
               this.visible=false;
            }
        }

        Color c = Color.WHITE;
        void draw(Graphics g){
            if(visible) {
                g.setColor(Color.GRAY);
                g.fillRoundRect(x + 3, y + 3, width, height, arc, arc);
                g.setColor(c);
                g.fillRoundRect(x, y, width, height, arc, arc);
                g.fillRoundRect(x, y, width, height, arc, arc);
                g.setColor(Color.BLACK);
                g.drawRoundRect(x, y, width, height, arc, arc);


                g.setColor(c);
                g.fillRoundRect(x + 6, y + 6, width - 12, height - 12, arc, arc);
                g.setColor(Color.BLACK);
                g.drawRoundRect(x + 6, y + 6, width - 12, height - 12, arc, arc);
                g.setColor(Color.WHITE);
                g.drawLine(x + 10, y + height / 2, x + width - 10, y + height / 2);
            }
        }
    }

    class Ball extends Point {
        Ball(int x, int y){
            super(x,y);
        }
        int size = 12;
    }

    public Wipeout(){
        setBackground(Color.LIGHT_GRAY);
    }

    public void start(){
        paddleWidth = origPaddleWith;
        lifes = 3;
        score=0;
        paddle = new Rectangle(getWidth()/2- paddleWidth /2,getHeight()-70, paddleWidth,paddleHeight);
        ball = new Ball(getWidth()/2,paddle.y-20);
        xAdd=1;
        yAdd=3;
        bars = getBars();
        barDrawIter=0;
        ballReleased=false;
        runner = new Thread(new Runnable() {
            @Override
            public void run() {
                while(Thread.currentThread()==runner && runner!=null){
                    try{
                        Thread.sleep(speed);
                        tick();
                        repaint();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        runner.start();
    }

    void tick(){
        if(ballReleased) {
            ball.y -= yAdd;
            ball.x += xAdd;
            bounceOfPaddle(ball);
            bounceOffWalls(ball);
            bounceOfBar(ball);
            setScore(score);
        }else{
            stickBallToPaddle(ball);
        }

        if(paddleLeft && paddle.x-paddleSpeed>=0){
            paddle.x-=paddleSpeed;
        }
        if(paddleRight && paddle.x+paddle.width+paddleSpeed<=getWidth()){
            paddle.x+=paddleSpeed;
        }
    }

    public void setEasyMode(boolean e){
        this.easyMode=e;
        repaint();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        g.setColor(Color.GRAY);
        g.fillRoundRect(6, 6, getWidth() - 12, 20, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(6, 6, getWidth() - 12, 20, 10, 10);

        if(paddle!=null) {
            g.setColor(Color.GRAY);
            g.fillRoundRect(paddle.x + 3, paddle.y + 3, paddle.width, paddle.height, 10, 10);
            if(!easyMode){
                g.setColor(Color.GREEN.darker());
            }else{
                g.setColor(Color.PINK);
            }
            g.fillRoundRect(paddle.x, paddle.y, paddle.width, paddle.height, 10, 10);
            g.fillRoundRect(paddle.x, paddle.y, paddle.width, paddle.height, 10, 10);
            g.setColor(Color.BLACK);
            g.drawRoundRect(paddle.x, paddle.y, paddle.width, paddle.height, 10, 10);

            g.setColor(Color.GRAY);
            g.fillRoundRect(paddle.x + 6, paddle.y + 6, paddle.width - 12, paddle.height - 12, 10, 10);
            g.setColor(Color.BLACK);
            g.drawRoundRect(paddle.x + 6, paddle.y + 6, paddle.width - 12, paddle.height - 12, 10, 10);
            g.setColor(Color.WHITE);
            g.drawLine(paddle.x + 10, paddle.y + paddle.height / 2, paddle.x + paddle.width - 10, paddle.y + paddle.height / 2);
        }

        if(ball!=null) {
            g.setColor(Color.GRAY);
            g.fillOval((ball.x - ball.size / 2) + 2, (ball.y - ball.size / 2) + 2, ball.size, ball.size);

            g.setColor(Color.CYAN.darker());
            g.fillOval(ball.x - ball.size / 2, ball.y - ball.size / 2, ball.size, ball.size);
            g.setColor(Color.BLACK);
            g.drawOval(ball.x - ball.size / 2, ball.y - ball.size / 2, ball.size, ball.size);

            g.setColor(Color.LIGHT_GRAY);
            g.fillOval(ball.x,ball.y-4 ,4,4);

            //g.setColor(Color.BLACK);
            //g.drawRect(ball.x - ball.size / 2, ball.y - ball.size / 2, ball.size,ball.size);
            
        }

        if(bars!=null){
            for(int i = 0; i < bars.size(); i++){
                if(i<barDrawIter) {
                    bars.get(i).draw(g);
                }
            }
            if(barDrawIter<bars.size()){
                barDrawIter++;
            }
        }
        if(bars!=null && !ballReleased && barDrawIter > bars.size()-1 && gameOver) {
            g.setFont(font);
            int sw = g.getFontMetrics().stringWidth(TITLE);
            g.setColor(Color.GRAY);
            g.drawString(TITLE, (getWidth() / 2 - sw / 2) + 4, (getHeight() / 2) + 34);
            g.setColor(Color.RED);
            g.drawString(TITLE, getWidth() / 2 - sw / 2, (getHeight() / 2) + 30);

            g.setFont(smallfont);
            g.setColor(Color.GRAY);
            sw = g.getFontMetrics().stringWidth(desc);
            g.drawString(desc, getWidth() / 2 - sw / 2, (getHeight() / 2) + 80);
            g.setColor(Color.BLACK);
            int ss = g.getFontMetrics().stringWidth("P R E S S  ");
            if(marqueeIter++ > 20) {
                g.drawString("S P A C E", (getWidth() / 2 - sw / 2) + ss, (getHeight() / 2) + 80);
                if(marqueeIter>40) marqueeIter=0;
            }
        }
        if(!gameOver) {
            for (int i = 1; i < lifes + 1; i++) {
                if (!easyMode) {
                    g.setColor(Color.GREEN.darker());
                } else {
                    g.setColor(Color.PINK);
                }
                g.fillRoundRect(getWidth() - 34, getHeight() - (14 * i), 30, 10, 4, 4);
                g.setColor(Color.BLACK);
                g.drawRoundRect(getWidth() - 34, getHeight() - (14 * i), 30, 10, 4, 4);
                String l = ""+i;
                g.setFont(new Font("Verdana", Font.BOLD,10));
                int lw = g.getFontMetrics().stringWidth(l);
                g.drawString(l, getWidth() - 24, getHeight() - (14 * i)+9);
            }
        }
    }

    Vector<Bar> getBars(){

        Vector<Bar> bars = new Vector<Bar>();
        int cols = 11;
        for(int r = barColors.length-1; r >= 0; r--){
            for(int c = 0; c < cols; c++){
                int x = (c*barWidth) + barSpace;
                int y = (r*barHeight) + barSpace;
                if(level>1 && new Random().nextInt((level*20)-100)==10){
                    bars.add(new Bar(x,y+60,barWidth-barSpace,barHeight-barSpace));
                }else{
                    bars.add(new Bar(x,y+60,barWidth-barSpace,barHeight-barSpace, barColors[r],points[r]));
                }
            }
        }
        return bars;
    }

    void stickBallToPaddle(Ball b){

        b.x = paddle.x + (paddle.width/2);
        b.y = paddle.y-(b.size/2)-2;

    }

    int first = 0;
    void releaseBall(){
        if(ballReleased) return;
        if(gameOver){
            if(first>0) {
                barDrawIter = 0;
            }
            xAdd=1;
            yAdd=3;
            bars = getBars();
            paddleWidth = origPaddleWith;
            paddle.width = paddleWidth;
            level=1;
        }
        gameOver=false;
        lifes--;
        ballReleased=true;
        first++;
    }

    void resizePaddle(){
        paddleWidth = paddleWidth / 2;
        paddle.width= paddleWidth;
    }

    void bounceOfBar(Ball b){

        for(Bar bar:bars){
            if(bar.visible){
                if(b.x+b.size>=bar.x && b.x<=bar.x+bar.width && b.y>=bar.y && b.y-b.size<bar.y+bar.height){
                    bar.setVisible(false);
                    score+=bar.points;
                    totalBarsHit++;
                    if(bar.c==Color.ORANGE) {
                        orangeHit++;
                    }
                    if(bar.c==Color.RED) {
                        redHit++;
                    }
                    if(totalBarsHit==4 || totalBarsHit == 12){
                        if(!easyMode) yAdd = yAdd+1;
                    }
                    if(orangeHit==1){
                        orangeHit++;
                        if(!easyMode) yAdd = yAdd+1;
                    }
                    if(redHit==1){
                        redHit++;
                        if(!easyMode) resizePaddle();
                    }
                    Toolkit.getDefaultToolkit().beep();
                    yAdd = yAdd - (yAdd*2);
                }
            }
        }
    }

    void bounceOfPaddle(Ball b){
        if(b.x+b.size>=paddle.x && b.x<=paddle.x+paddle.width && b.y+b.size/2>=paddle.y && b.y+b.size<paddle.y+paddle.height){
            yAdd = yAdd - (yAdd*2);
            if(ball.x + b.size >= paddle.x && ball.x <= paddle.x+10){
                xAdd = xAdd - (xAdd*2);
            }
            if(ball.x + ball.size >= paddle.x + paddle.width-10 && ball.x<paddle.x+paddle.width){
                xAdd = xAdd - (xAdd*2);
            }
            if(paddleLeft){
                xAdd++;
            }
            if(paddleRight){
                xAdd--;
            }
            if(xAdd==0) nollBounceIter++;
            if(nollBounceIter>3){
                xAdd += 1;
                nollBounceIter=0;
            }
            Toolkit.getDefaultToolkit().beep();
        }
    }

    void bounceOffWalls(Ball b){

        if(b.y-(b.size/2)<=26){
            yAdd = yAdd - (yAdd*2);
        }
        if(b.x+(b.size/2)>=getWidth()){
            xAdd = xAdd - (xAdd*2);
        }
        if(b.x-(b.size/2)<=0){
            xAdd = xAdd - (xAdd*2);
        }
        if(b.y>=getHeight()){
            stickBallToPaddle(b);
            ballReleased=false;
            if(yAdd<0){
                yAdd = yAdd - (yAdd*2);
            }
            if(lifes<1){
                gameOver=true;
                lifes=3;
            }
        }

    }
    Wipeout.Spacer topSpacer;
    DigitalDigit d1,d2,d3,d4;

    void setScore(int score){
        String s = ""+score;
        if(s.length()==1) {
            d4.setDigit(Integer.parseInt("" + s.charAt(0)));
        }else
        if(s.length()==2) {
            d3.setDigit(Integer.parseInt("" + s.charAt(0)));
            d4.setDigit(Integer.parseInt("" + s.charAt(1)));
        }
        else if(s.length()==3) {
            d2.setDigit(Integer.parseInt("" + s.charAt(0)));
            d3.setDigit(Integer.parseInt("" + s.charAt(1)));
            d4.setDigit(Integer.parseInt("" + s.charAt(2)));
        }else if(s.length()==4) {
            d4.setDigit(Integer.parseInt("" + s.charAt(3)));
            d3.setDigit(Integer.parseInt("" + s.charAt(2)));
            d2.setDigit(Integer.parseInt("" + s.charAt(1)));
            d1.setDigit(Integer.parseInt("" + s.charAt(0)));
        }
    }

    Image getIconImage(){
        Image img=new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 32, 32);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 31, 31);
        g.setColor(Color.GREEN);
        g.fillRoundRect(3, 10, 24, 10, 2, 2);
        g.setColor(Color.BLACK);
        g.drawRoundRect(3, 10, 24, 10, 2, 2);
        g.setColor(Color.WHITE);
        g.drawRoundRect(6, 12, 18, 4, 2, 2);
        return img;
    }

    public static void main(String argv[]) throws Exception{

        final Wipeout w = new Wipeout();
        UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());

        JFrame f = new JFrame(Wipeout.TITLE);
        f.setIconImage(w.getIconImage());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add("North", (w.topSpacer = new Wipeout.Spacer(120, 40)));
        f.add("West", new Wipeout.Spacer(120, 60));
        f.add("Center", w);
        f.add("East", new Wipeout.Spacer(120, 100));
        f.add("South", new Wipeout.Spacer(120, 30));
        f.setResizable(false);
        f.setSize(1024, 768);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(screen.width / 2 - 512, screen.height / 2 - 384);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scorePanel.setBackground(Color.BLACK);
        scorePanel.add(w.d1 = new DigitalDigit(0, 30));
        scorePanel.add(w.d2 = new DigitalDigit(0, 30));
        scorePanel.add(w.d3 = new DigitalDigit(0, 30));
        scorePanel.add(w.d4 = new DigitalDigit(0, 30));
        w.topSpacer.add(scorePanel);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                w.start();
            }
        });
        f.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F1:
                        w.start();
                        break;
                    case KeyEvent.VK_F2:
                        break;
                    case KeyEvent.VK_E:
                        w.setEasyMode(w.easyMode ? false : true);
                        break;
                    case KeyEvent.VK_SPACE:
                        w.releaseBall();
                        break;
                    case KeyEvent.VK_LEFT:
                        w.paddleLeft = true;
                        w.paddleRight = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        w.paddleLeft = false;
                        w.paddleRight = true;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        w.paddleLeft = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        w.paddleRight = false;
                        break;
                }
            }
        });
        f.setVisible(true);
    }

    static class Spacer extends JPanel {
        Dimension dim;
        boolean marquee=false;
        public Spacer(int w, int h){
            super(new FlowLayout(FlowLayout.CENTER));
            setBackground(Color.GRAY);
            dim = new Dimension(w,h);
        }
        public synchronized Dimension getPreferredSize(){
            return dim;
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            if(marquee){

            }
        }

    }

}













