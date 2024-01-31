package com.android.settings.network_fde.http;

public interface HttpRequestCallBack {

    void callBackListener(String result);

    void requestFail(String errorString, int code);
}
