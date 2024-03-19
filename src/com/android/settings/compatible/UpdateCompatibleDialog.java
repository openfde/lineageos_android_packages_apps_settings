package com.android.settings.compatible;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.ImageView;
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
    TextView txtAppName;
    RecyclerView recyclerView;
    EditText editText;
    TextView txtTitleName;
    LinearLayout layoutEditText;
    LinearLayout layoutSwitch;
    Switch switchComp;
    ImageView imgApp;
    AutoCompleteTextView autoCompleteTextView;

    CompatibleSetAdapter compatibleSetAdapter;
    List<Compatible> list;
    List<AppData> appList;
    Map<String, Object> mp;
    String packageName;
    String appName;
    String optionJson;
    String inputType;
    String keyDesc;
    String keyCode;

    int position = -1;

    OnRefreshListener onRefreshListener;

    public UpdateCompatibleDialog(@NonNull Context context, String packageName, String appName,
            Map<String, Object> mp) {
        super(context);
        this.context = context;
        this.packageName = packageName;
        this.appName = appName;
        this.mp = mp;
    }

    public UpdateCompatibleDialog(@NonNull Context context, String packageName, String appName,
            Map<String, Object> mp, OnRefreshListener onRefreshListener) {
        super(context);
        this.context = context;
        this.packageName = packageName;
        this.appName = appName;
        this.mp = mp;
        this.onRefreshListener = onRefreshListener;
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
        layoutEditText = (LinearLayout) findViewById(R.id.layoutEditText);
        layoutSwitch = (LinearLayout) findViewById(R.id.layoutSwitch);
        switchComp = (Switch) findViewById(R.id.switchComp);
        editText = (EditText) findViewById(R.id.editText);
        txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtConfirm = (TextView) findViewById(R.id.txtConfirm);
        txtAppName = (TextView) findViewById(R.id.txtAppName);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtTitleName = (TextView) findViewById(R.id.txtTitleName);
        imgApp = (ImageView) findViewById(R.id.imgApp);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        keyCode = StringUtils.ToString(mp.get("KEY_CODE"));
        optionJson = StringUtils.ToString(mp.get("OPTION_JSON"));
        inputType = StringUtils.ToString(mp.get("INPUT_TYPE"));
        keyDesc = CompUtils.parseEnChJson(context, StringUtils.ToString(mp.get("KEY_DESC")));

        txtTitleName.setText(keyDesc);

        appList = CompUtils.getAllApps(context);

        String[] arrayApp = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            AppData appData = appList.get(i);
            arrayApp[i] = appData.getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line,
                arrayApp);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        if (appName != null && !"".equals(appName)) {
            txtAppName.setText(appName);
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

        AppData appData = CompUtils.getAppInfo(context, packageName);
        if (appData != null) {
            imgApp.setImageDrawable(appData.getIcon());
        } else {
            imgApp.setImageDrawable(context.getDrawable(R.drawable.icon_vnc));
        }

        if (TYPE_SELECT.equals(inputType)) {
            recyclerView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            switchComp.setVisibility(View.GONE);

            list = new ArrayList<>();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            compatibleSetAdapter = new CompatibleSetAdapter(context, list, this);
            recyclerView.setAdapter(compatibleSetAdapter);

            List<Map<String, Object>> tempList = parseJson(optionJson);
            if (tempList != null) {
                String result = CompatibleConfig.queryValueData(context, packageName, keyCode);
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
        } else if (TYPE_SWITCH.equals(inputType)) {
            recyclerView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            switchComp.setVisibility(View.VISIBLE);
            String result = CompatibleConfig.queryValueData(context, packageName, keyCode);
            if (result != null) {
                switchComp.setChecked(result.contains("true") ? true : false);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            switchComp.setVisibility(View.GONE);

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

        // holder.switchComp.setOnCheckedChangeListener(new
        // CompoundButton.OnCheckedChangeListener() {
        // @Override
        // public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        // }
        // });

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // packageName =
                // getCurPackageName(StringUtils.ToString(autoCompleteTextView.getText()));
                // if (packageName == null || "".equals(packageName)) {
                // Toast.makeText(context, context.getString(R.string.fde_input_appname_hint),
                // Toast.LENGTH_SHORT)
                // .show();
                // return;
                // }
                if (TYPE_SELECT.equals(inputType)) {
                    Compatible compatible = list.get(position);
                    String content = StringUtils.ToString(compatible.getMp());
                    CompatibleConfig.insertUpdateValueData(context, appName, packageName, keyCode, content);
                } else if (TYPE_SWITCH.equals(inputType)) {
                    boolean isChecked = switchComp.isChecked();
                    CompatibleConfig.insertUpdateValueData(context, appName, packageName, keyCode,
                            String.valueOf(isChecked));
                } else {
                    String content = editText.getText().toString();
                    if (!"".equals(content)) {
                        CompatibleConfig.insertUpdateValueData(context, appName, packageName, keyCode, content);
                    } else {
                        Toast.makeText(context, context.getString(R.string.fde_input_hint), Toast.LENGTH_SHORT).show();
                    }
                }
                if (onRefreshListener != null) {
                    onRefreshListener.OnRefresh();
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

    private String getCurPackageName(String appName) {
        String packageName = "";
        for (AppData appData : appList) {
            if (appName.equals(appData.getName())) {
                packageName = appData.getPackageName();
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
