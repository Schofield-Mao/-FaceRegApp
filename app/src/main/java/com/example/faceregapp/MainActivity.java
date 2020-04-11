package com.example.faceregapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.Toast;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;


public class MainActivity extends Activity implements CvCameraViewListener2 {
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

    private CameraBridgeViewBase mOpenCvCameraView;

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
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

            int RoiX = (int)Math.round(facesArray[i].tl().x);
            int RoiY = (int)Math.round(facesArray[i].tl().y);
            int RoiW = (int)Math.round(facesArray[i].br().x-facesArray[i].tl().x);
            int RoiH = (int)Math.round(facesArray[i].br().y-facesArray[i].tl().y);

            Rect rect = new Rect(RoiX, RoiY, RoiW, RoiH);
            Mat imgRectROI= new Mat(mGray, rect);

            //nodes
            MatOfRect nodes = new MatOfRect();
            double maxNodeSize = 0.5*RoiW;
            double minNodeSize = 0.3*RoiW;
            if (mNoseDetector.getDetecotr() != null){
                mNoseDetector.getDetecotr().detectMultiScale(imgRectROI, nodes,
                        1.1, 2, 2,
                        new Size(minNodeSize, minNodeSize), new Size(maxNodeSize,maxNodeSize));
            }

            Rect[] nodesArray = nodes.toArray();
            if(nodesArray.length != 0) {
                Point temp = new Point();
                temp.x = (nodesArray[0].tl().x + nodesArray[0].br().x) / 2 + RoiX;
                temp.y = (nodesArray[0].tl().y + nodesArray[0].br().y) / 2 + RoiY;
                Core.circle(mRgba, temp, 15, FACE_RECT_COLOR, 15);
            }

            MatOfRect eyes = new MatOfRect();
            double maxEyeSize = 0.5*RoiW;
            double minEyeSize = 0.3*RoiW;
            if (mEyeDetector.getDetecotr() != null) {
                mEyeDetector.getDetecotr().detectMultiScale(imgRectROI, eyes, 1.1, 2, 2,
                    new Size(minEyeSize, minEyeSize), new Size(maxEyeSize, maxEyeSize));
            }
            Rect[] eyesArray = eyes.toArray();
            for (int j = 0; j < eyesArray.length; j++) {
                Point temp = new Point();
                temp.x = (eyesArray[j].tl().x + eyesArray[j].br().x)/2+RoiX;
                temp.y = (eyesArray[j].tl().y + eyesArray[j].br().y)/2+RoiY;
                Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
            }

            MatOfRect mouths = new MatOfRect();
            double maxMouthSize = 0.6*RoiW;
            double minMouthSize = 0.1*RoiW;
            if (mMouthDetector.getDetecotr() != null){
                mMouthDetector.getDetecotr().detectMultiScale(imgRectROI, mouths, 1.1, 2, 2,
                        new Size(minMouthSize, minMouthSize), new Size(maxMouthSize ,maxMouthSize));
            }

            Rect[] mouthsArray = mouths.toArray();
            if(mouthsArray.length != 0) {
                Point temp = new Point();
                temp.x = (mouthsArray[0].tl().x + mouthsArray[0].br().x)/2+RoiX;
                temp.y = (mouthsArray[0].tl().y + mouthsArray[0].br().y)/2+RoiY;
                Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
            }


        }
            //Core.putText(mRgba,"hello world", facesArray[i].tl(), FONT_HERSHEY_SIMPLEX, 3, FACE_RECT_COLOR);
        return mRgba;
    }
}
