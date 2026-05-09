/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.settings.spa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.android.settings.spa.app.appinfo.AppInfoSettingsProvider
import com.android.settingslib.core.lifecycle.HideNonSystemOverlayMixin
import com.android.settingslib.spa.framework.BrowseActivity
import com.android.settingslib.spa.framework.common.SettingsPage
import com.android.settingslib.spa.framework.util.SESSION_BROWSE
import com.android.settingslib.spa.framework.util.appendSpaParams
import com.google.android.setupcompat.util.WizardManagerHelper
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.net.Uri;


class SpaActivity : BrowseActivity() {
    override fun isPageEnabled(page: SettingsPage) =
        super.isPageEnabled(page) && !isSuwAndPageBlocked(page.sppName)

    var  isTop = false ;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(HideNonSystemOverlayMixin(this))

        contentResolver.registerContentObserver(
                Settings.System.getUriFor("BACK_KEY_TIME"),
                true,
                object : ContentObserver(Handler()) {
                    override fun onChange(selfChange: Boolean, uri: Uri?) {
                        Log.w("bella", "SpaActivity onChange  $selfChange")
                        runOnUiThread {
                           if(isTop){
                               onBackPressedDispatcher.onBackPressed()
                           }
                        }
                    }
                }
        )
    }

    override fun onResume() {
        super.onResume()
        isTop = true;
        android.provider.Settings.System.putString(getContentResolver(), "sub_title", getTitle().toString());
    }

    override fun onPause() {
        super.onPause()
        isTop = false;
    }

    companion object {
        private const val TAG = "SpaActivity"

        /** The pages that blocked from SUW. */
        private val SuwBlockedPages = setOf(AppInfoSettingsProvider.name)

        @VisibleForTesting
        fun Context.isSuwAndPageBlocked(name: String): Boolean =
            if (name in SuwBlockedPages && !WizardManagerHelper.isDeviceProvisioned(this)) {
                Log.w(TAG, "$name blocked before SUW completed.")
                true
            } else {
                false
            }

        @JvmStatic
        fun Context.startSpaActivity(destination: String) {
            val intent = Intent(this, SpaActivity::class.java)
                .appendSpaParams(destination = destination)
                .appendSpaParams(sessionName = SESSION_BROWSE)
            startActivity(intent)
        }
    }
}
