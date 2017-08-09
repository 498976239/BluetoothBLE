package com.ss.www.bluetoothble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.www.bluetoothble.dbmanager.CommUtils;
import com.ss.www.bluetoothble.entity.Bean;
import com.ss.www.bluetoothble.service.BluetoothLeService;
import com.ss.www.bluetoothble.utils.LogUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final int RECEIVE_DATA = 4;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private boolean connect_flag ;
    private int tem;
    private int count_re_ask;//当校验未成功，自动请求次数
    private boolean count_re_ask_flag;//当校验未成功，自动请求次数
    private BluetoothAdapter mBluetoothAdapter;
    private DrawerLayout mDrawer;
    private ProgressBar mProgressBar;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private String mSend;
    private Button mButton;
    private List<TextView> mListTextView;
    private List<byte[]> mList = new ArrayList<>();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mCharacteristic;
    private SimpleDateFormat mSimpleDateFormat;
    private CommUtils mCommUtils;
    private List<Bean> mBeanList;
    private byte[] compare;
    private String mDeviceName;
    private String mDeviceAddress;
    private int count;
    private int time_count;
    private boolean flag;
    private int c;
    private Timer mTimer;
    //定时执行的代码
    private TimerTask  mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(flag == true){
                time_count++;//用来记录时间，单位ms
            }
            if(time_count >200 && flag == true){
               receiveFinish();//当大于200ms时表示没有数据发过来了，将数据进行处理
                time_count = 0;
            }
        }
    };
    /*
    * 通过handler接收传过来的数据*/
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECEIVE_DATA:
                    tem = msg.arg1;
                    mProgressBar.setVisibility(View.GONE);
                    byte[] data = (byte[]) msg.obj;
                    byte[] result = new byte[msg.arg1-2];//除去校验位的数据
                    byte[] result_temp = new byte[msg.arg1];
                    System.arraycopy(data,0,result_temp,0,msg.arg1);
                    byte[] crc16 = new byte[2];
                    System.arraycopy(data,msg.arg1-2,crc16,0,2);//获取字节数组的CRC校验码，最后两个字节
                    for (int i = 0; i < crc16.length; i++) {
                        LogUtil.i("main--crc",crc16[i]+"");
                    }
                    System.arraycopy(data,0,result,0,msg.arg1-2);//获取除去CRC校验码之后的数据信息
                    byte[] myCRC =  intToByteArray(getCrc16(result));//生成CRC校验码,和获取的刚好相反
                    for (int j = 0; j < myCRC.length; j++) {
                        LogUtil.i("main--crc2",myCRC[j]+"");
                    }
                    if(myCRC[3]==crc16[0]&&myCRC[2]==crc16[1]){
                        count_re_ask_flag = false;
                        count_re_ask = 0;
                        boolean equals = Arrays.equals(result, compare);//判断两个数组是否相等
                        for (int j = 0; j < result.length; j++) {
                            LogUtil.i("main--result",result[j]+"");
                        }
                        if(!equals){
                            byteToFloat2(result);
                        }else {
                            Toast.makeText(MainActivity.this,"数据相同,请更新后采集",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(MainActivity.this,"校验未通过，重新请求",Toast.LENGTH_SHORT).show();
                        count_re_ask_flag = true;
                        if(count_re_ask_flag){
                            if(count_re_ask < 3){
                                byte[] b = {0x01,(byte)0xa3,0xE,0xE,0,0x8};
                                byte[] orderCRC =  intToByteArray(getCrc16(b));
                                byte[] end = {0x01,(byte)0xa3,0xE,0xE,0,0x8,orderCRC[2],orderCRC[3]};
                                mCharacteristic.setValue(end);
                                mBluetoothLeService.writeCharacteristic(mCharacteristic);
                            }else {
                                count_re_ask = 4;
                            }
                            count_re_ask++;
                        }


                    }
                   LogUtil.i("main---data",data.length+"");
                    break;
            }
        }
    };
    private IntentFilter intentFilter;
    private TextView mChannel1_1, mChannel1_2, mChannel1_3, mChannel2_1, mChannel2_2, mChannel2_3, mChannel3_1, mChannel3_2, mChannel3_3,
            mChannel4_1, mChannel4_2, mChannel4_3, mChannel5_1, mChannel5_2, mChannel5_3, mChannel6_1, mChannel6_2, mChannel6_3,
            mChannel7_1, mChannel7_2, mChannel7_3, mChannel8_1, mChannel8_2, mChannel8_3, mChannel9_1, mChannel9_2, mChannel9_3,
            mChannel10_1, mChannel10_2, mChannel10_3, mChannel11_1, mChannel11_2, mChannel11_3, mChannel12_1, mChannel12_2, mChannel12_3,
            mChannel13_1, mChannel13_2, mChannel13_3, mChannel14_1, mChannel14_2, mChannel14_3, mChannel15_1, mChannel15_2, mChannel15_3,
            mChannel16_1, mChannel16_2, mChannel16_3, mChannel17_1, mChannel17_2, mChannel17_3, mChannel18_1, mChannel18_2, mChannel18_3,
            mChannel19_1, mChannel19_2, mChannel19_3, mChannel20_1, mChannel20_2, mChannel20_3, mChannel21_1, mChannel21_2, mChannel21_3,
            mChannel22_1, mChannel22_2, mChannel22_3, mChannel23_1, mChannel23_2, mChannel23_3, mChannel24_1, mChannel24_2, mChannel24_3,
            mChannel25_1, mChannel25_2, mChannel25_3, mChannel26_1, mChannel26_2, mChannel26_3, mChannel27_1, mChannel27_2, mChannel27_3,
            mChannel28_1, mChannel28_2, mChannel28_3, mChannel29_1, mChannel29_2, mChannel29_3, mChannel30_1, mChannel30_2, mChannel30_3,
            mChannel31_1, mChannel31_2, mChannel31_3, mChannel32_1, mChannel32_2, mChannel32_3, mChannel33_1, mChannel33_2, mChannel33_3,
            mChannel34_1, mChannel34_2, mChannel34_3, mChannel35_1, mChannel35_2, mChannel35_3, mChannel36_1, mChannel36_2, mChannel36_3;
    //管理服务生命周期的代码
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
             mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取本地蓝牙设备
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListTextView = new ArrayList<>();
        mBeanList = new ArrayList<>();//用来存放要存储的Bean对象
        mCommUtils = new CommUtils(this);//操作数据库的工具类
        init();
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer,mToolbar, 0, 0);//显示左侧图标
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        if(mBluetoothLeService == null){
            mBluetoothLeService = new BluetoothLeService();
        }
        mToolbar.setSubtitle("没有连接设备");
        mTimer = new Timer();
        mTimer.schedule(mTimerTask,1,1);
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    if(connect_flag == true){
                        tem = 0;
                        notice();
                        mProgressBar.setVisibility(View.VISIBLE);
                        byte[] b = {0x01,(byte)0xa3,0xE,0xE,0,0x8};
                        byte[] orderCRC =  intToByteArray(getCrc16(b));
                        byte[] end = {0x01,(byte)0xa3,0xE,0xE,0,0x8,orderCRC[2],orderCRC[3]};
                        mCharacteristic.setValue(end);
                        mBluetoothLeService.writeCharacteristic(mCharacteristic);
                        mButton.setEnabled(false);
                        mButton.setVisibility(View.GONE);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mButton.setEnabled(true);
                                mButton.setVisibility(View.VISIBLE);
                            }
                        },2000);
                    }else {
                        Toast.makeText(MainActivity.this,"蓝牙已经断开，请重连",Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(MainActivity.this,"蓝牙没有打开",Toast.LENGTH_SHORT).show();
                }

            }

        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, intentFilter);
        if(mBluetoothLeService != null && mBluetoothAdapter.isEnabled()){
            if(mDeviceAddress != null)
                mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(mBroadcastReceiver);//一般解除广播是在 onPause()里的，但是为了防止黑屏就收不到广播，所以一到这里来了
        mBluetoothLeService = null;
        if(mTimer != null){
            mTimer.cancel();
        }
        time_count = 0;
    }
    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.mToolBar);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawer = (DrawerLayout) findViewById(R.id.mDrawer);
        mButton = (Button) findViewById(R.id.mUpdate);
        mProgressBar = (ProgressBar) findViewById(R.id.waiting);
        mChannel1_1 = (TextView) findViewById(R.id.channel1_1);mChannel1_2 = (TextView) findViewById(R.id.channel1_2);mChannel1_3 = (TextView) findViewById(R.id.channel1_3);
        mChannel2_1 = (TextView) findViewById(R.id.channel2_1);mChannel2_2 = (TextView) findViewById(R.id.channel2_2);mChannel2_3 = (TextView) findViewById(R.id.channel2_3);
        mChannel3_1 = (TextView) findViewById(R.id.channel3_1);mChannel3_2 = (TextView) findViewById(R.id.channel3_2);mChannel3_3 = (TextView) findViewById(R.id.channel3_3);
        mChannel4_1 = (TextView) findViewById(R.id.channel4_1);mChannel4_2 = (TextView) findViewById(R.id.channel4_2);mChannel4_3 = (TextView) findViewById(R.id.channel4_3);
        mChannel5_1 = (TextView) findViewById(R.id.channel5_1);mChannel5_2 = (TextView) findViewById(R.id.channel5_2);mChannel5_3 = (TextView) findViewById(R.id.channel5_3);
        mChannel6_1 = (TextView) findViewById(R.id.channel6_1);mChannel6_2 = (TextView) findViewById(R.id.channel6_2);mChannel6_3 = (TextView) findViewById(R.id.channel6_3);
        mChannel7_1 = (TextView) findViewById(R.id.channel7_1);mChannel7_2 = (TextView) findViewById(R.id.channel7_2);mChannel7_3 = (TextView) findViewById(R.id.channel7_3);
        mChannel8_1 = (TextView) findViewById(R.id.channel8_1);mChannel8_2 = (TextView) findViewById(R.id.channel8_2);mChannel8_3 = (TextView) findViewById(R.id.channel8_3);
        mChannel9_1 = (TextView) findViewById(R.id.channel9_1);mChannel9_2 = (TextView) findViewById(R.id.channel9_2);mChannel9_3 = (TextView) findViewById(R.id.channel9_3);
        mChannel10_1 = (TextView) findViewById(R.id.channel10_1);mChannel10_2 = (TextView) findViewById(R.id.channel10_2);mChannel10_3 = (TextView) findViewById(R.id.channel10_3);
        mChannel11_1 = (TextView) findViewById(R.id.channel11_1);mChannel11_2 = (TextView) findViewById(R.id.channel11_2);mChannel11_3 = (TextView) findViewById(R.id.channel11_3);
        mChannel12_1 = (TextView) findViewById(R.id.channel12_1);mChannel12_2 = (TextView) findViewById(R.id.channel12_2);mChannel12_3 = (TextView) findViewById(R.id.channel12_3);
        mChannel13_1 = (TextView) findViewById(R.id.channel13_1);mChannel13_2 = (TextView) findViewById(R.id.channel13_2);mChannel13_3 = (TextView) findViewById(R.id.channel13_3);
        mChannel14_1 = (TextView) findViewById(R.id.channel14_1);mChannel14_2 = (TextView) findViewById(R.id.channel14_2);mChannel14_3 = (TextView) findViewById(R.id.channel14_3);
        mChannel15_1 = (TextView) findViewById(R.id.channel15_1);mChannel15_2 = (TextView) findViewById(R.id.channel15_2);mChannel15_3 = (TextView) findViewById(R.id.channel15_3);
        mChannel16_1 = (TextView) findViewById(R.id.channel16_1);mChannel16_2 = (TextView) findViewById(R.id.channel16_2);mChannel16_3 = (TextView) findViewById(R.id.channel16_3);
        mChannel17_1 = (TextView) findViewById(R.id.channel17_1);mChannel17_2 = (TextView) findViewById(R.id.channel17_2);mChannel17_3 = (TextView) findViewById(R.id.channel17_3);
        mChannel18_1 = (TextView) findViewById(R.id.channel18_1);mChannel18_2 = (TextView) findViewById(R.id.channel18_2);mChannel18_3 = (TextView) findViewById(R.id.channel18_3);
        mChannel19_1 = (TextView) findViewById(R.id.channel19_1);mChannel19_2 = (TextView) findViewById(R.id.channel19_2);mChannel19_3 = (TextView) findViewById(R.id.channel19_3);
        mChannel20_1 = (TextView) findViewById(R.id.channel20_1);mChannel20_2 = (TextView) findViewById(R.id.channel20_2);mChannel20_3 = (TextView) findViewById(R.id.channel20_3);
        mChannel21_1 = (TextView) findViewById(R.id.channel21_1);mChannel21_2 = (TextView) findViewById(R.id.channel21_2);mChannel21_3 = (TextView) findViewById(R.id.channel21_3);
        mChannel22_1 = (TextView) findViewById(R.id.channel22_1);mChannel22_2 = (TextView) findViewById(R.id.channel22_2);mChannel22_3 = (TextView) findViewById(R.id.channel22_3);
        mChannel23_1 = (TextView) findViewById(R.id.channel23_1);mChannel23_2 = (TextView) findViewById(R.id.channel23_2);mChannel23_3 = (TextView) findViewById(R.id.channel23_3);
        mChannel24_1 = (TextView) findViewById(R.id.channel24_1);mChannel24_2 = (TextView) findViewById(R.id.channel24_2);mChannel24_3 = (TextView) findViewById(R.id.channel24_3);
        mChannel25_1 = (TextView) findViewById(R.id.channel25_1);mChannel25_2 = (TextView) findViewById(R.id.channel25_2);mChannel25_3 = (TextView) findViewById(R.id.channel25_3);
        mChannel26_1 = (TextView) findViewById(R.id.channel26_1);mChannel26_2 = (TextView) findViewById(R.id.channel26_2);mChannel26_3 = (TextView) findViewById(R.id.channel26_3);
        mChannel27_1 = (TextView) findViewById(R.id.channel27_1);mChannel27_2 = (TextView) findViewById(R.id.channel27_2);mChannel27_3 = (TextView) findViewById(R.id.channel27_3);
        mChannel28_1 = (TextView) findViewById(R.id.channel28_1);mChannel28_2 = (TextView) findViewById(R.id.channel28_2);mChannel28_3 = (TextView) findViewById(R.id.channel28_3);
        mChannel29_1 = (TextView) findViewById(R.id.channel29_1);mChannel29_2 = (TextView) findViewById(R.id.channel29_2);mChannel29_3 = (TextView) findViewById(R.id.channel29_3);
        mChannel30_1 = (TextView) findViewById(R.id.channel30_1);mChannel30_2 = (TextView) findViewById(R.id.channel30_2);mChannel30_3 = (TextView) findViewById(R.id.channel30_3);
        mChannel31_1 = (TextView) findViewById(R.id.channel31_1);mChannel31_2 = (TextView) findViewById(R.id.channel31_2);mChannel31_3 = (TextView) findViewById(R.id.channel31_3);
        mChannel32_1 = (TextView) findViewById(R.id.channel32_1);mChannel32_2 = (TextView) findViewById(R.id.channel32_2);mChannel32_3 = (TextView) findViewById(R.id.channel32_3);
        mChannel33_1 = (TextView) findViewById(R.id.channel33_1);mChannel33_2 = (TextView) findViewById(R.id.channel33_2);mChannel33_3 = (TextView) findViewById(R.id.channel33_3);
        mChannel34_1 = (TextView) findViewById(R.id.channel34_1);mChannel34_2 = (TextView) findViewById(R.id.channel34_2);mChannel34_3 = (TextView) findViewById(R.id.channel34_3);
        mChannel35_1 = (TextView) findViewById(R.id.channel35_1);mChannel35_2 = (TextView) findViewById(R.id.channel35_2);mChannel35_3 = (TextView) findViewById(R.id.channel35_3);
        mChannel36_1 = (TextView) findViewById(R.id.channel36_1);mChannel36_2 = (TextView) findViewById(R.id.channel36_2);mChannel36_3 = (TextView) findViewById(R.id.channel36_3);
        mListTextView.add( mChannel1_1);mListTextView.add( mChannel1_2);mListTextView.add( mChannel1_3);mListTextView.add( mChannel2_1);mListTextView.add( mChannel2_2);mListTextView.add( mChannel2_3);
        mListTextView.add( mChannel3_1);mListTextView.add( mChannel3_2);mListTextView.add( mChannel3_3);mListTextView.add( mChannel4_1);mListTextView.add( mChannel4_2);mListTextView.add( mChannel4_3);
        mListTextView.add( mChannel5_1);mListTextView.add( mChannel5_2);mListTextView.add( mChannel5_3);mListTextView.add( mChannel6_1);mListTextView.add( mChannel6_2);mListTextView.add( mChannel6_3);
        mListTextView.add( mChannel7_1);mListTextView.add( mChannel7_2);mListTextView.add( mChannel7_3);mListTextView.add( mChannel8_1);mListTextView.add( mChannel8_2);mListTextView.add( mChannel8_3);
        mListTextView.add( mChannel9_1);mListTextView.add( mChannel9_2);mListTextView.add( mChannel9_3);mListTextView.add( mChannel10_1);mListTextView.add( mChannel10_2);mListTextView.add( mChannel10_3);
        mListTextView.add( mChannel11_1);mListTextView.add( mChannel11_2);mListTextView.add( mChannel11_3);mListTextView.add( mChannel12_1);mListTextView.add( mChannel12_2);mListTextView.add( mChannel12_3);
        mListTextView.add( mChannel13_1);mListTextView.add( mChannel13_2);mListTextView.add( mChannel13_3);mListTextView.add( mChannel14_1);mListTextView.add( mChannel14_2);mListTextView.add( mChannel14_3);
        mListTextView.add( mChannel15_1);mListTextView.add( mChannel15_2);mListTextView.add( mChannel15_3);mListTextView.add( mChannel16_1);mListTextView.add( mChannel16_2);mListTextView.add( mChannel16_3);
        mListTextView.add( mChannel17_1);mListTextView.add( mChannel17_2);mListTextView.add( mChannel17_3);mListTextView.add( mChannel18_1);mListTextView.add( mChannel18_2);mListTextView.add( mChannel18_3);
        mListTextView.add( mChannel19_1);mListTextView.add( mChannel19_2);mListTextView.add( mChannel19_3);mListTextView.add( mChannel20_1);mListTextView.add( mChannel20_2);mListTextView.add( mChannel20_3);
        mListTextView.add( mChannel21_1);mListTextView.add( mChannel21_2);mListTextView.add( mChannel21_3);mListTextView.add( mChannel22_1);mListTextView.add( mChannel22_2);mListTextView.add( mChannel22_3);
        mListTextView.add( mChannel23_1);mListTextView.add( mChannel23_2);mListTextView.add( mChannel23_3);mListTextView.add( mChannel24_1);mListTextView.add( mChannel24_2);mListTextView.add( mChannel24_3);
        mListTextView.add( mChannel25_1);mListTextView.add( mChannel25_2);mListTextView.add( mChannel25_3);mListTextView.add( mChannel26_1);mListTextView.add( mChannel26_2);mListTextView.add( mChannel26_3);
        mListTextView.add( mChannel27_1);mListTextView.add( mChannel27_2);mListTextView.add( mChannel27_3);mListTextView.add( mChannel28_1);mListTextView.add( mChannel28_2);mListTextView.add( mChannel28_3);
        mListTextView.add( mChannel29_1);mListTextView.add( mChannel29_2);mListTextView.add( mChannel29_3);mListTextView.add( mChannel30_1);mListTextView.add( mChannel30_2);mListTextView.add( mChannel30_3);
        mListTextView.add( mChannel31_1);mListTextView.add( mChannel31_2);mListTextView.add( mChannel31_3);mListTextView.add( mChannel32_1);mListTextView.add( mChannel32_2);mListTextView.add( mChannel32_3);
        mListTextView.add( mChannel33_1);mListTextView.add( mChannel33_2);mListTextView.add( mChannel33_3);mListTextView.add( mChannel34_1);mListTextView.add( mChannel34_2);mListTextView.add( mChannel34_3);
        mListTextView.add( mChannel35_1);mListTextView.add( mChannel35_2);mListTextView.add( mChannel35_3);mListTextView.add( mChannel36_1);mListTextView.add( mChannel36_2);mListTextView.add( mChannel36_3);
    }

    /**
     * 定义广播接收信息
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    setMessage("连接成功");
                    connect_flag = true;
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    setMessage("连接断开");
                    connect_flag = false;
            }
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                LogUtil.i("MainActivity----","---------------------------------");
                BluetoothGattService service = mBluetoothLeService.getSupportedGattServices();
                UUID uuid = service.getUuid();
                LogUtil.i("MainActivity----",uuid.toString());
                mCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                 int charaProp = mCharacteristic.getProperties();
                if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                    mBluetoothLeService.readCharacteristic(mCharacteristic);
                }
                if((charaProp | BluetoothGattCharacteristic.PERMISSION_WRITE) > 0){
                    ///mCharacteristic.setValue(mSend);
                    //mBluetoothLeService.writeCharacteristic(mCharacteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                    mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
                }

            }
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                flag = true;
                time_count = 0;
                byte[] result = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                mList.add(result);
              //  Log.i("MainActivity-mList---",mList.size()+"");
               // Log.i("MainActivity-flag---",flag+"");
              //  Log.i("MainActivity-c---",c+"");
                count = count +result.length;
            }
        }
    };

    //字节数组转成整形
    public int bytesToInt(byte[] b) {
        if(b.length == 4){
            int i = (b[0] << 24) & 0xFF000000;
            i |= (b[1] << 16) & 0xFF0000;

            i |= (b[2] << 8) & 0xFF00;

            i |= b[3] & 0xFF;
            return i;
        }
        return 0;
    }
    //int转换成byte[]
    public  byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
    //生成crc校验码
    public  int getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;
        //预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            //把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                //把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc & 0x0001) > 0) {
                    //如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    //如果移出位为 0,再次右移一位
                    crc = crc >> 1;
            }
        }
        return crc;

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    //当蓝牙被开启之后可以写入一些内容，目前不做处理
                } else {
                    Toast.makeText(MainActivity.this, "您没有开启蓝牙", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    //启动服务并绑定
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    if(mBluetoothLeService == null){
                        mBluetoothLeService = new BluetoothLeService();
                    }
                    connect(data);
                }
        }
    }

    private void connect(Intent intent) {
        //得到需要连接设备的地址
        mDeviceAddress = intent.getExtras().getString(DeviceListActivity.EXTRAS_DEVICE_ADDRESS);
        //连接蓝牙
        mBluetoothLeService.connect(mDeviceAddress);
    }



    private void setMessage(String str) {
        mToolbar.setSubtitle(str);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.open_bluetooth:
                if (!mBluetoothAdapter.isEnabled()) {
                    /*要请求启用蓝牙，请使用 ACTION_REQUEST_ENABLE */
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                         /*请求开启成功后，定义一个REQUEST_ENABLE_BT返回值，被回调*/
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(MainActivity.this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.secure_connect_scan:
                if(mBluetoothAdapter.isEnabled()){
                    if(mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                        Toast.makeText(MainActivity.this,"请先断开已有连接",Toast.LENGTH_SHORT).show();
                    }else {
                        Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    }
                }else {
                    Toast.makeText(MainActivity.this,"蓝牙没有开启",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close_blue_service:
                if(mBluetoothAdapter.isEnabled()){
                    if(mBluetoothLeService.getState() == BluetoothLeService.STATE_DISCONNECTED){
                        Toast.makeText(MainActivity.this,"已经断开连接了",Toast.LENGTH_SHORT).show();
                    }
                    if(mBluetoothLeService != null && mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                        AlertDialog.Builder close = new AlertDialog.Builder(MainActivity.this);
                        close.setTitle("温馨提示");
                        close.setMessage("您确定要断开蓝牙连接吗？");
                        close.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mBluetoothLeService.disconnect();
                            }
                        });
                        close.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        });
                        close.create().show();
                    }

                }else {
                    Toast.makeText(MainActivity.this,"蓝牙没有开启,不需要断开",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close_software:
            AlertDialog.Builder out = new AlertDialog.Builder(MainActivity.this);
            out.setTitle("温馨提示");
            out.setMessage("您确定要关闭软件吗？");
            out.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBluetoothAdapter.disable();
                    MainActivity.this.finish();
                }
            });
            out.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            out.create().show();
            break;
            case R.id.history:
                Intent intent = new Intent(this,QueryActivity.class);
                startActivity(intent);
        }
        return true;
    }
    public void byteToFloat2(byte[] b){
        if(b.length>0){
            compare = b.clone();//用来和下一次的数据比较
            float tem;
            int a = 0;
            int c =10;
            int d = 0;
            int h = 1;
            byte[] child;
            for (int i = 0; i <(b.length-10)/4 ; i++) { //前十个字节是不需要解析的，所以从第九位开始循环
                if(true){//如果从循环位加4大于字节数组的长度，则不能进行解析，
                    child = new byte[4];
                    System.arraycopy(b,c,child,0,4);
                    tem = Float.intBitsToFloat(bytesToInt(child));//将小的byte[]转换成float
                    tem=(float)(Math.round(tem*100))/100;//四舍五入
                    if(a<108){
                        mListTextView.get(a).setVisibility(View.VISIBLE);//让有数据的TextView显示
                        mListTextView.get(a).setText(tem+"");//以字符串的形式显示在TextView
                    }
                    a++;//还有数据，就让textview显示
                    d = a;//赋值，用来隐藏没有接收到数据的TextView
                }
                for (int j = d; j <mListTextView.size() ; j++) {
                    mListTextView.get(j).setVisibility(View.GONE);
                }
                c = c+4;//往后截取四个字节
            }
            if(a>=3){//a表示mListTextView目前含有的TextView数，一般是3的整数倍，因为每3个TextView表示一个通道的三个数据
                saveData(a);
            }
        }
        else {
            Toast.makeText(this,"返回的字节数是0",Toast.LENGTH_SHORT).show();
        }
    }

    private long firstTime;//用来定义再按一次退出的变量
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mDrawer.isDrawerOpen(mNavigationView)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (firstTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                System.exit(0);
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            }
            firstTime = System.currentTimeMillis();
        }
    }
    public void receiveFinish(){
        if(time_count>30){
            int c = 0;
            byte[] temp = new byte[count];//这里是否需要每一次都new，以后考虑
            //将集合里的多个byte[]对象转换成一个count长度的byte[]
            for (int i = 0; i < mList.size(); i++) {
                for (int j = 0; j < mList.get(i).length; j++) {
                    temp[c] = mList.get(i)[j];
                    c = c+1;
                }
            }
           // byte[] theLastResult = ArrayUtils.addAll(temp,mList.get(mList.size()-1));
           // Log.i("MainActivity-result---",theLastResult.length+"");
            //Log.i("MainActivity-finsh---",time_count+"");
            //Log.i("MainActivity-count---",count+"");
            //Log.i("MainActivity-mList---",mList.size()+"");
            //将得到的byte[]通过Message传出去
            Message message = mHandler.obtainMessage(RECEIVE_DATA, count, -1, temp);
            message.sendToTarget();
            mList.clear();
            count = 0;
            flag = false;
        }
    }

    /**保存数据
     * @param a 传递过来的数据个数
     */
    private void saveData(int a) {
        mBeanList.clear();//每次需要保存数据时，将期清空，防止上次残留的数据
        int h = 0,m = 0,n = 0,z=0;//用来从mListTextView集合中取出相应TextView的角标
        Date date = new Date();//定义时间
        mSimpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String t = mSimpleDateFormat.format(date);
        for (int i = 0; i < (a/3); i++) {//根据有多少TextView有数据来确定要生成多少个Bean，一般一个bean对应三个TextView
            Bean bean = new Bean();
            bean.setName("通道"+String.valueOf(i+1));//设置名字，数据库的查询是以名字来查询的
            z = m + n + h;
            bean.setData1(Float.parseFloat(mListTextView.get(z).getText().toString()));//设置数据1
            bean.setData2(Float.parseFloat(mListTextView.get(z+1).getText().toString()));//设置数据2
            bean.setData3(Float.parseFloat(mListTextView.get(z+2).getText().toString()));//设置数据3
            bean.setNow(date);//设置时间
            bean.setTimeDetail(t);//存放格式化的时间
            mBeanList.add(bean);
            h++;//让他们自增，算出来的角标刚好吻合
            m++;
            n++;

        }
         mCommUtils.insertMultBean(mBeanList);//批量插入数据
    }
    private void notice(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(tem <= 0){
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,"设备无响应",Toast.LENGTH_SHORT).show();
                }
            }
        },4000);

    }
}
