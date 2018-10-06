package com.ss.www.bluetoothble.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ss.www.bluetoothble.R;
import com.ss.www.bluetoothble.dispaly.InfoData;

import java.util.List;

/**
 * Created by 小松松 on 2017/11/15.
 */

public class Main2Adapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<InfoData> mList;

    public Main2Adapter(Context mContext, List<InfoData> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_layout,parent,false);
        return new Main2ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof Main2ViewHolder){
            Main2ViewHolder viewHolder = (Main2ViewHolder) holder;
            viewHolder.t1.setText(mList.get(position).getName());
            viewHolder.t2.setText(mList.get(position).getData1());
            viewHolder.t3.setText(mList.get(position).getData2());
            viewHolder.t4.setText(mList.get(position).getData3());
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    class Main2ViewHolder extends RecyclerView.ViewHolder {
        TextView t1;
        TextView t2;
        TextView t3;
        TextView t4;
        public Main2ViewHolder(View itemView) {
            super(itemView);
            t1 = itemView.findViewById(R.id.channel_title);
            t2 = itemView.findViewById(R.id.value1);
            t3 = itemView.findViewById(R.id.value2);
            t4 = itemView.findViewById(R.id.value3);
        }
    }
}
