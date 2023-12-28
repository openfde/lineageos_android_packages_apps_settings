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
import android.os.Handler;
import android.os.Message;
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
import com.android.settings.network_fde.api.NetApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.settings.network_fde.adapter.FdeWifiAdapter;
import com.android.settings.network_fde.dialog.AddWlanDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.settings.network_fde.dialog.SelectWlanDialog;
import com.android.settings.network_fde.dialog.WifiInfoDialog;
import android.app.ProgressDialog;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import java.util.Collections;
import java.util.Comparator;

/**
 * The class for allowing UIs like {@link WifiDialog} and {@link FdeWifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class ConnectWifiController implements 
        // TextWatcher
        // ,AdapterView.OnItemSelectedListener
        // , OnCheckedChangeListener
        // ,TextView.OnEditorActionListener
        //  View.OnKeyListener,
         AdapterItem {
    // private static final String TAG = "FdeWifiConfigController";

    private final FdeWifiConfigUiBase mConfigUi;
    private final View mView;
    private final Fde mFde;


    // private ScrollView mDialogContainer;
    // private Spinner mSecuritySpinner;
    // private TextView mPasswdView;
    private RecyclerView recyclerView;
    private RelativeLayout layoutRefresh;
    private ImageView imgRefresh;
    private TextView switchWifi ;
    private TextView txtAddWifi ;

    // ArrayAdapter<String> spinnerAdapter;

    // private NetConfiguration mNetConfiguration = null;

    private Context mContext;
    ArrayList<String> mInterfacesInPosition = new ArrayList<>();
    // int mInterfaceNamePosition;
    // int mIPTypePosition;
    long lastSwitchTime = 0;
    int clickCount = 0;
    boolean isSwitchOpen = false ;

    FdeWifiAdapter fdeWifiAdapter ;
    List<Map<String,Object>> list ;
    List<Map<String,Object>> allSavelist ;
    ProgressDialog progressDialog ;

    private String isOpenWifi = "1" ;
    private String curWifiName = "";

    public static final int UPDATE_LIST = 1001 ;
    public static final int ENABLE_WIFI = 1002 ;
    public static final int QUERY_WIFI_STATUS = 1003 ;
    public static final int QUERY_WIFI_INFO = 1004 ;
    public static final int CONNECT_WIFI = 1005 ;
    public static final int GET_ALL_SAVED_LIST = 1006 ;
    public static final int GET_ACTIVED = 1007;
    public static final int FORGET_WIFI = 1008 ;
    

    public ConnectWifiController(FdeWifiConfigUiBase parent, View view, Fde accessPoint) {
        mConfigUi = parent;

        mView = view;
        mFde = accessPoint;
        mContext = mConfigUi.getContext();
        initWifiConfigController(accessPoint);
    }
    private void initWifiConfigController(Fde accessPoint) {

        final Resources res = mContext.getResources();

        // mDialogContainer = mView.findViewById(R.id.dialog_scrollview);
        // mPasswdView = (TextView) mView.findViewById(R.id.passwd);
        // mPasswdView.addTextChangedListener(this);

        // if (mFde == null) {
        //     configureInterfaceSpinner();
        //     mConfigUi.setSubmitButton(res.getString(R.string.wifi_save));
        // }
        // mConfigUi.setCancelButton(res.getString(R.string.wifi_cancel));
        // if (mConfigUi.getSubmitButton() != null) {
        //     enableSubmitIfAppropriate();
        // }

        // After done view show and hide, request focus from parent view
        mView.findViewById(R.id.dialog_scrollview).requestFocus();


        layoutRefresh = (RelativeLayout)mView.findViewById(R.id.layoutRefresh);
        recyclerView =(RecyclerView) mView.findViewById(R.id.recyclerView);
        switchWifi = (TextView)mView.findViewById(R.id.switchWifi);
        imgRefresh = (ImageView)mView.findViewById(R.id.imgRefresh);
        txtAddWifi = (TextView)mView.findViewById(R.id.txtAddWifi);
        list = new ArrayList<>();
        allSavelist = new ArrayList<>();
        fdeWifiAdapter = new FdeWifiAdapter(mContext,list,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

 

        recyclerView.setAdapter(fdeWifiAdapter);

    //    for(int i=0 ;i < 30;i++){
    //         Map<String,Object> mp = new HashMap<>();
    //         mp.put("name","wifi"+i);
    //         mp.put("isEncrypted", (i%5 !=0)?"加密":"开放");
    //         list.add(mp);
    //     }
    //     fdeWifiAdapter.notifyDataSetChanged();

        // txtAddWifi.setOnClickListener(this);
        // imgRefresh.setOnClickListener(this);

        new Thread(new QueryWifiStatus(0)).start();
      
        switchWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if(System.currentTimeMillis() - lastSwitchTime < 2 *1000){
                //     Toast.makeText(mContext,mContext.getString(R.string.fde_btn_operating_error),Toast.LENGTH_SHORT).show();
                //     return ;
                // }
                showProgressDialog(mContext.getString(R.string.fde_connecting));
                new Thread(new QueryWifiStatus(1)).start();
                
                lastSwitchTime = System.currentTimeMillis();
            }
        });

        // switchWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //     @Override
        //     public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //         // if(System.currentTimeMillis() - lastSwitchTime < 1 *1000 ){
        //         //     return ;
        //         // }
        //         if(b){
        //            new Thread(new OpenAndCloseWifiThread(1)).start();
        //            openWifiView();
        //         }else{
        //            new Thread(new OpenAndCloseWifiThread(0)).start();
        //            closeWifiView();
        //         }
                
        //     }
        // });

        txtAddWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddWlanDialog addWlanDialog = new AddWlanDialog(mContext,ConnectWifiController.this);
                if(addWlanDialog !=null && !addWlanDialog.isShowing()){
                    addWlanDialog.show();
                }
                // clickCount++;
                // if(clickCount % 3 == 1){
                //     new Thread(new GetAllSavedWifiThread()).start();
                // }else if(clickCount % 3 == 2){
                //     new Thread(new GetActivedWifiThread()).start();
                // }else{

                // }
            }
        });

        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanWifi();
            }
        });
    }

    void closeWifiView(){
        //如果关闭WiFi
        txtAddWifi.setVisibility(View.GONE);
        layoutRefresh.setVisibility(View.GONE);
        if(list !=null){
            list.clear();
            fdeWifiAdapter.notifyDataSetChanged();
        }    
        switchWifi.setBackgroundDrawable(mContext.getDrawable(R.mipmap.icon_switch_close));
    }

    void openWifiView(){
        //如果开启WiFi
        switchWifi.setBackgroundDrawable(mContext.getDrawable(R.mipmap.icon_switch_open));
        txtAddWifi.setVisibility(View.VISIBLE);
        layoutRefresh.setVisibility(View.VISIBLE);
    }

    // void hideSubmitButton() {
    //     Button submit = mConfigUi.getSubmitButton();
    //     if (submit == null) return;

    //     submit.setVisibility(View.GONE);
    // }

    /* show submit button if password, ip and proxy settings are valid */
    // void enableSubmitIfAppropriate() {
    //     Button submit = mConfigUi.getSubmitButton();
    //     if (submit == null) return;

    //     submit.setEnabled(isSubmittable());
    // }


    // boolean isSubmittable() {
    //     return passswdValid();
    // }

    // public NetConfiguration getConfig() {
    //     NetConfiguration config = new NetConfiguration();
    //     config.interfaceName = mInterfacesInPosition.get(mInterfaceNamePosition);
    //     config.ipAddress = mPasswdView.getText().toString();
    //     return config;
    // }

    // private boolean passswdValid() {
    //     if (mSecuritySpinner != null) {
    //         mNetConfiguration = new NetConfiguration();
    //         int result = validatePasswd(mNetConfiguration);
    //         if (result != 0) {
    //             return false;
    //         }
    //     }
    //     return true;
    // }

    // private int validatePasswd(NetConfiguration staticIpConfiguration) {
    //     if (mSecuritySpinner == null) return 0;

    //     String passwd = mPasswdView.getText().toString();
    //     if (TextUtils.isEmpty(passwd)) return R.string.wifi_ip_settings_invalid_ip_address;
    //     return 0;
    // }
    // private void setVisibility(int id, int visibility) {
    //     final View v = mView.findViewById(id);
    //     if (v != null) {
    //         v.setVisibility(visibility);
    //     }
    // }

    // @Override
    // public void afterTextChanged(Editable s) {
    //     ThreadUtils.postOnMainThread(() -> {
    //         //showWarningMessagesIfAppropriate();

    //         LogTools.i("file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    //         enableSubmitIfAppropriate();
    //     });
    // }

    // @Override
    // public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    //     // work done in afterTextChanged

    //     LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    // }

    // @Override
    // public void onTextChanged(CharSequence s, int start, int before, int count) {
    //     // work done in afterTextChanged

    //     LogTools.i("file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    // }

    // @Override
    // public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

    //     LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    //     return false;
    // }

    // @Override
    // public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

    //     LogTools.i("file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    //     return false;
    // }

    // @Override
    // public void onCheckedChanged(CompoundButton view, boolean isChecked) {

    //     LogTools.i("file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    //     //
    // }

    // @Override
    // public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    //     if (parent == mSecuritySpinner) {

    //         LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName()
    //                 + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
    //                 + " ,position: " + position);
    //         mInterfaceNamePosition = position;
    //         //showSecurityFields(/* refreshEapMethods */ true, /* refreshCertificates */ true);
    //     } else {
    //         //showIpConfigFields();
    //         //enableSubmitIfAppropriate();

    //         LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName()
    //                 + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
    //                 + " ,position: " + position);
    //     }
    // }

    // @Override
    // public void onNothingSelected(AdapterView<?> parent) {

    //    LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName() + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber());
    //     //
    // }


    public Fde getAccessPoint() {
        return mFde;
    }

