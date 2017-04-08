package shadon.technologies.app.craigslistdiffchecker.Notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;

import static android.media.AudioManager.RINGER_MODE_SILENT;

/**
 * Created by Maveric on 4/8/2017.
 */

public class NotificationGenerator {

    private static final String TAG = "NotificationGenerator";

    public static void pushNewAdNotification(Service backgroundService, CraigslistAd ad, String searchTitle){

        Log.d(TAG, "Pushing notification!");

        Log.d(TAG, "Notification will take you to " + ad.url);

        Vibrator v = (Vibrator) backgroundService.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 500, 200, 500, 200, 500, 200, 500, 100, 200, 100, 200, 100};

        AudioManager am = (AudioManager)backgroundService.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = am.getRingerMode();

        if (ringerMode != RINGER_MODE_SILENT) {
            v.vibrate(pattern, -1);
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.url));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(backgroundService, 0, browserIntent, 0);

        Notification notification = new Notification.Builder(backgroundService)
                .setTicker(ad.title)
                .setContentTitle(ad.title)
                .setContentText(searchTitle)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pIntent).build();

        NotificationManager nm = (NotificationManager) backgroundService.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(((int) System.currentTimeMillis() % 10000), notification);
    }
}
