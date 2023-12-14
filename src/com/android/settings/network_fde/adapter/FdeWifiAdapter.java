package com.android.settings.network_fde.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.network_fde.AdapterItem;

import java.util.List;
import java.util.Map;

public class FdeWifiAdapter extends   RecyclerView.Adapter<FdeWifiAdapter.FdeWifiHolder>{
     Context context ;
     List<Map<String,Object>> list ;
     LayoutInflater layoutInflater; //声明布局填充器
     AdapterItem aditem;


    public FdeWifiAdapter(Context context, List<Map<String, Object>> list,AdapterItem aditem) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
        this.aditem = aditem;
    }

    @NonNull
    @Override
    public FdeWifiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_fde_wifi_info,parent,false);
        FdeWifiHolder fdeWifiHolder = new FdeWifiHolder(view);
        return fdeWifiHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FdeWifiHolder holder, int position) {
        String wifiName = list.get(position).get("name").toString();
        holder.txtWifiName.setText(wifiName);
        // holder.txtEncrypted.setText(list.get(position).get("isEncrypted").toString());
        String isSaved = list.get(position).get("isSaved").toString();
        int curNet = Integer.valueOf(list.get(position).get("curNet").toString());
 
        if(curNet == position){
            holder.txtEncrypted.setText("已连接");
            holder.txtWifiName.setTextColor(R.color.palette_list_color_blue);
        }else{
            if("1".equals(isSaved)){
                holder.txtEncrypted.setText("已保存");
            }else{
                holder.txtEncrypted.setText("");
            }
            holder.txtWifiName.setTextColor(R.color.black);
        }
 
        holder.layoutWifiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aditem.onItemClick(position,wifiName);    
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FdeWifiHolder extends RecyclerView.ViewHolder {
        TextView txtWifiName ;
        TextView txtEncrypted;
        LinearLayout layoutWifiInfo;

        public FdeWifiHolder(@NonNull View itemView) {
            super(itemView);
            txtWifiName = itemView.findViewById(R.id.txtWifiName);
            txtEncrypted = itemView.findViewById(R.id.txtEncrypted);
            layoutWifiInfo = itemView.findViewById(R.id.layoutWifiInfo);
        }
    }

}
