/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
