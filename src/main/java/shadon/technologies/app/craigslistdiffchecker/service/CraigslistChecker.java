package shadon.technologies.app.craigslistdiffchecker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.files.ConfigFiles;
import shadon.technologies.app.craigslistdiffchecker.sleeper.Sleep;
import shadon.technologies.app.craigslistdiffchecker.uniquenessCheckers.LinkCheck;

import java.net.URL;
import java.util.ArrayList;

import static android.media.AudioManager.RINGER_MODE_SILENT;

/**
 * Created by Maveric on 6/24/2016.
 */
public class CraigslistChecker extends AsyncTask<URL, String, Boolean> {

    public static final String TAG = "CraigslistChecker";

    public ArrayList<CraigSearch> listSearches;

    public CraigsDiffBackgroundService parentActivity;

    public CraigslistChecker(CraigsDiffBackgroundService craigsDiffBackgroundService){
        parentActivity = craigsDiffBackgroundService;
    }

    public void init() {
        Log.d(TAG, "Loading application state!");
        listSearches = ConfigFiles.loadAllSavedSearches();
        Log.d(TAG, "Finished loading application state!");
    }

    @Override
    protected Boolean doInBackground(URL... params) {

        checkCraigslist();
        return true;
    }

    public void callPublishProgress(String... searchData) {
        publishProgress(searchData);
    }

    protected void onProgressUpdate(String... searchData){

        Log.d(TAG, "Pushing notification!");

        String searchName = searchData[0];
        String searchURL = searchData[1];
        String searchSearchName = searchData[2];

        Log.d(TAG, "Notification will take you to " + searchURL);

        Vibrator v = (Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 500, 200, 500, 200, 500, 200, 500, 100, 200, 100, 200, 100};

        AudioManager am = (AudioManager)parentActivity.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = am.getRingerMode();

        if (ringerMode != RINGER_MODE_SILENT) {
            v.vibrate(pattern, -1);
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchURL));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(parentActivity, 0, browserIntent, 0);

        Notification notification = new Notification.Builder(parentActivity)
                .setTicker(searchName)
                .setContentTitle(searchName)
                .setContentText(searchSearchName)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent).build();

        NotificationManager nm = (NotificationManager) parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(((int) System.currentTimeMillis() % 10000), notification);
    }

    public void checkCraigslist() {

        while (!isCancelled()) {
            for (CraigSearch search : listSearches) {
                LinkCheck.CheckSaleLinks(this, search);

                try {
                    Sleep.waitThenContinueShort();
                } catch (InterruptedException e) {
                    Log.d(TAG, "Sleep interrupted. Service may be shutting down");
                    return;
                }
            }

            try {
                Sleep.waitThenContinueLong();
            } catch (InterruptedException e) {
                Log.d(TAG, "Sleep interrupted. Service may be shutting down");
                return;
            }
        }
    }
}
