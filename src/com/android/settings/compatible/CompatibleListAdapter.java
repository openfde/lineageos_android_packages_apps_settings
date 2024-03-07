package com.android.settings.compatible;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        holder.txtValue.setText(StringUtils.ToString(mp.get("KEY_DESC")));

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(position, "");

            }
        });

        if (packageName == null || "".equals(packageName)) {
            holder.recyclerView.setVisibility(View.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            holder.recyclerView.setLayoutManager(linearLayoutManager);
            List<Map<String, Object>> listItem = CompatibleConfig.queryValueListData(context, keyCode);
            if (listItem == null) {
                listItem = new ArrayList<>();
            }
            CompatibleItemAdapter compatibleItemAdapter = new CompatibleItemAdapter(context, listItem,
                    list.get(position));
            holder.recyclerView.setAdapter(compatibleItemAdapter);
        } else {
            holder.recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        TextView txtKey;
        TextView txtValue;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtKey = itemView.findViewById(R.id.txtKey);
            txtValue = itemView.findViewById(R.id.txtValue);
            rootView = itemView.findViewById(R.id.rootView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
}
