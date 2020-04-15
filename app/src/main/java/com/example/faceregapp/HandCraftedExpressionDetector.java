package com.example.faceregapp;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

public class HandCraftedExpressionDetector {

    private String TAG = "HandCraftedExpressionDetector";
    private ArrayList<Point> landmakrs;

    public HandCraftedExpressionDetector(ArrayList<Point> landmakrs){
        this.landmakrs = landmakrs;
    }

    public boolean isSuprise(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isHappy(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isAngry(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isCalm(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isSad(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isSmile(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isDisgust(ArrayList<android.graphics.Point> landmakrs){
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

    public boolean isScared(ArrayList<android.graphics.Point> landmakrs){
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

    public String expressionDetector(){
        String res;
        if(isCalm(landmakrs)){
            if(isDisgust(landmakrs)){
                res = "disgust";
            }else if(isSad(landmakrs)){
                res = "sad";
            }else if(isSmile(landmakrs)){
                res = "smile";
            }else {
                res = "calm";
            }
        }else if(isSuprise(landmakrs)) {
            res = "suprise";
        }else if(isAngry(landmakrs)) {
            res = "angry";
        }else if(isScared(landmakrs)){
            res = "scared";
        }else if(isHappy(landmakrs)) {
            res = "happy";
        }else {
            res = "calm";
        }
        return res;
    }

}
