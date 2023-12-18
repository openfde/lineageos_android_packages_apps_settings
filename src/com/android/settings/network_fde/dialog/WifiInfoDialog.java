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
 *  add dialog add wifi
 */
package com.android.settings.network_fde.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.android.settings.network_fde.AdapterItem;
import com.android.settings.R;

public class WifiInfoDialog extends Dialog implements View.OnClickListener {
    TextView txtCancel ;
    TextView txtConfirm;
    TextView txtStatus;
    TextView txtSignal;
    TextView txtEncryption;
    TextView txtWifiName;
    String strWifiInfo ;
    String status ;
    String wifiName ;
    Context context ;

    AdapterItem dialogClick ;

    public WifiInfoDialog(@NonNull Context context, AdapterItem dialogClick ,String status ,String wifiName,String strWifiInfo) {
        super(context);
        this.context = context;
        this.strWifiInfo = strWifiInfo;
        this.status = status ;
        this.wifiName = wifiName ;
        this.dialogClick = dialogClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fde_wifi_info);
        setLayout();
        initView();
    }

      private void initView(){
        txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtConfirm = (TextView) findViewById(R.id.txtConfirm);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtSignal = (TextView) findViewById(R.id.txtSignal);
        txtEncryption = (TextView) findViewById(R.id.txtEncryption);
        txtWifiName = (TextView) findViewById(R.id.txtWifiName);

        txtCancel.setOnClickListener(this);
        txtConfirm.setOnClickListener(this);


        if(status !=null){
            txtStatus.setText("1".equals(status) ? context.getString(R.string.fde_has_connected): context.getString(R.string.fde_has_saved));
        }

        if(wifiName !=null){
            txtWifiName.setText(wifiName);
        }

        if(strWifiInfo !=null){
            try{
                String[] arrWifis	= strWifiInfo.split("\n");
                txtSignal.setText(arrWifis[0]);
                txtEncryption.setText(arrWifis[1]);
            }catch(Exception e){
                txtSignal.setText("--");
                txtEncryption.setText("--");
                e.printStackTrace();
            }
        }    

        if("1".equals(status)){
            //如果已连接
            txtConfirm.setText(context.getString(R.string.fde_unconnect)); 
        }else {
            //如果已保存
            txtConfirm.setText(context.getString(R.string.fde_del_network));
        }
    }

    private  void setLayout(){
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.x = 0;
        p.y = 200;
//        p.width = 400;// WindowManager.LayoutParams.MATCH_PARENT;
//        p.height = 250;///WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(p);
    }

       @Override
    public void onClick(View view) {
       if(view.getId() == R.id.txtCancel){
            dismiss();
        }else if(view.getId() == R.id.txtConfirm){
            if("1".equals(status)){
                dialogClick.onDialogClick(3,wifiName,"");
            }else{
                dialogClick.onDialogClick(4,wifiName,"");
            }
            dismiss();
        }
    }
}
