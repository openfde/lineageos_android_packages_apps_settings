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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.settings.network_fde.api.NetApi;
import android.os.Handler;
import android.os.Message;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;
import android.widget.Toast;
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
    List<String> listNetName ;
    String curNetName ;

    public static final int GET_STATIC_NET = 1001 ;
    public static final int GET_ACTIVED_NET = 1002 ;
    public static final int GET_IP_CONFIG = 1003 ;


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

        new Thread(new GetActivedInterfaceThread()).start();
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

    private void setEditViewEnable (boolean isEnable) {
        mIpAddressView.setEnabled(isEnable);
        mGatewayView.setEnabled(isEnable);
        mNetworkPrefixLengthView.setEnabled(isEnable);
        mDns1View.setEnabled(isEnable);
        mDns2View.setEnabled(isEnable);
    }

    private void showIpConfigFields() {
        NetConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mFde != null && true) {
            config = mFde.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            // mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            setEditViewEnable(true);
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
            setEditViewEnable(false);
            // mView.findViewById(R.id.staticip).setVisibility(View.GONE);
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
            getIpConfig();    
    	} else {
    		//showIpConfigFields();
            //enableSubmitIfAppropriate();
            
			android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
			+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
			+ " ,position: " + position);
    	}  
    }

     /**
     * get static IP config
     */
    class GetStaticIpConfThread implements  Runnable{
        private String staticIpConf ;

        public GetStaticIpConfThread(String staticIpConf){
            this.staticIpConf = staticIpConf;
        }

        @Override
            public void run() {
                String info = NetApi.getStaticIpConf(mContext,staticIpConf);  
                LogTools.i("getStaticIpConf info "+info + " ,staticIpConf "+staticIpConf);  
                Message msg = new Message();
                msg.what = GET_STATIC_NET;
                msg.obj = info;
                handler.sendMessage(msg);
            }
    }


    /**
     *getActivedInterface
     */
    class GetActivedInterfaceThread implements  Runnable{

        // public GetActivedInterfaceThread(){
        // }

        @Override
            public void run() {
                String info = NetApi.getActivedInterface(mContext);  
                LogTools.i("getActivedInterface info "+info);  
                curNetName = info;
                new Thread(new GetIpConfigureThread(curNetName,1)).start();
                Message msg = new Message();
                msg.what = GET_ACTIVED_NET;
                msg.obj = info;
                handler.sendMessage(msg);
            }
    }

    /**
     * getIpConfigure
     */
    class GetIpConfigureThread implements  Runnable{
        private String staticIpConf ;
        private int isFirst ;

        public GetIpConfigureThread(String staticIpConf,int isFirst){
            this.staticIpConf = staticIpConf;
            this.isFirst = isFirst;
        }

        @Override
            public void run() {
                String info = NetApi.getIpConfigure(mContext,staticIpConf);  
                LogTools.i("getIpConfigure info "+info + " ,staticIpConf "+staticIpConf);  
                Message msg = new Message();
                msg.what = GET_IP_CONFIG;
                msg.obj = info;
                msg.arg1 = isFirst;
                handler.sendMessage(msg);
            }
    }

    public void getIpConfig(){
         if(mSecuritySpinner.getSelectedItem() !=null){
            new Thread(new GetIpConfigureThread(mSecuritySpinner.getSelectedItem().toString(),0)).start();
         }else{
            Toast.makeText(mContext,mContext.getString(R.string.fde_no_wifi_module),Toast.LENGTH_SHORT).show();
         }
    }


      Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                  switch(msg.what){
                    case GET_STATIC_NET:
                        if(msg.obj !=null){
                            try{
                                String info = msg.obj.toString();
                                String[] arrInfo = info.split("\n");
                                String strGateway = arrInfo[1].replace("ipv4.gateway:","");
                                mGatewayView.setText(strGateway);
                                String strDns = arrInfo[2].replace("ipv4.dns:","");
                                String[] arrDns = strDns.split(",");
                                mDns1View.setText(arrDns[0]);
                                mDns2View.setText(arrDns[1]);
                                String strIp = arrInfo[0].replace("ipv4.addresses:","");
                                String[] arrIp = strIp.split("/");
                                mIpAddressView.setText(arrIp[0]);
                                mNetworkPrefixLengthView.setText(arrIp[1]);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        break ;

                    case GET_ACTIVED_NET:
                        int pos = findListIndex(listNetName,curNetName);
                        if(pos == -1){
                             Toast.makeText(mContext,mContext.getString(R.string.radioInfo_data_disconnected),Toast.LENGTH_SHORT).show();
                        }else{
                            mSecuritySpinner.setSelection(pos);
                        }
                        break;   
                    case GET_IP_CONFIG:
                        if(msg.obj !=null){
                            try{
                                String info = msg.obj.toString();
                                String[] arrInfo = info.split("\n");
                                mGatewayView.setText(arrInfo[2]);
                                String[] arrIp = arrInfo[1].split("/");
                                mIpAddressView.setText(arrIp[0]);
                                mNetworkPrefixLengthView.setText(arrIp[1]);
   
                                mDns1View.setText(arrInfo[3]);
                                mDns2View.setText("");
                                if(arrInfo[3].contains("|")){
                                    String[] arrDns = arrInfo[3].split("\\|");
                                    mDns1View.setText(arrDns[0]);
                                    mDns2View.setText(arrDns[1]);
                                }else if(arrInfo[3].contains(",")){
                                    String[] arrDns = arrInfo[3].split(",");
                                    mDns1View.setText(arrDns[0]);
                                    mDns2View.setText(arrDns[1]);
                                }
                                int isFirst = msg.arg1 ;
                                if(isFirst == 1){
                                    if("auto".equals(arrInfo[0])){
                                      mIpSettingsSpinner.setSelection(0);
                                    }else{
                                      mIpSettingsSpinner.setSelection(1);
                                    }
                                }                             
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        break ;     

                    default:

                        break;

                }
            }

      };      


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        //
    }


    public Fde getAccessPoint() {
        return mFde;
    }

    private void configureInterfaceSpinner() {
        mConfigUi.setTitle(R.string.fde_ethernet);

        mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
        mSecuritySpinner.setOnItemSelectedListener(this);
        listNetName = new ArrayList<>();

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
                if(isEthernet(line)){
                    listNetName.add(line);
                    spinnerAdapter.add(line);
                    mInterfacesInPosition.add(line);
                }
			}

			int exitCode = process.waitFor();
			android.util.Log.e(TAG, "Exit code: " + exitCode);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
        spinnerAdapter.notifyDataSetChanged();

        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);

        showIpConfigFields();
		// mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
    }

    /**
     * is ethernet
     * @param param
     * @return
     */
    private  boolean isEthernet(String param) {
        String check = "en.+|usb\\d{1,10}";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(param);
        return matcher.matches();
    }

    private int findListIndex(List<String> list ,String content){
        if(list !=null && content != null && !"".equals(content)){
            for(int i=0;i< list.size();i++){
                if(content.equals(list.get(i))){
                     return i ;
                }
            }
        }
        return -1 ;
    }


}
