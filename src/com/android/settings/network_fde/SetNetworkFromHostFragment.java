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
package com.android.settings.network_fde;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SettingsBaseActivity;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.app.Activity;
import android.view.View;

import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class SetNetworkFromHostFragment extends DashboardFragment {

    private static final String TAG = "SetNetworkFromHostFragment";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // final PreferenceScreen screen = getPreferenceScreen();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final Activity activity = getActivity();
        if (activity instanceof SettingsBaseActivity) {
            ((SettingsBaseActivity) activity).showTitle(false);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SET_NET_FROM_HOST;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.fde_network_settings;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.fde_network_settings;
            return Arrays.asList(sir);
        }
    };
}
