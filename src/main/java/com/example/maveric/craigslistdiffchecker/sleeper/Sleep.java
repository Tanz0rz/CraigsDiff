package com.example.maveric.craigslistdiffchecker.sleeper;

import android.util.Log;

/**
 * Created by Maveric on 6/25/2016.
 */
public class Sleep {

    private static final String TAG = "Sleep";

    static int CHECK_INTERVAL = 60000;

    public static void waitThenContinueLong(){

        double baseSleep = CHECK_INTERVAL;
        double randomVariance = CHECK_INTERVAL;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        try {
            Thread.sleep((long) baseSleep);
        } catch (InterruptedException e) {
            Log.d(TAG, "Sleep was interrupted! This should be the result of stopping the service through the UI");
        }
    }

    public static void waitThenContinueShort(){

        double baseSleep = (double) CHECK_INTERVAL / 10d;
        double randomVariance = (double) CHECK_INTERVAL / 10d;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        try {
            Thread.sleep((long) baseSleep);
        } catch (InterruptedException e) {
            Log.d(TAG, "Sleep was interrupted! This should be the result of stopping the service through the UI");
        }
    }
}
