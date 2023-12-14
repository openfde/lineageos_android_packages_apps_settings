package com.android.settings.network_fde;

public interface AdapterItem {
    void onItemClick(int pos,String content);
    void onDialogClick(int type,String ssid,String password);
}

