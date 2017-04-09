package shadon.technologies.app.craigslistdiffchecker.files;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shadon.technologies.app.craigslistdiffchecker.craigsObjects.SavedSearch;

/**
 * Created by Monday on 7/31/2016.
 */
public class ConfigFiles {

    public static String TAG = ConfigFiles.class.getName();

    public static ArrayList<SavedSearch> loadAllSavedSearches() {
        File savedSearchesFile = new File(Paths.saveSearchesPath);
        savedSearchesFile.getParentFile().mkdirs();

        if (savedSearchesFile.exists()) {
            try {
                return loadSavedSearches(savedSearchesFile);
            } catch (IOException e) {
                // TODO: exit program here? Continue without any searches loaded?
                Log.e(TAG, "Failed to load saved searches: " + Log.getStackTraceString(e));
            }
        }
        return new ArrayList<>();
    }

    private static ArrayList<SavedSearch> loadSavedSearches(File savedSearchesFile) throws IOException {
        ArrayList<SavedSearch> searchesList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(savedSearchesFile));
        JsonReader reader = new JsonReader(bufferedReader);
        if (reader.hasNext()) {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                reader.nextName();
                String name = reader.nextString();
                reader.nextName();
                String url = reader.nextString();
                Log.i(TAG, "Found search line: " + name + " = '" + url + "'");
                searchesList.add(new SavedSearch(name, url));
                reader.endObject();
            }
            reader.endArray();
        }
        reader.close();
        return searchesList;
    }

    public static void saveSearchesToFile(List<SavedSearch> searchesToSave) throws IOException {
        File savedSearchesFile = new File(Paths.saveSearchesPath);
        Log.e(TAG, "Trying to make the directory: " + Paths.saveSearchesPath);
        savedSearchesFile.getParentFile().mkdirs();
        writeToFile(searchesToSave);
    }

    private static void writeToFile(List<SavedSearch> searches) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Paths.saveSearchesPath));
        JsonWriter writer = new JsonWriter(bufferedWriter);

        writer.beginArray();
        for (SavedSearch search : searches) {
            writer.beginObject();
            writer.name("name");
            writer.value(search.name);
            writer.name("url");
            writer.value(search.url);
            writer.endObject();
        }
        writer.endArray();
        writer.close();
    }
}
