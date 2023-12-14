package com.android.settings.network_fde.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import lineageos.waydroid.Net;
import androidx.annotation.NonNull;
import com.android.settings.network_fde.AdapterItem;
import com.android.settings.R;
import android.util.Log;


public class SelectWlanDialog extends Dialog implements View.OnClickListener {
    Context context ;
    AdapterItem dialogClick ;

    TextView txtCancel ;
    TextView txtConfirm;
    TextView txtWifiName;
    EditText editPassword;
    String wifiName ;

    public SelectWlanDialog(@NonNull Context context,String wifiName,AdapterItem dialogClick) {
        super(context);
        this.context = context;
        this.wifiName = wifiName;
        this.dialogClick = dialogClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fde_select_wlan);
        setLayout();
        initView();
    }

    private  void setLayout(){
        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 24);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        p.x = 0;
        p.y = 0;
        p.width =  400;//WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(p);
    }

    private void initView(){
        txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtConfirm = (TextView) findViewById(R.id.txtConfirm);
        txtWifiName = (TextView) findViewById(R.id.txtWifiName);
        editPassword = (EditText) findViewById(R.id.editPassword);

        txtCancel.setOnClickListener(this);
        txtConfirm.setOnClickListener(this);

        txtWifiName.setText(wifiName);    
    }


    @Override
    public void onClick(View view) {
       if(view.getId() == R.id.txtCancel){
            dismiss();
        }else if(view.getId() == R.id.txtConfirm){
            Net net = Net.getInstance(context);
            String wifiName = txtWifiName.getText().toString();
            String password = editPassword.getText().toString();
            dialogClick.onDialogClick(2,wifiName,password);
            dismiss();
        }
    }
}
