package com.android.settings.location;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SettingsBaseActivity;
import com.android.settings.utils.LogTools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.app.Activity;

public class SetGpsFragment extends InstrumentedFragment {

    private static final String TAG = "LocationSettings";
    private SetGpsController setGpsController;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fde_gps_settings, container, false);
        final Activity activity = getActivity();

        setGpsController = new SetGpsController(activity, rootView);

        activity.setTitle(getString(R.string.location_settings_title));
        if (activity instanceof SettingsBaseActivity) {
            ((SettingsBaseActivity) activity).showTitle(false);
        }
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
