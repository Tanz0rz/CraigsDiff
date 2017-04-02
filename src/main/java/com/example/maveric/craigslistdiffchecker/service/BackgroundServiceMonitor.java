package com.example.maveric.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by Maveric on 6/24/2016.
 */
public class BackgroundServiceMonitor extends Service {

    CraigslistChecker craigslistChecker;

    @Override
    public void onCreate(){

        Toast.makeText(this, "Service Created!", Toast.LENGTH_SHORT).show();

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
        craigslistChecker.cancel(true);
        super.onDestroy();
        Toast.makeText(this, "Service stopped!", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
