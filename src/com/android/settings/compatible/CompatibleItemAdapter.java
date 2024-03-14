package com.android.settings.compatible;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class CompatibleItemAdapter extends RecyclerView.Adapter<CompatibleItemAdapter.ViewHolder>
        implements OnRefreshListener {
    Context context;
    List<Map<String, Object>> list;
    Map<String, Object> mp;

    public CompatibleItemAdapter(Context context, List<Map<String, Object>> list, Map<String, Object> mp) {
        this.context = context;
        this.list = list;
        this.mp = mp;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compatible_item, parent, false);
        return new CompatibleItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> itemMap = list.get(position);
        String packageName = StringUtils.ToString(itemMap.get("PACKAGE_NAME"));
        String valueStr = StringUtils.ToString(itemMap.get("VALUE"));
        // LogTools.i("mp " + mp.toString() + " , valueStr: " + valueStr);

        AppData appData = CompUtils.getAppInfo(context, packageName);
        if (appData != null) {
            holder.txtAppName.setText(appData.getName());
            holder.imgIcon.setImageDrawable(appData.getIcon());
        }

        String showText = valueStr + "";
        if (valueStr.contains("=")) {
            String[] keyValue = valueStr.substring(1, valueStr.length() - 1).split("=");
            if (keyValue.length == 2) {
                showText = keyValue[1] + "";
            }
        }

        if ("switch".equals(StringUtils.ToString(mp.get("INPUT_TYPE")))) {
            holder.layoutSwitch.setVisibility(View.VISIBLE);
            holder.txtKey.setVisibility(View.GONE);
            holder.switchComp.setChecked("true".equals(showText) ? true : false);

            holder.switchComp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    CompatibleConfig.updateValueData(context, appData.getName(), packageName,
                            StringUtils.ToString(itemMap.get("KEY_CODE")), String.valueOf(b));
                }
            });
        } else {
            holder.layoutSwitch.setVisibility(View.GONE);
            holder.txtKey.setVisibility(View.VISIBLE);
            holder.txtKey.setText(showText);
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!"switch".equals(StringUtils.ToString(mp.get("INPUT_TYPE")))) {
                    UpdateCompatibleDialog updateComatibleDialog = new UpdateCompatibleDialog(context, packageName,
                            appData.getName(),
                            mp, CompatibleItemAdapter.this);
                    if (!updateComatibleDialog.isShowing()) {
                        updateComatibleDialog.show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void OnRefresh() {
        // getData();
        String keyCode = StringUtils.ToString(mp.get("KEY_CODE"));
        list = CompatibleConfig.queryValueListData(context, keyCode);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        TextView txtKey;
        TextView txtAppName;
        ImageView imgIcon;
        LinearLayout layoutSwitch;
        Switch switchComp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.rootView);
            txtKey = itemView.findViewById(R.id.txtKey);
            txtAppName = itemView.findViewById(R.id.txtAppName);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            layoutSwitch = itemView.findViewById(R.id.layoutSwitch);
            switchComp = itemView.findViewById(R.id.switchComp);
        }
    }
}
