package DeviceDataManager;

import Help.*;
import Importer.*;
import Settings.Settings;
import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceDataManager {


    private static String _lastInsertedDevice;
    private int amountOfFiles;
    private BigInteger amountOfFilesMemory;

    public static boolean isDeviceOpen(PortableDevice device){
        try{
            device.open();
            device.close();
            return false;
        }
        catch(Exception e){
            //device was already open
            return true;
        }
    }

    public static PortableDevice getDevice(String sSerial){
        HashMap<String,PortableDevice> portableDeviceHashMap = getPortableDevicesHash();
        return portableDeviceHashMap.get(sSerial);
    }


    public static HashMap<String,PortableDevice> getPortableDevicesHash(){

        HashMap<String,PortableDevice> hm = new HashMap<>();

        PortableDeviceManager manager = new PortableDeviceManager();
        for (PortableDevice portableDevice: manager.getDevices()) {
            //PortableDeviceType sType = portableDevice.getType();
            String sClass = String.valueOf(portableDevice.getClass());

            Logger.log(sClass);
            //if(!isDeviceOpen(portableDevice))
            portableDevice.open();
            PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(portableDevice.getSerialNumber());
            if(predefinedDevice == null || (predefinedDevice != null && !predefinedDevice.ignoreDevice))
                hm.put(portableDevice.getSerialNumber(),portableDevice);
            portableDevice.close();
        }
        return hm;
    }

    public static PortableDevice[] getPortableDevices(){
        return getPortableDevices(false);
    }

    public static PortableDevice[] getPortableDevices(boolean bOpenDevices) {
        List<PortableDevice> lPortableDevices = new ArrayList<>();

        PortableDeviceManager manager = new PortableDeviceManager();
        for (PortableDevice portableDevice: manager.getDevices()) {
            //PortableDeviceType sType = portableDevice.getType();
            String sClass = String.valueOf(portableDevice.getClass());

            Logger.log(sClass);
            //if(!isDeviceOpen(portableDevice))
                portableDevice.open();
            PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(portableDevice.getSerialNumber());
            if(predefinedDevice == null || (predefinedDevice != null && !predefinedDevice.ignoreDevice))
                lPortableDevices.add(portableDevice);
            if(!bOpenDevices)
                portableDevice.close();
        }
        return lPortableDevices.toArray(new PortableDevice[0]);
    }

    public static String getDeviceFullName(PortableDevice portableDevice) {
        String sDeviceNameToDisplay;
        String sFriendlyName = portableDevice.getFriendlyName();
        String sDeviceModel = portableDevice.getModel();
        if (!BasicHelp.containsHanScript(sFriendlyName))
            sDeviceNameToDisplay = sFriendlyName + " (" + sDeviceModel + ")";
        else
            sDeviceNameToDisplay = sDeviceModel;
        return sDeviceNameToDisplay;
    }

    public List<String[]> getPortableDeviceNames() {
        List<String[]> deviceNames = new ArrayList<>();
        PortableDevice[] portableDevices = getPortableDevices();
        for (PortableDevice portableDevice : portableDevices) {
            if(!isDeviceOpen(portableDevice))
                portableDevice.open();
            String sDeviceNameToDisplay = getDeviceFullName(portableDevice);
            PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(portableDevice.getSerialNumber());
            if (predefinedDevice == null || (predefinedDevice != null && !predefinedDevice.ignoreDevice))
                deviceNames.add(new String[]{portableDevice.getSerialNumber(),sDeviceNameToDisplay});
            portableDevice.close();
        }
        return deviceNames;
    }

    public String getDeviceModelName(String iDevice){
        PortableDevice device = getDevice(iDevice);
        if(!isDeviceOpen(device))
            device.open();
        String sName = device.getModel();
        device.close();
        return sName;
    }

    public String getDeviceFriendlyName(String iDevice){
        PortableDevice device = getDevice(iDevice);
        if(!isDeviceOpen(device))
            device.open();
        String sName = device.getFriendlyName();
        device.close();
        return sName;
    }

    public String getDevicePictureDirectory(PortableDevice device){
        String folderDirectory;
        PredefinedDevice predefinedDevice = Settings.getPredefinedDevice(device.getSerialNumber());
        if(predefinedDevice != null)
            folderDirectory = predefinedDevice.localPictureDirectory;
        else
            folderDirectory = FileData.phonePictureFolder;
        return folderDirectory;

    }

    public List<DeviceFileInfo> getDeviceFileNames(String sSerial) {
        List<DeviceFileInfo> deviceFilesInfo = new ArrayList<>();
        PortableDevice device = getDevice(sSerial);
        // Connect to USB tablet
        if(!isDeviceOpen(device))
            device.open();
        String folderDirectory = getDevicePictureDirectory(device);
        Logger.log("Retrieving files from: " + getDeviceFullName(device));
        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                PortableDeviceFolderObject targetFolder = getDeviceFolder(storage, folderDirectory);
                if(targetFolder == null){
                    String sMessage = "Folder " + folderDirectory +" not found on device!";
                    BasicHelp.showMessage(sMessage, "Folder not found!");
                    Logger.log(sMessage);
                    break;
                }
                createChildren(targetFolder, deviceFilesInfo);
            }
        }
        device.close();
        if (deviceFilesInfo.isEmpty())
            deviceFilesInfo.add(DeviceFileInfo.empty());
        return deviceFilesInfo;
    }

    private PortableDeviceFolderObject getDeviceFolder(PortableDeviceStorageObject storage, String sFolderName) {
        PortableDeviceFolderObject targetFolder = null;
        for (PortableDeviceObject o2 : storage.getChildObjects()) {
            try{
                if (o2.getOriginalFileName().equalsIgnoreCase(sFolderName)){
                    targetFolder = (PortableDeviceFolderObject) o2;
                    Logger.log("Target folder: " + o2.getOriginalFileName());
                }
            }
            catch(Exception e){
                Logger.log(e.getStackTrace());
            }
        }
        return targetFolder;
    }

    private void createChildren(PortableDeviceObject fileRoot, List<DeviceFileInfo> node) {

        if (fileRoot instanceof PortableDeviceFolderObject) {
            for (PortableDeviceObject file : ((PortableDeviceFolderObject) fileRoot).getChildObjects()) {
                createChildren(file, node);
            }
        }else{
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
        Logger.log("Checking for change in list of devices...");
        if(devices.length != updatedDevices.length)
            return false;
        for (int i = 0; i < devices.length && i < updatedDevices.length; i++){
            PortableDevice device = devices[i];
            PortableDevice updatedDevice = updatedDevices[i];
            if(!isDeviceOpen(device))
                device.open();
            if(!isDeviceOpen(updatedDevice))
                updatedDevice.open();
            String deviceName = device.getSerialNumber();
            String updatedDeviceName = updatedDevice.getSerialNumber();
            if(!deviceName.equals(updatedDeviceName)){
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

    public ImageIcon getPortableDeviceIcon(String fileName, String iDevice) {
        PortableDevice device = getDevice(iDevice);
        // Connect to USB tablet
        if(!isDeviceOpen(device))
            device.open();
        Logger.log(device.getModel());
        // Iterate over deviceObjects
        String path = null;
        String sPictureDirectory = getDevicePictureDirectory(device);
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                PortableDeviceFolderObject targetFolder = getDeviceFolder(storage,sPictureDirectory);
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
        }else{
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

            if(!DeviceDataManager.isDeviceOpen(pd))
                pd.open();
            //todo find out how this object is already open.
            String sDeviceName = getDeviceFullName(pd);
            lDeviceNames.add(sDeviceName);
            pd.close();
        }
        _lastInsertedDevice = String.join(",", lDeviceNames);
        Logger.log(devices.length + " devices connected.");
        return devices.length > 0;
    }

}
