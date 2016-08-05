package com.example.maveric.craigslistdiffchecker.ui;

import android.app.ActivityManager;
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
import com.example.maveric.craigslistdiffchecker.exception.SearchSaveException;
import com.example.maveric.craigslistdiffchecker.files.ConfigFiles;
import com.example.maveric.craigslistdiffchecker.service.BackgroundServiceMonitor;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;

import java.util.List;

/**
 * Created by Monday on 7/31/2016.
 */
public class ManageSearchesScreenActivity extends AppCompatActivity {

    public static String TAG = "ManageSearch";

    ListView lstSearches;
    List<CraigSearch> allSearches;
    ArrayAdapter<CraigSearch> arrayAdapter;

    Button btnAddSearch;

    Intent backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_searches);

        backgroundService = new Intent(getBaseContext(), BackgroundServiceMonitor.class);

        lstSearches = (ListView) findViewById(R.id.lstSearches);

        allSearches = ConfigFiles.loadAllSavedSearches();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allSearches);

        lstSearches.setAdapter(arrayAdapter);

        addLongClickListener();

        addShortClickListener();

        btnAddSearch = (Button) findViewById(R.id.btnAddSearch);

        btnAddSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add search clicked");
                showDialog(-1);
            }
        });
    }

    private void addShortClickListener() {
        lstSearches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                showDialog(position);
            }
        });
    }


    private void addLongClickListener() {
        lstSearches.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
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
                        CraigSearch toBeRemoved = arrayAdapter.getItem(position);
                        arrayAdapter.remove(toBeRemoved);
                        try {
                            ConfigFiles.saveSearchesToFile(allSearches);
                            signalRefresh();
                        } catch (SearchSaveException e) {
                            Log.e(TAG, "Failed to save searches: " + Log.getStackTraceString(e));
                            arrayAdapter.insert(toBeRemoved, position);
                            Toast.makeText(getApplicationContext(), "Failed to remove. Try again", Toast.LENGTH_SHORT).show();
                        }
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
                return true;
            }
        });
    }

    protected Dialog onCreateDialog(int index) {
        final boolean newSearch = index < 0;
        CraigSearch modifyingSearch = null;
        if (!newSearch) {
            modifyingSearch = arrayAdapter.getItem(index);
        }
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LinearLayout lila1 = new LinearLayout(this);
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

        if (!newSearch) {
            nameInput.setText(modifyingSearch.name);
            nameInput.setEnabled(false);
            linkInput.setText(modifyingSearch.url);
        }

        alert.setView(lila1);

        alert.setTitle(modifyingSearch == null ? "Add Search" : "Update Search");

        final CraigSearch finalModifyingSearch = modifyingSearch;
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                CraigSearch searchToPersist = null;
                if (newSearch) {
                    searchToPersist = new CraigSearch(nameInput.getText().toString(), linkInput.getText().toString());
                    arrayAdapter.add(searchToPersist);
                } else {
                    searchToPersist = finalModifyingSearch;
                }
                //save to file
                try {
                    ConfigFiles.saveSearchesToFile(allSearches);
                } catch (SearchSaveException e) {
                    Log.e(TAG, "Failed to save searches: " + Log.getStackTraceString(e));
                    if (newSearch) {
                        arrayAdapter.remove(searchToPersist);
                    }
                    Toast.makeText(getApplicationContext(), "Failed to add. Try again", Toast.LENGTH_SHORT).show();
                }
                String toastStatus = newSearch ? "created" : "updated";
                Toast.makeText(getApplicationContext(), "'" + searchToPersist.name + "' " + toastStatus, Toast.LENGTH_SHORT).show();
                signalRefresh();
            }
        });
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        return alert.create();
    }

    private void signalRefresh() {
        if (serviceIsRunning()) {
            Log.i(TAG, "Service was running. Restarting");
            stopService(backgroundService);
            startService(backgroundService);
        }
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BackgroundServiceMonitor.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
