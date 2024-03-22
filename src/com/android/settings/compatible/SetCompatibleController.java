package com.android.settings.compatible;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import android.content.DialogInterface;
import android.app.AlertDialog;
import com.android.settings.R;

public class SetCompatibleController implements OnItemClickListener {
    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private LinearLayout layoutAppName;
    private TextView txtAppName;
    private ImageView imgClean;
    private ImageView imgApp;
    private ImageView imgRecovery;

    List<Map<String, Object>> list;
    CompatibleListAdapter compatibleListAdapter;

    String packageName;
    String appName;

    public SetCompatibleController(Context context, String packageName, String appName, View view) {
        this.context = context;
        this.view = view;
        this.packageName = packageName;
        this.appName = appName;

        initView();
    }

    private void initView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        txtAppName = (TextView) view.findViewById(R.id.txtAppName);
        imgClean = (ImageView) view.findViewById(R.id.imgClean);
        imgApp = (ImageView) view.findViewById(R.id.imgApp);
        imgRecovery = (ImageView) view.findViewById(R.id.imgRecovery);
        layoutAppName = (LinearLayout) view.findViewById(R.id.layoutAppName);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        compatibleListAdapter = new CompatibleListAdapter(context, packageName, list, this);
        recyclerView.setAdapter(compatibleListAdapter);

        if (packageName == null) {
            layoutAppName.setVisibility(View.GONE);
        } else {
            layoutAppName.setVisibility(View.VISIBLE);
            txtAppName.setText(appName);
            AppData appData = CompUtils.getAppInfo(context, packageName);
            if (appData != null) {
                imgApp.setImageDrawable(appData.getIcon());
            } else {
                imgApp.setImageDrawable(context.getDrawable(R.drawable.icon_vnc));
            }

        }

        imgRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.fde_tips))
                        .setMessage(context.getString(R.string.fde_reset_tips))
                        .setPositiveButton(context.getString(R.string.fde_btn_confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CompatibleConfig.recoveryValueData(context, packageName, "");
                                        getData();
                                    }
                                })
                        .setNegativeButton(context.getString(R.string.fde_btn_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }
        });

        imgClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.fde_tips))
                        .setMessage(context.getString(R.string.fde_clean_tips))
                        .setPositiveButton(context.getString(R.string.fde_btn_confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CompatibleConfig.deleteValueData(context, packageName);
                                        getData();
                                    }
                                })
                        .setNegativeButton(context.getString(R.string.fde_btn_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }
        });

        getData();
    }

    private void getData() {
        list.clear();
        List<Map<String, Object>> tempList = CompatibleConfig.queryListData(context);
        if (tempList != null) {
            list.addAll(tempList);
        }
        compatibleListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position, String type) {
        Map<String, Object> mp = list.get(position);
        UpdateCompatibleDialog updateComatibleDialog = new UpdateCompatibleDialog(context, packageName, appName,
                mp);
        if (!updateComatibleDialog.isShowing()) {
            updateComatibleDialog.show();
        }
    }
}
