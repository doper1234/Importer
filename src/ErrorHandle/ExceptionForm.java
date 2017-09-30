package ErrorHandle;

import Help.Logger;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Chris on 2017-07-06.
 */
public class ExceptionForm extends JFrame{
    private UnsatisfiedLinkError unsatisfiedLinkError;
    private String message;
    private Throwable m_Throwable;
    private JTextArea exceptionStacktraceArea;
    private JPanel mainPanel;

    public ExceptionForm(Throwable Throwable){
        m_Throwable = Throwable;
        message = Throwable.getMessage();
        initForm();
        initExceptionData();
    }

    private void initExceptionData() {

        Logger.log(message);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if(m_Throwable != null)
            m_Throwable.printStackTrace(pw);
        else if(unsatisfiedLinkError !=null)
            unsatisfiedLinkError.printStackTrace(pw);
        Logger.log(sw.toString());
        exceptionStacktraceArea.append(sw.toString());

    }

    private void initForm() {
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setFocusable(true);
        setSize(500,500);
        setLocationRelativeTo(null);
    }

}
