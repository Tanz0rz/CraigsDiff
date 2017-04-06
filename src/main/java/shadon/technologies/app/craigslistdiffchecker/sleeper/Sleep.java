package shadon.technologies.app.craigslistdiffchecker.sleeper;

import android.util.Log;

/**
 * Created by Maveric on 6/25/2016.
 */
public class Sleep {

    private static final String TAG = "Sleep";

    static int CHECK_INTERVAL = 120000;

    public static void waitThenContinueLong() throws InterruptedException {

        double baseSleep = CHECK_INTERVAL;
        double randomVariance = CHECK_INTERVAL;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        Thread.sleep((long) baseSleep);
    }

    public static void waitThenContinueShort() throws InterruptedException {

        double baseSleep = (double) CHECK_INTERVAL / 10d;
        double randomVariance = (double) CHECK_INTERVAL / 10d;

        baseSleep += Math.random() * randomVariance;

        Log.i(TAG, "Sleeping for " + baseSleep + " milliseconds total!");

        Thread.sleep((long) baseSleep);
    }
}
