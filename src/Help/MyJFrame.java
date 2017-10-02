package Help;

import javax.swing.*;

/**
 * Created by Chris on 2017-10-01.
 */
public abstract class MyJFrame extends JFrame {

    public abstract JPanel mainPanel();

    public MyJFrame(String sTitle){
        super(sTitle);
        initForm();
    }

    public MyJFrame(){
        this("");
    }

    private void initForm(){
        setContentPane(mainPanel());
        setVisible(true);
        setFocusable(true);
        setSize(1000,1000);
        setLocationRelativeTo(null);
    }
}
