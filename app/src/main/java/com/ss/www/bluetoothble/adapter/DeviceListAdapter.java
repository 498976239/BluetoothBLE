package com.ss.www.bluetoothble.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ss.www.bluetoothble.R;

import java.util.List;

/**
 * Created by SS on 17-6-12.
 */
public class DeviceListAdapter extends BaseAdapter{
    private Context mContext;
    private List<BluetoothDevice> mList;

    public DeviceListAdapter(Context mContext, List<BluetoothDevice> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.device_name,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mDeviceName = (TextView) convertView.findViewById(R.id.mDeviceName);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mDeviceName.setText(mList.get(position).getName());
        return convertView;
    }
    class ViewHolder{
        TextView mDeviceName;
    }
}
