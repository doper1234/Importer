package Importer;

import ErrorHandle.FileAlreadyExistsForm;
import Help.*;
import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static DeviceDataManager.DeviceDataManager.getDevice;
import static DeviceDataManager.DeviceDataManager.getPortableDevices;

/**
 * Created by Chris on 2017-06-25.
 */
public class Importer {

    private static String[] m_filesToImport;

    /**
     * Speaks for itself. Import files.
     * @param sSelection
     * @param importerUI importer ui, used to update progress bar
     * @param sDirectory destination directory for files
     * @param filesToImport array of files to import
     * @param amountOfFiles amount of files to import
     */
    public void importFiles(String sSelection, ImporterUI importerUI, String sDirectory, String[] filesToImport, int amountOfFiles){
        (new Thread(new ImportRunnable(sSelection, amountOfFiles, importerUI, sDirectory, filesToImport))).start();
    }

    /**
     * Import files
     * @param sSelection index of devices to import from
     * @param iAmountOfFiles amount of files to import
     * @param importerUI the ui to update progress
     * @param sDirectory destination directory to import to
     * @param filesToImport array of file names to import
     */
    private static void importFiles(String sSelection, int iAmountOfFiles, ImporterUI importerUI, String sDirectory, String[] filesToImport) {
    m_filesToImport = filesToImport;
        importerUI.startProgress(iAmountOfFiles);
        PortableDeviceFolderObject targetFolder = null;
        PortableDevice device = getDevice(sSelection);
        device.open();

        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects()) {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;

                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    if(o2.getOriginalFileName() != null) {
                        if (o2.getOriginalFileName().equalsIgnoreCase(FileData.phonePictureFolder)) {
                            targetFolder = (PortableDeviceFolderObject) o2;
                            Logger.log("Found folder :" + o2.getOriginalFileName());
                            break;
                        }
                    }
                }
                if(targetFolder != null) {
                    PortableDeviceObject[] folderFiles = targetFolder.getChildObjects();
                    for (PortableDeviceObject pDO : folderFiles)
                        copyFiles(pDO, device, importerUI, sDirectory);
                    if (BasicHelp.askQuestion("Delete imported photos?", "Import")) {
                        importerUI.resetProgress();
                        for (PortableDeviceObject pDO : folderFiles)
                            deleteFileFromDevice(pDO, importerUI);
                    }
                    importerUI.endProgress();
                }
            }
        }
        device.close();
    }

    /**
     * Copy files from device
     * @param pDO object to copy or iterate through if its a folder
     * @param device device to copy files from
     * @param progress importer ui to update progress bar
     * @param sDirectory path to destination directory
     */
    private static void copyFiles(PortableDeviceObject pDO, PortableDevice device, ImporterUI progress, String sDirectory) {
        if (pDO instanceof PortableDeviceFolderObject) {
            for (PortableDeviceObject portableDeviceObject : ((PortableDeviceFolderObject) pDO).getChildObjects()) {
                if(Arrays.asList(m_filesToImport).contains(portableDeviceObject.getOriginalFileName())) {
                    progress.updateProgressBar(portableDeviceObject.getOriginalFileName(), false);
                    Logger.log("Copying " + portableDeviceObject.getOriginalFileName());
                    copyFileFromDeviceToComputerFolder(portableDeviceObject, device, sDirectory, true);
                }
            }
        } else {
            progress.updateProgressBar(pDO.getOriginalFileName(), false);
            Logger.log("Copying " + pDO.getOriginalFileName());
            copyFileFromDeviceToComputerFolder(pDO, device, sDirectory, true);
        }
    }

    /**
     * Deletes file on device
     * @param pDO file to delete
     * @param importerUI importer form to update progress bar
     */
    private static void deleteFileFromDevice(PortableDeviceObject pDO, ImporterUI importerUI){

        if (pDO instanceof PortableDeviceFolderObject) {
            for (PortableDeviceObject portableDeviceObject : ((PortableDeviceFolderObject) pDO).getChildObjects()) {
                String sFileName = portableDeviceObject.getOriginalFileName();
                if(Arrays.asList(m_filesToImport).contains(sFileName)) {
                    Logger.log("Deleting " + sFileName);
                    importerUI.updateProgressBar(portableDeviceObject.getOriginalFileName(), true);
                    portableDeviceObject.delete();
                    Logger.log(portableDeviceObject.getOriginalFileName() + " deleted");
                }
            }
        } else
           deleteFileFromDevice(pDO, importerUI);
    }

    /**
     * Function used to copy files on a device to the computer
     * @param portableDeviceObject the file to import
     * @param device the device in which to import from
     * @param sDirectory directory path on the computer
     * @param bCheckIfExists the method will check if the file already exists and ask the user how to resolve this
     */
    public static void copyFileFromDeviceToComputerFolder(PortableDeviceObject portableDeviceObject, PortableDevice device, String sDirectory, boolean bCheckIfExists) {
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();

        try {
            String sFileName = portableDeviceObject.getOriginalFileName();
            File file = new File(sDirectory + "\\" + sFileName);
            BasicHelp.CreateTempIfNotExists();
            if(bCheckIfExists && file.exists() && !file.isDirectory()) {
                String sPortableDeviceId = portableDeviceObject.getID();
                copy.copyFromPortableDeviceToHost(sPortableDeviceId, FileData.tempFileLocation, device);
                String sTempFilePath = FileData.tempFileLocation + "\\"+ (sFileName);
                FileAlreadyExistsForm form = new FileAlreadyExistsForm(sDirectory, file.getPath(), sTempFilePath);
                switch(form.getOption()){
                    case JOptionPane.YES_OPTION:
                        copy.copyFromPortableDeviceToHost(sPortableDeviceId, sDirectory, device);
                        break;
                    case JOptionPane.NO_OPTION:
                        m_filesToImport = BasicHelp.removeElement(m_filesToImport, sFileName);
                        break;
                    case JOptionPane.CANCEL_OPTION:

                        copy.copyFromPortableDeviceToHost(sPortableDeviceId, sDirectory, device);
                        break;
                }
                File fTempFile = new File(sTempFilePath);
                fTempFile.delete();
            }
            else
                copy.copyFromPortableDeviceToHost(portableDeviceObject.getID(), sDirectory, device);
        } catch (COMException ex) {
            Logger.log(ex.getStackTrace());
        }

    }

    /**
     * Copy a file from the computer to device.
     * @param targetFolder the destination folder on said device.
    * */
    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder) {
        BigInteger bigInteger1 = new BigInteger("123456789");
        File file = new File("C:\\GettingJMTP.pdf");
        try {
            targetFolder.addAudioObject(file, "jj", "jj", bigInteger1);
        } catch (Exception e) {
            Logger.log("Exception e = " + e);
        }
    }

    private class ImportRunnable implements Runnable {

        private int iAmount;
        private ImporterUI importerUI;
        private String sSelection;
        private Boolean bImport = true;
        private String sDirectory;
        private String[] filesToImport;

        public ImportRunnable(String sSelection, int iAmount, ImporterUI importerUI, String sDirectory, String[] filesToImport){
            this.sSelection = sSelection;
            this.iAmount = iAmount;
            this.importerUI = importerUI;
            this.sDirectory = sDirectory;
            this.filesToImport = filesToImport;
        }

        @Override
        public void run() {
            if(bImport)
            {
                importFiles(sSelection,iAmount, importerUI, sDirectory, filesToImport);
                bImport = false;
            }


        }
    }
}
