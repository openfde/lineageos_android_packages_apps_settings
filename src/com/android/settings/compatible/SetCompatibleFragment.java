package com.android.settings.compatible;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.utils.LogTools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;

public class SetCompatibleFragment extends InstrumentedFragment {

    private SetCompatibleController setCompatibleController;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fde_compatible_all_list_view, container, false);

        String packageName = getIntent().getStringExtra("packageName");
        String appName = getIntent().getStringExtra("appName");
        setCompatibleController = new SetCompatibleController(getActivity(), packageName, appName, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SET_NET_FROM_HOST;
    }

    protected Intent getIntent() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getIntent();
    }

}
