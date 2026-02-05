/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.system;

import android.app.settings.SettingsEnums;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.util.Log;
import android.database.ContentObserver;
import android.net.Uri;
import static android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import com.android.settingslib.development.DevelopmentSettingsEnabler;

@SearchIndexable
public class SystemDashboardFragment extends DashboardFragment {

    private static final String TAG = "SystemDashboardFrag";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen screen = getPreferenceScreen();
        // We do not want to display an advanced button if only one setting is hidden
        if (getVisiblePreferenceCount(screen) == screen.getInitialExpandedChildrenCount() + 1) {
            screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
        }

         getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(DEVELOPMENT_SETTINGS_ENABLED), false, mDeveloperSettingsObserver);
         String developmentEnabledState = Settings.Global.getString(getContext().getContentResolver(), DEVELOPMENT_SETTINGS_ENABLED);
         Preference developerLine = findPreference("developer_line");
         developerLine.setVisible("1".equals(developmentEnabledState));   
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().getContentResolver().unregisterContentObserver(mDeveloperSettingsObserver);
    }


    private final Uri mDevelopEnabled = Settings.Global.getUriFor(DEVELOPMENT_SETTINGS_ENABLED);
    private final ContentObserver mDeveloperSettingsObserver = new ContentObserver(new Handler(
            Looper.getMainLooper())) {

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            try{
                 String developmentEnabledState = Settings.Global.getString(getContext().getContentResolver(), DEVELOPMENT_SETTINGS_ENABLED);
                 Preference developerLine = findPreference("developer_line");
                 developerLine.setVisible("1".equals(developmentEnabledState));
            }catch(Exception e){
                Log.e(TAG, " mDeveloperSettingsObserver e  "+e.toString() );
            }

        }
    };

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SYSTEM_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.system_dashboard_fragment;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }

    private int getVisiblePreferenceCount(PreferenceGroup group) {
        int visibleCount = 0;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            final Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                visibleCount += getVisiblePreferenceCount((PreferenceGroup) preference);
            } else if (preference.isVisible()) {
                visibleCount++;
            }
        }
        Log.w(TAG,"visibleCount "+visibleCount);
        return visibleCount;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.system_dashboard_fragment);
}
