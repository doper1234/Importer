import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Chris on 2017-06-25.
 */
public class Importer {

    private static String m_sDirectory;
    private static String[] m_filesToImport;
    private PortableDevice[] portableDevices;
    private String[] portableDeviceNames;

    private final int OPTION_DELETE_ON_SAVE = 0;
    private final int OPTION_NO_DELETE_ON_SAVE = 1;
    private List<String> deviceFileNames;
    private int amountOfFiles;
    private BigInteger amountOfFilesMemory;

    public Importer() {

    }


    public PortableDevice[] getPortableDevices() {


            //if (portableDevices == null) {
        PortableDeviceManager manager = null;
            manager = new PortableDeviceManager();
            portableDevices = manager.getDevices();
        //}

        return portableDevices;
    }

    public List<String> getPortableDeviceNames() {
        List<String> deviceNames = new ArrayList<>();
        PortableDevice[] portableDevices = getPortableDevices();
        for (PortableDevice portableDevice : portableDevices) {
            try{
                portableDevice.open();
            }
            catch (Exception e){

            }
            /*if(!portableDevice.getFriendlyName().equalsIgnoreCase(""))
                deviceNames.add(portableDevice.getFriendlyName() + " ("+portableDevice.getModel() + ")");
            else*/
                deviceNames.add(portableDevice.getModel());
            portableDevice.close();
        }

        return deviceNames;
    }

    public void importFiles(int iSelection, ImporterUI importerUI, String sDirectory, String[] filesToImport, int amountOfFiles){
        (new Thread(new ImportRunnable(iSelection, amountOfFiles, importerUI, sDirectory, filesToImport))).start();
    }

    private static void importFiles(int iSelection, int iAmountOfFiles, ImporterUI importerUI, String sDirectory, String[] filesToImport) {

    m_filesToImport = filesToImport;
        importerUI.startProgress(iAmountOfFiles);
        PortableDeviceFolderObject targetFolder = null;
        PortableDeviceManager manager = new PortableDeviceManager();
        PortableDevice device = manager.getDevices()[iSelection];
        // Connect to USB tablet
        for (PortableDevice portableDevice : manager.getDevices()) {
            portableDevice.open();
            System.out.println(portableDevice.getModel());
            System.out.println("---------------");
        }

        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    if(o2.getOriginalFileName() != null) {
                        System.out.println(o2.getOriginalFileName());
                        if (o2.getOriginalFileName().equalsIgnoreCase("DCIM")) {
                            targetFolder = (PortableDeviceFolderObject) o2;
                            break;
                        }
                    }
                }

                PortableDeviceObject[] folderFiles = targetFolder.getChildObjects();

