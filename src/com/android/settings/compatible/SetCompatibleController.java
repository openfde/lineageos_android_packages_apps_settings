package com.android.settings.compatible;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.utils.LogTools;

import androidx.recyclerview.widget.RecyclerView;

import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.widget.LinearLayout;
import android.widget.TextView;

public class SetCompatibleController implements OnItemClickListener {
    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private LinearLayout layoutAppName;
    private TextView txtAppName;

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
        }

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
