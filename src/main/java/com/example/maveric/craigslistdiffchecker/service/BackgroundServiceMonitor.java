package com.example.maveric.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Maveric on 6/24/2016.
 */
public class BackgroundServiceMonitor extends Service {

    private static final String TAG = "BackgroundServiceMon";

    public String folderLocationLinksState;
    CraigslistChecker craigslistChecker;
    public HashMap<String, ArrayList<String>> mapSearches;

    @Override
    public void onCreate(){

        mapSearches = new HashMap<>();

        folderLocationLinksState = Environment.getExternalStorageDirectory() + File.separator + "CraigslistChecker";

        Toast.makeText(this, "Service Created!", Toast.LENGTH_SHORT).show();

        reloadState();

        craigslistChecker = new CraigslistChecker(this);
        craigslistChecker.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        craigslistChecker.cancel(true);
        Toast.makeText(this, "Service stopped!", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void reloadState(){

        Log.d(TAG, "Loading application state!");

        File folder = new File(folderLocationLinksState);

        folder.mkdirs();

        for (final File fileEntry : folder.listFiles()) {
            Log.d(TAG, "Scanning directory. Found file: " + fileEntry.getName());
            readFile(fileEntry);
        }
    }

    public void readFile(File fileToRead){

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
        mapSearches.put(fileToRead.getName().substring(0, fileToRead.getName().length()-4), urls);
    }
}
