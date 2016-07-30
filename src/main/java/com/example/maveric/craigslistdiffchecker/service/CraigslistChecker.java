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
import com.example.maveric.craigslistdiffchecker.sleeper.Sleep;
import com.example.maveric.craigslistdiffchecker.uniquenessCheckers.LinkCheck;

import java.net.URL;

/**
 * Created by Maveric on 6/24/2016.
 */
public class CraigslistChecker extends AsyncTask<URL, String, Boolean> {

    private static final String TAG = "CraigslistChecker";

    static final String SEARCH_STRING = "https://boulder.craigslist.org/search/sss?query=Super+Nintendo+-ds+-3ds+-wii&excats=20-170&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
    static final String SEARCH_NAME = "Super-Nintendo";

    static final String SEARCH_STRING2 = "https://boulder.craigslist.org/search/sss?query=SNES+-ds+-3ds+-wii&excats=20-170&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
    static final String SEARCH_NAME2 = "SNES";

    static final String SEARCH_STRING3 = "https://boulder.craigslist.org/search/sss?query=garage+sale+super+nintendo&sort=rel&postedToday=1&searchNearby=2&nearbyArea=13&max_price=1001";
    static final String SEARCH_NAME3 = "Garage-Sales";

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
            LinkCheck.CheckSaleLinks(this, SEARCH_STRING, SEARCH_NAME);
            Sleep.waitThenContinueShort();
            LinkCheck.CheckSaleLinks(this, SEARCH_STRING2, SEARCH_NAME2);
            Sleep.waitThenContinueShort();
            LinkCheck.CheckSaleLinks(this, SEARCH_STRING3, SEARCH_NAME3);
            Sleep.waitThenContinueLong();
        }
    }

    public void callPublishProgress(String... searchData) {
        publishProgress(searchData);
    }
}
