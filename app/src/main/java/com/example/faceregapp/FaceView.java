package com.example.faceregapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

public class FaceView extends View {
    public Paint mPaint;
    private String mCorlor = "#42ed45";
    private ArrayList<RectF> mFace;

    public FaceView(Context context){
        super(context);
        init();
    }

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

    public void setFaces(ArrayList<RectF> faces) {  //设置人脸信息，然后刷新FaceView
        mFace = faces;
        invalidate();
    }

    ArrayList<RectF> transForm(ArrayList<Camera.Face> faces, int mCameraFacing, int orientation, int width, int height){
        Matrix matrix = new Matrix();
        boolean mirror = (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (mirror)
            matrix.setScale(-1,1);
        else
            matrix.setScale(1,1);
        matrix.postRotate(new Float(orientation));
        matrix.postScale(new Float(width/2000f),new Float(height/2000f));
        matrix.postTranslate(new Float(width/2f), new Float(height/2f));
        ArrayList recList = new ArrayList<RectF>();
        for (int i=0;i<faces.size();i++){
            RectF srcRec = new RectF(faces.get(i).rect);
            RectF desRec = new RectF(0f,0f,0f,0f);
            matrix.mapRect(desRec,srcRec);
            recList.add(desRec);
        }
        return recList;
    }

}
