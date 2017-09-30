package Help;

import javax.swing.*;

/**
 * Created by Chris on 2017-09-24.
 */
public class DeviceFileInfo {
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

