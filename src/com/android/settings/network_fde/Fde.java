/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.settings.network_fde.NetConfiguration;
import android.content.Context;

public class Fde {
    static final String TAG = "Fde";

	static final String KEY_INTERFACES = "key_interfaces";
	private NetConfiguration mConfig;

    public int interfaces = 0;

    private final Context mContext;

    public Fde(Context context) {
        mContext = context;
		interfaces = 10;
        mConfig = null;
    }


    public Fde(Context context, NetConfiguration config) {
        mContext = context;
        loadConfig(config);
    }

    void loadConfig(NetConfiguration config) {
        mConfig = config;
    }

    public NetConfiguration getConfig() {
        return mConfig;
    }
	public int getInterfaces() {
		return interfaces;
	}

    public void clearConfig() {
        mConfig = null;
    }
}
