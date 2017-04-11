package shadon.technologies.app.craigslistdiffchecker.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.config.CraigsConfig;
import shadon.technologies.app.craigslistdiffchecker.service.AndroidBackgroundService;

public class CraigsDiff extends AppCompatActivity {

    //todo Handle bad search errors
    //todo Handle no-internet errors(?)

    public static final String TAG = "CraigsDiff";

    Button btnStartService;
    Button btnStopService;
    Button btnManageSearches;
    TextView textViewServiceStatus;
    Intent backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CraigsConfig.SetConfigDefaults();

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnManageSearches = (Button) findViewById(R.id.btnManageSearches);

        textViewServiceStatus = (TextView) findViewById(R.id.textViewServiceStatus);

        backgroundService = new Intent(getBaseContext(), AndroidBackgroundService.class);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serviceIsRunning()) {
                    CraigsConfig.USER_STOPPED = false;
                    startService(backgroundService);
                    updateServiceStatusText();
                }
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CraigsConfig.USER_STOPPED = true;
                stopService(backgroundService);
                updateServiceStatusText();
            }
        });

        btnManageSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(),ManageSearches.class);
                startActivity(nextScreen);
            }
        });
    }

    private void updateServiceStatusText() {
        if (serviceIsRunning()) {
            textViewServiceStatus.setText("RUNNING");
            textViewServiceStatus.setTextColor(Color.GREEN);
        } else {
            textViewServiceStatus.setText("NOT RUNNING");
            textViewServiceStatus.setTextColor(Color.RED);
        }
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AndroidBackgroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyStoragePermissions();
        updateServiceStatusText();
    }

    // stopService is explicitly called here to trigger the service restart logic when the Android
    //  OS decides to kill the CraigsDiff app (this happens regularly for resource optimization)
    @Override
    protected void onDestroy() {
        stopService(backgroundService);
        Log.i(TAG, "onDestroy was called");
        super.onDestroy();

    }

    // This is done to stop the back button from temporarily killing the service when the user
    //  goes back to the home screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private void verifyStoragePermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
//            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
}
