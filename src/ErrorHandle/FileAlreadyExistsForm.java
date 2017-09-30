package ErrorHandle;

import Help.BasicHelp;

import javax.swing.*;

/**
 * Created by Chris on 2017-07-03.
 */
public class FileAlreadyExistsForm {
    private JPanel optionsPanel;
    private JPanel compareFilesPanel;
    private JLabel imageInFileLabel;
    private JLabel imageOnDeviceLabel;
    private JPanel imageInFilePanel;
    private JPanel imageOnDevicePanel;
    public JPanel mainPanel;
    private JLabel imageOnDeviceIconLabel;
    private JLabel imageInFileIconLabel;
    private String rename;
    private String m_sPathToFileInDestination;
    private String m_sDirectory;
    private String oldName;

    public FileAlreadyExistsForm(String sDirectory, String fileInDestination, String fileOnDevice) {
        m_sDirectory = sDirectory;
        initForm();
        initButtons();
        initImages(fileInDestination, fileOnDevice);
        //JOptionPane.showConfirmDialog(null, mainPanel, "JOptionPane Example : ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        //JOptionPane.showOptionDialog();
}

    private void initForm() {

        //setContentPane(mainPanel);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setVisible(true);
        //setFocusable(true);
        //setLocationRelativeTo(null);
        //setSize(500,500);
    }

    private void initImages(String sPathToFileInDestination, String sPathToFileOnDevice) {

        m_sPathToFileInDestination = sPathToFileInDestination;

        ImageIcon imageIconDestination = BasicHelp.resizedImage(sPathToFileInDestination, 120,120);
        if(imageIconDestination != null) {
            imageInFileIconLabel.setIcon(imageIconDestination);
            imageInFilePanel.repaint();
        }

        ImageIcon imageIconDevice =  BasicHelp.resizedImage (sPathToFileOnDevice, 120,120);
        if(imageIconDevice != null) {
            imageOnDeviceIconLabel.setIcon(imageIconDevice);
            imageOnDevicePanel.repaint();
        }
    }

    private void initButtons(){
       /* yesButton.addActionListener(e -> exit(0));
        noButton.addActionListener(e -> exit(1));
        renameButton.addActionListener(e -> exit(2));*/
    }

    public int getOption(){

        int iOption = JOptionPane.showOptionDialog(null, mainPanel, "Attention!",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Yes", "No", "Rename"}, null);

        if(iOption == JOptionPane.CANCEL_OPTION) {
            iOption = JOptionPane.YES_OPTION;

            String sNewName = null;
            do {
                sNewName = JOptionPane.showInputDialog("Enter new file name");
                if (sNewName == null)
                    break;
            }
            while (!BasicHelp.renameFile(m_sDirectory, getOldName(), sNewName));
        }

        return (iOption);

    }


    private void exit(int iOption) {
        switch (iOption) {
            case 0:
                //yes, overwrite
                break;
            case 1:
                //no, don't copy
                break;
            case 2:
                break;
        }
    }

    public String getRename() {
        return rename;
    }

    public String getOldName() {
        String[] split = m_sPathToFileInDestination.split("\\\\");

        return split[split.length-1];
    }
}
