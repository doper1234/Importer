import be.derycke.pieter.com.Guid;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 2017-07-02.
 */
public class BasicHelp {

    public static String tempFileLocation = "C:\\Users\\Chris\\Desktop\\Temp";//"C\\Temp\\Pictures";

    public static Boolean askQuestion(String sQuestion, String sTitle) {
        return (JOptionPane.showConfirmDialog(null, sQuestion, sTitle, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }

    public static ImageIcon resizedImage(String sName, int iWidth, int iHeight) {

        /*String[] file = sName.split(".");
        String sPath = null;
        try {
            sPath = File.createTempFile(file[0], file[1]).getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        ImageIcon imageIcon = new ImageIcon(sName);
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(image.getWidth(null) / 15, image.getHeight(null) / 15, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way

        return new ImageIcon(newimg);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String getGuidFileType(Guid guid) {
        switch (guid.toString()) {
            case "38010000-ae6c-4804-98ba-c57b46965fe7":
                return "jpg";
            default:
                return "";

        }
    }

    public static String tempFile(String sFileName) {

        CreateTempIfNotExists();

        String sPath = tempFileLocation + "\\" + sFileName;
        File f = new File(sPath);
        if (f.exists() && !f.isDirectory()) {
            return sPath;
        } else {
            try {
                return File.createTempFile(tempFileLocation, sFileName).getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void CreateTempIfNotExists() {
        File theDir = new File(tempFileLocation);

// if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try {
                theDir.mkdirs();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (result) {
                System.out.println("DIR created");
            }
        }
    }

    public static Boolean renameFile(String path, String oldName, String rename) {
        // File (or directory) with old name

        if (isNullOrWhiteSpace(rename) || containsIllegals(rename)) {
            JOptionPane.showMessageDialog(null, "File " + rename + " contains illegal characters!");
            return false;
        }

        String[] fileExtention = oldName.split("\\.");
        String extension = fileExtention[fileExtention.length - 1];

        File file = new File(path + "\\" + oldName);

// File (or directory) with new name
        File file2 = new File(path + "\\" + rename + "." + extension);

        if (file2.exists()) {
            JOptionPane.showMessageDialog(null, "File " + rename + " already exists!");
            return false;
        }

// Rename file (or directory)
        boolean success = file.renameTo(file2);

        if (!success) {
            // File was not successfully renamed
            return false;
        }
        return true;
    }

    public static boolean containsIllegals(String toExamine) {
        String[] arr = toExamine.split("[~#@*+%{}<>\\[\\]|\"\\_^]", 2);
        return arr.length > 1;
    }

    public static boolean isNullOrWhiteSpace(String string) {
        return /*string == null ||*/ string.trim().length() <= 0;
    }

    public static void createDirectory(String sNewFolder, String sPath) {

        new File(fileName(sPath, sNewFolder)).mkdirs();
    }

    public static String fileName(String sPath, String sFileName) {
        return sPath + "\\" + sFileName;
    }

    private static boolean fileExsists(String sNewFolderName, String sPath, Boolean bShowMessage) {
        File file = new File(fileName(sPath, sNewFolderName));
        Boolean bExists = file.exists();
        if (bExists && bShowMessage)
            JOptionPane.showMessageDialog(null, "Folder " + sNewFolderName + " already exists!");
        return bExists;
    }

    public static boolean isValidFileName(String sNewFolderName, String sPath) {
        return !isNullOrWhiteSpace(sNewFolderName) &&
                !containsIllegals((sNewFolderName)) &&
                !fileExsists(sNewFolderName, sPath, true);
    }

    public static String[] removeElement(String[] input, String deleteMe) {
        List<String> result = new ArrayList<>();

        for (String item : input)
            if (!deleteMe.equals(item))
                result.add(item);

        return result.toArray(input);
    }
}

