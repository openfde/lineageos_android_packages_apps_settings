/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.settings;

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R;
import android.widget.RelativeLayout;
import android.view.View;
import android.util.Log;

class CustomDeviceInfoPreference(context: Context, val attrs: AttributeSet) :Preference(context,attrs) {
    private val arrayTop =
        arrayOf("hardware_info_device_model")
    private val arrayBottom = arrayOf( "build_number","hardware_info_device_serial")
    private val arrayRound = arrayOf("bottom")
    private var key: String = ""



    init{
        layoutResource = R.layout.preference_deviceinfo_corners
        key = attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "key").toString()
        Log.w("TwoTargetPreference","init key: "+key);
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val rootView = holder.findViewById(R.id.rootView)
        val viewLine = holder.findViewById(R.id.viewLine)
        rootView?.setBackgroundResource(R.drawable.card_square_white)
        try {
            if (attrs != null) {
                if (arrayTop.any { it == key }) {
                    rootView?.setBackgroundResource(R.drawable.card_round_top_white)
                } else if (arrayBottom.any { it == key }) {
                    rootView?.setBackgroundResource(R.drawable.card_round_bottom_white)  
                    viewLine?.visibility = View.GONE 
                } else if (arrayRound.any { it == key }) {
                    rootView?.setBackgroundResource(R.drawable.card_round_white)
                }
            }
        }
        catch(e: Exception) {
            e.printStackTrace()
        }

    }
}