package com.ss.www.bluetoothble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ss.www.bluetoothble.adapter.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private ListView mListView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private DeviceListAdapter mDeviceListAdapter;
    private List<BluetoothDevice> mList;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBtAdapter;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    //扫描结果的回调函数
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if(!mList.contains(device)){
                        mList.add(device);
                        mDeviceListAdapter.notifyDataSetChanged();
                    }

                }
            });
        }
    };
    //当使用API>21时，使用如下的程序
   /* private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result == null || result.getDevice() == null || TextUtils.isEmpty(result.getDevice().getName())){
                mToolbar.setSubtitle("没有扫描到设备");
                return;
            }
            mProgressBar.setVisibility(View.INVISIBLE);
            BluetoothDevice mDevice = result.getDevice();
            if(!mList.contains(mDevice)){
                mList.add(mDevice);
                mDeviceListAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            mToolbar.setSubtitle("搜索失败");
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        init();
        //获取BluetoothAdapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //mBluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
        mProgressBar.setVisibility(View.VISIBLE );
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("搜索设备");
        mList = new ArrayList();
        mHandler = new Handler();
        //为ListView设置点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mList.get(position);
                if(bluetoothDevice == null)
                    return;
                Intent intent = new Intent(DeviceListActivity.this,MainActivity.class);
                String address = bluetoothDevice.getAddress();
                String name = bluetoothDevice.getName();
                intent.putExtra(EXTRAS_DEVICE_NAME,name);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS,address);
                setResult(Activity.RESULT_OK,intent);
                DeviceListActivity.this.finish();
            }
        });
    }

    private void init() {
        mListView = (ListView) findViewById(R.id.deviceList);
        mProgressBar = (ProgressBar) findViewById(R.id.waiting);
        mToolbar = (Toolbar) findViewById(R.id.mDeviceListToolBar);
    }

    /**
     * 在该方法里面调取扫描程序
     */
    @Override
    protected void onResume() {
        super.onResume();
        mDeviceListAdapter = new DeviceListAdapter(this,mList);
        mListView.setAdapter(mDeviceListAdapter);
        scanLeDevice(true);
    }

    /**扫面设备方法
     * @param enable
     */
    private void scanLeDevice(boolean enable) {
        if(enable){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBtAdapter.stopLeScan(mLeScanCallback);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if(mList.size()>0){
                        mToolbar.setSubtitle("扫描结束，请连接");
                    }else {
                        mToolbar.setSubtitle("无设备可用");
                    }

                }
            }, SCAN_PERIOD);
            mBtAdapter.startLeScan(mLeScanCallback);
        }else{
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 在该方法里面调取停止扫描的程序
     */
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
