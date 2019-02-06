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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //get register from pre activity
        Bundle extras = getIntent().getExtras();
        String name = extras.getString("NAME");

        //get face img stream
        File file = getOutputFile();
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //build bitmap and do face detection
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Detector<Face> safeDetector = new SafeFaceDetector(detector);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        //error handler
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

        if(faces.size() == 1){
            Face face = faces.valueAt(0);
            String data = name + ":";
            for (Landmark landmark : face.getLandmarks()) {
                double cx = landmark.getPosition().x;
                double cy = landmark.getPosition().y;
                data += cx + "," + cy + ",";
            }
            Log.v("TEXTDATA", data);
            writeTextFile(data);
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

    private static void writeTextFile(String data){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MyFaceIdApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyFaceIdApp", "failed to create directory");
            }
        }

        try{
            PrintWriter o = new PrintWriter(new BufferedWriter(new FileWriter(mediaStorageDir.getPath() + File.separator +
                    "faceData"+".txt", true)));
            o.println(data);
            o.close();
        } catch (IOException e) {
            Log.w("ERROR", "Cannot Override The Text File");
        }
    }
}
