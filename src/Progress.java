import javax.swing.*;

import static com.sun.glass.ui.Cursor.setVisible;

/**
 * Created by Chris on 2017-07-01.
 */
public class Progress extends JFrame{
    private final int iProgressMax;
    private JProgressBar progressBar;
    private JPanel panel1;
    private JLabel label;
    private DefaultBoundedRangeModel model;

    public Progress(int iProgressMax){
        this.iProgressMax = iProgressMax;
        model = new DefaultBoundedRangeModel();
        model.setMaximum(iProgressMax);
        progressBar.setModel(model);
        progressBar.setMaximum(iProgressMax);
        label.setText("Importing 1 of " +  iProgressMax);
        initForm();
    }

    private void initForm(){
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /*setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);*/
        setVisible(true);
        setLocationRelativeTo(null);
        setSize(500,100);

    }

    public void update(String fileName){
        int iCurrentValue =model.getValue();
        model.setValue(iCurrentValue+1);

        label.setText("Importing "+iCurrentValue + " of " + iProgressMax + " : " + fileName);
        if(iCurrentValue >= model.getMaximum())
            close();
    }

    public void close(){
        dispose();
    }

}
