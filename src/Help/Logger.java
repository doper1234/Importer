package Help;

import Help.BasicHelp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;

/**
 * Created by Chris on 2017-09-24.
 */
public class Logger {


    public static void setupLog() throws Exception {
            LocalDateTime date = LocalDateTime.now();
            String sDirectory = BasicHelp.getCurrentDirectory(); //todo change this
            String logFileName = sDirectory + "\\Logs\\Log_" + date.getYear() + date.getMonthValue() + date.getDayOfMonth() + ".txt";
        try {
            File fLogFile = new File(logFileName);
            if (!fLogFile.exists()) {
                fLogFile.getParentFile().mkdirs();
                fLogFile.createNewFile();
            }
            PrintStream out = new PrintStream(new FileOutputStream(logFileName, true));
            System.setOut(out);
            Logger.log("Help.Logger setup");
        }catch(IOException ex){
            throw new Exception(ex.getMessage() + " " + logFileName);
        }
    }

    private static void log(String message, String sLogType){
        String sTime = getDateString();
        String sLog = String.format("%s: %s | %s ", sLogType,sTime, message);
        System.out.println(sLog);
    }

    public static void log(String message){
        log(message, "log");
    }

    public static void logError(String message){
        log(message, "Error");
    }

    public static void logError(Throwable throwable){
        logError(throwable.getMessage());
    }

    public static void log(StackTraceElement[] stackTrace) {
        String message = "";
        for (StackTraceElement ste: stackTrace) {
            message+= ste.toString();
        }
        log(message);
    }

    private static String getDateString(){
        LocalDateTime date = LocalDateTime.now();
        String sHour = BasicHelp.padLeft(date.getHour(), 2);
        String sMinute = BasicHelp.padLeft(date.getMinute(),2);
        String sSecond = BasicHelp.padLeft(date.getSecond(),2);
        String sNanoValue = String.valueOf(date.getNano());
        String sNano;
        if(sNanoValue.length() > 3)
            sNano =sNanoValue.substring(0, 3);
        else
            sNano = BasicHelp.padLeft(date.getNano(), 3);
        return String.format("%s:%s:%s.%s", sHour, sMinute, sSecond, sNano);
    }



}
