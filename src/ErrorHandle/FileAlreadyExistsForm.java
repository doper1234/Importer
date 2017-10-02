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
    private String m_sPathToFileInDestination;
    private String m_sDirectory;

    public FileAlreadyExistsForm(String sDirectory, String fileInDestination, String fileOnDevice) {
        m_sDirectory = sDirectory;
        initImages(fileInDestination, fileOnDevice);
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

    public int getOption() {

        int iOption = JOptionPane.showOptionDialog(null, mainPanel, "Attention!",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Yes", "No", "Rename"}, null);

        if (iOption == JOptionPane.CANCEL_OPTION) {
            iOption = JOptionPane.YES_OPTION;

            String sNewName;
            do {
                sNewName = JOptionPane.showInputDialog("Enter new file name");
                if (sNewName == null)
                    break;
            }
            while (!BasicHelp.renameFile(m_sDirectory, getOldName(), sNewName));
        }
        return (iOption);
    }

    public String getOldName() {
        String[] split = m_sPathToFileInDestination.split("\\\\");

        return split[split.length-1];
    }
}
