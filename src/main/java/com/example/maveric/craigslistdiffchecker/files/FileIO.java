package com.example.maveric.craigslistdiffchecker.files;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
}
