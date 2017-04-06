package shadon.technologies.app.craigslistdiffchecker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Maveric on 4/1/2017.
 */

public class CraigsDiffServiceRestarterBroadcastReceiver extends BroadcastReceiver {

    final String TAG = "CraigsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "The phone just booted up or the service was consumed by Android. Starting the service up.");
        Intent myIntent = new Intent(context, CraigsDiffBackgroundService.class);
        context.startService(myIntent);
    }
}
