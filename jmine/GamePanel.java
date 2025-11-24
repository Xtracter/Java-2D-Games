package jmine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    int x;
    int y;
    int _x;
    int _y;
    int s;
    int rows;
    int cols;
    boolean gameOver;
    int bombCount;
    boolean centered;
    int clicks;
    static final int MARK = 1;
    static final int UNMARK = 2;
    static final int CLEAR = 3;
    static final int DRAW = 4;
    static final int RESET = 5;
    static final int BOOM = 6;
    Font font;
    Color[] colors;
    List<List<Rect>> rects;
    List<GameListener> listeners;

    public GamePanel() {
        this(new GameSettings());
    }

    public synchronized Dimension getPreferredSize() {
        return new Dimension(this.cols * this.s + this.cols, this.rows * this.s + this.rows);
    }

    public GamePanel(GameSettings settings) {
        this.s = 18;
        this.rows = 24;
        this.cols = 24;
        this.gameOver = false;
        this.bombCount = 100;
        this.centered = false;
        this.clicks = 0;
        this.font = new Font("Verdana", 1, 14);
        this.colors = new Color[]{Color.BLUE, Color.GREEN, Color.RED};
        this.rects = new ArrayList();
        this.listeners = new ArrayList();
        this.rows = settings.getRows();
        this.cols = settings.getCols();
        this.s = settings.getBlockSize();
        this.bombCount = settings.getBombs();
        this.centered = settings.isCenteredLayout();
        this.setBackground(Color.LIGHT_GRAY);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (GamePanel.this.gameOver) {
                    GamePanel.this.reset();
                } else {
                    if (GamePanel.this.clicks++ == 0) {
                        GamePanel.this.fireEvent(0);
                    }

                    GamePanel.this.mark(e.getX(), e.getY(), 1);
                    GamePanel.this.repaint();
                }
            }
        });
        this.reset();
    }

    public void reset(GameSettings settings) {
        this.rows = settings.getRows();
        this.cols = settings.getCols();
        this.s = settings.getBlockSize();
        this.bombCount = settings.getBombs();
        this.centered = settings.isCenteredLayout();
        this.reset();
    }

    public void reset() {
        this.clicks = 0;
        this.gameOver = false;
        this.mark(2, (Object)null);
        this.setField();
        this.repaint();
        this.fireEvent(3);
    }

    private Point getCenter() {
        Point p = new Point();
        p.x = (this.getWidth() - this.rows * this.s) / 2;
        p.y = (this.getHeight() - this.cols * this.s) / 2;
        return p;
    }

    private void setField() {
        if (this.centered) {
            this._x = this.getCenter().x;
            this._y = this.getCenter().y;
        }

        this.x = this._x;
        this.y = this._y;
        List<List<Rect>> rs = new ArrayList();

        for(int i = 0; i < this.rows; ++i) {
            List<Rect> row = new ArrayList();

            for(int j = 0; j < this.cols; ++j) {
                if (this.rects.size() > 0) {
                    try {
                        row.add(new Rect(j + this.x, this.y, this.s, this.s, i, j, ((Rect)((List)this.rects.get(i)).get(j)).marked));
                    } catch (Exception var6) {
                        row.add(new Rect(j + this.x, this.y, this.s, this.s, i, j, false));
                    }
                } else {
                    row.add(new Rect(j + this.x, this.y, this.s, this.s, i, j, false));
                }

                this.x += this.s;
            }

            this.x = 0 + this._x;
            this.y += this.s + 1;
            rs.add(row);
        }

        this.rects = rs;
        this.randomizeBombs(this.bombCount);

        for(int i = 0; i < this.rows; ++i) {
            for(int j = 0; j < this.cols; ++j) {
                this.countBombBuddies((Rect)((List)this.rects.get(i)).get(j));
            }
        }

    }

    private void mark(int mode, Object obj) {
        this.mark(this.x, this.y, mode, obj);
    }

    private void mark(int x, int y, int mode) {
        this.mark(x, y, mode, (Object)null);
    }

    private void mark(int x, int y, int mode, Object obj) {
        for(List<Rect> row : this.rects) {
            for(Rect r : row) {
                switch (mode) {
                    case 1:
                        if (r.marked || this.gameOver) {
                            break;
                        }

                        if (r.contains(x, y) && !r.bomb) {
                            r.marked = true;
                            this.markBuddies(r);
                            this.fireEvent(1);
                        } else if (r.contains(x, y) && r.bomb) {
                            this.mark(6, (Object)null);
                            this.fireEvent(2);
                        }
                        break;
                    case 2:
                        r.marked = false;
                        break;
                    case 3:
                        r.marked = false;
                        break;
                    case 4:
                        Graphics g = (Graphics)obj;
                        g.setColor(Color.GRAY);
                        if (r.marked) {
                            g.setColor(this.getBackground());
                            g.fillRect(r.x, r.y, r.width, r.height);
                            g.setColor(Color.GRAY);
                            g.drawRect(r.x, r.y, r.width, r.height);
                            if (r.bomb) {
                                g.drawImage(r.bombImg.getImage(), r.x, r.y, this.s, this.s, this);
                            } else if (r.bombNr > 0) {
                                g.setColor(r.bombNr < 4 ? this.colors[r.bombNr - 1] : Color.WHITE);
                                drawCenteredString("" + r.bombNr, r.x, r.y, this.s, this.s, g);
                            }
                        } else {
                            g.setColor(Color.LIGHT_GRAY);
                            g.fill3DRect(r.x, r.y, r.width, r.height, true);
                        }
                    case 5:
                    default:
                        break;
                    case 6:
                        if (r.bomb) {
                            r.marked = true;
                        }

                        this.gameOver = true;
                }
            }
        }

    }

    private void randomizeBombs(int num) {
        for(int i = 0; i < num; ++i) {
            int row;
            int col;
            do {
                Random rand = new Random();
                row = rand.nextInt(this.rows);
                col = rand.nextInt(this.cols);
            } while(((Rect)((List)this.rects.get(row)).get(col)).bomb);

            ((Rect)((List)this.rects.get(row)).get(col)).bomb = true;
        }

    }

    private void markBuddies(Rect r) {
        List<Rect> marked = new ArrayList();

        for(int[] is : this.getBuddies(r)) {
            try {
                Rect rr = (Rect)((List)this.rects.get(is[0])).get(is[1]);
                if (!rr.bomb && !rr.marked) {
                    rr.marked = true;
                    marked.add(rr);
                }
            } catch (Exception var7) {
            }
        }

        for(Rect rr : marked) {
            if (rr.bombNr == 0) {
                this.markBuddies(rr);
            }
        }

    }

    private void countBombBuddies(Rect r) {
        List<int[]> ints = this.getBuddies(r);
        int n = 0;

        for(int[] is : ints) {
            try {
                Rect rr = (Rect)((List)this.rects.get(is[0])).get(is[1]);
                if (rr.bomb) {
                    ++n;
                }
            } catch (Exception var7) {
            }
        }

        r.bombNr = n;
    }

    private List<int[]> getBuddies(Rect r) {
        int row = r.row;
        int col = r.col;
        List<int[]> ints = new ArrayList();
        ints.add(new int[]{row - 1, col - 1});
        ints.add(new int[]{row - 1, col});
        ints.add(new int[]{row - 1, col + 1});
        ints.add(new int[]{row, col - 1});
        ints.add(new int[]{row, col + 1});
        ints.add(new int[]{row + 1, col - 1});
        ints.add(new int[]{row + 1, col});
        ints.add(new int[]{row + 1, col + 1});
        return ints;
    }

    public static void drawCenteredString(String s, int _x, int _y, int w, int h, Graphics g) {
        try {
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(s)) / 2 + _x;
            int y = fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2 + _y;
            g.drawString(s, x, y);
        } catch (Exception var9) {
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(this.font);
        this.mark(4, g);
        g.setColor(Color.BLACK);
        if (this.rects.size() > 0) {
            g.drawRect(((Rect)((List)this.rects.get(0)).get(0)).x - 1, ((Rect)((List)this.rects.get(0)).get(0)).y - 1, this.cols * this.s + this.cols, this.rows * this.s + this.rows);
        }

    }

    public void addGameListener(GameListener gl) {
        this.listeners.add(gl);
    }

    public void removeGameListener(GameListener gl) {
        this.listeners.remove(gl);
    }

    protected void fireEvent(int event) {
        for(GameListener gl : this.listeners) {
            gl.gameStateChanged(new GameEvent(this, event));
        }

    }

    private class Rect extends Rectangle {
        int row;
        int col;
        ImageIcon bombImg;
        int bombNr = 0;
        boolean marked = false;
        boolean bomb = false;

        Rect(int x, int y, int width, int height, int r, int c, boolean marked) {
            super(x, y, width, height);
            this.row = r;
            this.col = c;
            this.marked = marked;
            this.bombImg = new ImageIcon(this.getClass().getResource("/jmine/bomb.png"));
        }

        public boolean equals(Object o) {
            if (o != null && o instanceof Rect) {
                Rect r = (Rect)o;
                return r.row == this.row && r.col == this.col;
            } else {
                return false;
            }
        }
    }
}
