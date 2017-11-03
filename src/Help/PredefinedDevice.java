package Help;

/**
 * Created by Chris on 2017-09-24.
 */
public class PredefinedDevice{
    public String serialNumber;
    public String friendlyName;
    public String modelName;
    public boolean ignoreDevice;
    public String computerPictureDirectory;
    public String localPictureDirectory;



    public PredefinedDevice(String serialNumber,String friendlyName,String modelName, boolean ignoreDevice, String pictureDirectory){
        this.serialNumber = serialNumber;
        this.friendlyName = friendlyName;
        this.modelName = modelName;
        this.ignoreDevice = ignoreDevice;
        this.computerPictureDirectory = pictureDirectory;
        this.localPictureDirectory = "DCIM";
    }

    public PredefinedDevice(String serialNumber,String friendlyName,String modelName, boolean ignoreDevice, String pictureDirectory, String localPictureDirectory){
        this.serialNumber = serialNumber;
        this.friendlyName = friendlyName;
        this.modelName = modelName;
        this.ignoreDevice = ignoreDevice;
        this.computerPictureDirectory = pictureDirectory;
        this.localPictureDirectory= localPictureDirectory;
    }
}
