package com.android.settings;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.util.Log;
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.android.settings.network.fde.NetApi;
import android.provider.Settings

class WifiBroadcastReceiver : BroadcastReceiver() {
    val SYSTEMUI_PACKAGE =  "com.android.systemui"
    val Wifi_ACTION =  SYSTEMUI_PACKAGE+".CONNECTIVITY_CHANGE"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val wifiStatus = intent.getIntExtra("wifiStatus",0)
        Log.w("NetWorkBroadcastReceiver","bsettings-WifiBroadcastReceiver  action: $action ---wifiStatus: $wifiStatus")
        
        if(-1 == wifiStatus){
            //network changed 
            GlobalScope.launch(Dispatchers.IO) {
                val status = NetApi.isWifiEnable(context!!)  
                if(status == 1){
                    val wifiName = NetApi.getActivedWifi(context!!);
                }else{
                    Settings.Global.putString(context.contentResolver, "wifi_name", "");
                }
                val inte = Intent(Wifi_ACTION)
                inte.putExtra("wifiStatus", status)
                inte.setPackage(SYSTEMUI_PACKAGE)
                context.sendBroadcast(inte)
            }
        }else{
            //open or close wifi
            GlobalScope.launch(Dispatchers.IO) {
                NetApi.enableWifi(context!!, wifiStatus)          
                if(wifiStatus == 1){
                    NetApi.getActivedWifi(context!!);
                }
                val inte = Intent(Wifi_ACTION)
                inte.putExtra("wifiStatus", wifiStatus)
                inte.setPackage(SYSTEMUI_PACKAGE)
                context.sendBroadcast(inte)
            }
        }
        
       

    }
}
