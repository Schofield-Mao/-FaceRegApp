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
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.VisionDetRet;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;


public class MainActivity extends Activity implements View.OnTouchListener ,CvCameraViewListener2 {
    private static final String  TAG              = "MainActivity";
    private Mat                  mRgba;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private Mat                    mGray;
    private JavaDetector mFaceDetector;
    private JavaDetector mEyeDetector;
    private JavaDetector mNoseDetector;
    private JavaDetector mMouthDetector;
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private boolean isViewStart = true;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView imgView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mFaceDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_frontalface_default),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_frontalface_default.xml");

                    mEyeDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_eye),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_eye.xml");
                    mNoseDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_nose),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_nose.xml");
                    mMouthDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_mouth),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_mouth.xml");
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


//        final String targetPath = Constants.getFaceShapeModelPath();
//        if (!new File(targetPath).exists()) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(MainActivity.this, "Copy landmark model to " + targetPath, Toast.LENGTH_SHORT).show();
//                }
//            });
//            Log.d(TAG, "oncreate: copy: "+Constants.getFaceShapeModelPath());
            //FileStorageHelper.copyFilesFromRaw(this, R.raw.shape_predictor_68_face_landmarks, targetPath);
//        }
//        Log.d(TAG, "oncreate: copy done: "+Constants.getFaceShapeModelPath());
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

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if (mFaceDetector.getDetecotr() != null) {
            mFaceDetector.getDetecotr().detectMultiScale(mGray, faces, 1.1, 3, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
//        Rect[] facesArray = faces.toArray();
//        for (int i = 0; i < facesArray.length; i++) {
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//        }

        Point temp = new Point(1,1);
        Core.putText(mRgba,"happy: ", temp, FONT_HERSHEY_SIMPLEX, 3, FACE_RECT_COLOR);
        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Toast.makeText(this,"you touch me", 1).show();
        if(isViewStart){
            try{
                Mat img = mRgba;
                if(img.cols() > 0 && img.rows() > 0) {
                    processImage(img);
                    Bitmap bitmapTemp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(img, bitmapTemp);
                    imgView.setImageBitmap(bitmapTemp);
                    imgView.setVisibility(ImageView.VISIBLE);
                    mOpenCvCameraView.disableView();
                    mOpenCvCameraView.setVisibility(View.INVISIBLE);
                    isViewStart = !isViewStart;
                    Log.d(TAG, "ouTouch: stop on preview");
                }
            }catch (Exception ex){
                Log.e(TAG, "onTouch: "+ex.getMessage());
            }
        }else {
            mOpenCvCameraView.enableView();
            imgView.setVisibility(ImageView.INVISIBLE);
            mOpenCvCameraView.setVisibility(View.VISIBLE);
            isViewStart = !isViewStart;
            Log.d(TAG, "ouTouch: set on preview");
        }


        return true;
    }

    private Mat processImage(Mat img){
        int resizeRatio = 1;
        FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
        Bitmap bitmapTemp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmapTemp);

        long now = System.currentTimeMillis();
        List<VisionDetRet> results = faceDet.detect(bitmapTemp);
        Log.d(TAG, "processImage: cost: "+(System.currentTimeMillis()-now));
        for (final VisionDetRet ret : results) {
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "processImage: "+label);
            int rectLeft = ret.getLeft();
            int rectTop= ret.getTop();
            int rectRight = ret.getRight();
            int rectBottom = ret.getBottom();
            Point lt = new Point();
            Point br = new Point();
            lt.x = rectLeft;
            lt.y = rectTop;
            br.x = rectRight;
            br.y = rectBottom;
            Log.d(TAG, "processImage: lt: "+lt.toString()+" br: "+br.toString());
            ArrayList<android.graphics.Point> landmarks = ret.getFaceLandmarks();
            for (android.graphics.Point point : landmarks) {
                Point temp = new Point(point.x * resizeRatio, point.y * resizeRatio);
                //Log.d(TAG, "processImage: landmark: "+temp.toString());
                Core.circle(img, temp,3,FACE_RECT_COLOR, 6);
            }
        }
        return img;
    }
}
