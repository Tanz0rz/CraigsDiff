package shadon.technologies.app.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.config.CraigsConfig;
import shadon.technologies.app.craigslistdiffchecker.files.ConfigFiles;
import shadon.technologies.app.craigslistdiffchecker.network.NetworkCommunication;

/**
 * Created by Maveric on 6/24/2016.
 */
public class AndroidBackgroundService extends Service {

    final String TAG = "CraigsBackgroundService";

    WorkerThread thread;

    @Override
    public void onCreate(){
        Log.i(TAG, "Service created!");
        NetworkCommunication.writeLogsToS3(this);
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

        if (CraigsConfig.USER_STOPPED) {
            Log.i(TAG, "Service stopped by user");
        } else {
            Log.i(TAG, "Service killed by Android OS");
            Intent broadcastIntent = new Intent("shadon.technologies.app.craigslistdiffchecker.RestartSensor");
            sendBroadcast(broadcastIntent);
        }
        NetworkCommunication.writeLogsToS3(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
