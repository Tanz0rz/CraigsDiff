package shadon.technologies.app.craigslistdiffchecker.ui;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.SavedSearch;
import shadon.technologies.app.craigslistdiffchecker.dialog.DeleteSearchDialogFragment;
import shadon.technologies.app.craigslistdiffchecker.dialog.SearchEditDialogFragment;
import shadon.technologies.app.craigslistdiffchecker.files.ConfigFiles;
import shadon.technologies.app.craigslistdiffchecker.files.FileIO;
import shadon.technologies.app.craigslistdiffchecker.service.AndroidBackgroundService;

/**
 * Created by Monday on 7/31/2016.
 */
public class ManageSearchesScreenActivity extends AppCompatActivity {

    public static String TAG = "ManageSearch";

    ListView lstSearches;
    List<SavedSearch> allSearches;
    ArrayAdapter<SavedSearch> arrayAdapter;

    Button btnAddSearch;

    Intent backgroundService;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_searches);

        fm = getFragmentManager();


        backgroundService = new Intent(getBaseContext(), AndroidBackgroundService.class);

        lstSearches = (ListView) findViewById(R.id.lstSearches);

        allSearches = ConfigFiles.loadAllSavedSearches();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allSearches);

        lstSearches.setAdapter(arrayAdapter);

        lstSearches.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                SavedSearch clickedSearchItem = arrayAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", clickedSearchItem.name);
                showRemoveSearchDialog(bundle);
                return true;
            }
        });

        lstSearches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                SavedSearch clickedSearchItem = arrayAdapter.getItem(position);
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
            if (AndroidBackgroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void addSearch(SavedSearch fromUser, boolean updating) {
        SavedSearch updatingSearch = null;
        String previousURL = null;

        for (SavedSearch search : allSearches) {
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
            SavedSearch savedSearch = allSearches.get(i);
            if (savedSearch.name.equals(searchName)) {

                arrayAdapter.remove(savedSearch);
                try {
                    ConfigFiles.saveSearchesToFile(allSearches);
                    signalRefresh();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save searches: " + Log.getStackTraceString(e));
                    arrayAdapter.insert(savedSearch, i);
                    Toast.makeText(getApplicationContext(), "Failed to remove. Try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FileIO.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
