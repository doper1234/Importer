import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Chris on 2017-07-06.
 */
public class ExceptionForm extends JFrame{
    private final String message;
    private final Class<? extends Exception> aClass;
    private final Exception exception;
    private JPanel headerPanel;
    private JPanel causePanel;
    private JPanel stacktracePanel;
    private JLabel exceptionTypLabel;
    private JTextArea exceptionStacktraceArea;
    private JLabel causeLabel;
    private JPanel mainPanel;

    public ExceptionForm(Exception e){
        this.exception = e;
        this.message = e.getMessage();
        this.aClass = e.getClass();
        initForm();
        initExceptionData();
    }

    private void initExceptionData() {
        exceptionTypLabel.setText(aClass.getName());
        System.out.println(message);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        System.out.println(sw.toString());
        exceptionStacktraceArea.append(sw.toString());

    }

    private void initForm() {
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setFocusable(true);
        setLocationRelativeTo(null);
        setSize(500,500);
    }

}
