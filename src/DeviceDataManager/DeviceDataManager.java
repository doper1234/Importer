package DeviceDataManager;

import Help.*;
import Importer.*;
import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DeviceDataManager {


    private static String _lastInsertedDevice;
    private int amountOfFiles;
    private BigInteger amountOfFilesMemory;

    public static PortableDevice[] getPortableDevices(){
        return getPortableDevices(false);
    }

    public static PortableDevice[] getPortableDevices(boolean bOpenDevices) {
        List<PortableDevice> lPortableDevices = new ArrayList<>();

        PortableDeviceManager manager = new PortableDeviceManager();
        for (PortableDevice portableDevice: manager.getDevices()) {
            try {
                portableDevice.open();
            } catch (Exception e) {
                Logger.log("Tried to open an already open device");
            }
            PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(portableDevice.getModel());
            if(predefinedDevice == null || (predefinedDevice != null && !predefinedDevice.ignoreDevice))
                lPortableDevices.add(portableDevice);
            if(!bOpenDevices)
                portableDevice.close();
        }
        return lPortableDevices.toArray(new PortableDevice[0]);
    }

    public List<String> getPortableDeviceNames() {
        List<String> deviceNames = new ArrayList<>();
        PortableDevice[] portableDevices = getPortableDevices();
        for (PortableDevice portableDevice : portableDevices) {
            try {
                portableDevice.open();
            } catch (Exception e) {
                //device is already open
            }
            String sDeviceNameToDisplay;
            String sFriendlyName =portableDevice.getFriendlyName();
            String sDeviceModel = portableDevice.getModel();
            if(!BasicHelp.containsHanScript(sFriendlyName))
                sDeviceNameToDisplay = sFriendlyName + " (" + sDeviceModel + ")";
            else
                sDeviceNameToDisplay = sDeviceModel;

            PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(sDeviceModel);
            if(predefinedDevice == null || (predefinedDevice != null && !predefinedDevice.ignoreDevice))
                deviceNames.add(sDeviceNameToDisplay);
            portableDevice.close();
        }

        return deviceNames;
    }

    public String getDeviceName(int iDevice){
        PortableDevice device = getPortableDevices()[iDevice];
        device.open();
        String sName = device.getModel();
        device.close();
        return sName;
    }

    public List<DeviceFileInfo> getDeviceFileNames(int iDevice) {
        List<DeviceFileInfo> deviceFilesInfo = new ArrayList<>();
        PortableDeviceFolderObject targetFolder = null;
        PortableDevice device = getPortableDevices()[iDevice];
        // Connect to USB tablet
        device.open();
        Logger.log("Retrieving files from: " + device.getModel());
        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    try{
                        if (o2.getOriginalFileName().equalsIgnoreCase(FileData.phonePictureFolder))
                        {
                            targetFolder = (PortableDeviceFolderObject) o2;
                            Logger.log("Target folder: " + o2.getOriginalFileName());
                        }
                    }
                    catch(Exception e){
                        Logger.log(e.getStackTrace());
                    }
                }
                createChildren(targetFolder, deviceFilesInfo);
            }
        }
        getPortableDevices()[iDevice].close();
        if(deviceFilesInfo.isEmpty()) {
            deviceFilesInfo.add(DeviceFileInfo.empty());
        }
        return deviceFilesInfo;
    }

    private void createChildren(PortableDeviceObject fileRoot, List<DeviceFileInfo> node) {

        if (fileRoot instanceof PortableDeviceFolderObject) {

            for (PortableDeviceObject file : ((PortableDeviceFolderObject) fileRoot).getChildObjects()) {
                createChildren(file, node);
            }
        }
        else{
            amountOfFiles++;
            if(amountOfFilesMemory == null)
                amountOfFilesMemory = new BigInteger(String.valueOf(fileRoot.getSize()));
            else
                amountOfFilesMemory.add(fileRoot.getSize());
            DeviceFileInfo dfi = new DeviceFileInfo();
            dfi.name = fileRoot.getOriginalFileName();
            dfi.fileSize = BasicHelp.round(fileRoot.getSize().doubleValue()/1024, 2) + " kb";
            dfi.image = new ImageIcon(fileRoot.getOriginalFileName());
            dfi.fileType = BasicHelp.getGuidFileType(fileRoot.getFormat());
            node.add(dfi);
        }
    }

    public static boolean devicesAreEqual(PortableDevice[] devices, PortableDevice[] updatedDevices) {
        /*if(devices.length != updatedDevices.length)
            return false;*/
        Logger.log("Checking for change in list of devices...");
        for (int i = 0; i < devices.length && i < updatedDevices.length; i++){
            PortableDevice device = devices[i];
            PortableDevice updatedDevice = updatedDevices[i];

            try{
                device.open();
            }
            catch(DeviceAlreadyOpenedException e){
                //Help.Logger.log("Device already open!");
            }

            try{
                updatedDevice.open();
            }
            catch(DeviceAlreadyOpenedException e){
                //Help.Logger.log("Updated device already open!");
            }


            String deviceName = device.getModel();
            String updatedDeviceName = updatedDevice.getModel();


            if(!deviceName.equals(updatedDeviceName))
            {
                _lastInsertedDevice = updatedDeviceName;
                device.close();
                updatedDevice.close();
                return false;
            }
            device.close();
            updatedDevice.close();


        }
        return true;
    }

    public ImageIcon getPortableDeviceIcon(String fileName, int iDevice) {
        PortableDeviceFolderObject targetFolder = null;
        PortableDevice device = getPortableDevices()[iDevice];
        // Connect to USB tablet
        device.open();
        Logger.log(device.getModel());
        // Iterate over deviceObjects
        String path = null;
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    if(o2.getOriginalFileName() != null) {
                        Logger.log(o2.getOriginalFileName());
                        if (o2.getOriginalFileName().equalsIgnoreCase(FileData.phonePictureFolder))
                        {targetFolder = (PortableDeviceFolderObject) o2; break;}

                    }
                }
                path = searchThroughChildren(targetFolder, device, fileName);
                if (path != null)
                    break;
            }
        }
        device.close();
        if (path != null)
            return BasicHelp.resizedImage(path, 120,120);
        return null;
    }

    public String searchThroughChildren(PortableDeviceFolderObject fileRoot, PortableDevice device, String fileName){
        String path;
        if (fileRoot instanceof PortableDeviceFolderObject) {
            //searchThroughChildren(fileRoot, device, fileName);
            for (PortableDeviceObject file : fileRoot.getChildObjects()) {
                if (file instanceof PortableDeviceFolderObject) {
                    path = searchThroughChildren((PortableDeviceFolderObject) file, device, fileName);
                    if(path != null)
                        return path;
                } else {
                    String orgFileName = file.getOriginalFileName();
                    Logger.log(orgFileName);
                    if (orgFileName.contains(fileName)) {

                        Importer.copyFileFromDeviceToComputerFolder(file, device, FileData.tempFileLocation, false);
                        return FileData.tempFileLocation + "\\" + fileName;
                    }
                }
            }
        }

        else{
            String orgFileName = fileRoot.getOriginalFileName();
            if (orgFileName.contains(fileName)) {

                PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
                try {
                    copy.copyFromPortableDeviceToHost(fileRoot.getID(), "C:\\Users\\Chris\\Desktop\\Here", device);
                } catch (COMException ex) {
                    ex.printStackTrace();
                }
                return "C:\\Users\\Chris\\Desktop\\Here\\" + fileRoot.getOriginalFileName();
            }
        }
        return null;
    }

    public String getLastInsertedDeviceName() {

        return _lastInsertedDevice;
    }

    public static boolean thereIsAPhoneConnected(PortableDevice[] devices) {
        Logger.log("First boot; checking for connected devices...");
        List<String> lDeviceNames = new ArrayList<>();
        for (PortableDevice pd: devices) {
            try {
                pd.open();
            }
            catch(Exception e){

            }
            String deviceModel = pd.getModel().toLowerCase();
            String deviceName = pd.getFriendlyName();
            String deviceDescription = pd.getDescription();
            String deviceManufacturer = pd.getManufacturer();
            lDeviceNames.add(deviceModel + "("+ deviceName + ")");
          /*  if (deviceName.contains("phone"))
            {
                pd.close();
                return true;
            }*/
            pd.close();
        }
        _lastInsertedDevice = String.join(",", lDeviceNames);
        Logger.log(devices.length + " devices connected.");
        return devices.length > 0;
        //return false;
    }

}
