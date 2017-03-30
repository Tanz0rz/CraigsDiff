package com.example.maveric.craigslistdiffchecker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.maveric.craigslistdiffchecker.service.BackgroundServiceMonitor;
import com.example.maveric.craigslistdiffchecker.R;

public class CraigsDiff extends AppCompatActivity {

    Button btnStartService;
    Button btnStopService;

    Button btnManageSearches;

    Intent backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnManageSearches = (Button) findViewById(R.id.btnManageSearches);

        backgroundService = new Intent(getBaseContext(), BackgroundServiceMonitor.class);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(backgroundService);
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(backgroundService);
            }
        });

        btnManageSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(),ManageSearchesScreenActivity.class);

                startActivity(nextScreen);
            }
        });
    }
}
