package com.android.settings.network_fde.http;

import java.util.Map;
import android.content.Context;

public class NetCtrl {

    public static void get(Context mContext, final String url, final Map<String, Object> params,
            HttpRequestCallBack listener) {
        new GetTask(mContext, url, params, listener).execute();
    }

    public static void getData(Context mContext, final String url, DatabaseRequestCallBack listener) {
        new GetDataTask(mContext, url, listener).execute();
    }
}
