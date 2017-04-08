package shadon.technologies.app.craigslistdiffchecker.uniquenessCheckers;

import android.util.Log;

import shadon.technologies.app.craigslistdiffchecker.files.FileIO;
import shadon.technologies.app.craigslistdiffchecker.files.Paths;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigsListSavedSearch;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;
import shadon.technologies.app.craigslistdiffchecker.service.CraigslistChecker;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maveric on 6/25/2016.
 */
public class LinkCheck {

    private static final String TAG = "LinkCheck";

    public static void CheckSaleLinks(CraigslistChecker checker, CraigsListSavedSearch search){

        Log.i(TAG, "RUNNING SEARCH NAMED: " + search.name);
        Log.d(TAG, "Search url: " + search.url);

        ArrayList<CraigslistAd> listCraigslistPageLinks;
        ArrayList<CraigslistAd> listCraigslistAds;

        File linkCacheFolder = new File(Paths.cachedSearchesFileLocation);
        linkCacheFolder.getParentFile().mkdirs();
        ArrayList<String> listOldSearches = FileIO.readFile(linkCacheFolder);

        if(listOldSearches == null){
            listOldSearches = new ArrayList<>();
        }

        listCraigslistPageLinks = readAllLinksFromPageSource(search);

        listCraigslistAds = findAdLinks(listCraigslistPageLinks);

        CraigslistAd newAd = findNewLink(listCraigslistAds, listOldSearches);
        if(newAd == null) {
            Log.i(TAG, "There are no new links on the page");
        } else {
            checker.callPublishProgress(newAd.title, newAd.url, search.name);
            FileIO.writeLinksFile(newAd);
        }
    }

    static private ArrayList<CraigslistAd> findAdLinks(ArrayList<CraigslistAd> listAllPageLinks) {

        ArrayList<CraigslistAd> listCraigslistAds = new ArrayList<>();

        for (CraigslistAd craigslistAd : listAllPageLinks) {

            Pattern p = Pattern.compile(".*craigslist.org/.../[0-9]*.html");
            Matcher m = p.matcher(craigslistAd.url);
            if (m.matches()) {
                listCraigslistAds.add(craigslistAd);
            }
        }

        return listCraigslistAds;

    }

    static private ArrayList<CraigslistAd> readAllLinksFromPageSource(CraigsListSavedSearch search) {

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
            return null;
        }

        Elements links = document.select("a");

        ArrayList<CraigslistAd> listLinksFoundOnPage = new ArrayList<>();

        for (Element e : links) {
            if (e.childNodes().size() == 1){
                CraigslistAd craigslistAd = new CraigslistAd();
                craigslistAd.url = e.attr("abs:href");
                craigslistAd.title = ((TextNode) e.childNode(0)).text();
                listLinksFoundOnPage.add(craigslistAd);
            }
        }

        return listLinksFoundOnPage;
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
}
