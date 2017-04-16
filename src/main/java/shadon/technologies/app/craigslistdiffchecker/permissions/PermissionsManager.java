package shadon.technologies.app.craigslistdiffchecker.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import shadon.technologies.app.craigslistdiffchecker.dialog.InfoDialog;

/**
 * Created by Maveric on 4/15/2017.
 */

public class PermissionsManager {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public  static boolean CheckStoragePermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void RequestStoragePermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    public static void showPremissionsExplanation(Activity activity) {
        FragmentManager manager = activity.getFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString("title", "Permission is critical for app function");
        bundle.putString("message", "This app cannot work without writing your search configuration to the device. If the dialog requesting permissions is no longer showing up," +
                " go to your application settings and explicitly allow this app to access your device's storage.");

        InfoDialog myDialog = new InfoDialog();
        myDialog.setArguments(bundle);
        myDialog.show(manager, "info");
    }
}
