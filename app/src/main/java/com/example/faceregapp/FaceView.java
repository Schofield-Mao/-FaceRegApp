package com.example.faceregapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

public class FaceView extends View {
    public Paint mPaint;
    private String mCorlor = "#42ed45";
    private ArrayList<Rect> mFace;

    public FaceView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        init();
    }

    public void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(mCorlor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1f, getContext().getResources().getDisplayMetrics()));
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mFace!=null){
            for(int i=0;i<mFace.size();i++){
                canvas.drawRect(mFace.get(i), mPaint);
            }
        }
    }

    public void setFaces(ArrayList<Rect> faces) {  //设置人脸信息，然后刷新FaceView
        mFace = faces;
        invalidate();
    }

//    ArrayList<Rect> transForm(ArrayList<Camera.Face> faces, int mCameraFacing, int orientation){
//        https://www.jianshu.com/p/3bb301c302e8
//    }


}
