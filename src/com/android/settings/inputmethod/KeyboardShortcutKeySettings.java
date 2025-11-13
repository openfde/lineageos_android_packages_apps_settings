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

package com.android.settings.inputmethod;


import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.util.FeatureFlagUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.util.Log;
import java.util.Locale;


// @SearchIndexable
public class KeyboardShortcutKeySettings extends DashboardFragment {

    private static final String TAG = "KeyboardSettingsShortcutKey";

    private static final String KEY_KEYBOARDS_CATEGORY = "keyboard_shortcuts_helper";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_KEYBOARDS_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
      
       List<ShortcutKey> list = parseKeyListData();

       PreferenceCategory testList = findPreference("keyList");
       for(ShortcutKey key : list) {
           Log.d(TAG, "Shortcut Key: " + key.getName() + ", Summary: " + key.getSummary());
            Preference preference = new Preference(getActivity());
            if (Locale.getDefault().getLanguage().equals("zh")) {
                preference.setTitle(key.getNameZh());
            }else {
                preference.setTitle(key.getName());
            }
            
            preference.setSummary( key.getSummary());
            preference.setLayoutResource(R.layout.preference_item_corners);
            testList.addPreference(preference);
       }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.keyboard_shortcut_key_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            @NonNull Context context, @Nullable Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        return controllers;
    }

/**
 * parse key list data from xml file
 */
    private List<ShortcutKey> parseKeyListData(){
        List<ShortcutKey> shortcutList = new ArrayList<>();
        InputStream  inputStream = null;
        try {
            inputStream = getResources().openRawResource(R.raw.shortcut_key_list);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            
            Element root = document.getDocumentElement();
            NodeList itemList = root.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    ShortcutKey shortcut = new ShortcutKey();

                    shortcut.setId(Long.valueOf(getNodeText(element, "id")));
                    shortcut.setName(getNodeText(element, "name"));
                    shortcut.setNameZh(getNodeText(element, "name_zh"));
                    shortcut.setSummary(getNodeText(element, "summary"));
                    shortcut.setDesc(getNodeText(element, "desc"));
                    shortcut.setType(getNodeText(element, "type"));
                    shortcutList.add(shortcut);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseKeyListData", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return shortcutList;
    }    

/**
 * get node text content by tag name
 */
    private String getNodeText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent().trim();
        }
        return "";
    }

}