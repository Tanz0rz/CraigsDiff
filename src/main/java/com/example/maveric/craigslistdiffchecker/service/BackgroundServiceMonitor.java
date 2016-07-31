package com.example.maveric.craigslistdiffchecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.example.maveric.craigslistdiffchecker.exception.SearchLoadException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
        super.onDestroy();
        craigslistChecker.cancel(true);
        Toast.makeText(this, "Service stopped!", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
