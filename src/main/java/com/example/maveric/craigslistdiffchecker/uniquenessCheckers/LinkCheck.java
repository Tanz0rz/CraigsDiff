package com.example.maveric.craigslistdiffchecker.uniquenessCheckers;

import android.util.Log;

import com.example.maveric.craigslistdiffchecker.files.Paths;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;
import com.example.maveric.craigslistdiffchecker.service.CraigslistChecker;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maveric on 6/25/2016.
 */
public class LinkCheck {

    private static final String TAG = "LinkCheck";

    public static void CheckSaleLinks(CraigslistChecker checker, CraigSearch search){

        Log.i(TAG, "RUNNING SEARCH NAMED: " + search.name);
        Log.d(TAG, "Search url: " + search.url);

        HashSet<String> setUrls = new HashSet<>();
        ArrayList<String> listJustPulledUrls = new ArrayList<>();

        HashMap<CraigSearch, ArrayList<String>> mapMasterURLList = checker.mapSearches;
        ArrayList<String> listCachedUrls = mapMasterURLList.get(search);

        if(listCachedUrls == null){
            listCachedUrls = new ArrayList<>();
        }

        Connection.Response html;
        Document document = null;

        Connection jsoup = Jsoup.connect(search.url);
        jsoup.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36");
        try {
            html = jsoup.execute();
            document = html.parse();
        } catch (IOException e) {
            Log.e(TAG, "Unable to contact the website!!!");
            e.printStackTrace();
            return;
        }

        Elements links = document.select("a");

        for (Element e : links) {
            setUrls.add(e.attr("abs:href"));
        }

//        Log.d(TAG, "Raw links: ");
//        for(String s : setUrls){
//            Log.d(TAG, s);
//        }

        Iterator urlsCursor = setUrls.iterator();

        while (urlsCursor.hasNext()) {

            String url = (String) urlsCursor.next();

            Pattern p = Pattern.compile(".*craigslist.org/.../[0-9]*.html");
            Matcher m = p.matcher(url);
            if (m.matches()) {
                listJustPulledUrls.add(url);
            }
        }

        Log.d(TAG, "Matching urls: ");
        for (String url : listJustPulledUrls) {
            Log.d(TAG, url);
        }

        Log.d(TAG, "Old urls: ");
        for (String url : listCachedUrls) {
            Log.d(TAG, url);
        }

        if(checkIfChanged(listJustPulledUrls, listCachedUrls)) {
            Log.i(TAG, "The links have changed since I last saw them!");

            if(checkIfShouldNotify(listJustPulledUrls.size(), listCachedUrls.size())){
                String newSearchURL = findNewLink(listJustPulledUrls, listCachedUrls);
                if(newSearchURL == null){
                    Log.e(TAG, "The results of diff on the lists was greater than 1 link! Unable to spawn notification.");
                } else {
                    checker.callPublishProgress(search.name, newSearchURL);
                }
            } else {
                Log.i(TAG, "It looks like a link was removed from the page. No reason to send notification.");
            }

            // Just pass the search "name" in for the 3rd argument and that can be the search file to check against
            writeLinksFile(listJustPulledUrls, Paths.folderLocationLinkCache, search.name);
            mapMasterURLList.get(search).addAll(listJustPulledUrls);
        } else {
            Log.i(TAG, "No changes spotted on the page.");
        }
    }

    private static String findNewLink(ArrayList<String> listNewSaleUrls, ArrayList<String> listSavedSaleUrls) {

        Log.d(TAG, "List of new links:");

        for(String s : listNewSaleUrls){
            Log.d(TAG, s);
        }

        Log.d(TAG, "List of old links:");

        for(String s : listSavedSaleUrls){
            Log.d(TAG, s);
        }

        ArrayList<String> linkListDifferences = new ArrayList<>(CollectionUtils.subtract(listNewSaleUrls, listSavedSaleUrls));

        Log.i(TAG, "Printing out all link differences");
        for(String s : linkListDifferences){
            Log.i(TAG, s);
        }

        // Need to add logic that makes this noisy when multiple links show up at the same time. Just picking the first one I see for now
//        if (linkListDifferences.size() > 1){
//            return null;
//        }

        String newSearch = linkListDifferences.get(0);

        return newSearch;
    }

    private static boolean checkIfShouldNotify(int newListSize, int savedListSize) {
        if(newListSize > savedListSize){
            return true;
        }
        return false;
    }

    private static boolean checkIfChanged(ArrayList<String> currentListUrls, ArrayList<String> savedListUrls) {

        if(currentListUrls.size() != savedListUrls.size()){
            return true;
        }
        return false;
    }

    private static void writeLinksFile(ArrayList<String> links, String folderLocation, String fileName){

        Log.d(TAG, "Persisting page link state. Link count: " + links.size());

        File linksFileLocation = new File(folderLocation);

        if(!linksFileLocation.exists()){
            Log.i(TAG, "The directory doesn't exist! Let's fix that");
            Boolean result = linksFileLocation.mkdir();
            Log.d(TAG, "Result of creating the directory: " + result);
        }

        String absoluteFileLocation = folderLocation + File.separator + fileName + ".txt";

        try (PrintWriter out = new PrintWriter(new File(absoluteFileLocation))) {
            for(String url : links){
                out.println(url);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to write file");
            e.printStackTrace();
        }
    }
}
