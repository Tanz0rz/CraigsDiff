package com.example.maveric.craigslistdiffchecker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Maveric on 4/1/2017.
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, BackgroundServiceMonitor.class);
        context.startService(myIntent);

    }
}
