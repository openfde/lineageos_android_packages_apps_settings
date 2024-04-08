package com.android.settings.network_fde.http;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetDataTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {
    Context mContext;
    String url;
    DatabaseRequestCallBack listener;

    public GetDataTask(Context mContext, String url, DatabaseRequestCallBack listener) {
        this.mContext = mContext;
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected List<Map<String, Object>> doInBackground(Void... voids) {
        List<Map<String, Object>> list = new ArrayList<>();
        if ("save".equals(url)) {
            list = WifiUtils.queryWifiList(mContext, "IS_SAVE = ?", new String[] { "1" });
        } else if ("unsave".equals(url)) {
            list = WifiUtils.queryWifiList(mContext, "IS_SAVE = ?", new String[] { "0" });
        } else {
            list = WifiUtils.queryAllWifiList(mContext);
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Map<String, Object>> s) {
        super.onPostExecute(s);
        listener.callBackListener(s);
    }
}
