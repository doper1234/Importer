package Importer;

import DeviceDataManager.DeviceDataManager;
import Help.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 2017-07-01.
 */
public class ImporterUI extends JFrame{

    //ui components
    private JComboBox devicesComboBox;
    private JPanel panel1;
    private JComboBox optionComboBox;
    private JButton importButton;
    private JButton closeButton;
    private JPanel buttonsPanel;
    private JTree devicePhotosTree;
    private JButton showFilesButton;
    private JPanel fileExplorerPanel;
    private JTable filedataTable;
    private JProgressBar progressBar;
    private JLabel importLabel;
    private JPanel imagePreviewPanel;
    private JPanel destinationFolderPanel;
    private JTextField destinationFolderTextField;
    private JButton chooseFolderButton;
    private JPanel imagePreviewDataPanel;
    private JButton previewImageButton;
    private JPanel importPanel;
    private JButton refreshButton;
    private JLabel previewPictureLabel;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JButton newFolderButton;
    private JPanel tablePanel;
    private JButton dontShowDeviceButton;
    private JButton saveDeviceDirectoryButton;

    //other variables
    private DeviceDataManager _deviceDataManager;
    private Importer importer;
    private DefaultBoundedRangeModel model;
    private int iProgressMax;

    //column constants
    public final int CHECKBOX_COLUMN = 0;
    public final int IMAGE_COLUMN = 1;

    public ImporterUI(){
        super ("Import files");
        importer = new Importer();
        _deviceDataManager = new DeviceDataManager();
        initForm();
        initComboBoxes();
        initButtons();
        if(getDevice() >= 0)
            initTable();
        initDestinationFolderTextField();
    }

    private void initButtons() {

        newFolderButton.addActionListener(e -> createNewFolder());
        importButton.addActionListener(e -> importFiles());
        showFilesButton.addActionListener(e -> initTable());
        dontShowDeviceButton.addActionListener(e -> addDeviceToIgnore());
        saveDeviceDirectoryButton.addActionListener(e -> saveDeviceDirectory());
        closeButton.addActionListener(e -> close());
        previewImageButton.addActionListener(e -> previewImage());
        refreshButton.addActionListener(e -> initComboBoxes());
        chooseFolderButton.addActionListener(e -> chooseDestinationFolder());
        selectAllButton.addActionListener(e -> selectAll(true));
        deselectAllButton.addActionListener(e -> selectAll(false));
        devicesComboBox.addActionListener(e -> {
            initTable();
            initDestinationFolderTextField();
        });
    }

    private void importFiles(){
        String[] files = getSelectedFiles();
        if(files.length <=0){
            JOptionPane.showMessageDialog(this, "No files selected!");
        }
        else if(files[0].equalsIgnoreCase("No files found")){
            JOptionPane.showMessageDialog(this, "No files found on device!");
        }
        else if(BasicHelp.askQuestion("Import "+files.length+" files from " + devicesComboBox.getSelectedItem() + " to " +destinationFolderTextField.getText()+ "?", "Import")){
            importer.importFiles(getDevice(), this, destinationFolderTextField.getText(), files, files.length);
            importPanel.setVisible(true);
            initTable();
        }
    }

    private void close(){
        if(BasicHelp.askQuestion("Close importer?", "Exit")){
            dispose();
        }
    }

    private void selectAll(Boolean bSelectAll){
        for (int i = 0; i <filedataTable.getRowCount(); i++){
            filedataTable.setValueAt(bSelectAll, i, 0);
        }
    }

