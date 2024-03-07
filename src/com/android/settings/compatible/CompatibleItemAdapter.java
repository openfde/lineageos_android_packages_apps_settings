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

import java.util.List;
import java.util.Map;

public class CompatibleItemAdapter extends RecyclerView.Adapter<CompatibleItemAdapter.ViewHolder> {
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
        String packageName = StringUtils.ToString(list.get(position).get("PACKAGE_NAME"));
        String valueStr = StringUtils.ToString(list.get(position).get("VALUE"));
        String appName = StringUtils.ToString(list.get(position).get("FIELDS1"));
        // LogTools.i("mp " + mp.toString() + " , valueStr: " + valueStr);

        holder.txtValue.setText(appName);
        if (valueStr.contains("=")) {
            String[] keyValue = valueStr.substring(1, valueStr.length() - 1).split("=");
            if (keyValue.length == 2) {
                holder.txtKey.setText(keyValue[1] + "");
            }
        } else {
            holder.txtKey.setText(valueStr + "");
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateCompatibleDialog updateComatibleDialog = new UpdateCompatibleDialog(context, packageName, appName,
                        mp);
                if (!updateComatibleDialog.isShowing()) {
                    updateComatibleDialog.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        TextView txtKey;
        TextView txtValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.rootView);
            txtKey = itemView.findViewById(R.id.txtKey);
            txtValue = itemView.findViewById(R.id.txtValue);
        }
    }
}
