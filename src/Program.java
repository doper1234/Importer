import DeviceDataManager.DeviceDataManager;
import ErrorHandle.ExceptionForm;
import Help.BasicHelp;
import Help.FileData;
import Help.Logger;
import Help.Settings;
import Importer.Importer;
import Importer.ImporterUI;
import jmtp.PortableDevice;

import java.io.*;
import java.time.LocalDateTime;

import static DeviceDataManager.DeviceDataManager.devicesAreEqual;
import static DeviceDataManager.DeviceDataManager.getPortableDevices;
import static DeviceDataManager.DeviceDataManager.thereIsAPhoneConnected;

/**
 * Created by Chris on 2017-07-01.
 */
public class Program {

    public static ImporterUI _importerForm;

    public static void main(String[] args) {

        try {
            Logger.setupLog();
            Settings.loadSettings();

            if (FileData.isAdmin()) {
                DeviceDataManager deviceDataManager = new DeviceDataManager();
                PortableDevice[] devices = deviceDataManager.getPortableDevices();
                boolean bFirstBoot = true;
                Logger.log("Starting waiting loop...");
                double i = 0;
                double iWaitTime = Settings.getCheckForNewDevicesFrequency();
                while (true) {
                    try {
                        if(_importerForm == null)
                            checkIfFirstBootOrNewDevicesAndAskToImport(bFirstBoot, deviceDataManager, devices);
                            Thread.sleep(1 *   // minutes to sleep
                                6 *   // seconds to a minute
                                1000); // milliseconds to a second {
                        bFirstBoot = false;
                            i = 0;
                    }
                    catch(Throwable e){
                        new ExceptionForm(e);
                    }
                    i++;
                }
            }
        }
        catch(Throwable e){
            new ExceptionForm(e);
        }
    }

    public static void checkIfFirstBootOrNewDevicesAndAskToImport(boolean bFirstBoot, DeviceDataManager deviceDataManager, PortableDevice[] devices){
        PortableDevice[] updatedDevices = getPortableDevices();
        if ((bFirstBoot && thereIsAPhoneConnected(devices)) || !devicesAreEqual(devices, updatedDevices)) {
            Logger.log("List of devices changed. Asking user if they want to import.");
            if (_importerForm == null && BasicHelp.askQuestion("Device inserted("+deviceDataManager.getLastInsertedDeviceName()+"), import photos?", "Import")) {
                Logger.log("Starting UI form.");
                _importerForm = new ImporterUI();
            }
        }
        else
            Logger.log("No changes in connected devices detected.");

    }
}
