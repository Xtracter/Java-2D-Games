package jmine;

import java.awt.Dimension;
import java.io.Serializable;

public class GameSettings implements Serializable {

    private int rows;
    private int cols;
    private int blockSize;
    private int bombs;
    private boolean centered = false;
    public static double NUM_BOMBS_EASY = 0.1;
    public static double NUM_BOMBS_MEDIUM = 0.15;
    public static double NUM_BOMBS_HARD = 0.2;
    private double bombPercent;

    public GameSettings() {
        this.bombPercent = NUM_BOMBS_MEDIUM;
        this.rows = 12;
        this.cols = 12;
        this.blockSize = 18;
        this.bombs = (int)((double)(this.rows * this.cols) * this.bombPercent);
    }

    public GameSettings(Dimension dim, int blockSize, double bombPercent) {
        this.bombPercent = NUM_BOMBS_MEDIUM;
        double dr = dim.getHeight() / (double)blockSize;
        double dc = dim.getWidth() / (double)blockSize;
        this.rows = (int)dr;
        this.cols = (int)dc;
        this.blockSize = blockSize;
        this.bombPercent = bombPercent;
        this.bombs = (int)((double)(this.rows * this.cols) * this.bombPercent);
    }

    public GameSettings(int rows, int cols, int blockSize, double bombPercent) {
        this.bombPercent = NUM_BOMBS_MEDIUM;
        this.rows = rows;
        this.cols = cols;
        this.blockSize = blockSize;
        this.bombPercent = bombPercent;
        this.bombs = (int)((double)(rows * cols) * this.bombPercent);
    }

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getBombs() {
        return this.bombs;
    }

    public void setCenteredLayout(boolean b) {
        this.centered = b;
    }

    public boolean isCenteredLayout() {
        return this.centered;
    }
}
