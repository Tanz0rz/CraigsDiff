package shadon.technologies.app.craigslistdiffchecker.files;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;

/**
 * Created by Maveric on 3/29/2017.
 */

public class FileIO {

    public static final String TAG = "FileIO";

    public static ArrayList<String> readFile(File fileToRead){

        Scanner scanner = null;
        ArrayList<String> urls = new ArrayList<>();

        try {

            scanner = new Scanner(fileToRead);
            while(scanner.hasNext()) {
                urls.add(scanner.nextLine().trim());
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find file! We will just create it when we save.");
        } catch (Exception e) {
            Log.e(TAG, "Something went very wrong:");
            e.printStackTrace();
        } finally {
            if (scanner != null){
                scanner.close();
            }
        }
        return urls;
    }

    public static void writeLinksFile(CraigslistAd ad){

        Log.d(TAG, "Persisting a new URL: " + ad.url);

        File linksFileLocation = new File(Paths.cachedSearchesFileLocation);

        if(!linksFileLocation.exists()){
            Log.i(TAG, "The directory doesn't exist! Let's fix that");
            linksFileLocation.getParentFile().mkdirs();
        }

        try(FileWriter fw = new FileWriter(Paths.cachedSearchesFileLocation, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(ad.url);
        } catch (IOException e) {
            Log.e(TAG, "Unable to write file");
            e.printStackTrace();
        }
    }

    public static void writeLogcatLogsToFile() {

            Log.i(TAG, "Writing logcat files to disk");

            String debugLogFilePath = Paths.build(Paths.logFolderLocation, "logcatOutput-" + String.valueOf(System.currentTimeMillis()) + ".txt");

            File debugLogFileLocation = new File(debugLogFilePath);
            debugLogFileLocation.getParentFile().mkdirs();

            try(FileWriter fw = new FileWriter(debugLogFilePath, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {

                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    out.println(line);
                }

            } catch (IOException e) {
                Log.e(TAG, "Unable to write file");
                e.printStackTrace();
            }
    }
}
