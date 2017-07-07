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
    private JComboBox devicesComboBox;
    private JPanel panel1;
    private JComboBox optionComboBox;
    private JButton importButton;
    private JButton closeButton;
    private JPanel buttonsPanel;
    private JPanel titlePanel;
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

    private Importer importer;
    private DefaultBoundedRangeModel model;
    private int iProgressMax;

    public ImporterUI(){
        super ("Import files");
        importer = new Importer();
        initForm();
        initComboBoxes();
        initButtons();
        if(getDevice() >= 0)
            initTable();
        initOpenFileOnClick();
    }

    private void initButtons() {

        newFolderButton.addActionListener(e -> createNewFolder());
        importButton.addActionListener(e -> importFiles());
        showFilesButton.addActionListener(e -> initTable());
        closeButton.addActionListener(e -> close());
        previewImageButton.addActionListener(e -> previewImage());
        refreshButton.addActionListener(e -> initComboBoxes());
        chooseFolderButton.addActionListener(e -> choosDestinationFolder());
        selectAllButton.addActionListener(e -> selectAll(true));
        deselectAllButton.addActionListener(e -> selectAll(false));
    }


    private void importFiles(){
        String[] files = getSelectedFiles();
        if(files.length <=0){
            JOptionPane.showMessageDialog(this, "No files selected!");
        }
        else if(BasicHelp.askQuestion("Import "+files.length+" files from " + devicesComboBox.getSelectedItem() + "?", "Import")){
            importer.importFiles(getDevice(), this, destinationFolderTextField.getText(), files, files.length);
            importPanel.setVisible(false);
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

            ImageIcon imageIcon = importer.getPortableDeviceIcon(fileName, getDevice());
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

    private String getPicturesPath(){
        return destinationFolderTextField.getText();
    }

    private void setPicturesPath(String sPath){
        destinationFolderTextField.setText(sPath);
    }

    private int getDevice(){
        return devicesComboBox.getSelectedIndex();
    }

    private void choosDestinationFolder(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(destinationFolderTextField.getText()));
        chooser.setDialogTitle("Select destination folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationFolderTextField.setText(chooser.getSelectedFile().getPath());
            System.out.println("getCurrentDirectory(): "
                    + chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "
                    + chooser.getSelectedFile());
        } else {
            System.out.println("No Selection ");
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

    private void initOpenFileOnClick(){
        destinationFolderTextField.setText(System.getProperty("user.home") + "\\Pictures\\");
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
                ex.printStackTrace();
            }
        }
    }

    private void initForm(){
        progressBar.setVisible(false);
        importLabel.setVisible(false);
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setFocusable(true);
        setLocationRelativeTo(null);
        setSize(1000,500);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){

                    case 27://escape
                        close();
                        break;
                }
            }
        });
    }

    private void initComboBoxes() {

        devicesComboBox.removeAllItems();
        for (String sDeviceName : importer.getPortableDeviceNames()) {
            devicesComboBox.addItem(sDeviceName);
        }

        Boolean bEnabled = devicesComboBox.getItemCount() > 0;
        importButton.setEnabled(bEnabled);
        showFilesButton.setEnabled(bEnabled);
        previewImageButton.setEnabled(bEnabled);
        selectAllButton.setEnabled(bEnabled);
        deselectAllButton.setEnabled(bEnabled);
    }

   private void initTable(){

        Boolean bVisible = filedataTable.isVisible();
       //filedataTable.setVisible(!bVisible);
       if(bVisible) {
           List<DeviceFileInfo> dfi = importer.getDeviceFileNames(getDevice());
           Object[][] data = new Object[dfi.size()][5];
           for (int i = 0; i < dfi.size(); i++) {
               DeviceFileInfo deviceFileInfo = dfi.get(i);
               data[i] = new Object[]{true, deviceFileInfo.name, deviceFileInfo.fileSize, /*deviceFileInfo.image, */deviceFileInfo.fileType};
           }

           String[] columnNames = {"", "Name", "File Size", /*"Icon", */"File Type"};
           DefaultTableModel model = new DefaultTableModel(data, columnNames) {

               public Class<?> getColumnClass(int column) {
                   switch (column) {
                       case 0:
                           return Boolean.class;
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
                   switch(e.getKeyCode()){
                        //case 38://up
                           // if(iRow > 0)
                           // filedataTable.getSelectionModel().setSelectionInterval(iRow, iRow);
                        /*case 37://left
                           // if(iColumn > 0)
                                filedataTable.getSelectionModel().setSelectionInterval(iRow, iColumn -1);*/
                        //case 40://down
                           // if(iRow <= filedataTable.getRowCount())
                               // filedataTable.getSelectionModel().setSelectionInterval(iRow, iRow);
                        /*case 39://right
                           // if(iColumn <= filedataTable.getColumnCount())
                                filedataTable.getSelectionModel().setSelectionInterval(iRow, iColumn+1);*/
                        case 10:// enter
                            if(iColumn == 0)//checkbox
                            {
                                Boolean bool = (Boolean)filedataTable.getValueAt(iRow, 0);
                                filedataTable.setValueAt(!bool, iRow, 0);
                            }

                        else if(iColumn == 1)//img name
                            {
                                previewImage();

                            }

                            System.out.println("ENTER!!!");
                           System.out.println(e.getKeyCode());
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

    public void updateProgressBar(String fileName){
        int iCurrentValue =model.getValue();
        model.setValue(iCurrentValue+1);
        importLabel.setText("Importing "+iCurrentValue + " of " + iProgressMax + " : " + fileName);
    }

}
