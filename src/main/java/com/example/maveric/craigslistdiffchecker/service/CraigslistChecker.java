package com.example.maveric.craigslistdiffchecker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.util.JsonReader;
import android.util.Log;

import com.example.maveric.craigslistdiffchecker.R;
import com.example.maveric.craigslistdiffchecker.exception.SearchLoadException;
import com.example.maveric.craigslistdiffchecker.sleeper.Sleep;
import com.example.maveric.craigslistdiffchecker.uniquenessCheckers.LinkCheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

//    static final String SEARCH_STRING = "https://boulder.craigslist.org/search/sss?query=Super+Nintendo+-ds+-3ds+-wii&excats=20-170&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
//    static final String SEARCH_NAME = "Super-Nintendo";
//
//    static final String SEARCH_STRING2 = "https://boulder.craigslist.org/search/sss?query=SNES+-ds+-3ds+-wii&excats=20-170&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
//    static final String SEARCH_NAME2 = "SNES";
//
//    static final String SEARCH_STRING3 = "https://boulder.craigslist.org/search/sss?query=garage+sale+super+nintendo&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
//    static final String SEARCH_NAME3 = "Garage-Sales";

    public final String baseFolder = Environment.getExternalStorageDirectory() + File.separator + "CraigslistChecker";

    public final String dataFolderLocation = baseFolder + File.separator + "data";
    public final String saveSearchesPath = dataFolderLocation + File.separator + "savedSearches";

    public final String folderLocationLinkCache = baseFolder + File.separator + "linkCache";
    public HashMap<CraigSearch, ArrayList<String>> mapSearches;

    public BackgroundServiceMonitor parentActivity;

    private List<CraigSearch> trackedSearches;

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
                LinkCheck.CheckSaleLinks(this, search);
                Sleep.waitThenContinueShort();
            }
//            LinkCheck.CheckSaleLinks(this, SEARCH_STRING, SEARCH_NAME);
//            Sleep.waitThenContinueShort();
//            LinkCheck.CheckSaleLinks(this, SEARCH_STRING2, SEARCH_NAME2);
//            Sleep.waitThenContinueShort();
//            LinkCheck.CheckSaleLinks(this, SEARCH_STRING3, SEARCH_NAME3);
//            Sleep.waitThenContinueLong();
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

        File savedSearchesFile = new File(saveSearchesPath);
        savedSearchesFile.mkdirs();

        List<CraigSearch> savedSearches = new ArrayList<>();

        if (savedSearchesFile.exists()) {
            try {
                savedSearches = loadSavedSearches(savedSearchesFile);
            } catch (SearchLoadException e) {
                // TODO: exit program here? Continue without any searches loaded?
                Log.e(TAG, "Failed to load saved searches: " + Log.getStackTraceString(e));
            }
        }

        File linkCacheFolder = new File(folderLocationLinkCache);
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

    private List<CraigSearch> loadSavedSearches(File savedSearchesFile) throws SearchLoadException {
        try {
            List<CraigSearch> searchesList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(savedSearchesFile));
            JsonReader reader = new JsonReader(bufferedReader);
            if (reader.hasNext()) {
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    reader.nextName();
                    String name = reader.nextString();
                    reader.nextName();
                    String url = reader.nextString();
                    Log.i(TAG, "Found search line: " + name + " = '" + url + "'");
                    searchesList.add(new CraigSearch(name, url));
                    reader.endObject();
                }
                reader.endArray();
            }
            reader.close();
            return searchesList;
        } catch (IOException e) {
            throw new SearchLoadException("Failed to read file", e);
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
