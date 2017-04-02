package com.example.maveric.craigslistdiffchecker.uniquenessCheckers;

import android.util.Log;

import com.example.maveric.craigslistdiffchecker.files.FileIO;
import com.example.maveric.craigslistdiffchecker.files.Paths;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;
import com.example.maveric.craigslistdiffchecker.service.CraigslistChecker;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

        ArrayList<CraigslistAd> listCraigslistAds = new ArrayList<>();
        ArrayList<CraigslistAd> listNewCraigslistAds = new ArrayList<>();

        File linkCacheFolder = new File(Paths.cachedSearchesFileLocation);
        linkCacheFolder.getParentFile().mkdirs();
        ArrayList<String> listCachedURLs = FileIO.readFile(linkCacheFolder);

        if(listCachedURLs == null){
            listCachedURLs = new ArrayList<>();
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
            if (e.childNodes().size() == 1){
                CraigslistAd craigslistAd = new CraigslistAd();
                craigslistAd.url = e.attr("abs:href");
                craigslistAd.title = ((TextNode) e.childNode(0)).text();
                listCraigslistAds.add(craigslistAd);
            }
        }

        for (CraigslistAd craigslistAd : listCraigslistAds) {

            Pattern p = Pattern.compile(".*craigslist.org/.../[0-9]*.html");
            Matcher m = p.matcher(craigslistAd.url);
            if (m.matches()) {
                listNewCraigslistAds.add(craigslistAd);
            }
        }

        CraigslistAd newAd = findNewLink(listNewCraigslistAds, listCachedURLs);
        if(newAd == null) {
            Log.e(TAG, "There are no new links on the page");
        } else {
            checker.callPublishProgress(newAd.title, newAd.url);
            writeLinksFile(newAd);
        }
    }

    private static CraigslistAd findNewLink(ArrayList<CraigslistAd> listNewCriagslistAds, ArrayList<String> listSavedSaleUrls) {

        Log.d(TAG, "List of new links:");

        for(CraigslistAd ad : listNewCriagslistAds){
            Log.d(TAG, ad.url);
        }

        Log.d(TAG, "List of old links:");

        for(String s : listSavedSaleUrls){
            Log.d(TAG, s);
        }

        ArrayList<CraigslistAd> listUnseenCraigslistAds = new ArrayList<>(listNewCriagslistAds);

        for (CraigslistAd ad : listNewCriagslistAds) {
            for (String savedUrl : listSavedSaleUrls) {
                if (ad.url.equals(savedUrl)) {
                    listUnseenCraigslistAds.remove(ad);
                    continue;
                }
            }
        }

        Log.i(TAG, "Printing out all link differences");
        for(CraigslistAd ad : listUnseenCraigslistAds){
            Log.i(TAG, ad.url);
        }

        if (listUnseenCraigslistAds.size() == 0) {
            return null;
        }

        // Just grab the first new link for now
        CraigslistAd newAd = listUnseenCraigslistAds.get(0);

        return newAd;
    }

    private static void writeLinksFile(CraigslistAd ad){

        Log.d(TAG, "Persisting a new URL: " + ad.url);

        File linksFileLocation = new File(Paths.cachedSearchesFileLocation);

        if(!linksFileLocation.exists()){
            Log.i(TAG, "The directory doesn't exist! Let's fix that");
            linksFileLocation.getParentFile().mkdirs();
        }

        try(FileWriter fw = new FileWriter(Paths.cachedSearchesFileLocation, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(ad.url);
        } catch (IOException e) {
            Log.e(TAG, "Unable to write file");
            e.printStackTrace();
        }
    }
}
