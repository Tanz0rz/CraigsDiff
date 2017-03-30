package com.example.maveric.craigslistdiffchecker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;

import com.example.maveric.craigslistdiffchecker.R;
import com.example.maveric.craigslistdiffchecker.files.ConfigFiles;
import com.example.maveric.craigslistdiffchecker.files.Paths;
import com.example.maveric.craigslistdiffchecker.sleeper.Sleep;
import com.example.maveric.craigslistdiffchecker.uniquenessCheckers.LinkCheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Maveric on 6/24/2016.
 */
public class CraigslistChecker extends AsyncTask<URL, String, Boolean> {

    public static final String TAG = "CraigslistChecker";

    public HashMap<CraigSearch, ArrayList<String>> mapSearches;

    public BackgroundServiceMonitor parentActivity;

    public CraigslistChecker(BackgroundServiceMonitor backgroundServiceMonitor){
        parentActivity = backgroundServiceMonitor;
    }

    @Override
    protected Boolean doInBackground(URL... params) {

        checkCraigslist();
        return true;
    }

    protected void onProgressUpdate(String... searchData){

        Log.d(TAG, "Pushing notification!");

        String searchName = searchData[0];
        String searchURL = searchData[1];

        Log.d(TAG, "Notification will take you to " + searchURL);

        Vibrator v = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 500, 200, 500, 200, 500, 200, 500, 100, 200, 100, 200, 100};
        v.vibrate(pattern, -1);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchURL));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(parentActivity, 0, browserIntent, 0);

        Notification notification = new Notification.Builder(parentActivity)
                .setTicker("New link posted under '" + searchName + "'")
                .setContentTitle("New link - '" + searchName + "'")
                .setContentText("Click this to go directly there")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent).build();

        NotificationManager nm = (NotificationManager) parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(0, notification);
    }

    public void checkCraigslist() {

        while (!isCancelled()) {
            for (CraigSearch search : mapSearches.keySet()) {
                Sleep.waitThenContinueShort();
                LinkCheck.CheckSaleLinks(this, search);
            }
            Sleep.waitThenContinueLong();
        }
    }

    public void callPublishProgress(String... searchData) {
        publishProgress(searchData);
    }

    public void init() {
        mapSearches = new HashMap<>();
        reloadState();
    }

    public void reloadState(){

        Log.d(TAG, "Loading application state!");

        List<CraigSearch> savedSearches = ConfigFiles.loadAllSavedSearches();

        File linkCacheFolder = new File(Paths.folderLocationLinkCache);
        linkCacheFolder.mkdirs();

        // try to find cache files for any searches we have saved and load them
        if (savedSearches.size() > 0) {
            for (CraigSearch savedSearch : savedSearches) {
                mapSearches.put(savedSearch, new ArrayList<String>());
                boolean cacheFileFound = false;
                for (final File cacheFile : linkCacheFolder.listFiles()) {
                    if ((savedSearch.name + ".txt").equals(cacheFile.getName())) {
                        Log.d(TAG, "Found cache file for saved search: " + cacheFile.getName());
                        mapSearches.get(savedSearch).addAll(readFile(cacheFile));
                        cacheFileFound = true;
                        break;
                    }
                }
                if (!cacheFileFound) {
                    Log.d(TAG, "No cache file found for: " + savedSearch.name);
                }
            }
        }
    }

    public List<String> readFile(File fileToRead){

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
