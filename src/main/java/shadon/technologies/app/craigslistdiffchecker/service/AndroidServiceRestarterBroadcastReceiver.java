package shadon.technologies.app.craigslistdiffchecker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Maveric on 4/1/2017.
 */

public class AndroidServiceRestarterBroadcastReceiver extends BroadcastReceiver {

    final String TAG = "CraigsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "The phone just booted up, the app was just updated, or (possibly) the service was" +
                " consumed by Android and it triggered this BroadcastREceiver. Starting the service up.");
        Intent myIntent = new Intent(context, AndroidBackgroundService.class);
        context.startService(myIntent);
    }
}
