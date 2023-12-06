/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.ThreadUtils;
import lineageos.waydroid.Net;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * The class for allowing UIs like {@link WifiDialog} and {@link FdeWifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class ConnectWifiController implements TextWatcher,
        AdapterView.OnItemSelectedListener, OnCheckedChangeListener,
        TextView.OnEditorActionListener, View.OnKeyListener {
    private static final String TAG = "FdeWifiConfigController";

    private final FdeWifiConfigUiBase mConfigUi;
    private final View mView;
    private final Fde mFde;

    private ScrollView mDialogContainer;
    private Spinner mSecuritySpinner;
    private TextView mPasswdView;

    private NetConfiguration mNetConfiguration = null;

    private Context mContext;
	ArrayList<String> mInterfacesInPosition = new ArrayList<>();
	int mInterfaceNamePosition;
	int mIPTypePosition;

    public ConnectWifiController(FdeWifiConfigUiBase parent, View view, Fde accessPoint) {
        mConfigUi = parent;

        mView = view;
        mFde = accessPoint;
        mContext = mConfigUi.getContext();
        initWifiConfigController(accessPoint);
    }
    private void initWifiConfigController(Fde accessPoint) {

        final Resources res = mContext.getResources();

        mDialogContainer = mView.findViewById(R.id.dialog_scrollview);
		mPasswdView = (TextView) mView.findViewById(R.id.passwd);
		mPasswdView.addTextChangedListener(this);

        if (mFde == null) {
            configureInterfaceSpinner();
            mConfigUi.setSubmitButton(res.getString(R.string.wifi_save));
        }
        mConfigUi.setCancelButton(res.getString(R.string.wifi_cancel));
        if (mConfigUi.getSubmitButton() != null) {
            enableSubmitIfAppropriate();
        }

        // After done view show and hide, request focus from parent view
        mView.findViewById(R.id.l_wifidialog).requestFocus();
    }
	
    void hideSubmitButton() {
        Button submit = mConfigUi.getSubmitButton();
        if (submit == null) return;

        submit.setVisibility(View.GONE);
    }

    /* show submit button if password, ip and proxy settings are valid */
    void enableSubmitIfAppropriate() {
        Button submit = mConfigUi.getSubmitButton();
        if (submit == null) return;

        submit.setEnabled(isSubmittable());
    }


    boolean isSubmittable() {
        return passswdValid();
    }

    public NetConfiguration getConfig() {
        NetConfiguration config = new NetConfiguration();
		config.interfaceName = mInterfacesInPosition.get(mInterfaceNamePosition);
    	config.ipAddress = mPasswdView.getText().toString();
        return config;
    }

    private boolean passswdValid() {
        if (mSecuritySpinner != null) {
            mNetConfiguration = new NetConfiguration();
            int result = validatePasswd(mNetConfiguration);
            if (result != 0) {
                return false;
            }
        }
        return true;
    }
	
    private int validatePasswd(NetConfiguration staticIpConfiguration) {
        if (mSecuritySpinner == null) return 0;

        String passwd = mPasswdView.getText().toString();
        if (TextUtils.isEmpty(passwd)) return R.string.wifi_ip_settings_invalid_ip_address;
        return 0;
    }
    private void setVisibility(int id, int visibility) {
        final View v = mView.findViewById(id);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }
	
    @Override
    public void afterTextChanged(Editable s) {
        ThreadUtils.postOnMainThread(() -> {
            //showWarningMessagesIfAppropriate();
            
			android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
            enableSubmitIfAppropriate();
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // work done in afterTextChanged
        
		android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // work done in afterTextChanged
        
		android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    }

    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        //
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mSecuritySpinner) {
			
		android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
			+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
			+ " ,position: " + position);
		mInterfaceNamePosition = position;
    		//showSecurityFields(/* refreshEapMethods */ true, /* refreshCertificates */ true);
    	} else {
    		//showIpConfigFields();
            //enableSubmitIfAppropriate();
            
			android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
			+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
			+ " ,position: " + position);
    	}  
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        //
    }


    public Fde getAccessPoint() {
        return mFde;
    }

    private void configureInterfaceSpinner() {
        mConfigUi.setTitle(R.string.set_net_from_host_net_set);

        mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
        mSecuritySpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecuritySpinner.setAdapter(spinnerAdapter);
/*
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("ls", "/sys/class/net");
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				spinnerAdapter.add(line);
				mInterfacesInPosition.add(line);
			}

			int exitCode = process.waitFor();
			android.util.Log.e(TAG, "Exit code: " + exitCode);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		*/
		Net net = Net.getInstance(mContext);
        
        String str = net.getAllSsid();

		android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
			+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
			+ " ,AllSsid: " + str);

		
        spinnerAdapter.add("wifi1");
	    mInterfacesInPosition.add("wifi1");
		spinnerAdapter.add("wifi2");
	    mInterfacesInPosition.add("wifi2");
		spinnerAdapter.add("wifi3");
	    mInterfacesInPosition.add("wifi3");
		
        spinnerAdapter.notifyDataSetChanged();

        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
    }
}
