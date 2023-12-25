/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import lineageos.waydroid.Net;
import android.os.AsyncTask;
import android.widget.Toast;
import android.content.Context;
import com.android.settings.utils.LogTools;

public class SetEthernetFromHostFragment extends InstrumentedFragment implements FdeWifiConfigUiBase,
        View.OnClickListener {

    final static String WIFI_CONFIG_KEY = "fde_net_config_key";
    @VisibleForTesting
    final static int SUBMIT_BUTTON_ID = android.R.id.button1;
    @VisibleForTesting
    final static int CANCEL_BUTTON_ID = android.R.id.button2;

    private EthernetConfigController mUIController;
    private Button mSubmitBtn;
    private Button mCancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SET_NET_FROM_HOST;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fde_set_ethernet_view, container, false);

        final Button neutral = rootView.findViewById(android.R.id.button3);
        if (neutral != null) {
            neutral.setVisibility(View.GONE);
        }

        mSubmitBtn = rootView.findViewById(SUBMIT_BUTTON_ID);
        mCancelBtn = rootView.findViewById(CANCEL_BUTTON_ID);

        mSubmitBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

        mUIController = new EthernetConfigController(this, rootView, null);

        return rootView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case SUBMIT_BUTTON_ID:
                handleSubmitAction();
                break;
            case CANCEL_BUTTON_ID:
                handleCancelAction();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
/*
    @Override
    public FdeWifiConfigUiBase getController() {
        return mUIController;
    }
*/
    @Override
    public void dispatchSubmit() {
        handleSubmitAction();
    }

    @Override
    public void setTitle(int id) {
        getActivity().setTitle(id);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    @Override
    public void setSubmitButton(CharSequence text) {
        mSubmitBtn.setText(text);
    }

    @Override
    public void setCancelButton(CharSequence text) {
        mCancelBtn.setText(text);
    }

    @Override
    public Button getSubmitButton() {
        return mSubmitBtn;
    }

    @Override
    public Button getCancelButton() {
        return mCancelBtn;
    }

    @VisibleForTesting
    void handleSubmitAction() {
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        successfullyFinish(mUIController.getConfig());
    }
	public  class NetTask extends AsyncTask<Void, Void, Exception> {
	    private final NetConfiguration mConfig;
		private final Context mContext;
        public NetTask(Context context, NetConfiguration config) {
			mContext = context;
            mConfig = config;
        }
        
        @Override
        protected Exception doInBackground(Void... params) {
        try {
		    Net net = Net.getInstance(mContext);
        	if (mConfig.ipType == 0) {
        	    int status = net.setDHCP(mConfig.interfaceName);
                LogTools.i("setDHCP "+mConfig.interfaceName + " , status "+status)  ;  
        	} else {
                int status = net.setStaticIp(mConfig.interfaceName, mConfig.ipAddress, mConfig.networkPrefixLength, 
        			mConfig.gateway, mConfig.dns1, mConfig.dns2);
                LogTools.i("setStaticIp "+mConfig.toString() + " , status "+status)  ;      
        	}
           //mUIController.getIpConfig();
          return null;
        } catch (Exception e) {
          return e;
        }
	  }

	  @Override
	  protected void onPostExecute(Exception e) {
		  if (e == null) {
			  Toast.makeText(mContext, "net set success!!", Toast.LENGTH_SHORT).show();
		  } else {
			  android.util.Log.e("MYLOG", "Failed to set: " + mConfig, e);
			  Toast.makeText(mContext, "net set fail!!", Toast.LENGTH_SHORT).show();
		  }
	  }
	}

    private void successfullyFinish(NetConfiguration config) {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() 
		+ " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
		+ " ," + config);
	    new NetTask(getContext(), config).execute();
        //final Intent intent = new Intent();
        final Activity activity = getActivity();
        //intent.putExtra(WIFI_CONFIG_KEY, config);
        //activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @VisibleForTesting
    void handleCancelAction() {
    
	android.util.Log.e("MYLOG", "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
        final Activity activity = getActivity();
        //activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }
}
