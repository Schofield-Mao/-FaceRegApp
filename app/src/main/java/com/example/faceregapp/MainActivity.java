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

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
                    }
                }).start();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.face_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);

        imgView = (ImageView) findViewById(R.id.img_view);
        imgView.setOnTouchListener(this);
        mVibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
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
        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mVibrator.vibrate(10);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isViewStart && mFaceDet != null) {
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
                Core.putText(img, Integer.toString(i), temp, FONT_HERSHEY_SIMPLEX, 0.5, FACE_RECT_COLOR,2);
            }

            Point explocat = lt;
            double fontscale = Math.abs(lt.x-br.x)*0.006;
            int fontthikness = (int)Math.round(Math.abs(lt.x-br.x)*0.006);
            HandCraftedExpressionDetector expressionDetector = new HandCraftedExpressionDetector(landmarks);
            Core.putText(img, expressionDetector.expressionDetector()+"!", explocat, FONT_HERSHEY_SIMPLEX, fontscale, FONT_COLOR, fontthikness);
        }
        return img;
    }
}
