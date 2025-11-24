package jmine;

/**
* @author: Fredrik Roos
*/

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    GamePanel game;
    GameTimer timer;

    public MainFrame(String title) {
        super(title);
        this.setDefaultCloseOperation(3);
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 112) {
                    MainFrame.this.game.reset();
                }

            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                JPanel topPanel = new JPanel(new FlowLayout(0));
                topPanel.add(new GameSettingsPanel());
                topPanel.add(MainFrame.this.timer = new GameTimer());
                MainFrame.this.add("North", topPanel);
                //MainFrame.this.add("Center", MainFrame.this.game = new GamePanel(new GameSettings(MainFrame.this.getContentPane().getSize(), 18, GameSettings.NUM_BOMBS_HARD)));
                MainFrame.this.add("Center", MainFrame.this.game = new GamePanel(new GameSettings()));
                MainFrame.this.game.addGameListener(new GameListener() {
                    public void gameStateChanged(GameEvent e) {
                        switch (e.getEvent()) {
                            case 0:
                                MainFrame.this.timer.start();
                            case 1:
                            default:
                                break;
                            case 2:
                                MainFrame.this.timer.stop();
                                break;
                            case 3:
                                MainFrame.this.timer.setTimeString("00:00:00");
                        }

                    }
                });
                MainFrame.this.pack();
                MainFrame.this.validate();
            }
        });
    }

    public static void main(String[] argv) {
        JFrame f = new MainFrame("JMine");
        f.setIconImage((new ImageIcon(f.getClass().getResource("/jmine/bomb.png"))).getImage());
        f.setSize(600, 500);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(dim.width / 2 - 300, dim.height / 2 - 250);
        f.setVisible(true);
    }
}
