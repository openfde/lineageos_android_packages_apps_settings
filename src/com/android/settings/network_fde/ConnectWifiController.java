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
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.android.settings.network_fde.adapter.FdeWifiAdapter;
import com.android.settings.network_fde.dialog.AddWlanDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.settings.network_fde.dialog.SelectWlanDialog;
import com.android.settings.network_fde.dialog.WifiInfoDialog;
import com.android.settings.network_fde.http.HttpRequestCallBack;
import com.android.settings.network_fde.http.NetCtrl;

import android.app.ProgressDialog;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;
import com.android.settings.wifi.WifiDialog;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import java.util.Collections;
import java.util.Comparator;

/**
 * The class for allowing UIs like {@link WifiDialog} and
 * {@link FdeWifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class ConnectWifiController implements
        // TextWatcher
        // ,AdapterView.OnItemSelectedListener
        // , OnCheckedChangeListener
        // ,TextView.OnEditorActionListener
        // View.OnKeyListener,
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
    private Switch switchWifi;
    private TextView txtAddWifi;

    // ArrayAdapter<String> spinnerAdapter;

    // private NetConfiguration mNetConfiguration = null;

    private Context mContext;
    ArrayList<String> mInterfacesInPosition = new ArrayList<>();
    // int mInterfaceNamePosition;
    // int mIPTypePosition;

    FdeWifiAdapter fdeWifiAdapter;
    List<Map<String, Object>> list;
    List<Map<String, Object>> allSavelist;
    ProgressDialog progressDialog;

    private int wifiStatus = 1;// 0-close ,1-open,3-disable
    private String curWifiName = "";
    boolean isScaning = false;
    long lastSwitchTime = 0;

    Timer timer;
    TimerTask timerTask;
    Timer timerLong;
    TimerTask timerTaskLong;

    // public static final int UPDATE_LIST = 1001;
    // public static final int ENABLE_WIFI = 1002;
    // public static final int QUERY_WIFI_STATUS = 1003;
    // public static final int QUERY_WIFI_INFO = 1004;
    // public static final int CONNECT_WIFI = 1005;
    // public static final int GET_ALL_SAVED_LIST = 1006;
    // public static final int GET_ACTIVED = 1007;
    // public static final int FORGET_WIFI = 1008;

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
        // configureInterfaceSpinner();
        // mConfigUi.setSubmitButton(res.getString(R.string.wifi_save));
        // }
        // mConfigUi.setCancelButton(res.getString(R.string.wifi_cancel));
        // if (mConfigUi.getSubmitButton() != null) {
        // enableSubmitIfAppropriate();
        // }

        // After done view show and hide, request focus from parent view
        mView.findViewById(R.id.dialog_scrollview).requestFocus();

        layoutRefresh = (RelativeLayout) mView.findViewById(R.id.layoutRefresh);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        switchWifi = (Switch) mView.findViewById(R.id.switchWifi);
        imgRefresh = (ImageView) mView.findViewById(R.id.imgRefresh);
        txtAddWifi = (TextView) mView.findViewById(R.id.txtAddWifi);
        list = new ArrayList<>();
        allSavelist = new ArrayList<>();
        fdeWifiAdapter = new FdeWifiAdapter(mContext, list, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(fdeWifiAdapter);

        // for(int i=0 ;i < 30;i++){
        // Map<String,Object> mp = new HashMap<>();
        // mp.put("name","wifi"+i);
        // mp.put("isEncrypted", (i%5 !=0)?"加密":"开放");
        // list.add(mp);
        // }
        // fdeWifiAdapter.notifyDataSetChanged();

        // txtAddWifi.setOnClickListener(this);
        // imgRefresh.setOnClickListener(this);

        // new Thread(new QueryWifiStatus(0)).start();
        openWifiView();
        isWifiEnable();

        switchWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    openWifiView();
                    enableWifi(1);
                    wifiStatus = 1;
                } else {
                    // if (list != null) {
                    // list.clear();
                    // }
                    enableWifi(0);
                    closeWifiView();
                    wifiStatus = 0;
                }

            }
        });

        // switchWifi.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        // // if(System.currentTimeMillis() - lastSwitchTime < 2 *1000){
        // // //
        // // Toast.makeText(mContext,
        // mContext.getString(R.string.fde_btn_operating_error), Toast.LENGTH_SHORT)
        // // .show();
        // // return ;
        // // }

        // if (wifiStatus == 0) {
        // wifiStatus = 1;
        // openWifiView();
        // enableWifi(1);
        // } else if (wifiStatus == 1) {
        // wifiStatus = 0;
        // enableWifi(0);
        // closeWifiView();
        // } else {
        // closeWifiView();
        // }

        // lastSwitchTime = System.currentTimeMillis();
        // }
        // });

        txtAddWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddWlanDialog addWlanDialog = new AddWlanDialog(mContext, ConnectWifiController.this);
                if (addWlanDialog != null && !addWlanDialog.isShowing()) {
                    addWlanDialog.show();
                }
            }
        });

        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (2 == wifiStatus) {
                    Toast.makeText(mContext, mContext.getString(R.string.fde_no_wifi_module), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    getAllSsid();
                }
            }
        });
    }

    void closeWifiView() {
        // 如果关闭WiFi
        txtAddWifi.setVisibility(View.GONE);
        layoutRefresh.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        // switchWifi.setBackgroundDrawable(mContext.getDrawable(R.mipmap.icon_switch_close));
    }

    void openWifiView() {
        // 如果开启WiFi
        // switchWifi.setBackgroundDrawable(mContext.getDrawable(R.mipmap.icon_switch_open));
        txtAddWifi.setVisibility(View.VISIBLE);
        layoutRefresh.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // LogTools.i("-------TimerTask run------------ wifiStatus " + wifiStatus);
                if (wifiStatus == 1) {
                    if (list == null || list.size() == 0) {
                        getAllSsid();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1 * 1000, 2 * 1000);
    }

    public void destTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        isScaning = false;
    }

    public void initTimerLong() {
        timerLong = new Timer();
        timerTaskLong = new TimerTask() {
            @Override
            public void run() {
                // LogTools.i("-------TimerTask run------------ wifiStatus " + wifiStatus);
                isWifiEnable();
            }
        };
        timerLong.schedule(timerTaskLong, 3 * 1000, 30 * 1000);
    }

    public void destTimerLong() {
        if (timerLong != null) {
            timerLong.cancel();
            timerLong = null;
        }
        if (timerTaskLong != null) {
            timerTaskLong.cancel();
            timerTaskLong = null;
        }
        isScaning = false;
    }

    public Fde getAccessPoint() {
        return mFde;
    }

    @Override
    public void onDialogClick(int type, String ssid, String password, int pos) {
        if (type == 1) {
            // new Thread(new ConnectHideWifiThread(ssid, password)).start();
            connectHidedWifi(ssid, password);
        } else if (type == 2) {
            // new Thread(new ConnectWifiByPasswordThread(ssid, password)).start();
            setConntectingShow(pos);
            connectSsid(ssid, password);
        } else if (type == 3) {
            // new Thread(new ConnectWifiThread(ssid, 0)).start();
            connectActivedWifi(ssid, 0);
        } else if (type == 4) {
            // new Thread(new ForgetWifiThread(ssid)).start();
            forgetWifi(ssid);
        }
    }

    @Override
    public void onContextClick(int pos, String content) {
        // mouse Right click
        Map<String, Object> mp = list.get(pos);
        if (curWifiName.equals(content)) {
            // new Thread(new GetWifiInfo(content, "1")).start();
            WifiInfoDialog wifiInfoDialog = new WifiInfoDialog(mContext, ConnectWifiController.this,
                    "1", content,
                    mp);
            if (wifiInfoDialog != null && !wifiInfoDialog.isShowing()) {
                wifiInfoDialog.show();
            }
        } else {
            String isSaved = list.get(pos).get("isSaved").toString();
            if ("1".equals(isSaved)) {
                WifiInfoDialog wifiInfoDialog = new WifiInfoDialog(mContext, ConnectWifiController.this,
                        "0", content,
                        mp);
                if (wifiInfoDialog != null && !wifiInfoDialog.isShowing()) {
                    wifiInfoDialog.show();
                }
            } else {
                // not saved network
                SelectWlanDialog selectWlanDialog = new SelectWlanDialog(mContext, content, pos,
                        ConnectWifiController.this);
                if (selectWlanDialog != null && !selectWlanDialog.isShowing()) {
                    selectWlanDialog.show();
                }
            }
        }
    }

    @Override
    public void onItemClick(int pos, String content) {
        LogTools.i("onItemClick " + pos + " , content " + content + ",curWifiName " + curWifiName);
        Map<String, Object> mp = list.get(pos);
        if (content.equals(curWifiName)) {
            LogTools.i("onItemClick  mp " + mp.toString());
            WifiInfoDialog wifiInfoDialog = new WifiInfoDialog(mContext, ConnectWifiController.this,
                    "1", content,
                    mp);
            if (wifiInfoDialog != null && !wifiInfoDialog.isShowing()) {
                wifiInfoDialog.show();
            }
        } else {
            String isSaved = mp.get("isSaved").toString();
            LogTools.i("onItemClick  isSaved " + isSaved);

            if ("1".equals(isSaved)) {
                // new Thread(new ConnectWifiThread(content, 1)).start();
                setConntectingShow(pos);
                connectActivedWifi(content, 1);
            } else {
                // if not saved ,enter password
                SelectWlanDialog selectWlanDialog = new SelectWlanDialog(mContext, content, pos,
                        ConnectWifiController.this);
                if (selectWlanDialog != null && !selectWlanDialog.isShowing()) {
                    selectWlanDialog.show();
                }
            }
        }

    }

    // private void cleanSaveStatus() {
    // try {
    // for (int i = 0; i < list.size(); i++) {
    // Map<String, Object> mp = list.get(i);
    // mp.put("isSaved", "1");
    // list.set(i, mp);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    private void setConntectingShow(int pos) {
        try {
            Map<String, Object> mp = list.get(pos);
            mp.put("isSaved", "2");
            list.set(pos, mp);
            fdeWifiAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * wifi is enable
     * 
     * @param type
     */
    private void isWifiEnable() {
        NetCtrl.get(mContext, "isWifiEnable", null, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: isWifiEnable result-- >" + result);
                wifiStatus = StringUtils.ToInt(result);
                if (wifiStatus == 1) {
                    switchWifi.setChecked(true);
                    openWifiView();
                    getAllSsid();
                } else {
                    closeWifiView();
                    switchWifi.setChecked(false);
                }
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:isWifiEnable  errorString-- >" + errorString + " ,code " + code);
                wifiStatus = 2;
                closeWifiView();
                switchWifi.setChecked(false);
            }
        });
    }

    /**
     * 
     * @param enable
     */
    private void enableWifi(int enable) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("enable", enable);
        NetCtrl.get(mContext, "enableWifi", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: enableWifi result-- >" + result + " ,enable  " + enable);
                if (enable == 1) {
                    openWifiView();
                    getAllSsid();
                } else {
                    isScaning = false;
                    closeWifiView();
                }
            }

            @Override
            public void requestFail(String errorString, int code) {
                isScaning = false;
                LogTools.i("requestFail:enableWifi  errorString-- >" + errorString + " ,code " + code);
            }
        });
    }

    // private void getSignalAndSecurity(String ssid) {
    // Map<String, Object> mp = new HashMap<>();
    // mp.put("ssid", ssid);
    // NetCtrl.get(mContext, "getSignalAndSecurity", mp, new HttpRequestCallBack() {
    // @Override
    // public void callBackListener(String result) {
    // LogTools.i("callBackListener: getSignalAndSecurity result-- >" + result);

    // }

    // @Override
    // public void requestFail(String errorString, int code) {
    // LogTools.i("requestFail:getSignalAndSecurity errorString-- >" + errorString +
    // " ,code " + code);
    // }
    // });
    // }

    private void connectActivedWifi(String ssid, int connect) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("ssid", ssid);
        mp.put("connect", connect);
        NetCtrl.get(mContext, "connectActivedWifi", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: connectActivedWifi result-- >" + result);
                // getActivedWifi();
                connectedWifiList();
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:connectActivedWifi  errorString-- >" + errorString + " ,code " + code);
            }
        });
    }

    private void connectSsid(String ssid, String password) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("ssid", ssid);
        mp.put("password", password);
        NetCtrl.get(mContext, "connectSsid", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: connectSsid result-- >" + result);
                connectedWifiList();
                // getActivedWifi();
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:connectSsid  errorString-- >" + errorString + " ,code " + code);
            }
        });
    }

    private void connectHidedWifi(String ssid, String password) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("ssid", ssid);
        mp.put("password", password);
        NetCtrl.get(mContext, "connectHidedWifi", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: connectHidedWifi result-- >" + result);
                getActivedWifi();
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:connectHidedWifi  errorString-- >" + errorString + " ,code " + code);
            }
        });
    }

    private void connectedWifiList() {
        Map<String, Object> mp = new HashMap<>();
        NetCtrl.get(mContext, "connectedWifiList", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: connectedWifiList result-- >" + result);
                String strSaved = result;
                try {
                    if (strSaved != null) {
                        if (allSavelist != null) {
                            allSavelist.clear();
                        }
                        String[] arrWifis = strSaved.split("\n");
                        for (String wi : arrWifis) {
                            Map<String, Object> mp = new HashMap<>();
                            mp.put("name", wi);
                            allSavelist.add(mp);
                        }

                        for (int i = 0; i < list.size(); i++) {
                            Map<String, Object> mpScan = list.get(i);
                            for (Map<String, Object> mm : allSavelist) {
                                if (mpScan.get("name").toString().equals(mm.get("name"))) {
                                    mpScan.put("isSaved", "1");
                                }
                            }
                            list.set(i, mpScan);
                        }
                        getActivedWifi();
                    } else {
                        isScaning = false;
                        fdeWifiAdapter.notifyDataSetChanged();
                        hideProgressDialog();
                    }
                } catch (Exception e) {
                    isScaning = false;
                    fdeWifiAdapter.notifyDataSetChanged();
                    hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:connectedWifiList  errorString-- >" + errorString + " ,code " + code);
                isScaning = false;
                fdeWifiAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getActivedWifi() {
        Map<String, Object> mp = new HashMap<>();
        NetCtrl.get(mContext, "getActivedWifi", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener: getActivedWifi result-- >" + result);
                String strAc = result;
                if (strAc == null || "".equals(strAc)) {
                    curWifiName = "";
                } else {
                    curWifiName = strAc;
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> mAc = list.get(i);
                        if (!"".equals(strAc) && strAc.equals(mAc.get("name").toString())) {
                            mAc.put("curNet", i);
                            list.set(i, mAc);
                        } else {
                            mAc.put("curNet", -1);
                            list.set(i, mAc);
                        }
                    }
                }
                fdeWifiAdapter.notifyDataSetChanged();
                hideProgressDialog();
                isScaning = false;
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail:getActivedWifi  errorString-- >" + errorString + " ,code " + code);
                curWifiName = "";
                isScaning = false;
                fdeWifiAdapter.notifyDataSetChanged();
                hideProgressDialog();
            }
        });
    }

    private void getAllSsid() {
        if (isScaning) {
            LogTools.i("getAllSsid  isScaning");
            return;
        }
        isScaning = true;
        showProgressDialog();
        NetCtrl.get(mContext, "getAllSsid", null, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener:getAllSsid  result-- >" + result);
                String str = result;
                try {
                    if (str != null) {
                        String[] arrWifis = str.split("\n");
                        if (arrWifis != null && arrWifis.length > 0) {
                            if (list != null) {
                                list.clear();
                            }
                        }
                        for (String wi : arrWifis) {
                            // spinnerAdapter.add(wi);
                            // mInterfacesInPosition.add(wi);
                            if (!wi.startsWith(":")) {
                                String[] arrInfo = wi.split(":");
                                Map<String, Object> mp = new HashMap<>();
                                mp.put("name", arrInfo[0]);
                                mp.put("isEncrypted", "");
                                mp.put("isSaved", "0");
                                mp.put("signal", arrInfo[1]);
                                mp.put("encryption", arrInfo[2]);
                                mp.put("curNet", -1);
                                list.add(mp);
                            }
                        }

                    } else {
                        fdeWifiAdapter.notifyDataSetChanged();
                        hideProgressDialog();
                        isScaning = false;
                    }
                    Collections.sort(list, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            return Long.compare(StringUtils.ToLong(o2.get("signal")),
                                    StringUtils.ToLong(o1.get("signal")));
                        }
                    });
                    // fdeWifiAdapter.notifyDataSetChanged();
                    connectedWifiList();
                } catch (Exception e) {
                    e.printStackTrace();
                    fdeWifiAdapter.notifyDataSetChanged();
                    hideProgressDialog();
                    isScaning = false;
                }

            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail: getAllSsid errorString-- >" + errorString + " ,code " + code);
                hideProgressDialog();
                fdeWifiAdapter.notifyDataSetChanged();
                isScaning = false;
            }
        });
    }

    private void forgetWifi(String ssid) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("ssid", ssid);
        NetCtrl.get(mContext, "forgetWifi", mp, new HttpRequestCallBack() {
            @Override
            public void callBackListener(String result) {
                LogTools.i("callBackListener:forgetWifi  result-- >" + result);
                connectedWifiList();
            }

            @Override
            public void requestFail(String errorString, int code) {
                LogTools.i("requestFail: forgetWifi errorString-- >" + errorString + " ,code " + code);
            }
        });
    }

    private void showProgressDialog() {
        // progressDialog = ProgressDialog.show(mContext, "", content, true,true);
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360, // Start and end values for the rotation
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X rotation
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y rotation
        rotateAnimation.setDuration(2000); // Duration in milliseconds
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imgRefresh.startAnimation(rotateAnimation);
    }

    private void hideProgressDialog() {
        // progressDialog.dismiss();
        imgRefresh.clearAnimation();
    }
}
