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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    private Button mScanButton,mStopButton;
    private ProgressBar mProgressBar;
    private DeviceListAdapter mDeviceListAdapter;
    private List<BluetoothDevice> mList;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBtAdapter;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 15000;
    //扫描结果的回调函数
    // Device scan callback.
    //<18API<21使用该代码
   /* private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //在  onLeScan() 回调中只做尽量少的工作，可以把扫描到的设备，扔到另外一个线程中去处理，
            // 让  onLeScan() 尽快返回。
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
    };*/
    //当使用API>21时，使用如下的程序
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result == null || result.getDevice() == null || TextUtils.isEmpty(result.getDevice().getName())){
                //mToolbar.setSubtitle("没有扫描到设备");
               // Log.i("main-3","------");
                return;
            }
            //Log.i("main-1","------");
            //mProgressBar.setVisibility(View.INVISIBLE);
            BluetoothDevice mDevice = result.getDevice();
            if(!mList.contains(mDevice)){
                mList.add(mDevice);
                //Log.i("main-2","------");
                mDeviceListAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
           // Log.i("main-3","------");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("main-4","------");
            mScanButton.setVisibility(View.VISIBLE);
            mToolbar.setSubtitle("搜索失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        init();
        //获取BluetoothAdapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
        mProgressBar.setVisibility(View.VISIBLE );
        mScanButton.setVisibility(View.INVISIBLE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("搜索设备");
        mList = new ArrayList();
        mHandler = new Handler();
        //为ListView设置点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothLeScanner.stopScan(mScanCallback);//再一次连接时，确定蓝牙已经不再搜索了
                BluetoothDevice bluetoothDevice = mList.get(position);
                if(bluetoothDevice == null)
                    return;
                Intent intent = new Intent(DeviceListActivity.this,Main2Activity.class);
                String address = bluetoothDevice.getAddress();
                String name = bluetoothDevice.getName();
                intent.putExtra(EXTRAS_DEVICE_NAME,name);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS,address);
                setResult(Activity.RESULT_OK,intent);
                DeviceListActivity.this.finish();
            }
        });
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolbar.setSubtitle("正在扫描...");
                mScanButton.setVisibility(View.VISIBLE);
                mBluetoothLeScanner.stopScan(mScanCallback);
                mScanButton.setEnabled(false);
                mScanButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE );
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanLeDevice(true);
                        mScanButton.setEnabled(true);
                        //mScanButton.setVisibility(View.VISIBLE);
                    }
                },3000);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeScanner.stopScan(mScanCallback);
                mScanButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mToolbar.setSubtitle("");
            }
        });
    }

    private void init() {
        mListView = (ListView) findViewById(R.id.deviceList);
        mProgressBar = (ProgressBar) findViewById(R.id.waiting);
        mToolbar = (Toolbar) findViewById(R.id.mDeviceListToolBar);
        mScanButton = (Button) findViewById(R.id.Scan_btn);
        mStopButton = (Button) findViewById(R.id.Stop_btn);
    }

    /**
     * 在该方法里面调取扫描程序
     */
    @Override
    protected void onResume() {
        super.onResume();
        mList.clear();
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
                    //mBtAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if(mList.size()>0){
                        mToolbar.setSubtitle("扫描结束，请连接");
                    }else {
                        mToolbar.setSubtitle("无设备可用");
                        mScanButton.setVisibility(View.VISIBLE);
                       // Log.i("main-12",mList.size()+"");
                    }
                }
            }, SCAN_PERIOD);
           // mBtAdapter.startLeScan(mLeScanCallback);//该方法已经被弃用
            mBluetoothLeScanner.startScan(mScanCallback);

        }else{
            //mBtAdapter.stopLeScan(mLeScanCallback);//该方法已经被弃用
             mBluetoothLeScanner.stopScan(mScanCallback);
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
                mBluetoothLeScanner.stopScan(mScanCallback);//返回的时候也要将mBluetoothLeScanner关掉
                break;
        }
        return true;
    }
}
