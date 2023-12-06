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

import java.net.Inet4Address;
import java.net.InetAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * The class for allowing UIs like {@link WifiDialog} and {@link FdeWifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class EthernetConfigController implements TextWatcher,
        AdapterView.OnItemSelectedListener, OnCheckedChangeListener,
        TextView.OnEditorActionListener, View.OnKeyListener {
    private static final String TAG = "EthernetConfigController";

    private final FdeWifiConfigUiBase mConfigUi;
    private final View mView;
    private final Fde mFde;

    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    private ScrollView mDialogContainer;
    private Spinner mSecuritySpinner;

    private Spinner mIpSettingsSpinner;
    private TextView mIpAddressView;
    private TextView mGatewayView;
    private TextView mNetworkPrefixLengthView;
    private TextView mDns1View;
    private TextView mDns2View;

    private NetConfiguration mNetConfiguration = null;

    private Context mContext;
	ArrayList<String> mInterfacesInPosition = new ArrayList<>();
	int mInterfaceNamePosition;
	int mIPTypePosition;

    public EthernetConfigController(FdeWifiConfigUiBase parent, View view, Fde accessPoint) {
        mConfigUi = parent;

        mView = view;
        mFde = accessPoint;
        mContext = mConfigUi.getContext();
        initWifiConfigController(accessPoint);
    }
    private void initWifiConfigController(Fde accessPoint) {

        final Resources res = mContext.getResources();

        mDialogContainer = mView.findViewById(R.id.dialog_scrollview);
        mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
        mIpSettingsSpinner.setOnItemSelectedListener(this);

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
        return ipFieldsAreValid();
    }

    public NetConfiguration getConfig() {
        NetConfiguration config = new NetConfiguration();
		config.interfaceName = mInterfacesInPosition.get(mInterfaceNamePosition);
		config.ipType = (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ? STATIC_IP : DHCP;
        if (config.ipType == STATIC_IP) {
    		config.ipAddress = mIpAddressView.getText().toString();;
    		config.networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
    		config.gateway = mGatewayView.getText().toString();
    		config.dns1 = mDns1View.getText().toString();
    		config.dns2 = mDns2View.getText().toString();
        }
        return config;
    }

    private boolean ipFieldsAreValid() {
        if (mIpSettingsSpinner != null
                    && mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            mNetConfiguration = new NetConfiguration();
            int result = validateIpConfigFields(mNetConfiguration);
            if (result != 0) {
                return false;
            }
        }
        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    private int validateIpConfigFields(NetConfiguration staticIpConfiguration) {
        if (mIpAddressView == null) return 0;

        String ipAddr = mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) return R.string.wifi_ip_settings_invalid_ip_address;

        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null || inetAddr.equals(Inet4Address.ANY)) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
            staticIpConfiguration.ipAddress = mIpAddressView.getText().toString();
			staticIpConfiguration.networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            mNetworkPrefixLengthView.setText(mConfigUi.getContext().getString(
                    R.string.wifi_network_prefix_length_hint));
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        String gateway = mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length - 1] = 1;
                mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
            } catch (RuntimeException ee) {
            } catch (java.net.UnknownHostException u) {
            }
        } else {
            InetAddress gatewayAddr = getIPv4Address(gateway);
            if (gatewayAddr == null) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            if (gatewayAddr.isMulticastAddress()) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            staticIpConfiguration.gateway = mGatewayView.getText().toString();
        }

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            //If everything else is valid, provide hint as a default option
            mDns1View.setText(mConfigUi.getContext().getString(R.string.wifi_dns1_hint));
        } else {
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dns1 = mDns1View.getText().toString();
        }

        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dns2 = mDns2View.getText().toString();
        }
        return 0;
    }

    private void showIpConfigFields() {
        NetConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mFde != null && true) {
            config = mFde.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {				
                if (config.ipAddress != null) {
                    mIpAddressView.setText(config.ipAddress);
                    mNetworkPrefixLengthView.setText(Integer.toString(config.networkPrefixLength));
                }

                if (config.gateway != null) {
                    mGatewayView.setText(config.gateway);
                }

                if (config.dns1 != null) {
                    mDns1View.setText(config.dns1);
                }
                if (config.dns2 != null) {
                    mDns2View.setText(config.dns2);
                }
            }
        } else {
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }
		mIPTypePosition = (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ? STATIC_IP : DHCP;
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
    	} else if (parent == mIpSettingsSpinner) {
    		//showSecurityFields(/* refreshEapMethods */ false, /* refreshCertificates */ true);
    		
			android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
			+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
			+ " ,position: " + position);
			showIpConfigFields();
            enableSubmitIfAppropriate();
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
        spinnerAdapter.notifyDataSetChanged();

        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);

        showIpConfigFields();
		mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
    }
}
