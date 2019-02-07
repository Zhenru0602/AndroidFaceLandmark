package com.example.zhenru.myfaceid;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class RecognitionActivity extends AppCompatActivity {
    //data for face compare
    private static int NUMPOINT = 8;
    private static int NUMPAIRS = NUMPOINT*(NUMPOINT-1)/2;
    private static int DIM = 2;
    private static double THRESHOLD = 1.7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

//        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//        InputStream stream = getResources().openRawResource(R.raw.face);
        File file = getOutputFile();
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Detector<Face> safeDetector = new SafeFaceDetector(detector);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            Log.w("ERROR", "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                Log.w("ERROR", "Low Storage");
            }
        }

        Log.v("LENGTHOF","Size"+faces.size());
        String data =  "Tester:";
        if(faces.size() == 1){
            Face face = faces.valueAt(0);
            for (Landmark landmark : face.getLandmarks()) {
                double cx = landmark.getPosition().x;
                double cy = landmark.getPosition().y;
                data += cx + "," + cy + ",";
            }
            Log.v("TEXTDATA", data);
            String result = getName(data,getTextFilePath());
            Log.v("RECOGRESULT", result);
        }
        else{
            Log.w("ERROR", "No Face Or More Than One Face Be Detected");
        }

        safeDetector.release();
    }

    /** Create a File for saving an image or video */
    private static File getOutputFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MyFaceIdApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyFaceIdApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "face"+".jpg");
        return mediaFile;
    }

    //method to get txt file path
    private static String getTextFilePath(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MyFaceIdApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyFaceIdApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "faceData"+".txt");
        return mediaFile.getAbsolutePath();
    }

    //class to store personal info
    static class PersonalInfo
    {
        public PersonalInfo(String nm, double[][] coord) {
            name = nm;
            coordinates = coord;
        }

        public String name;
        public double[][] coordinates;
    }

    //helper method for face compare
    private static double calculateMSE(double[] cmp1, double[] cmp2) {
        // calculate the MSE between two distance array
        assert cmp1.length == cmp2.length;

        double squareSum = 0.0;

        for (int i = 0; i < cmp1.length; i++) {
            squareSum += Math.pow(cmp1[i] - cmp2[i], 2);
        }

        return Math.sqrt(squareSum);
    }


    private static double[] calculateDist(double[][] points) {
        // calculate the distance between points in order
        double[] distances = new double[NUMPAIRS];
        int k = 0;

        for (int i = 0; i < points.length; i++) {

            double[] point1 = points[i];

            for (int j = i+1; j < points.length; j++) {

                double[] point2 = points[j];
                double x_diff = point1[0]-point2[0];
                double y_diff = point1[1]-point2[1];
                double sq_sum = Math.pow(x_diff, 2) + Math.pow(y_diff, 2);
                double dist = Math.sqrt(sq_sum);

                distances[k++] = dist;
            }
        }

        return distances;
    }


    private static double[] normalize(double[] distances) {
        // normalize each distance data
        double[] normalizedDist = new double[distances.length];
        double normalizer = distances[0]/10;

        for (int i = 0; i < distances.length; i++) {
            normalizedDist[i] = distances[i]/normalizer;
        }

        return normalizedDist;
    }


    private static PersonalInfo textToArray(String pplinfo) {
        // file read functions, need to be in another class or file
        String[] tokens = pplinfo.split(":");
        String name = tokens[0];
        String[] rawcoords = tokens[1].split(",");

        double[][] coords = new double[NUMPOINT][DIM];

        for (int i = 0; i < NUMPOINT; i++) {
            double x = Double.parseDouble(rawcoords[2*i]);
            double y = Double.parseDouble(rawcoords[2*i+1]);
            coords[i][0] = x;
            coords[i][1] = y;
        }

        System.out.println("Persona read successful");
        return new PersonalInfo(name, coords);
    }



    public static String getName(String test, String fileName) {
        // main()
        // source data below for test only
        PersonalInfo testInfo = textToArray(test);
        // test object ends
        // init variables
        double[] distances1 = calculateDist(testInfo.coordinates);
        distances1 = normalize(distances1);

        String candidate = "";
        double mse = 0.0;
        double mse_min = 100;

        // start comparisons with data in databse
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                PersonalInfo target = textToArray(line);
                String target_name = target.name;
                double[][] target_coords = target.coordinates;
                double[] distances2 = calculateDist(target_coords);
                distances2 = normalize(distances2);
                mse = calculateMSE(distances1, distances2);
                System.out.println("MSE = " + mse);
                if (mse < mse_min) {
                    mse_min = mse;
                    candidate = target_name;
                }
            }
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to connect to database");
        }
        catch(IOException ex) {
            System.out.println("Error reading data from database");
        }
        catch(ArrayIndexOutOfBoundsException ex) {
            System.out.println("Error: data not conformed to rule");
        }

        // provide final evaluation
        if (mse_min < 100) {
            return candidate;
        }
        else {
            return "NOT RECOGNIZED.";
        }
    }
}
