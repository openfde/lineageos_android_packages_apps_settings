package com.android.settings.compatible;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class CompatibleListAdapter extends RecyclerView.Adapter<CompatibleListAdapter.ViewHolder> {
    Context context;
    List<Map<String, Object>> list;

    String packageName;

    OnItemClickListener onItemClickListener;

    public CompatibleListAdapter(Context context, String packageName, List<Map<String, Object>> list,
            OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.packageName = packageName;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compatible_list, parent, false);
        return new CompatibleListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> mp = list.get(position);

        String keyCode = StringUtils.ToString(mp.get("KEY_CODE"));
        holder.txtKey.setText(keyCode);
        String keyDescStr = CompUtils.parseEnChJson(context, StringUtils.ToString(mp.get("KEY_DESC")));
        holder.txtTitle.setText(keyDescStr);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LogTools.i("onClick rootView " + position + " ,mp " + mp.toString());
                if (packageName == null || "".equals(packageName)) {

                } else {
                    onItemClickListener.onItemClick(position, "");
                }
            }
        });

        String noteStr = CompUtils.parseEnChJson(context, StringUtils.ToString(mp.get("NOTES")));
        holder.imgRemarks.setVisibility("".equals(noteStr) ? View.GONE : View.VISIBLE);
        holder.imgRemarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LogTools.i("onClick imgRemarks " + position + " ,packageName " +
                // packageName);

                TipDialog tipDialog = new TipDialog(context, noteStr);
                if (!tipDialog.isShowing()) {
                    tipDialog.show();
                }
            }
        });

        holder.switchDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (packageName == null || "".equals(packageName)) {
                    if (b) {
                        holder.recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        holder.recyclerView.setVisibility(View.GONE);
                    }
                }
            }
        });

        if (packageName == null || "".equals(packageName)) {
            holder.switchDown.setVisibility(View.VISIBLE);
            holder.imgRight.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            holder.recyclerView.setLayoutManager(linearLayoutManager);
            List<Map<String, Object>> listItem = CompatibleConfig.queryValueListData(context, keyCode);
            if (listItem == null) {
                listItem = new ArrayList<>();
            }

            List<Map<String, Object>> listTemp = new ArrayList<>();

            for (Map<String, Object> mmm : listItem) {
                AppData appData = CompUtils.getAppInfo(context, StringUtils.ToString(mmm.get("PACKAGE_NAME")));
                if (appData == null) {
                    if (StringUtils.ToString(mmm.get("FIELDS1")).contains("VNC_")) {
                        listTemp.add(mmm);
                    }
                } else {
                    listTemp.add(mmm);
                }
            }

            CompatibleItemAdapter compatibleItemAdapter = new CompatibleItemAdapter(context, listTemp,
                    list.get(position));
            holder.recyclerView.setAdapter(compatibleItemAdapter);
        } else {
            holder.recyclerView.setVisibility(View.GONE);
            holder.switchDown.setVisibility(View.INVISIBLE);
            holder.imgRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        TextView txtKey;
        TextView txtTitle;
        RecyclerView recyclerView;
        ImageView imgRemarks;
        ImageView imgRight;
        Switch switchDown;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtKey = itemView.findViewById(R.id.txtKey);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            rootView = itemView.findViewById(R.id.rootView);
            imgRemarks = itemView.findViewById(R.id.imgRemarks);
            imgRight = itemView.findViewById(R.id.imgRight);
            switchDown = itemView.findViewById(R.id.switchDown);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
}
