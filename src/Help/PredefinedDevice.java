package Help;

/**
 * Created by Chris on 2017-09-24.
 */
public class PredefinedDevice{
    public String name;
    public boolean ignoreDevice;
    public String pictureDirectory;
    public PredefinedDevice(String name, boolean ignoreDevice, String pictureDirectory){

        this.name = name;
        this.ignoreDevice = ignoreDevice;
        this.pictureDirectory = pictureDirectory;
    }
}
