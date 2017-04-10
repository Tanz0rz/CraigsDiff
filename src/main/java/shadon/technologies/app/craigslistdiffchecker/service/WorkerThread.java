package shadon.technologies.app.craigslistdiffchecker.service;

import android.util.Log;

import java.util.ArrayList;

import shadon.technologies.app.craigslistdiffchecker.notifications.NotificationGenerator;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.SavedSearch;
import shadon.technologies.app.craigslistdiffchecker.uniquenessCheckers.LinkCheck;

/**
 * Created by Maveric on 4/8/2017.
 */

public class WorkerThread extends Thread {

    final String TAG = "WorkerThread";

    private AndroidBackgroundService service;
    private ArrayList<SavedSearch> listSavedSearches;
    private boolean continueRunning = true;

    public WorkerThread(AndroidBackgroundService service, ArrayList<SavedSearch> listSavedSearches){
        this.service = service;
        this.listSavedSearches = listSavedSearches;
    }

    public void stopExecution(){
        continueRunning = false;
    }

    public void run() {

        while(continueRunning) {
            for (SavedSearch search : listSavedSearches) {

                CraigslistAd newAd = LinkCheck.CheckSaleLinks(search);

                if (newAd != null) {
                    NotificationGenerator.pushNewAdNotification(service, newAd, search.name);
                }

                try {
                    waitThenContinueShort();
                } catch (InterruptedException e) {
                    Log.d(TAG, "Sleep interrupted. Service may be shutting down");
                    return;
                }
            }

            try {
                Log.i(TAG, "Cycled through all searches. Sleeping for a few minutes.");
                waitThenContinueLong();
            } catch (InterruptedException e) {
                Log.d(TAG, "Sleep interrupted. Service may be shutting down");
                return;
            }
        }
    }

    final int CHECK_INTERVAL = 120000;

    private void waitThenContinueLong() throws InterruptedException {

        double baseSleep = CHECK_INTERVAL;
        double randomVariance = CHECK_INTERVAL;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        Thread.sleep((long) baseSleep);
    }

    private void waitThenContinueShort() throws InterruptedException {

        double baseSleep = (double) CHECK_INTERVAL / 10d;
        double randomVariance = (double) CHECK_INTERVAL / 10d;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        Thread.sleep((long) baseSleep);
    }

}
