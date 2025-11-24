package jmine;

import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameSettingsPanel extends JPanel {
    JComboBox bombBox;

    public GameSettingsPanel() {
        super(new FlowLayout(0));
        String[] bombs = new String[]{"10%", "15%", "20%", "25%", "30%"};
        this.bombBox = new JComboBox(bombs);
        this.add(new JLabel("Mines:"));
        this.add(this.bombBox);
    }
}
