package jmine;

public class GameEvent {
    public static final int START = 0;
    public static final int CLICK = 1;
    public static final int GAME_OVER = 2;
    public static final int GAME_RESET = 3;
    private int event;
    private GamePanel gamePanel;

    public GameEvent(GamePanel gamePanel, int event) {
        this.gamePanel = gamePanel;
        this.event = event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getEvent() {
        return this.event;
    }

    public GamePanel getGamePanel() {
        return this.gamePanel;
    }
}
