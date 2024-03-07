package com.android.settings.compatible;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.android.settings.R;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

public class UpdateCompatibleDialog extends Dialog implements OnItemClickListener {
    public static final String TYPE_SELECT = "select";
    public static final String TYPE_INPUT = "input";
    public static final String TYPE_SWITCH = "switch";

    Context context;

    TextView txtCancel;
    TextView txtConfirm;
    RecyclerView recyclerView;
    EditText editText;
    TextView txtTitleName;
    AutoCompleteTextView autoCompleteTextView;

    CompatibleSetAdapter compatibleSetAdapter;
    List<Compatible> list;
    List<Map<String, Object>> appList;
    Map<String, Object> mp;
    String packageName;
    String appName;
    String optionJson;
    String inputType;
    String keyDesc;
    String keyCode;

    int position = -1;

    public UpdateCompatibleDialog(@NonNull Context context, String packageName, String appName,
            Map<String, Object> mp) {
        super(context);
        this.context = context;
        this.packageName = packageName;
        this.appName = appName;
        this.mp = mp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fde_update_compatible);
        setLayout();
        initView();
    }

    private void setLayout() {
        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 24);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        p.x = 0;
        p.y = 0;
        p.width = 400;// WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(p);
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.editText);
        txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtConfirm = (TextView) findViewById(R.id.txtConfirm);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtTitleName = (TextView) findViewById(R.id.txtTitleName);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        keyCode = StringUtils.ToString(mp.get("KEY_CODE"));
        optionJson = StringUtils.ToString(mp.get("OPTION_JSON"));
        inputType = StringUtils.ToString(mp.get("INPUT_TYPE"));
        keyDesc = StringUtils.ToString(mp.get("KEY_DESC"));

        txtTitleName.setText(keyDesc);

        appList = getAllApps();

        String[] arrayApp = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            Map<String, Object> map = appList.get(i);
            // 这里假设你需要获取名为 "key" 的值
            String value = StringUtils.ToString(map.get("appName"));
            arrayApp[i] = value;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line,
                arrayApp);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        if (appName != null && !"".equals(appName)) {
            autoCompleteTextView.setText(appName);
            autoCompleteTextView.setEnabled(false);
        } else {
            autoCompleteTextView.setEnabled(true);
        }

        // autoCompleteTextView.setOnClickListener(v -> {
        // String strText = StringUtils.ToString(autoCompleteTextView.getText());
        // if ("".equals(strText)) {
        // AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setItems(arrayApp, new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // String selected = arrayApp[which];
        // autoCompleteTextView.setText(selected);
        // }
        // });
        // builder.show();
        // }
        // });

        // LogTools.i("packageName " + packageName + " ,keyCode " + keyCode + " , mp " +
        // mp.toString());

        if (TYPE_SELECT.equals(inputType)) {
            recyclerView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);

            list = new ArrayList<>();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            compatibleSetAdapter = new CompatibleSetAdapter(context, list, this);
            recyclerView.setAdapter(compatibleSetAdapter);

            List<Map<String, Object>> tempList = parseJson(optionJson);
            if (tempList != null) {
                String result = CompatibleConfig.queryValueData(context, packageName, keyCode);
                // LogTools.i("result " + result);

                for (int i = 0; i < tempList.size(); i++) {
                    Compatible compatible = new Compatible();
                    compatible.setId(i);
                    if (result != null && result.equals(StringUtils.ToString(tempList.get(i)))) {
                        compatible.setSelect(true);
                    } else {
                        compatible.setSelect(false);
                    }
                    compatible.setMp(tempList.get(i));
                    list.add(compatible);
                }
            }
            compatibleSetAdapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);

            String result = CompatibleConfig.queryValueData(context, packageName, keyCode);
            if (result != null) {
                editText.setText(result);
            }
        }

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packageName = getCurPackageName(StringUtils.ToString(autoCompleteTextView.getText()));
                if (packageName == null || "".equals(packageName)) {
                    Toast.makeText(context, context.getString(R.string.fde_input_appname_hint), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TYPE_SELECT.equals(inputType)) {
                    Compatible compatible = list.get(position);
                    String content = StringUtils.ToString(compatible.getMp());
                    CompatibleConfig.insertUpdateValueData(context, appName, packageName, keyCode, content);
                } else {
                    String content = editText.getText().toString();
                    if (!"".equals(content)) {
                        CompatibleConfig.insertUpdateValueData(context, appName, packageName, keyCode, content);
                    } else {
                        Toast.makeText(context, context.getString(R.string.fde_input_hint), Toast.LENGTH_SHORT).show();
                    }
                }
                dismiss();
            }
        });

    }

    private List<Map<String, Object>> parseJson(String jsonArrayString) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = jsonToMap(jsonObject);
                if (map != null) {
                    list.add(map);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> jsonToMap(JSONObject jsonObject) {
        try {
            Map<String, Object> map = new HashMap<>();
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                map.put(key, value);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Map<String, Object>> getAllApps() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            List<UserHandle> userHandles = userManager.getUserProfiles();
            List<LauncherActivityInfo> activityInfoList = new ArrayList<>();
            for (UserHandle userHandle : userHandles) {
                activityInfoList.addAll(launcherApps.getActivityList(null, userHandle));
            }

            for (LauncherActivityInfo info : activityInfoList) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("appName", StringUtils.ToString(info.getLabel()));
                mp.put("packageName", StringUtils.ToString(info.getComponentName().getPackageName()));
                list.add(mp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getCurPackageName(String appName) {
        String packageName = "";
        for (Map<String, Object> mp : appList) {
            if (appName.equals(StringUtils.ToString(mp.get("appName")))) {
                packageName = StringUtils.ToString(mp.get("packageName"));
                break;
            }
        }
        return packageName;
    }

    @Override
    public void onItemClick(int position, String type) {
        for (int i = 0; i < list.size(); i++) {
            Compatible ca = list.get(i);
            ca.setSelect(false);
            list.set(i, ca);
        }

        Compatible compatible = list.get(position);
        compatible.setSelect(true);
        list.set(position, compatible);
        compatibleSetAdapter.notifyDataSetChanged();
        this.position = position;
    }

}
