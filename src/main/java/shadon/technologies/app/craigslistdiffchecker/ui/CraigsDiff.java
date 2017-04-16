package shadon.technologies.app.craigslistdiffchecker.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.config.CraigsConfig;
import shadon.technologies.app.craigslistdiffchecker.permissions.PermissionsManager;
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
        if (!PermissionsManager.CheckStoragePermissions(this)) {
            PermissionsManager.RequestStoragePermissions(this);
        }

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnManageSearches = (Button) findViewById(R.id.btnManageSearches);

        textViewServiceStatus = (TextView) findViewById(R.id.textViewServiceStatus);

        backgroundService = new Intent(getBaseContext(), AndroidBackgroundService.class);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serviceIsRunning() && PermissionsManager.CheckStoragePermissions(getBaseContext())) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    PermissionsManager.showPremissionsExplanation(this);
                }
                return;
            }
        }
    }
}
