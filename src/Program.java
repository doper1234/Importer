import DeviceDataManager.DeviceDataManager;
import ErrorHandle.ExceptionForm;
import Help.BasicHelp;
import Help.FileData;
import Help.Logger;
import Settings.Settings;
import Importer.ImporterUI;
import jmtp.PortableDevice;

import java.io.File;

import static DeviceDataManager.DeviceDataManager.devicesAreEqual;
import static DeviceDataManager.DeviceDataManager.getPortableDevices;
import static DeviceDataManager.DeviceDataManager.thereIsAPhoneConnected;

/**
 * Created by Chris on 2017-07-01.
 */
public class Program {

    public static ImporterUI _importerForm;

    public static void main(String[] args) {
        test();
        try {
            Logger.setupLog();
            Settings.loadSettings();

            if (FileData.isAdmin()) {
                DeviceDataManager deviceDataManager = new DeviceDataManager();
                PortableDevice[] devices = deviceDataManager.getPortableDevices();
                boolean bFirstBoot = true;
                Logger.log("Starting waiting loop...");
                while (true) {
                    try {
                        long iWaitTime = Settings.getSaveFrequencySettingsInMilliseconds();
                        if(needNewImporter())
                            checkIfFirstBootOrNewDevicesAndAskToImport(bFirstBoot, deviceDataManager, devices);
                        Thread.sleep(iWaitTime);
                        bFirstBoot = false;
                    }
                    catch(Throwable e){
                        new ExceptionForm(e);
                    }
                }
            }
        }
        catch(Throwable e){
            new ExceptionForm(e);
        }
    }

    private static void test() {
        File[] files = File.listRoots();
    }

    public static void checkIfFirstBootOrNewDevicesAndAskToImport(boolean bFirstBoot, DeviceDataManager deviceDataManager, PortableDevice[] devices){
        PortableDevice[] updatedDevices = getPortableDevices();
        if ((bFirstBoot && thereIsAPhoneConnected(devices)) || !devicesAreEqual(devices, updatedDevices)) {
            Logger.log("List of devices changed. Asking user if they want to import.");
            if (needNewImporter() && BasicHelp.askQuestion("Device inserted("+deviceDataManager.getLastInsertedDeviceName()+"), import photos?", "Import")) {
                Logger.log("Starting UI form.");
                _importerForm = new ImporterUI();
            }
        }
        else
            Logger.log("No changes in connected devices detected.");

    }

    private static boolean needNewImporter(){
        return _importerForm == null || _importerForm.isDisposed();
    }
}
