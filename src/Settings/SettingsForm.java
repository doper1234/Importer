package Settings;

import Help.MyJFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Chris on 2017-10-01.
 */
public class SettingsForm extends MyJFrame {
    private JPanel mainPanel;
    private JPanel frequencyPanel;
    private JPanel devicesPanel;
    private JPanel optionsPanel;
    private JButton saveButton;
    private JButton cancelButton;
    private JScrollPane deviceScrollPane;
    private JTextField textField1;


    @Override
    public JPanel mainPanel() {
        return mainPanel;
    }
}