                for (PortableDeviceObject pDO : folderFiles) {
                     copyFiles(pDO, device, importerUI, sDirectory);
                }
                if(BasicHelp.askQuestion("Delete imported photos?", "Import")){
                    for (PortableDeviceObject pDO : folderFiles) {
                            deleteFileFromDevice(pDO);
                    }
                }

            }
        }

        manager.getDevices()[0].close();
    }

    private static void copyFiles(PortableDeviceObject pDO, PortableDevice device, ImporterUI progress, String sDirectory) {
        if (pDO instanceof PortableDeviceFolderObject) {
            for (PortableDeviceObject portableDeviceObject : ((PortableDeviceFolderObject) pDO).getChildObjects()) {
                if(Arrays.asList(m_filesToImport).contains(portableDeviceObject.getOriginalFileName())) {
                    progress.updateProgressBar(portableDeviceObject.getOriginalFileName());
                    System.out.println("Copying " + portableDeviceObject.getOriginalFileName());
                    copyFileFromDeviceToComputerFolder(portableDeviceObject, device, sDirectory, true);
                }
            }
        } else {
            progress.updateProgressBar(pDO.getOriginalFileName());
            System.out.println("Copying " + pDO.getOriginalFileName());
            copyFileFromDeviceToComputerFolder(pDO, device, sDirectory, true);
        }
    }

    private static void deleteFileFromDevice(PortableDeviceObject pDO){

        if (pDO instanceof PortableDeviceFolderObject) {
            for (PortableDeviceObject portableDeviceObject : ((PortableDeviceFolderObject) pDO).getChildObjects()) {
                //progress.updateProgressBar(portableDeviceObject.getOriginalFileName());
                if(Arrays.asList(m_filesToImport).contains(portableDeviceObject.getOriginalFileName())) {
                    System.out.println("Deleting " + portableDeviceObject.getOriginalFileName());
                    //if (portableDeviceObject != null && portableDeviceObject.canDelete()) {
                    portableDeviceObject.delete();
                    System.out.println(portableDeviceObject.getOriginalFileName() + " deleted");
                }
                //}
                //else{
                    //System.out.println("Could not delete " + portableDeviceObject.getOriginalFileName());
                //}
            }
        } else {
            //progress.updateProgressBar(pDO.getOriginalFileName());
           deleteFileFromDevice(pDO);
        }

    }

    private static void copyFileFromDeviceToComputerFolder(PortableDeviceObject pDO, PortableDevice device, String sDirectory, boolean bCheckIfExists) {
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();

        try {
            String sFileName = pDO.getOriginalFileName();
            File f = new File(sDirectory + "\\" + sFileName);
            BasicHelp.CreateTempIfNotExists();
            if(bCheckIfExists && f.exists() && !f.isDirectory()) {

                FileAlreadyExistsForm form = new FileAlreadyExistsForm(sDirectory, f.getPath(), BasicHelp.tempFile(sFileName));
                switch(form.getOption()){
                    case JOptionPane.YES_OPTION:
                        copy.copyFromPortableDeviceToHost(pDO.getID(), sDirectory, device);
                        break;
                    case JOptionPane.NO_OPTION:
                        m_filesToImport = BasicHelp.removeElement(m_filesToImport, sFileName);
                         //copy.copyFromPortableDeviceToHost(pDO.getID(), sDirectory, device);
                        break;
                    case JOptionPane.CANCEL_OPTION:

                        copy.copyFromPortableDeviceToHost(pDO.getID(), sDirectory, device);
                        break;
                }
                //if(BasicHelp.askQuestion(sFileName + " already exsists in destination folder. Would you like to overwrite " + sFileName + "?", "Attention!")){
                //    copy.copyFromPortableDeviceToHost(pDO.getID(), sDirectory, device);
                //}
            }
            else
                copy.copyFromPortableDeviceToHost(pDO.getID(), sDirectory, device);
        } catch (COMException ex) {
            ex.printStackTrace();
        }

    }

    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder) {
        BigInteger bigInteger1 = new BigInteger("123456789");
        File file = new File("C:\\GettingJMTP.pdf");
        try {
            targetFolder.addAudioObject(file, "jj", "jj", bigInteger1);
        } catch (Exception e) {
            System.out.println("Exception e = " + e);
        }
    }

    public List<DeviceFileInfo> getDeviceFileNames(int iDevice) {
        List<DeviceFileInfo> deviceFilesInfo = new ArrayList<>();
        PortableDeviceFolderObject targetFolder = null;
        PortableDeviceManager manager = new PortableDeviceManager();
        PortableDevice device = manager.getDevices()[iDevice];
        // Connect to USB tablet
        device.open();
        System.out.println(device.getModel());
        System.out.println("---------------");
        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    try{
                        if (o2.getOriginalFileName().equalsIgnoreCase("DCIM"))
                            targetFolder = (PortableDeviceFolderObject) o2;
                        System.out.println(o2.getOriginalFileName());
                    }
                    catch(Exception e){
                        System.out.println(e.getStackTrace());
                    }

                    ;
                }
                createChildren(targetFolder, deviceFilesInfo);
            }
        }
        manager.getDevices()[iDevice].close();
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
        if(devices.length != updatedDevices.length)
            return false;
        for (int i = 0; i < devices.length; i++){
            PortableDevice device = devices[i];
            PortableDevice updatedDevice = updatedDevices[i];

            try{
                device.open();
                updatedDevice.open();
            }
            catch(DeviceAlreadyOpenedException e){

            }


            String deviceName = device.getModel();
            String updatedDeviceName = updatedDevice.getModel();


            if(!deviceName.equals(updatedDeviceName))
                return false;

            device.close();
            updatedDevice.close();
        }
        return true;
    }

    public ImageIcon getPortableDeviceIcon(String fileName, int iDevice) {
        PortableDeviceFolderObject targetFolder = null;
        PortableDeviceManager manager = new PortableDeviceManager();
        PortableDevice device = manager.getDevices()[iDevice];
        // Connect to USB tablet
        device.open();
        System.out.println(device.getModel());
        System.out.println("---------------");
        // Iterate over deviceObjects
        String path = null;
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    if(o2.getOriginalFileName() != null) {
                        System.out.println(o2.getOriginalFileName());
                        if (o2.getOriginalFileName().equalsIgnoreCase("DCIM"))
                        {targetFolder = (PortableDeviceFolderObject) o2; break;}

                    }
                }
                path = searchThroughChildren(targetFolder, device, fileName);
                if (path != null)
                    break;
            }
        }
        manager.getDevices()[iDevice].close();
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
                    System.out.println(orgFileName);
                    if (orgFileName.contains(fileName)) {

                        copyFileFromDeviceToComputerFolder(file, device, BasicHelp.tempFileLocation, false);
                        return BasicHelp.tempFileLocation + "\\" + fileName;
                        /*PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
                        try {
                            copy.copyFromPortableDeviceToHost(fileRoot.getID(), "C:\\Users\\Chris\\Desktop\\Here\\", device);
                        } catch (COMException ex) {
                            ex.printStackTrace();
                        }
                        return "C:\\Users\\Chris\\Desktop\\Here\\" + file.getOriginalFileName();*/
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


    private class ImportRunnable implements Runnable {

        private int iAmount;
        private ImporterUI importerUI;
        private int iSelection;
        private Boolean bImport = true;
        private String sDirectory;
        private String[] filesToImport;

        public ImportRunnable(int iSelection, int iAmount, ImporterUI importerUI, String sDirectory, String[] filesToImport){
            this.iSelection = iSelection;
            this.iAmount = iAmount;
            this.importerUI = importerUI;
            this.sDirectory = sDirectory;
            this.filesToImport = filesToImport;
        }

        @Override
        public void run() {
            if(bImport)
            {
                importFiles(iSelection,iAmount, importerUI, sDirectory, filesToImport);
                bImport = false;
            }


        }
    }
}

class DeviceFileInfo {
    public String name;
    public String fileSize;
    public Icon image;
    public String fileType;

    public static DeviceFileInfo empty() {
        DeviceFileInfo empty = new DeviceFileInfo();
        empty.name = "No files found";
        empty.fileType = null;
        return empty;
    }
}

