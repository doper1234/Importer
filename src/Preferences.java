import java.io.File;

/**
 * Created by Chris on 2017-07-02.
 */
public class Preferences {

    public static String defaultFolderLocation;
    public static String iPhoneFolderLocation;
    public static String androidFolderLocation;

    public static void init(){
        File preferencesFile = new File("C:\\Program Files\\JavaImporter");
    }


}
