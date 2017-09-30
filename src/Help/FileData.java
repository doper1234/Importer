package Help;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Chris on 2017-07-16.
 */
public class FileData {

    public static String phonePictureFolder = "DCIM";

    public static String tempFileLocation;

    private static void createTempFileLocation(){

        String tmp_dir_prefix = "ImporterPreviewPictures_";

        //get the default temporary folders path
        String default_tmp = System.getProperty("java.io.tmpdir");
        Logger.log(default_tmp);

        try {
            //set a prefix
            Path tmp_2 = Files.createTempDirectory(tmp_dir_prefix);
            tempFileLocation = tmp_2.toString();
            Logger.log("TMP: " + tmp_2.toString());

        } catch (IOException e) {
            Logger.logError(e);
        }

    }

    public static boolean isAdmin() {
        String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
        for (String group : groups) {
            if (group.equals("S-1-5-32-544"))
            {
                createTempFileLocation();
                return true;
            }
        }
        return false;
    }
}
