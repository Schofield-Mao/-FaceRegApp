package com.example.faceregapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvStatModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.VisionDetRet;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;


public class MainActivity extends Activity implements View.OnTouchListener ,CvCameraViewListener2 {
    private static final String  TAG              = "MainActivity";
    private Mat                  mRgba;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final Scalar    FONT_COLOR     = new Scalar(255, 0, 0, 255);
    private Mat                    mGray;
    private boolean isViewStart = true;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView imgView;
    private Vibrator mVibrator;
    private FaceDet mFaceDet;
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.face_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);

        imgView = (ImageView) findViewById(R.id.img_view);
        imgView.setOnTouchListener(this);
        mVibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
            }
        }).run();


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat();
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

//        if (mAbsoluteFaceSize == 0) {
//            int height = mGray.rows();
//            if (Math.round(height * mRelativeFaceSize) > 0) {
//                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//            }
//        }
//
//        MatOfRect faces = new MatOfRect();
//        if (mFaceDetector.getDetecotr() != null) {
//            mFaceDetector.getDetecotr().detectMultiScale(mGray, faces, 1.1, 3, 2,
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//        Rect[] facesArray = faces.toArray();
//        for (int i = 0; i < facesArray.length; i++) {
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//        }

        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mVibrator.vibrate(10);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isViewStart) {
                try {
                    Mat img = mRgba.clone();
                    mRgba.copyTo(img);
                    if (img.cols() > 0 && img.rows() > 0) {
                        Toast.makeText(this, "开始表情分析...", Toast.LENGTH_SHORT).show();
                        processImage(img);
                        Bitmap bitmapTemp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(img, bitmapTemp);
                        imgView.setImageBitmap(bitmapTemp);
                        imgView.setVisibility(ImageView.VISIBLE);
                        mOpenCvCameraView.disableView();
                        mOpenCvCameraView.setVisibility(View.INVISIBLE);
                        isViewStart = !isViewStart;
                        Log.d(TAG, "ouTouch: stop on preview");
                    }else {
                        Toast.makeText(this, "照相机未准备好...", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "onTouch: " + ex.getMessage());
                }
            } else {
                mOpenCvCameraView.enableView();
                imgView.setVisibility(ImageView.INVISIBLE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                isViewStart = !isViewStart;
                Toast.makeText(this, "恢复人像采集...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ouTouch: set on preview");
            }

        }
        return true;
    }

    private Mat processImage(Mat img){

        Bitmap bitmapTemp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmapTemp);

        long now = System.currentTimeMillis();
        List<VisionDetRet> results = mFaceDet.detect(bitmapTemp);
        Log.d(TAG, "processImage: cost: "+(System.currentTimeMillis()-now));

        for (final VisionDetRet ret : results) {
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "processImage: "+label);
            Point lt = new Point(ret.getLeft(), ret.getTop());
            Point br = new Point(ret.getRight(), ret.getBottom());
            Log.d(TAG, "processImage: lt: "+lt.toString()+" br: "+br.toString());
            ArrayList<android.graphics.Point> landmarks = ret.getFaceLandmarks();

            for (int i=0;i<landmarks.size();i++) {
                Point temp = new Point(landmarks.get(i).x, landmarks.get(i).y);
                //Core.circle(img, temp,3,FACE_RECT_COLOR, 6);
                Core.putText(img, Integer.toString(i), temp, FONT_HERSHEY_SIMPLEX, 0.5, FACE_RECT_COLOR,2);
            }

            Point explocat = lt;
            if(isCalm(landmarks)){
                if(isDisgust(landmarks)){
                    Core.putText(img, "disgust! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
                }else if(isSad(landmarks)){
                    Core.putText(img, "sad! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
                }else if(isSmile(landmarks)){
                    Core.putText(img, "smile! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
                }else {
                    Core.putText(img, "calm! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
                }
            }else if(isSuprise(landmarks)) {
                Core.putText(img, "suprise! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
            }else if(isAngry(landmarks)) {
                Core.putText(img, "angry! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
            }else if(isScared(landmarks)){
                Core.putText(img, "scared ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
            }else if(isHappy(landmarks)) {
                Core.putText(img, "happy! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
            }else {
                Core.putText(img, "none! ", explocat, FONT_HERSHEY_SIMPLEX, 5, FONT_COLOR, 10);
            }
        }
        return img;
    }

    boolean isSuprise(ArrayList<android.graphics.Point> landmakrs){
        //suprise:60 62 64 66
        android.graphics.Point p1,p2;
        p1 = landmakrs.get(60);
        p2 = landmakrs.get(62);
        Log.d(TAG, "isSuprise k1: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.15){
            return false;
        }
        p1 = landmakrs.get(62);
        p2 = landmakrs.get(64);
        Log.d(TAG, "isSuprise k2: "+(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)<0.15){
            return false;
        }
        p1 = landmakrs.get(64);
        p2 = landmakrs.get(66);
        Log.d(TAG, "isSuprise k3: "+(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.15){
            return false;
        }
        return true;
    }

    boolean isHappy(ArrayList<android.graphics.Point> landmakrs){
        //suprise:48,54,57
        android.graphics.Point p1,p2;
        p1 = landmakrs.get(48);
        p2 = landmakrs.get(57);
        Log.d(TAG, "isHappy k1: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)<0.15){
            return false;
        }
        p1 = landmakrs.get(57);
        p2 = landmakrs.get(54);
        Log.d(TAG, "isHappy k2: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.15){
            return false;
        }
        return true;
    }

    boolean isAngry(ArrayList<android.graphics.Point> landmakrs){
        //suprise:19,20,23,24
        android.graphics.Point p1,p2;
        p1 = landmakrs.get(19);
        p2 = landmakrs.get(20);
        Log.d(TAG, "isAngry k1: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)<0.15){
            return false;
        }
        p1 = landmakrs.get(23);
        p2 = landmakrs.get(24);
        Log.d(TAG, "isAngry k2: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.15){
            return false;
        }
        return true;
    }

    boolean isCalm(ArrayList<android.graphics.Point> landmakrs){
        //suprise:61,62,63,67,66,65
        android.graphics.Point p1,p2,p3,p4,p5,p6;
        p1 = landmakrs.get(61);
        p2 = landmakrs.get(67);
        p3 = landmakrs.get(66);
        p4 = landmakrs.get(62);
        p5 = landmakrs.get(63);
        p6 = landmakrs.get(65);
        Log.d(TAG, "isCalm k1: "+(float)Math.abs(p2.y-p1.y)+" p1: "+p1.toString()+" p2: "+p2.toString());
        Log.d(TAG, "isCalm k2: "+(float)Math.abs(p4.y-p3.y)+" p3: "+p3.toString()+" p4: "+p4.toString());
        Log.d(TAG, "isCalm k3: "+(float)Math.abs(p6.y-p5.y)+" p5: "+p5.toString()+" p6: "+p6.toString());
        if( Math.abs(p2.y-p1.y)>15 || Math.abs(p3.y-p4.y)>15 || Math.abs(p5.y-p6.y)>15){
            return false;
        }
        return true;
    }

    boolean isSad(ArrayList<android.graphics.Point> landmakrs){
        //suprise:21,22,31,35
        android.graphics.Point p1,p2,p3,p4;
        p1 = landmakrs.get(21);
        p2 = landmakrs.get(22);
        p3 = landmakrs.get(31);
        p4 = landmakrs.get(35);
        Log.d(TAG, "isSad k1: "+(float)Math.abs(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        Log.d(TAG, "isSad k2: "+(float)Math.abs(p3.x-p4.x)+" p3: "+p3.toString()+" p4: "+p4.toString());
        if( (float)Math.abs(p2.x-p1.x)/Math.abs(p3.x-p4.x)>0.5){
            return false;
        }
        return true;
    }

    boolean isSmile(ArrayList<android.graphics.Point> landmakrs){
        //suprise:48,54,57:48,60,54,64
        android.graphics.Point p1,p2;
        p1 = landmakrs.get(48);
        p2 = landmakrs.get(57);
        Log.d(TAG, "isHappy k1: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)<0.15){
            return false;
        }
        p1 = landmakrs.get(57);
        p2 = landmakrs.get(54);
        Log.d(TAG, "isHappy k2: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.15){
            return false;
        }
        p1 = landmakrs.get(48);
        p2 = landmakrs.get(60);
        Log.d(TAG, "isHappy k3: "+Math.abs(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if(Math.abs(p2.y-p1.y)<5){
            return false;
        }
        p1 = landmakrs.get(54);
        p2 = landmakrs.get(64);
        Log.d(TAG, "isHappy k4: "+Math.abs(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if(Math.abs(p2.y-p1.y)<5){
            return false;
        }
        return true;
    }

    boolean isDisgust(ArrayList<android.graphics.Point> landmakrs){
        //suprise:48,50,52,54
        android.graphics.Point p1,p2;
        p1 = landmakrs.get(48);
        p2 = landmakrs.get(50);
        Log.d(TAG, "isDisgust k1: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)>-0.2){
            return false;
        }
        p1 = landmakrs.get(52);
        p2 = landmakrs.get(54);
        Log.d(TAG, "isDisgust k2: "+(float)(p2.y-p1.y)/(p2.x-p1.x)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)(p2.y-p1.y)/(p2.x-p1.x)<0.2){
            return false;
        }
        return true;
    }

    boolean isScared(ArrayList<android.graphics.Point> landmakrs){
        //suprise:20,44,46
        android.graphics.Point p1,p2,p3;
        p1 = landmakrs.get(24);
        p2 = landmakrs.get(44);
        p3 = landmakrs.get(46);
        Log.d(TAG, "isScared k1: "+(float)Math.abs(p2.y-p1.y)/Math.abs(p2.y-p3.y)+" p1: "+p1.toString()+" p2: "+p2.toString());
        if((float)Math.abs(p2.y-p1.y)/Math.abs(p2.y-p3.y)>3.5){
            return false;
        }
        return true;
    }

}
