package com.example.faceregapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.FrameLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    FRAClickListener fraClickListener;
    FloatingActionButton btnCamera;
    FloatingActionButton fab;
    FrameLayout camera_preview;
    CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.CAMERA,android
                            .Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);//自定义的code
        }
    }

    public static void showToast(Context context,String txt){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, txt, duration);
        toast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || PackageManager.PERMISSION_GRANTED != grantResults[0]) {
            Toast.makeText(this,"你拒绝了权限，无法创建!",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,"smart!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPreview != null)
            mPreview.releaseCamera();
            mPreview = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    public void init(){
        Camera open = Camera.open();    //初始化 Camera对象
        mPreview = new CameraPreview(this, getApplicationContext(), open, getResources());
        camera_preview = findViewById(R.id.camera_preview);
        camera_preview.addView(mPreview);

        fraClickListener = new FRAClickListener(mPreview, this);
        btnCamera = findViewById(R.id.cameraBtn);
        btnCamera.setOnClickListener(fraClickListener);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(fraClickListener);
    }
}
