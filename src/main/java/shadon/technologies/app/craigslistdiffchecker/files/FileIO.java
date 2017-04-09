package shadon.technologies.app.craigslistdiffchecker.files;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;

/**
 * Created by Maveric on 3/29/2017.
 */

public class FileIO {

    public static final String TAG = "FileIO";

    public static ArrayList<String> readFile(File fileToRead){

        Scanner scanner = null;
        ArrayList<String> urls = new ArrayList<>();

        try {

            scanner = new Scanner(fileToRead);
            while(scanner.hasNext()) {
                urls.add(scanner.nextLine().trim());
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find file! We will just create it when we save.");
        } catch (Exception e) {
            Log.e(TAG, "Something went very wrong:");
            e.printStackTrace();
        } finally {
            if (scanner != null){
                scanner.close();
            }
        }
        return urls;
    }

    public static void writeLinksFile(CraigslistAd ad){

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

    public static void writeLogsToS3(Context context) throws Exception {

        final int numberOfLinesToCapture = 100;

        // Get the stream for the logcat log files
        Process process = Runtime.getRuntime().exec("logcat -d");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        // Dump the log stream into a ring buffer in order to get the latest N logs
        Buffer ringBuffer = new CircularFifoBuffer(numberOfLinesToCapture);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            ringBuffer.add(line + "\n");
        }

        // Dump the ring buffer into a string
        String lastNLogLinesAsString = "";
        for (Object logLine : ringBuffer) {
            lastNLogLinesAsString += logLine;
        }

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-west-2:98988cbf-a82d-4fb8-af0d-26d71b480f60", // Identity Pool ID
                Regions.US_WEST_2 // Region
        );

        AmazonS3 s3client = new AmazonS3Client(credentialsProvider.getCredentials());

        String key = String.format("crash-logs/crashLog-%d.txt", System.currentTimeMillis());

        ByteArrayInputStream stream = new ByteArrayInputStream(lastNLogLinesAsString.getBytes("UTF-8"));
        Integer length=lastNLogLinesAsString.getBytes("UTF-8").length;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(length);
        s3client.putObject(new PutObjectRequest("craigsdiff", key, stream, metadata));
    }
}
