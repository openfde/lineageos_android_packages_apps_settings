package com.android.settings.utils;

import android.util.Log;

public class LogTools {
    public static final String TAG = "BELLA";
    public static final boolean isDebug = true ;

    public  static  void d(String msg){
        if(!isDebug) return ;
        Log.d(TAG,msg);
    }

    public  static void i(String msg){
        if(!isDebug) return ;
        Log.i(TAG,msg);
    }

    public  static void w(String msg){
        if(!isDebug) return ;
        Log.w(TAG,msg);
    }

    public  static void e(String msg){
        if(!isDebug) return ;
        Log.e(TAG,msg);
    }

}
