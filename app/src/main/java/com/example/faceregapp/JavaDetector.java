package com.example.faceregapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class JavaDetector{

    private File mCascadeFile;
    private String TAG = "JavaDetector";
    private CascadeClassifier detecotr;

    public JavaDetector(InputStream inputStream, File file, String model){
        try{
            InputStream is = inputStream;
            File cascadeDir = file;
            mCascadeFile = new File(cascadeDir, model);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            detecotr = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (detecotr.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                detecotr = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            cascadeDir.delete();
        }catch (Exception e){
            Log.d(TAG, "JavaDetector: exp");
        }

    }

    public CascadeClassifier getDetecotr() {
        return detecotr;
    }
}
