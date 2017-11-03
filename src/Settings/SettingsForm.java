package Settings;

import Help.BasicHelp;
import Help.MyJFrame;
import Help.PredefinedDevice;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static Help.BasicHelp.isNotEmpty;

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
    private JTextField frequencyTextField;
    private JPanel deviceScrollPanePanel;

    private HashMap<String,Component[]> _components;

    @Override
    public JPanel mainPanel() {
        return mainPanel;
    }

    public void showForm(){
        initForm();
        initData();
        saveButton.addActionListener(e -> save(true));
        cancelButton.addActionListener(e -> close());
    }

    private void initData() {
        initFrequency();
        initDevices();
    }

    private void initFrequency(){
        Long lFrequency = Settings.getSaveFrequencySettingsInSeconds();
        String sFrequency = lFrequency.toString();
        frequencyTextField.setText(sFrequency);
    }

    private void initDevices() {

        _components = new HashMap<>();
        List<PredefinedDevice> predefinedDevices = Settings.getPredefinedDevices();
        deviceScrollPanePanel.setLayout(new GridLayout(predefinedDevices.size(),10,0,10));

        for (PredefinedDevice predefinedDevice : predefinedDevices) {

            JPanel panel = new JPanel();

            JLabel checkLabel = new JLabel("Ignore device in device search:");
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(predefinedDevice.ignoreDevice);

            JLabel deviceSerialNumberLabel = new JLabel("Serial Nr:");
            JLabel deviceSerialNumber = new JLabel(predefinedDevice.serialNumber);
            
            Dimension dim = new Dimension(120,20);
            
            deviceSerialNumber.setPreferredSize(dim);

            JLabel deviceNameLabel = new JLabel("Name:");
            JLabel deviceName = new JLabel(predefinedDevice.friendlyName);
            deviceName.setPreferredSize(dim);

            JLabel deviceModelLabel = new JLabel("Model:");
            JLabel deviceModel = new JLabel(predefinedDevice.modelName);
            deviceModel.setPreferredSize(dim);

            String sComputerDirectory = predefinedDevice.computerPictureDirectory;
            JLabel deviceComputerPictureDirectoryLabel = new JLabel("Photo folder for device on computer:");
            JTextField deviceComputerPictureDirectory = new JTextField(sComputerDirectory);
            deviceComputerPictureDirectory.setPreferredSize(dim);
            deviceComputerPictureDirectory.setEditable(false);

            JButton chooseComputerFolderButton = new JButton("...");
            chooseComputerFolderButton.addActionListener(e ->{
                String sPath = BasicHelp.getDirectoryPath(this, sComputerDirectory);
                if(isNotEmpty(sPath))
                    deviceComputerPictureDirectory.setText(sPath);
            });

            JLabel deviceLocalPictureDirectoryLabel = new JLabel("Photo folder on device:");
            JTextField deviceLocalPictureDirectory = new JTextField(predefinedDevice.localPictureDirectory);
            deviceLocalPictureDirectory.setPreferredSize(dim);

            panel.add(deviceSerialNumberLabel);
            panel.add(deviceSerialNumber);
            panel.add(deviceNameLabel);
            panel.add(deviceName);
            panel.add(deviceModelLabel);
            panel.add(deviceModel);
            panel.add(checkLabel);
            panel.add(checkBox);
            panel.add(deviceComputerPictureDirectoryLabel);
            panel.add(deviceComputerPictureDirectory);
            panel.add(chooseComputerFolderButton);
            panel.add(deviceLocalPictureDirectoryLabel);
            panel.add(deviceLocalPictureDirectory);

            _components.put(predefinedDevice.serialNumber,panel.getComponents());
            //deviceScrollPane.add(panel);
            //deviceScrollPane.getViewport().add(panel,null);
            deviceScrollPanePanel.add(panel,null);
        }
    }

    private void close(){
        if(BasicHelp.askQuestion("Save changes before closing?", "Save?"))
            save(false);
        dispose();
    }

    private void save(boolean bAskToClose){
        HashMap<String, PredefinedDevice> predefinedDeviceList = new HashMap<>();
        for (PredefinedDevice predefinedDevice : Settings.getPredefinedDevices()){
            String sSerialNumber = predefinedDevice.serialNumber;
            Component[] components = _components.get(sSerialNumber);
            boolean bIgnore = ((JCheckBox)components[7]).isSelected();//checkbox
            String sComputerDirectory = ((JTextField)components[9]).getText();//comp dircheckbox
            String sLocalDirectory =    ((JTextField)components[12]).getText();//local dir
            predefinedDeviceList.put(sSerialNumber,new PredefinedDevice(sSerialNumber, "","", bIgnore, sComputerDirectory, sLocalDirectory));
        }
        Settings.saveAllDeviceSettings(predefinedDeviceList);
        if(bAskToClose && BasicHelp.askQuestion("Close settings?", "Close?"))
            dispose();
    }

}


