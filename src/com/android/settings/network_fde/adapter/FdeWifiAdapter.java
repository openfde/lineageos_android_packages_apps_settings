/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  wifi list adapter by xudq
 */
package com.android.settings.network_fde.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.network_fde.AdapterItem;

import com.android.settings.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class FdeWifiAdapter extends RecyclerView.Adapter<FdeWifiAdapter.FdeWifiHolder> {
    Context context;
    List<Map<String, Object>> list;
    LayoutInflater layoutInflater; //
    AdapterItem aditem;

    public FdeWifiAdapter(Context context, List<Map<String, Object>> list, AdapterItem aditem) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
        this.aditem = aditem;
    }

    @NonNull
    @Override
    public FdeWifiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_fde_wifi_info, parent, false);
        FdeWifiHolder fdeWifiHolder = new FdeWifiHolder(view);
        return fdeWifiHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FdeWifiHolder holder, int position) {
        String wifiName = list.get(position).get("WIFI_NAME").toString();
        holder.txtWifiName.setText(wifiName);
        // holder.txtEncrypted.setText(list.get(position).get("isEncrypted").toString());
        int signal = StringUtils.ToInt(list.get(position).get("WIFI_SIGNAL"));
        if (signal >= 80) {
            holder.imgWifi.setImageResource(R.mipmap.icon_wifi);
        } else if (signal >= 50) {
            holder.imgWifi.setImageResource(R.mipmap.icon_wifi_high);
        } else if (signal > 20) {
            holder.imgWifi.setImageResource(R.mipmap.icon_wifi_half);
        } else {
            holder.imgWifi.setImageResource(R.mipmap.icon_wifi_lower);
        }

        int curNet = StringUtils.ToInt(list.get(position).get("IS_CUR")); // Integer.valueOf(list.get(position).get("curNet").toString());
        int isSaved = StringUtils.ToInt(list.get(position).get("IS_SAVE"));
        if (curNet == 1) {
            holder.txtEncrypted.setText(context.getString(R.string.fde_has_connected));
            holder.txtEncrypted.setTextColor(context.getColor(R.color.palette_list_color_blue));
        } else if (curNet == 2) {
            holder.txtEncrypted.setText(context.getString(R.string.fde_connecting));
            holder.txtEncrypted.setTextColor(context.getColor(R.color.app_blue_light));
        } else {
            if (isSaved == 1) {
                holder.txtEncrypted.setText(context.getString(R.string.fde_has_saved));
                holder.txtEncrypted.setTextColor(context.getColor(R.color.app_gray));
            } else {
                holder.txtEncrypted.setText(StringUtils.ToString(list.get(position).get("IS_ENCRYPTION")));
                holder.txtEncrypted.setTextColor(context.getColor(R.color.app_gray));
            }
        }

        // if (curNet == position) {
        // holder.txtEncrypted.setText(context.getString(R.string.fde_has_connected));
        // holder.txtEncrypted.setTextColor(context.getColor(R.color.palette_list_color_blue));
        // } else {
        // if ("1".equals(isSaved)) {
        // holder.txtEncrypted.setText(context.getString(R.string.fde_has_saved));
        // holder.txtEncrypted.setTextColor(context.getColor(R.color.app_gray));
        // } else if ("2".equals(isSaved)) {
        // holder.txtEncrypted.setText(context.getString(R.string.fde_connecting));
        // holder.txtEncrypted.setTextColor(context.getColor(R.color.app_blue_light));
        // } else {
        // holder.txtEncrypted.setText(StringUtils.ToString(list.get(position).get("IS_ENCRYPTION")));
        // holder.txtEncrypted.setTextColor(context.getColor(R.color.app_gray));
        // }
        // // holder.txtWifiName.setTextColor(R.color.palette_list_color_blue);
        // }

        holder.layoutWifiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // holder.txtEncrypted.setText(context.getString(R.string.fde_connecting));
                aditem.onItemClick(position, wifiName);
            }
        });

        holder.layoutWifiInfo.setOnContextClickListener(new View.OnContextClickListener() {
            @Override
            public boolean onContextClick(View view) {
                aditem.onContextClick(position, wifiName);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FdeWifiHolder extends RecyclerView.ViewHolder {
        TextView txtWifiName;
        TextView txtEncrypted;
        LinearLayout layoutWifiInfo;
        ImageView imgWifi;

        public FdeWifiHolder(@NonNull View itemView) {
            super(itemView);
            txtWifiName = itemView.findViewById(R.id.txtWifiName);
            txtEncrypted = itemView.findViewById(R.id.txtEncrypted);
            layoutWifiInfo = itemView.findViewById(R.id.layoutWifiInfo);
            imgWifi = itemView.findViewById(R.id.imgWifi);
        }
    }

}
