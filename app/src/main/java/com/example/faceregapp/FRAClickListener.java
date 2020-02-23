package com.example.faceregapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CancellationException;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

public class FRAClickListener implements View.OnClickListener {

    CameraPreview mCameraPreview;
    Camera.PictureCallback mPictureCallback;
    Context mContext;

    FRAClickListener(CameraPreview cameraPreview, Context context){
        mContext = context;
        mCameraPreview = cameraPreview;
        mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fos = null;
                String mFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator+
                        "DCIM"+File.separator + "FRG_"+ System.currentTimeMillis() +".png";
                File tempFile = new File(mFilePath);
                try {
                    fos = new FileOutputStream(tempFile);
                    fos.write(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //实现连续拍多张的效果
//                mCamera.startPreview();
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
    };
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.cameraBtn:
                showToast(mContext,"button clike");
                Camera.Parameters parameters = mCameraPreview.getmCamera().getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setPreviewSize(800, 400);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCameraPreview.getmCamera().autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.takePicture(null, null, mPictureCallback);
                        }
                    }
                });
                break;
            case R.id.fab:
                mCameraPreview.switchCamera();
                break;
            default:
                break;
        }
    }

    public static void showToast(Context context,String txt){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, txt, duration);
        toast.show();
    }

}
