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
    private File                   mCascadeFile;
    private JavaDetector mFaceDetector;
    private JavaDetector mEyeDetector;
    private JavaDetector mNoseDetector;
    private JavaDetector mLeftEarDetector;
    private JavaDetector mRightEarDetector;
    private JavaDetector mLeftEyeDetector;
    private JavaDetector mRightEyeDetector;
    private JavaDetector mMouthDetector;

    private String[]               mDetectorName;

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
                    mLeftEarDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_leftear),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_leftear.xml");
                    mRightEarDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_rightear),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_rightear.xml");
                    mLeftEyeDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_lefteye),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_lefteye.xml");
                    mRightEyeDetector = new JavaDetector(getResources().openRawResource(R.raw.haarcascade_mcs_righteye),
                            getDir("cascade", Context.MODE_PRIVATE),
                            "haarcascade_mcs_righteye.xml");
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
            mFaceDetector.getDetecotr().detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

            //Core.putText(mRgba,"hello world", facesArray[i].tl(), FONT_HERSHEY_SIMPLEX, 3, FACE_RECT_COLOR);
        }



//        MatOfRect eyes = new MatOfRect();
//        if (mEyeDetector.getDetecotr() != null) {
//            mEyeDetector.getDetecotr().detectMultiScale(mGray, eyes, 1.1, 2, 2,
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//        Rect[] eyesArray = eyes.toArray();
//        for (int i = 0; i < eyesArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (eyesArray[i].tl().x + eyesArray[i].br().x)/2;
//            temp.y = (eyesArray[i].tl().y + eyesArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }
//
//        MatOfRect nodes = new MatOfRect();
//        if (mNoseDetector.getDetecotr() != null){
//            mNoseDetector.getDetecotr().detectMultiScale(mGray, nodes, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] nodesArray = nodes.toArray();
//        for (int i = 0; i < nodesArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (nodesArray[i].tl().x + nodesArray[i].br().x)/2;
//            temp.y = (nodesArray[i].tl().y + nodesArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }
//
//        MatOfRect mouths = new MatOfRect();
//        if (mMouthDetector.getDetecotr() != null){
//            mMouthDetector.getDetecotr().detectMultiScale(mGray, mouths, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] mouthsArray = mouths.toArray();
//        for (int i = 0; i < mouthsArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (mouthsArray[i].tl().x + mouthsArray[i].br().x)/2;
//            temp.y = (mouthsArray[i].tl().y + mouthsArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }

//        MatOfRect leftEar = new MatOfRect();
//        if (mLeftEarDetector.getDetecotr() != null){
//            mNoseDetector.getDetecotr().detectMultiScale(mGray, leftEar, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] leftEarArray = leftEar.toArray();
//        for (int i = 0; i < leftEarArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (leftEarArray[i].tl().x + leftEarArray[i].br().x)/2;
//            temp.y = (leftEarArray[i].tl().y + leftEarArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }
//
//        MatOfRect rightEar = new MatOfRect();
//        if (mLeftEarDetector.getDetecotr() != null){
//            mNoseDetector.getDetecotr().detectMultiScale(mGray, rightEar, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] rightEarArray = rightEar.toArray();
//        for (int i = 0; i < leftEarArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (rightEarArray[i].tl().x + rightEarArray[i].br().x)/2;
//            temp.y = (rightEarArray[i].tl().y + rightEarArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }

//        MatOfRect leftEye = new MatOfRect();
//        if (mLeftEarDetector.getDetecotr() != null){
//            mNoseDetector.getDetecotr().detectMultiScale(mGray, leftEye, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] leftEyeArray = leftEye.toArray();
//        for (int i = 0; i < leftEyeArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (leftEyeArray[i].tl().x + leftEyeArray[i].br().x)/2;
//            temp.y = (leftEyeArray[i].tl().y + leftEyeArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }
//
//        MatOfRect rightEye = new MatOfRect();
//        if (mLeftEarDetector.getDetecotr() != null){
//            mNoseDetector.getDetecotr().detectMultiScale(mGray, rightEye, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//
//        Rect[] rightEyeArray = rightEye.toArray();
//        for (int i = 0; i < rightEyeArray.length; i++) {
//            Point temp = new Point();
//            temp.x = (rightEyeArray[i].tl().x + rightEyeArray[i].br().x)/2;
//            temp.y = (rightEyeArray[i].tl().y + rightEyeArray[i].br().y)/2;
//            Core.circle(mRgba,temp,15,FACE_RECT_COLOR, 15);
//        }



        return mRgba;
    }
}
