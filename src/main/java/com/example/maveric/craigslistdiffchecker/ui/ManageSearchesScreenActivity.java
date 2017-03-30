package com.example.maveric.craigslistdiffchecker.ui;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.FragmentManager;
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
import com.example.maveric.craigslistdiffchecker.dialog.DeleteSearchDialogFragment;
import com.example.maveric.craigslistdiffchecker.dialog.SearchEditDialogFragment;
import com.example.maveric.craigslistdiffchecker.files.ConfigFiles;
import com.example.maveric.craigslistdiffchecker.service.BackgroundServiceMonitor;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;

import java.io.IOException;
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
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_searches);

        fm = getFragmentManager();


        backgroundService = new Intent(getBaseContext(), BackgroundServiceMonitor.class);

        lstSearches = (ListView) findViewById(R.id.lstSearches);

        allSearches = ConfigFiles.loadAllSavedSearches();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allSearches);

        lstSearches.setAdapter(arrayAdapter);

        lstSearches.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                CraigSearch clickedSearchItem = arrayAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", clickedSearchItem.name);
                showRemoveSearchDialog(bundle);
                return true;
            }
        });

        lstSearches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                CraigSearch clickedSearchItem = arrayAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", clickedSearchItem.name);
                bundle.putString("url", clickedSearchItem.url);
                showConfigureSearchDialog(bundle);
            }
        });

        btnAddSearch = (Button) findViewById(R.id.btnAddSearch);

        btnAddSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add search clicked");
                showConfigureSearchDialog(new Bundle());
            }
        });
    }

    private void showConfigureSearchDialog(Bundle bundle) {
        SearchEditDialogFragment fragment = new SearchEditDialogFragment();
        fragment.setArguments(bundle);
        fragment.show(fm, "ConfigureSearch");
    }

    private void showRemoveSearchDialog(Bundle bundle) {
        DeleteSearchDialogFragment fragment = new DeleteSearchDialogFragment();
        fragment.setArguments(bundle);
        fragment.show(fm, "RemoveSearch");
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

    public void addSearch(CraigSearch fromUser, boolean updating) {
        CraigSearch updatingSearch = null;
        String previousURL = null;

        for (CraigSearch search : allSearches) {
            if (search.name.equals(fromUser.name)) {
                if (updating) {
                    updatingSearch = search;
                    previousURL = search.url;
                    search.url = fromUser.url;
                    break;
                } else {
                    Toast.makeText(getApplicationContext(), "'" + search.name + "' already exists", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString("url", fromUser.url);
                    showConfigureSearchDialog(bundle);
                    return;
                }
            }
        }
        if (!updating) {
            // searched through all existing services, we have a new search with a unique name: add it
            arrayAdapter.add(fromUser);
        }

        try {
            ConfigFiles.saveSearchesToFile(allSearches);
            Toast.makeText(getApplicationContext(), "'" + fromUser.name + "' " + (updating ? "updated" : "created"), Toast.LENGTH_SHORT).show();
            signalRefresh();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save searches: " + Log.getStackTraceString(e));
            Toast.makeText(getApplicationContext(), "Failed to add. Try again", Toast.LENGTH_SHORT).show();
            if (!updating) {
                arrayAdapter.remove(fromUser);
            } else {
                if (updatingSearch != null && previousURL != null) {
                    updatingSearch.url = previousURL;
                }
            }
        }
    }

    public void removeSearch(String searchName) {
        for (int i = 0; i < allSearches.size(); i++) {
            CraigSearch craigSearch = allSearches.get(i);
            if (craigSearch.name.equals(searchName)) {

                arrayAdapter.remove(craigSearch);
                try {
                    ConfigFiles.saveSearchesToFile(allSearches);
                    signalRefresh();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save searches: " + Log.getStackTraceString(e));
                    arrayAdapter.insert(craigSearch, i);
                    Toast.makeText(getApplicationContext(), "Failed to remove. Try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
