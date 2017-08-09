package com.ss.www.bluetoothble.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ss.www.bluetoothble.R;
import com.ss.www.bluetoothble.entity.Bean;

import java.util.List;

/**
 * Created by SS on 17-6-24.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<Bean> mList;

    public MyRecyclerAdapter(Context mContext, List<Bean> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_adapter,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.name.setText(mList.get(position).getName());
            myViewHolder.mData1.setText(mList.get(position).getData1()+"");
            myViewHolder.mData2.setText(mList.get(position).getData2()+"");
            myViewHolder.mData3.setText(mList.get(position).getData3()+"");
            myViewHolder.data_time.setText(mList.get(position).getTimeDetail());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView mData1;
        TextView mData2;
        TextView mData3;
        TextView data_time;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.channel_name);
            mData1 = (TextView) itemView.findViewById(R.id.mData1);
            mData2 = (TextView) itemView.findViewById(R.id.mData2);
            mData3 = (TextView) itemView.findViewById(R.id.mData3);
            data_time = (TextView) itemView.findViewById(R.id.mData4);
        }
    }
}
