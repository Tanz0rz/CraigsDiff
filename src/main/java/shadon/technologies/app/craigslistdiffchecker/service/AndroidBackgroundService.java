package shadon.technologies.app.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.files.ConfigFiles;
import shadon.technologies.app.craigslistdiffchecker.files.FileIO;
import shadon.technologies.app.craigslistdiffchecker.ui.CraigsDiff;

/**
 * Created by Maveric on 6/24/2016.
 */
public class AndroidBackgroundService extends Service {

    final String TAG = "CraigsBackgroundService";

    WorkerThread thread;

    @Override
    public void onCreate(){

        Log.i(TAG, "Service created!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        thread = new WorkerThread(this, ConfigFiles.loadAllSavedSearches());
        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        thread.stopExecution();
        thread.interrupt();

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
