package com.android.settings.compatible;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class CompatibleSetAdapter extends RecyclerView.Adapter<CompatibleSetAdapter.ViewHolder> {
    Context context;
    List<Compatible> list;

    OnItemClickListener onItemClickListener;

    public CompatibleSetAdapter(Context context, List<Compatible> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compatible_set, parent, false);
        return new CompatibleSetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Compatible compat = list.get(position);
        Map<String, Object> mp = compat.getMp();
        String showContent = "";
        for (Map.Entry<String, Object> entry : mp.entrySet()) {
            String key = entry.getKey();
            String value = StringUtils.ToString(entry.getValue());
            // LogTools.i("onBindViewHolder key: " + key + " , value " + value);
            // showContent += key + ": " + value + "\n";
            showContent += value + ",";
            if (showContent.length() > 1) {
                holder.txtTitle.setText(showContent.substring(0, showContent.length() - 1));
            }
        }

        if (compat.isSelect()) {
            holder.radioButton.setImageResource(R.mipmap.radio_select);
        } else {
            holder.radioButton.setImageResource(R.mipmap.radio_unselect);
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            boolean isChcekcout = false;

            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(position, "");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        TextView txtTitle;
        ImageView radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = (LinearLayout) itemView.findViewById(R.id.rootView);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            radioButton = (ImageView) itemView.findViewById(R.id.radioButton);
        }
    }
}
