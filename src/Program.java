import jmtp.PortableDevice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Chris on 2017-07-01.
 */
public class Program {

    public static void main(String[] args) {

        try {

            ImporterUI importerForm = null;
/*        Importer importer = new Importer();
        PortableDevice[] devices = importer.getPortableDevices();
        int i = 0;
        while(true){
            if(i % 1000000000 == 0)
            {
                PortableDevice[] updatedDevices = importer.getPortableDevices();
                if(!Importer.devicesAreEqual(devices, updatedDevices)){
                    if(importerForm != null && BasicHelp.askQuestion("Device inserted, import photos?", "Import"))
                    {*/
            importerForm = new ImporterUI();
                    /*}
                }
                i = 0;
            }
            i++;
        }*/
        }
        catch(Exception e){
            new ExceptionForm(e);
        }

    }
}
