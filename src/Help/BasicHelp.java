package Help;

import be.derycke.pieter.com.Guid;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 2017-07-02.
 */
public class BasicHelp {

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
        int iWidthImage = image.getWidth(null);
        int iHeightImage = image.getHeight(null);
        if(iWidthImage > 0 && iHeightImage > 0) {
            Image newimg = image.getScaledInstance(iWidthImage / 15, iHeightImage / 15, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
            return new ImageIcon(newimg);
        }
        return null;
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

        String sPath = FileData.tempFileLocation + "\\" + sFileName;
        File f = new File(sPath);
        if (f.exists() && !f.isDirectory()) {
            return sPath;
        } else {
            try {
                return File.createTempFile(FileData.tempFileLocation, sFileName).getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void CreateTempIfNotExists() {
        File theDir = new File(FileData.tempFileLocation);

// if the directory does not exist, create it
        if (!theDir.exists()) {
            Logger.log("creating directory: " + theDir.getName());
            boolean result = false;

            try {
                theDir.mkdirs();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (result) {
                Logger.log("DIR created");
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

    public static String getFileDirectory(String sFilePath){
        if(sFilePath.contains(".")){
            String[] aSplit = sFilePath.split("\\\\");
            aSplit[aSplit.length-1] = "";
            return String.join("\\\\", aSplit);
        }
        else
            return sFilePath;
    }

    public static String padLeft(int iNumberToPad, int iAmountToPad){
        return String.format("%0"+iAmountToPad+"d", iNumberToPad);
    }

    public static void showMessage(String sMessage, String sTitle) {

        JOptionPane.showMessageDialog(null, sMessage, sTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean containsHanScript(String s) {
        return s.codePoints().anyMatch(
                codepoint ->
                        Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }
}