    private void previewImage(){
        int row = filedataTable.getSelectedRow();
        int column = filedataTable.getSelectedColumn();
        if(row != -1 && column != -1) {
            String fileName = (String) filedataTable.getValueAt(row, 1);

            ImageIcon imageIcon = _deviceDataManager.getPortableDeviceIcon(fileName, getDevice());
            previewPictureLabel.setIcon(imageIcon);
            imagePreviewPanel.repaint();
        }
        else{
            JOptionPane.showMessageDialog(this,"Select a row in the table first", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void createNewFolder(){
        if(BasicHelp.askQuestion("Create new folder in " + getPicturesPath() + "?", "Create new folder?")) {
            String sNewFolder;
            do {
                sNewFolder = JOptionPane.showInputDialog("Enter folder name:");
                if(sNewFolder == null)
                    break;
            } while (!BasicHelp.isValidFileName(sNewFolder, getPicturesPath()));
            if (sNewFolder != null) {
                BasicHelp.createDirectory(sNewFolder, getPicturesPath());
                if (BasicHelp.askQuestion("Set " + sNewFolder + " as new path for pictures?", " Set path?")) {
                    setPicturesPath(BasicHelp.fileName(getPicturesPath(), sNewFolder));
                }
            }
        }
    }

    private void saveDeviceDirectory(){
        String sName = _deviceDataManager.getDeviceName(getDevice());
        Settings.saveDeviceSettings(sName, false, destinationFolderTextField.getText());
    }

    private void addDeviceToIgnore(){
        if(BasicHelp.askQuestion("Are you sure you want to make this device invisible?", "Make device invisible to importer?")){
            String sName = _deviceDataManager.getDeviceName(getDevice());
            Settings.saveDeviceSettings(sName, true, null);
        }
    }

    private String getPicturesPath(){
        return destinationFolderTextField.getText();
    }

    private void setPicturesPath(String sPath){
        destinationFolderTextField.setText(sPath);
    }

    private int getDevice(){
        return devicesComboBox.getSelectedIndex();
    }

    private void chooseDestinationFolder(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(destinationFolderTextField.getText()));
        chooser.setDialogTitle("Select destination folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String sPath = chooser.getSelectedFile().getPath();
            destinationFolderTextField.setText(sPath);
            Logger.log("Picture path set to " + sPath);
        } else {
            Logger.log("No Selection ");
        }
    }

    private String[] getSelectedFiles(){

        List<String> files = new ArrayList<>();

        for (int i = 0; i <filedataTable.getRowCount(); i++){
            if((Boolean) filedataTable.getValueAt(i, 0)){
                files.add((String)filedataTable.getValueAt(i, 1));
            }
        }
        return files.stream().toArray(String[]::new);
    }

    /**
     * Initializes text for destinationFolderTextField. Either using the default pictures folder on the computer
     * or saved settings for the selected device.
     */
    private void initDestinationFolderTextField() {
        String sName = _deviceDataManager.getDeviceName(getDevice());
        PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(sName);
        String sDirectory;
        if (predefinedDevice != null) {
            sDirectory = predefinedDevice.pictureDirectory;
        } else {
            sDirectory = System.getProperty("user.home") + "\\Pictures\\";
        }
        destinationFolderTextField.setText(sDirectory);
        destinationFolderTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openFolderInExplorer();
            }
        });
    }

    private void openFolderInExplorer(){
        if(BasicHelp.askQuestion("Open path in explorer?", "Open path?")) {
            String sPath = destinationFolderTextField.getText();
            try {
                Desktop.getDesktop().open(new File(sPath));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ImporterUI.this, "Unable to open " + sPath, "Error", JOptionPane.ERROR_MESSAGE);
                Logger.log(ex.getStackTrace());
            }
        }
    }

    private void initForm(){
        progressBar.setVisible(false);
        importLabel.setVisible(false);
        setContentPane(panel1);
        setVisible(true);
        setFocusable(true);
        setSize(1000,1000);
        imagePreviewPanel.setSize(500,500);
        setLocationRelativeTo(null);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyCode.ESCAPE:
                        close();
                        break;
                }
            }
        });
    }

    private void initComboBoxes() {
        devicesComboBox.removeAllItems();
        for (String sDeviceName : _deviceDataManager.getPortableDeviceNames())
            devicesComboBox.addItem(sDeviceName);
        Boolean bEnabled = devicesComboBox.getItemCount() > 0;
        importButton.setEnabled(bEnabled);
        showFilesButton.setEnabled(bEnabled);
        previewImageButton.setEnabled(bEnabled);
        selectAllButton.setEnabled(bEnabled);
        deselectAllButton.setEnabled(bEnabled);
    }

   private void initTable() {
       Boolean bVisible = filedataTable.isVisible();
       if (bVisible) {
           List<DeviceFileInfo> dfi = _deviceDataManager.getDeviceFileNames(getDevice());
           Object[][] data = new Object[dfi.size()][5];
           for (int i = 0; i < dfi.size(); i++) {
               DeviceFileInfo deviceFileInfo = dfi.get(i);
               data[i] = new Object[]{true, deviceFileInfo.name, deviceFileInfo.fileSize, /*deviceFileInfo.image, */deviceFileInfo.fileType};
           }
           String[] columnNames = {"", "Name", "File Size", /*"Icon", */"File Type"};
           DefaultTableModel model = new DefaultTableModel(data, columnNames) {
               public Class<?> getColumnClass(int column) {
                   switch (column) {
                       case CHECKBOX_COLUMN:
                           return Boolean.class;
                       //TODO, find a way to add an icon to the table
                      /* case 1:
                       case 2:
                       case 4:
                           return String.class;
                       case 3:
                           return Icon.class;*/
                       default:
                           return String.class;
                   }
               }
           };

           filedataTable.setModel(model);
           filedataTable.addKeyListener(new KeyAdapter() {

               @Override
               public void keyPressed(KeyEvent e) {
                   int iRow = filedataTable.getSelectedRow();
                   int iColumn = filedataTable.getSelectedColumn();
                   switch (e.getKeyCode()) {
                       case KeyCode.ENTER:
                           if (iColumn == CHECKBOX_COLUMN){
                               Boolean bool = (Boolean) filedataTable.getValueAt(iRow, 0);
                               filedataTable.setValueAt(!bool, iRow, 0);
                           } else if (iColumn == IMAGE_COLUMN)
                               previewImage();
                   }
               }
           });
       }
   }

   public void startProgress(int iProgressMax) {
       this.iProgressMax = iProgressMax;
       importLabel.setVisible(true);
       progressBar.setVisible(true);
       model = new DefaultBoundedRangeModel();
       model.setMaximum(iProgressMax);
       progressBar.setModel(model);
       progressBar.setMaximum(iProgressMax);
       importLabel.setText("Importing 1 of " + iProgressMax);
   }

    public void updateProgressBar(String fileName, boolean bDelete){
        int iCurrentValue =model.getValue();
        model.setValue(iCurrentValue+1);
        String sLabel = bDelete ? "Deleting " : "Importing ";
        importLabel.setText(sLabel+iCurrentValue + " of " + iProgressMax + " : " + fileName);
    }

    public void resetProgress() {
        model.setValue(model.getMinimum());
        importLabel.setText("");
    }

    public void endProgress() {
        resetProgress();
        importPanel.setVisible(false);
    }
}
