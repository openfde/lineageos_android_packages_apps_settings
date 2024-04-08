package com.android.settings.network_fde.http;

import java.util.List;
import java.util.Map;

public interface DatabaseRequestCallBack {
    void callBackListener(List<Map<String, Object>> result);

    void requestFail(String errorString, int code);
}
