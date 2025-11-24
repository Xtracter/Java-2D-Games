package jmine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

public class GameTimer extends JPanel implements Runnable {
    long time;
    Thread runner;
    Graphics g;
    Font f = new Font("Verdana", 1, 18);
    String lastTime;

    public GameTimer() {
        this.setBackground(Color.LIGHT_GRAY);
    }

    public void setTimeString(String str) {
        this.lastTime = str;
        this.repaint();
    }

    public void start() {
        this.time = System.currentTimeMillis();
        this.runner = new Thread(this);
        this.runner.start();
        this.repaint();
    }

    public void stop() {
        this.runner = null;
        this.repaint();
    }

    public void reset() {
    }

    public synchronized Dimension getPreferredSize() {
        return new Dimension(200, 40);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(this.f);
        if (this.runner != null) {
            g.setColor(Color.LIGHT_GRAY);
            GamePanel.drawCenteredString(this.lastTime = this.getTimeString(), 1, 1, this.getWidth(), this.getHeight(), g);
            g.setColor(Color.GREEN);
            GamePanel.drawCenteredString(this.lastTime = this.getTimeString(), 0, 0, this.getWidth(), this.getHeight(), g);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            GamePanel.drawCenteredString(this.lastTime != null ? this.lastTime : "00:00:00", 1, 1, this.getWidth(), this.getHeight(), g);
            g.setColor(Color.RED);
            GamePanel.drawCenteredString(this.lastTime != null ? this.lastTime : "00:00:00", 0, 0, this.getWidth(), this.getHeight(), g);
        }

    }

    protected String getTimeString() {
        long millis = System.currentTimeMillis() - this.time;
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return hms;
    }

    public void run() {
        while(this.runner != null) {
            try {
                this.repaint();
                Thread.sleep(1000L);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