//     private void configureInterfaceSpinner() {
//         mConfigUi.setTitle(R.string.fde_ethernet);

//         mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
//         mSecuritySpinner.setOnItemSelectedListener(this);

//         spinnerAdapter = new ArrayAdapter<String>(mContext,
//                 android.R.layout.simple_spinner_item, android.R.id.text1);
//         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//         mSecuritySpinner.setAdapter(spinnerAdapter);
// /*
// 		try {
// 			ProcessBuilder processBuilder = new ProcessBuilder("ls", "/sys/class/net");
// 			Process process = processBuilder.start();
// 			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
// 			String line;
// 			while ((line = reader.readLine()) != null) {
// 				spinnerAdapter.add(line);
// 				mInterfacesInPosition.add(line);
// 			}

// 			int exitCode = process.waitFor();
// 			android.util.Log.e(TAG, "Exit code: " + exitCode);

// 		} catch (IOException | InterruptedException e) {
// 			e.printStackTrace();
// 		}
// 		*/

//         // ScanWifi();


//         //		  spinnerAdapter.add("wifi1");
//         //		mInterfacesInPosition.add("wifi1");
//         //		spinnerAdapter.add("wifi2");
//         //		mInterfacesInPosition.add("wifi2");
//         //		spinnerAdapter.add("wifi3");
//         //		mInterfacesInPosition.add("wifi3");
//     }

      @Override
    public void onDialogClick(int type,String ssid,String password){
        showProgressDialog(mContext.getString(R.string.fde_connecting));
        if(type == 1){
            new Thread(new ConnectHideWifiThread(ssid,password)).start();
        }else if(type == 2){
            new Thread(new ConnectWifiByPasswordThread(ssid,password)).start();
        }else if(type == 3){
            new Thread(new ConnectWifiThread(ssid,0)).start();     
        }else if(type == 4){
            new Thread(new ForgetWifiThread(ssid )).start();
        }
    }

    @Override
    public void onContextClick(int pos,String content){
        //mouse Right click
        if(curWifiName.equals(content)){
            new Thread(new GetWifiInfo(content,"1")).start();
        }else{
             String isSaved = list.get(pos).get("isSaved").toString();
             if("1".equals(isSaved)){
                new Thread(new GetWifiInfo(content,"0")).start();
             }else{
                //not saved network
                SelectWlanDialog selectWlanDialog = new SelectWlanDialog(mContext,content,ConnectWifiController.this);
                if(selectWlanDialog !=null && !selectWlanDialog.isShowing()){
                    selectWlanDialog.show();
                }
             }  
        }
    }

    @Override
    public void onItemClick(int pos,String content){
        LogTools.i("onItemClick "+pos + " , content "+content);
        Map<String,Object> mp = list.get(pos);
        if(content.equals(curWifiName)){
            //if cur network
            new Thread(new GetWifiInfo(content,"1")).start();
        }else{
            String isSaved = mp.get("isSaved").toString();
            if("1".equals(isSaved)){
                showProgressDialog(mContext.getString(R.string.fde_connecting));
                new Thread(new ConnectWifiThread(content,1)).start();
            }else{
            //if not saved ,enter password
                SelectWlanDialog selectWlanDialog = new SelectWlanDialog(mContext,content,ConnectWifiController.this);
                if(selectWlanDialog !=null && !selectWlanDialog.isShowing()){
                    selectWlanDialog.show();
                }
            }
        }
        
    }


    private void ScanWifi(){
        if(!isSwitchOpen){
            return ;
        }
        showProgressDialog(mContext.getString(R.string.fde_scanning));
        new Thread(new SearchThread()).start();
    }

    class OpenAndCloseWifiThread implements  Runnable{
        private int enable;

        public OpenAndCloseWifiThread( int enable){
            this.enable = enable;
        }

        @Override
        public void run() {
            int status = NetApi.enableWifi(mContext,enable);
            LogTools.i("status "+status + ",enable "+enable);    
            lastSwitchTime = System.currentTimeMillis();
            sendMsgDelayed(ENABLE_WIFI,String.valueOf(status),String.valueOf(enable),null,3* 1000);
            
        }
    }

    class QueryWifiStatus implements  Runnable{
        int type ;

        public QueryWifiStatus(int type){
            this.type = type ;
        }

        @Override
            public void run() {
                int status = NetApi.isWifiEnable(mContext);
                LogTools.i("isWifiEnable  "+status);    
                Map<String,Object> mp = new HashMap<>();
                mp.put("status",status);
                mp.put("type",type);
                sendMsg(QUERY_WIFI_STATUS,mp);
            }
    }

   class GetWifiInfo implements  Runnable{
        String ssid;
        String status ;
        
        
        public GetWifiInfo (String ssid,String status){
            this.ssid = ssid;
            this.status = status;
        }

        @Override
            public void run() {
                String wifiInfo = NetApi.getSignalAndSecurity(mContext,ssid);
                LogTools.i("wifiInfo "+wifiInfo);    
                sendMsgDelayed(QUERY_WIFI_INFO,status,ssid,wifiInfo,50 );
            }
    }

    class  ConnectWifiThread implements  Runnable{
        String ssid;
        int connect ;
        
        public ConnectWifiThread (String ssid,int connect){
            this.ssid = ssid;
            this.connect = connect;
        }

        @Override
            public void run() {
                int status = NetApi.connectActivedWifi(mContext,ssid,connect);
                LogTools.i("connectActivedWifi status "+status);    
                sendMsg(CONNECT_WIFI,status);
            }
    }

    class ConnectWifiByPasswordThread implements Runnable{
        private String wifiName ;
        private String password ;

        public ConnectWifiByPasswordThread(String wifiName,String password ){
            this.wifiName = wifiName;
            this.password = password ;
        }

         @Override
        public void run() {
            int status = NetApi.connectSsid(mContext,wifiName,password);
            LogTools.i("connectSsid--wifiName "+wifiName +" , password: "+password + ",status "+status);
            new Thread(new GetActivedWifiThread()).start();
        }
   } 


    /**
     * add hide wifi
     */
    class ConnectHideWifiThread implements Runnable{
        private String wifiName ;
        private String password ;

        public ConnectHideWifiThread(String wifiName,String password ){
            this.wifiName = wifiName;
            this.password = password ;
        }

         @Override
        public void run() {
            LogTools.i("wifiName "+wifiName +" , password: "+password);
            int status = NetApi.connectHidedWifi(mContext,wifiName,password);
            LogTools.i("connectHidedWifi--wifiName "+wifiName +" , password: "+password + ",status "+status);
            new Thread(new GetActivedWifiThread()).start();
        }
   } 

    /**
     * get all saved wifi list
     */
    class GetAllSavedWifiThread implements  Runnable{

        @Override
            public void run() {
                String allSavelist = NetApi.connectedWifiList(mContext);  
                LogTools.i("connectedWifiList allSavelist "+allSavelist + "\n");  
                sendMsg(GET_ALL_SAVED_LIST,allSavelist);  
            }
    }

     /**
     * delete wifi password
     */
    class ForgetWifiThread implements  Runnable{
        private String wifiName ;

        public ForgetWifiThread(String wifiName){
            this.wifiName = wifiName;
        }

        @Override
            public void run() {
                int status = NetApi.forgetWifi(mContext,wifiName);  
                LogTools.i("forgetWifi status "+status + " ,wifiName "+wifiName);  
                new Thread(new SearchThread()).start();
                sendMsg(FORGET_WIFI,status);  
            }
    }

    /**
     * get actived wifi
     */
    class GetActivedWifiThread implements  Runnable{

        @Override
            public void run() {
                String curNet = NetApi.getActivedWifi(mContext);        
                LogTools.i("GetActivedWifiThread curNet "+curNet);    
                if(curNet == null || "".equals(curNet)){
                    curWifiName = "";
                }else{
                    curWifiName = curNet ;
                }
                sendMsg(GET_ACTIVED,curNet);
            }
    }

    /**
     * scan all wifi list
     */
    class SearchThread implements  Runnable{
        @Override
        public void run() {
            String str = NetApi.getAllSsid(mContext);
            LogTools.i( "file: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getFileName()
                    + " ,Line: " + ((StackTraceElement)(new Throwable().getStackTrace()[0])).getLineNumber()
                    + " ,AllSsid: " + str + "\n");
        
            sendMsg(UPDATE_LIST,str);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_LIST:
                    hideProgressDialog();
                    if(msg.obj == null){
                        msg.obj  = "";
                    }
                    String str = msg.obj.toString() ;
                    try {
                        if(str != null ){
                            if(list !=null){
                                list.clear();
                            }
                            String[] arrWifis	= str.split("\n");
                            for (String wi : arrWifis) {
                                // spinnerAdapter.add(wi);
                                // mInterfacesInPosition.add(wi);
                                  if(!wi.startsWith(":")){
                                      String[] arrInfo = wi.split(":");
                                      Map<String,Object> mp = new HashMap<>();
                                      mp.put("name",arrInfo[0]);
                                      mp.put("isEncrypted","");
                                      mp.put("isSaved","0");
                                      mp.put("signal",arrInfo[1]);
                                      mp.put("encryption",arrInfo[2]);
                                      mp.put("curNet",-1);
                                      list.add(mp);
                                    }  
                            }

                        }
                         Collections.sort(list, new Comparator<Map<String,Object>>() {
                            @Override
                            public int compare(Map<String,Object> o1, Map<String,Object> o2) {
                                return Long.compare(StringUtils.ToLong(o2.get("signal")), StringUtils.ToLong(o1.get("signal")));
                            }
                        });
                        fdeWifiAdapter.notifyDataSetChanged();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                     new Thread(new GetAllSavedWifiThread()).start();
                break;

                case QUERY_WIFI_STATUS:
                    if(msg.obj == null){
                        msg.obj  = new HashMap<>();
                    }
                    Map<String,Object> mpStatus = (Map<String,Object>) msg.obj;
                    isOpenWifi = StringUtils.ToString(mpStatus.get("status")) ;
                    int type = StringUtils.ToInt(mpStatus.get("type")) ;
                    if("2".equals(isOpenWifi)){
                            Toast.makeText(mContext, mContext.getString(R.string.fde_no_wifi_module), Toast.LENGTH_SHORT)
                            .show();
                            // switchWifi.setEnabled(false);
                            isSwitchOpen = false ; 
                            closeWifiView();
                    }else{
                        if(type == 0){
                            //if  first watch
                            if("1".equals(isOpenWifi)){
                                // switchWifi.setEnabled(true);
                                isSwitchOpen = true ; 
                                openWifiView();
                                ScanWifi();
                            }else if("0".equals(isOpenWifi)){
                                // switchWifi.setEnabled(true);
                                isSwitchOpen = false ; 
                                closeWifiView();
                            }
                        }else{
                            isSwitchOpen = !isSwitchOpen ;
                            if(isSwitchOpen){
                                new Thread(new OpenAndCloseWifiThread(1)).start();
                                openWifiView();
                            }else{
                                new Thread(new OpenAndCloseWifiThread(0)).start();
                                closeWifiView();
                            }
                        }
                    
                    }

                    // if(type == 0){
                    //   //if wifi enable
                    //     if("1".equals(isOpenWifi)){
                    //         switchWifi.setEnabled(true);
                    //         isSwitchOpen = true ; 
                    //         openWifiView();
                    //         ScanWifi();
                    //     }else if("0".equals(isOpenWifi)){
                    //         switchWifi.setEnabled(true);
                    //         isSwitchOpen = false ; 
                    //         closeWifiView();
                    //     }else {
                    //         Toast.makeText(mContext, mContext.getString(R.string.fde_no_wifi_module), Toast.LENGTH_SHORT)
                    //         .show();
                    //         switchWifi.setEnabled(false);
                    //         isSwitchOpen = false ; 
                    //         closeWifiView();
                    //     }
                    // }else{
                    //     isSwitchOpen = !isSwitchOpen ;
                    //     if(isSwitchOpen){
                    //         new Thread(new OpenAndCloseWifiThread(1)).start();
                    //         openWifiView();
                    //     }else{
                    //         new Thread(new OpenAndCloseWifiThread(0)).start();
                    //         closeWifiView();
                    //     }
                    // }
                  
                break;

                case ENABLE_WIFI:
                    Map<String,Object> mapEnable = (Map<String,Object> )msg.obj ; 
                    int enable =  StringUtils.ToInt(mapEnable.get("arg2")); 
                    int status = StringUtils.ToInt(mapEnable.get("arg1")); 
                    LogTools.i("------ENABLE_WIFI-----enable "+enable + ",status  "+status);
                    if(enable == 1 && status == 0){
                        //if wifi able,scan wifi list
                        ScanWifi();
                    }
                break;

                case GET_ALL_SAVED_LIST:
                    if(msg.obj == null){
                     msg.obj  = "";
                    }
                    String strSaved = msg.obj.toString() ;
                    try {
                        if(strSaved != null ){
                            if(allSavelist !=null){
                                allSavelist.clear();
                            }
                            String[] arrWifis	= strSaved.split("\n");
                            for (String wi : arrWifis) {
                                Map<String,Object> mp = new HashMap<>();
                                mp.put("name",wi);
                                allSavelist.add(mp);
                            }

                            for(int i =0 ; i < list.size();i++){
                               Map<String,Object> mpScan = list.get(i);
                               for(Map<String,Object> mm :allSavelist ){
                                    if(mpScan.get("name").toString().equals(mm.get("name"))){
                                        mpScan.put("isSaved","1");
                                    }
                               }
                               list.set(i,mpScan);
                            }
                            fdeWifiAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    new Thread(new GetActivedWifiThread()).start();
                    break;

                 case GET_ACTIVED:
                    hideProgressDialog();
                    if(msg.obj == null){
                     msg.obj  = "";
                    }
                    String strAc = msg.obj.toString() ;
                    for(int i= 0 ; i < list.size();i++ ){
                        Map<String,Object> mAc = list.get(i);
                        if(!"".equals(strAc) && strAc.equals(mAc.get("name").toString())){
                            mAc.put("curNet",i);
                            list.set(i,mAc);
                        }else{
                            mAc.put("curNet",-1);
                            list.set(i,mAc);
                        }
                        fdeWifiAdapter.notifyDataSetChanged();
                    }
                    break ;   

                case CONNECT_WIFI:
                    new Thread(new GetActivedWifiThread()).start();
                    break;    

                case QUERY_WIFI_INFO:
                    Map<String,Object> mapInfo = (Map<String,Object>)msg.obj ;
                    WifiInfoDialog wifiInfoDialog = new WifiInfoDialog(mContext,ConnectWifiController.this,mapInfo.get("arg1").toString(),mapInfo.get("arg2").toString(),mapInfo.get("obj").toString());
                    if(wifiInfoDialog !=null && !wifiInfoDialog.isShowing()){
                        wifiInfoDialog.show();
                    }
                    break;     
                case FORGET_WIFI:

                    break;    

            }
            // spinnerAdapter.notifyDataSetChanged();

                //    for(int i=0 ;i < 30;i++){
    //         Map<String,Object> mp = new HashMap<>();
    //         mp.put("name","wifi"+i);
    //         mp.put("isEncrypted", (i%5 !=0)?"加密":"开放");
    //         list.add(mp);
    //     }
    //     fdeWifiAdapter.notifyDataSetChanged();
            mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        }
    };

    /**
     *  send message 
     */
    private void sendMsg(int what ,Object obj){
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }


    /**
     * delayed send message 
     */
    private void sendMsgDelayed(int what ,String arg1,String arg2,Object obj,long time){
        LogTools.i("------sendMsgDelayed-----");
        Message msg = new Message();
        Map<String ,Object> mp = new HashMap<>();
        mp.put("arg1",arg1);
        mp.put("arg2",arg2);
        mp.put("obj",obj);
        msg.what = what;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.obj = mp;
        handler.sendMessageDelayed(msg,time);
    }

    private void showProgressDialog(String content){
        // progressDialog = ProgressDialog.show(mContext, "", content, true,true);
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360, // Start and end values for the rotation
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X rotation
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y rotation
        rotateAnimation.setDuration(2000); // Duration in milliseconds
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imgRefresh.startAnimation(rotateAnimation);
    }

    private void hideProgressDialog(){
        // progressDialog.dismiss();
        imgRefresh.clearAnimation();
    }
}
