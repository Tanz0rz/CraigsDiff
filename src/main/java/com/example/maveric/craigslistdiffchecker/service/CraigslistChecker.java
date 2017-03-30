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

    public ArrayList<CraigSearch> listSearches;

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
            for (CraigSearch search : listSearches) {
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
        Log.d(TAG, "Loading application state!");
        listSearches = ConfigFiles.loadAllSavedSearches();
    }
}
