package Settings;

import Help.BasicHelp;
import Help.Logger;
import Help.PredefinedDevice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for getting/saving settings
 */
public class Settings {

    private final static String settingsFileName = "ImporterSettings";
    private final static String settingsFilePath = "Settings\\" + settingsFileName;
    private final static String settingsValueDelimiter = ">";
    private final static String settingsNameDelimiter = "/";

    private final static String _scanForDevicesFrequencyOptionName =  "scanfordevicesfrequency";
    private final static String _deviceOptionName =  "device";


    private static List<PredefinedDevice> _predefinedDevices;
    private static long _scanForDevicesFrequency = 10000000;


    private static  File getSettingsFile() throws IOException {
        String logFileName = settingsFilePath;
        File fLogFile = new File(logFileName);
        if(!fLogFile.exists()){//create settings if they don't exist yet
            fLogFile.getParentFile().mkdirs();
            fLogFile.createNewFile();
            Logger.log("Settings created");
        }
        else
            Logger.log("Settings loaded");
        return fLogFile;
    }

    public static void saveSettings(){
        try{
            PrintWriter writer = new PrintWriter(settingsFilePath, "UTF-8");
            writer.println(_scanForDevicesFrequencyOptionName + settingsNameDelimiter + _scanForDevicesFrequency);
                for (PredefinedDevice dDevice : _predefinedDevices) {
                    writer.println(_deviceOptionName + settingsNameDelimiter + dDevice.name + settingsValueDelimiter + dDevice.ignoreDevice + settingsValueDelimiter + dDevice.pictureDirectory);
                }
            writer.close();
        } catch (IOException e) {
            Logger.log(e.getStackTrace());
        }
    }

    /**
     *
     * @throws IOException
     */
    public static void loadSettings() throws IOException {
        _predefinedDevices = new ArrayList<>();
        File settingsFile = getSettingsFile();
        List<String> lines = Files.readAllLines(settingsFile.toPath(), Charset.defaultCharset());

        for (String line : lines) {
            String[] aOption = line.split(settingsNameDelimiter);
            String sOptionName = aOption[0];
            String sOptionValue = aOption[1];
            switch (sOptionName){
                case _scanForDevicesFrequencyOptionName:
                    try {
                        _scanForDevicesFrequency = Integer.parseInt(sOptionValue);
                    }
                    catch(NumberFormatException e){
                        Logger.log("Invalid value for " + sOptionName + " (" + sOptionValue +")");
                    }
                    break;
                case _deviceOptionName:
                    String[] aDeviceValues = sOptionValue.split(settingsValueDelimiter);
                    String deviceName = aDeviceValues[0];
                    boolean ignoreDevice = Boolean.parseBoolean(aDeviceValues[1]);
                    String pictureDirectory = aDeviceValues[2];
                    PredefinedDevice device = new PredefinedDevice(deviceName, ignoreDevice, pictureDirectory);
                    _predefinedDevices.add(device);
                    break;
            }
        }
    }


    public static long getCheckForNewDevicesFrequency(){

        return _scanForDevicesFrequency;
    }

    public static PredefinedDevice getPredefinedDevice(String deviceName){
        for (PredefinedDevice device : _predefinedDevices) {
            if (device.name.equalsIgnoreCase(deviceName))
                return device;
        }
        return null;
    }

    public static void saveFrequencySettings(int iSeconds) {
        // 1 *   // minutes to sleep
        //60 *   // seconds to a minute
        _scanForDevicesFrequency = iSeconds * 1000;
        saveSettings();

    }

    private static boolean deviceAlreadyHasSavedSettings(String sName){
        PredefinedDevice preExistingDevice = null;
        for (PredefinedDevice dev :_predefinedDevices) {
            if (dev.name.equalsIgnoreCase(sName)) {
                preExistingDevice = dev;
            }
        }
        if(preExistingDevice != null)
            _predefinedDevices.remove(preExistingDevice);
        return preExistingDevice != null;
    }


    public static void saveDeviceSettings(String sName, boolean ignoreDevice, String sPictureDirectory) {
        PredefinedDevice device = new PredefinedDevice(sName, ignoreDevice, sPictureDirectory);
        deviceAlreadyHasSavedSettings(sName);
        _predefinedDevices.add(device);
        saveSettings();
        BasicHelp.showMessage("Settings saved!", "Success");

    }
}
