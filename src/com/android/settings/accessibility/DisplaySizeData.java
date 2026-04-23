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

package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.Resources;

import com.android.settingslib.display.DisplayDensityUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import android.util.Log;
import android.os.SystemProperties;
import com.android.settings.R;
/**
 * Data class for storing the configurations related to the display size.
 */
class DisplaySizeData extends PreviewSizeData<Integer> {
    private final DisplayDensityUtils mDensity;

    DisplaySizeData(Context context) {
        super(context);

        mDensity = new DisplayDensityUtils(getContext());
        int initialIndex = mDensity.getCurrentIndexForDefaultDisplay();
        final Resources resources = getContext().getResources();
        int fdeDpi = resources.getDisplayMetrics().densityDpi;
        if (initialIndex < 0) {
            // Failed to obtain default density, which means we failed to
            // connect to the window manager service. Just use the current
            // density and don't let the user change anything.
            final int densityDpi = resources.getDisplayMetrics().densityDpi;
            fdeDpi = SystemProperties.getInt("FDE_DPI_DEFAULT",
                densityDpi);
            setDefaultValue(fdeDpi);
            setInitialIndex(0);
            setValues(Collections.singletonList(fdeDpi));
        } else {
            fdeDpi = SystemProperties.getInt("FDE_DPI_DEFAULT",
                mDensity.getDefaultDensityForDefaultDisplay());

            setDefaultValue(mDensity.getDefaultDensityForDefaultDisplay());
//            int[] result = Arrays.stream(resources.getStringArray(R.array.display_size))
//            .mapToDouble(Double::parseDouble)
//            .map(d -> d * defaultDensityDpi)
//            .mapToInt(d -> (int) Math.round(d))
//            .toArray();
            if(initialIndex > 4){
                initialIndex = 2;
            }
            setInitialIndex(initialIndex);
            setValues(Arrays.stream(mDensity.getDefaultDisplayDensityValues()).boxed()
                    .collect(Collectors.toList()));
        }
    }

    @Override
    void commit(int currentProgress) {
        final int densityDpi = getValues().get(currentProgress);
//        if (densityDpi == getDefaultValue()) {
//            mDensity.clearForcedDisplayDensity();
//        } else {
            mDensity.setForcedDisplayDensity(currentProgress);
//        }
    }
}
