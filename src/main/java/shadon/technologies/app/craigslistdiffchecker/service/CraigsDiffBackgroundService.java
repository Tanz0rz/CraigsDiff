package shadon.technologies.app.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.files.FileIO;
import shadon.technologies.app.craigslistdiffchecker.ui.CraigsDiff;

/**
 * Created by Maveric on 6/24/2016.
 */
public class CraigsDiffBackgroundService extends Service {

    final String TAG = "CraigsBackgroundService";
    CraigslistChecker craigslistChecker;

    @Override
    public void onCreate(){

        Log.i(TAG, "Service created!");

        craigslistChecker = new CraigslistChecker(this);
        craigslistChecker.init();
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
        if (CraigsDiff.USER_STOPPED) {
            Log.i(TAG, "Service stopped by user");
        } else {
            Log.i(TAG, "Service killed by Android OS");
            Intent broadcastIntent = new Intent("shadon.technologies.app.craigslistdiffchecker.RestartSensor");
            sendBroadcast(broadcastIntent);
        }

        FileIO.writeLogcatLogsToFile();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
