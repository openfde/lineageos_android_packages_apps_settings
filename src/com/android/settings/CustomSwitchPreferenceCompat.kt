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
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.android.settings.R;
import android.util.Log;

class CustomSwitchPreferenceCompat: SwitchPreferenceCompat {
    constructor(context: Context, attrs: AttributeSet,defStyleAttr :Int) : super(context,attrs,defStyleAttr){
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet,defStyleAttr :Int,defStyleRes :Int) : super(context,attrs,defStyleAttr,defStyleRes){
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs){
        init(context, attrs)
    }
    constructor(context: Context) : super(context){
        init(context, null)
    }

    private var key: String = ""
    private val arrayTop =
        arrayOf("show_virtual_keyboard_switch","accessibility_sticky_keys","trackpad_tap_to_click","auto_24hour")
    private val arrayBottom = arrayOf( "accessibility_bounce_keys","24 hour")
    private val arrayRound = arrayOf("")

    private fun init(context: Context, attrs: AttributeSet?)  {
        layoutResource = R.layout.preference_switch_corners
        try {
            key = attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "key").toString()
            Log.w("TwoTargetPreference","init key: "+key);
            if (arrayTop.any { it == key }) {
                layoutResource = R.layout.preference_switch_top_corners
            } else if (arrayBottom.any { it == key }) {
                layoutResource = R.layout.preference_switch_bottom_corners
            } else if (arrayRound.any { it == key }) {
                layoutResource = R.layout.preference_switch_card_corners
            }
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
        
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
    }
}