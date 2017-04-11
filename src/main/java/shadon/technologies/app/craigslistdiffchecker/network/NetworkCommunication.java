package shadon.technologies.app.craigslistdiffchecker.network;

import android.content.Context;
import android.os.Build;
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
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/**
 * Created by Maveric on 4/9/2017.
 */

public class NetworkCommunication {

    private static String TAG = "NetworkCommunication";

    public static void writeLogsToS3(Context context) {
        S3LogWriter logWriter = new S3LogWriter(context);
        logWriter.start();
    }

    private static class S3LogWriter extends Thread {

        private Context context;

        public S3LogWriter(Context context) {
            this.context = context;
        }

        public void run() {

            final int numberOfLinesToCapture = 100;

            try {
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

                String deviceManufacturer = Build.MANUFACTURER;
                String deviceModel = Build.MODEL;

                String key = String.format("diag-logs/%s-%s-log-%d.txt", deviceManufacturer, deviceModel, System.currentTimeMillis());

                ByteArrayInputStream stream = new ByteArrayInputStream(lastNLogLinesAsString.getBytes("UTF-8"));
                Integer length = lastNLogLinesAsString.getBytes("UTF-8").length;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(length);
                s3client.putObject(new PutObjectRequest("craigsdiff", key, stream, metadata));
            } catch (Exception e) {
                Log.e(TAG, "Exception was thrown while communicating with the cloud: " + e);
            }
        }
    }
}
