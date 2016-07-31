package com.example.maveric.craigslistdiffchecker.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maveric.craigslistdiffchecker.R;
import com.example.maveric.craigslistdiffchecker.files.ConfigFiles;
import com.example.maveric.craigslistdiffchecker.service.BackgroundServiceMonitor;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monday on 7/31/2016.
 */
public class ManageSearchesScreenActivity extends AppCompatActivity{

    public static String TAG = "ManageSearch";

    ListView lstSeaches;
    List<CraigSearch> allSearches;
    ArrayAdapter<CraigSearch> arrayAdapter;

    Button btnAddSearch;

    Intent backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_searches);

        backgroundService = new Intent(getBaseContext(), BackgroundServiceMonitor.class);

        lstSeaches = (ListView) findViewById(R.id.lstSearches);

        allSearches = ConfigFiles.loadAllSavedSearches();

        arrayAdapter =  new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allSearches);

        lstSeaches.setAdapter(arrayAdapter);

        lstSeaches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                Log.d(TAG, adapterView.getItemAtPosition(position).toString());
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        ManageSearchesScreenActivity.this);
                alert.setTitle("Remove");
                alert.setMessage("Would you like to remove this search?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do your work here
                        Log.d(TAG, "" + position);
                        arrayAdapter.remove(arrayAdapter.getItem(position));
                        ConfigFiles.saveSearchesToFile(allSearches);
                        signalRefresh();
                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });

        btnAddSearch = (Button) findViewById(R.id.btnAddSearch);

        btnAddSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add search clicked");
                showDialog(0);
            }
        });
    }

    protected Dialog onCreateDialog(int id)
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LinearLayout lila1= new LinearLayout(this);
        lila1.setOrientation(LinearLayout.VERTICAL); //1 is for vertical orientation
        final TextView nameLabel = new TextView(this);
        nameLabel.setText("Name");
        final EditText nameInput = new EditText(this);

        final TextView linkLabel = new TextView(this);
        linkLabel.setText("URL");
        final EditText linkInput = new EditText(this);

        lila1.addView(nameLabel);
        lila1.addView(nameInput);
        lila1.addView(linkLabel);
        lila1.addView(linkInput);
        alert.setView(lila1);

        alert.setTitle("Add Search");

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                CraigSearch newSearch = new CraigSearch(nameInput.getText().toString(), linkInput.getText().toString());
                arrayAdapter.add(newSearch);
                //save to file
                ConfigFiles.saveSearchesToFile(allSearches);
                Toast.makeText(getApplicationContext(), "'" + newSearch.name + "' created", Toast.LENGTH_SHORT).show();
                signalRefresh();
            }                     });
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();    }     });
        return alert.create();
    }

    private void signalRefresh() {
        stopService(backgroundService);
        startService(backgroundService);
    }
}
