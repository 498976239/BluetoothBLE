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
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.www.bluetoothble.adapter.Main2Adapter;
import com.ss.www.bluetoothble.dbmanager.CommUtils;
import com.ss.www.bluetoothble.dialog.ModeDialog;
import com.ss.www.bluetoothble.dispaly.InfoData;
import com.ss.www.bluetoothble.entity.Bean;
import com.ss.www.bluetoothble.service.BluetoothLeService;
import com.ss.www.bluetoothble.utils.ArraysUtil;
import com.ss.www.bluetoothble.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String TAG = getClass().getSimpleName();
    public static final int RECEIVE_DATA = 4;
    public static final int PASS_INFORMATION = 5;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int CHANGE_FLAG = 6;
    private boolean connect_flag ;
    private int tem;
    //private UUID uuid;
    private int mCurrentAPIVersion;
    private int count_re_ask;//当校验未成功，自动请求次数
    private boolean count_re_ask_flag;//当校验未成功，自动请求次数
    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager locationManager;
    private List<byte[]> mList = new ArrayList<>();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mCharacteristic;
    private SimpleDateFormat mSimpleDateFormat;
    private CommUtils mCommUtils;
    private List<Bean> mBeanList;
    private List<String> mFloatData; //用来存放解析出来的float数据
    private byte[] compare;
    private RecyclerView mRecyclerView;
    private Main2Adapter mAdapter;
    private List<InfoData> mInfoDataList;
    private DrawerLayout mDrawer;
    private SwipeRefreshLayout mFresh;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private String mDeviceAddress;
    private int count;
    private boolean connecting_flag;
    private int connecting_time;
    private int time_count;
    private TextView mMode_name;
    private boolean flag;
    private boolean single;//因为两边都有注册广播，为了防止同时都收到广播，使用一个标志位
    private int mode_condition;//点击，模式选择的条件，有可能并没有切换成功
    private int mode_choose_ok;//模式切换成功后的数值
    private int test_condition;//可以进去调试界面的条件
    private boolean change_success;//模式切换成功标志
    private ModeDialog myModeDialog;
    private Timer mTimer;
    //定时执行的代码
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(flag == true){
                time_count++;//用来记录时间，单位ms
            }
            if(time_count >200 && flag == true){
                receiveFinish();//当大于200ms时表示没有数据发过来了，将数据进行处理
                LogUtil.i("main",TAG+"---检测数据发送完成");
                time_count = 0;
            }
            if(connecting_flag == true){
                connecting_time++ ;//正在连接的计时
            }
            if(connecting_time > 15000){
                connecting_flag = false;
                LogUtil.i("main--connecting_flag3",connecting_flag+"");
                mBluetoothLeService.disconnect();
                connecting_time = 0;
                Message message = Message.obtain();
                message.what = PASS_INFORMATION;
                mHandler.sendMessage(message);

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
                    if (!single){
                        tem = msg.arg1;
                        mFresh.setRefreshing(false);
                        byte[] data = (byte[]) msg.obj;
                        for (int j = 0; j < data.length; j++) {
                            LogUtil.i("main",TAG+"--data--"+j+"----"+data[j]);
                        }
                        byte[] result = new byte[msg.arg1-2];//除去校验位的数据
                        byte[] result_temp = new byte[msg.arg1];
                        System.arraycopy(data,0,result_temp,0,msg.arg1);
                        byte[] crc16 = new byte[2];
                        System.arraycopy(data,msg.arg1-2,crc16,0,2);//获取字节数组的CRC校验码，最后两个字节

                        for (int i = 0; i < crc16.length; i++) {
                            LogUtil.i("main",TAG+"-crc-"+crc16[i]);
                        }
                        System.arraycopy(data,0,result,0,msg.arg1-2);//获取除去CRC校验码之后的数据信息

                        byte[] myCRC =  intToByteArray(getCrc16(result));//生成CRC校验码,和获取的刚好相反
                        for (int j = 0; j < myCRC.length; j++) {
                            LogUtil.i("main",TAG+"-crc2-"+myCRC[j]);
                        }
                        if(myCRC[3]==crc16[0]&&myCRC[2]==crc16[1]){
                            count_re_ask_flag = false;
                            count_re_ask = 0;
                            boolean equals = Arrays.equals(result, compare);//判断两个数组是否相等
                            //测试模式
                            if (((result[0]&0xff)==1)&&((result[1]&0xff)==163)&&((result[2]&0xff)==250)&&((result[3]&0xff)==250)&&((result[4]&0xff)==0)&&((result[5]&0xff)==9)&&((result[6]&0xff)==127)) {
                                change_success = true;
                                if (mode_condition == 1){
                                    mMode_name.setText("调试模式");
                                    test_condition = 1;
                                    mode_choose_ok = 1;
                                    single = true;
                                }
                                if (mode_condition == 0){
                                    mMode_name.setText("普通模式");
                                    test_condition = 0;
                                    mode_choose_ok = 0;
                                }
                                mMode_name.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            }
                            if (((result[0]&0xff)==1)&&((result[1]&0xff)==163)&&((result[2]&0xff)==250)&&((result[3]&0xff)==250)&&((result[4]&0xff)==0)&&((result[5]&0xff)==9)&&((result[6]&0xff)==255)){
                                change_success = false;
                                Toast.makeText(Main2Activity.this,"切换失败",Toast.LENGTH_SHORT).show();
                                mMode_name.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            }
                            if(!equals){
                                if (result.length>20){
                                    byteToFloat2(result);
                                }

                            }else {
                                Toast.makeText(Main2Activity.this,"数据相同,请更新后采集",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(Main2Activity.this,"校验未通过，重新请求",Toast.LENGTH_SHORT).show();
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
                    }

                    break;
                case PASS_INFORMATION:
                    setMessage("连接未成功，请重试");
                    break;
                case CHANGE_FLAG:
                    change_success = false;
                    mMode_name.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(Main2Activity.this,"设备无响应，切换失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private IntentFilter intentFilter;
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
        setContentView(R.layout.activity_main2);
        initData();
        initViews();
        //获取本地蓝牙设备
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBeanList = new ArrayList<>();//用来存放要存储的Bean对象
        mCommUtils = new CommUtils(this);//操作数据库的工具类
        mCurrentAPIVersion = Build.VERSION.SDK_INT;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(mBluetoothLeService == null){
            mBluetoothLeService = new BluetoothLeService();
        }
        if(mBluetoothLeService.getState() == 0){
            mToolbar.setSubtitle("没有连接设备");
        }
        mTimer = new Timer();
        mTimer.schedule(mTimerTask,1,1);
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_UNDISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

    }

    private void initViews() {
        mFresh = (SwipeRefreshLayout) findViewById(R.id.main2_mFresh);
        mFresh.setColorSchemeColors(Color.BLUE);
        mRecyclerView = (RecyclerView) findViewById(R.id.main2_RecyclerView);
        mToolbar = (Toolbar) findViewById(R.id.mToolBar);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawer = (DrawerLayout) findViewById(R.id.mDrawer);
        mMode_name = (TextView) findViewById(R.id.mode_name);
        mProgressBar = (ProgressBar) findViewById(R.id.main2_ProgressBar);
        mAdapter = new Main2Adapter(this,mInfoDataList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mAdapter);
        alphaAdapter.setFirstOnly(false);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        scaleAdapter.setFirstOnly(false);
        mRecyclerView.setAdapter(scaleAdapter);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer,mToolbar, 0, 0);//显示左侧图标
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        mFresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mBluetoothAdapter.isEnabled()){
                    if (connect_flag == true){
                        tem = 0;
                        notice();
                        byte[] b = {0x01,(byte)0xa3,0xE,0xE,0,0x8};
                        byte[] orderCRC =  intToByteArray(getCrc16(b));
                        byte[] end = {0x01,(byte)0xa3,0xE,0xE,0,0x8,orderCRC[2],orderCRC[3]};
                        mCharacteristic.setValue(end);
                        mBluetoothLeService.writeCharacteristic(mCharacteristic);
                    }else {
                        mFresh.setRefreshing(false);
                        Toast.makeText(Main2Activity.this,"蓝牙已经断开，请重连",Toast.LENGTH_SHORT).show();
                    }
                }else {
                        mFresh.setRefreshing(false);
                        Toast.makeText(Main2Activity.this,"蓝牙没有打开",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void initData() {
        mInfoDataList = new ArrayList<>();
        mFloatData = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            InfoData infoData = new InfoData("通道"+(i+1),"0000.00","0000.00","0000.00");
            mInfoDataList.add(infoData);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        single = false;
        if (mBluetoothLeService == null){
            mBluetoothLeService = new BluetoothLeService();
        }
        registerReceiver(mBroadcastReceiver, intentFilter);
        if(mBluetoothLeService != null && mBluetoothAdapter.isEnabled()){
            if(mDeviceAddress != null)
                mBluetoothLeService.connect(mDeviceAddress);
        }

        if(mBluetoothLeService != null){
            int a = mBluetoothLeService.getState();
            if(a ==0){
                setMessage("没有连接设备");
            }
            if(a ==1){
                setMessage("正在连接设备");
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("main","调用了onDestroy()");
        unbindService(mServiceConnection);
        unregisterReceiver(mBroadcastReceiver);//一般解除广播是在 onPause()里的，但是为了防止黑屏就收不到广播，所以一到这里来了
        mBluetoothLeService = null;
        if(mTimer != null){
            mTimer.cancel();
        }
        time_count = 0;
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
                    Toast.makeText(Main2Activity.this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.secure_connect_scan:
                if (Build.VERSION.SDK_INT >= 25){
                    if(!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
                        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                        dialog.setMessage("打开GPS后，您才可以扫描并连接蓝牙设备");
                        dialog.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // 转到手机设置界面，用户设置GPS
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                            }
                        });
                        dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        } );
                        dialog.show();
                    }else {
                        if(mBluetoothAdapter.isEnabled()){
                            if(mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                                Toast.makeText(Main2Activity.this,"请先断开已有连接",Toast.LENGTH_SHORT).show();
                            }else {
                                Intent serverIntent = new Intent(Main2Activity.this, DeviceListActivity.class);
                                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                            }
                        }else {
                            Toast.makeText(Main2Activity.this,"蓝牙没有开启",Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    if(mBluetoothAdapter.isEnabled()){
                        if(mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                            Toast.makeText(Main2Activity.this,"请先断开已有连接",Toast.LENGTH_SHORT).show();
                        }else {
                            Intent serverIntent = new Intent(Main2Activity.this, DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                        }
                    }else {
                        Toast.makeText(Main2Activity.this,"蓝牙没有开启",Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.close_blue_service:
                if(mBluetoothAdapter.isEnabled()){
                    if(mBluetoothLeService.getState() == BluetoothLeService.STATE_DISCONNECTED){
                        Toast.makeText(Main2Activity.this,"已经断开连接了",Toast.LENGTH_SHORT).show();
                    }
                    if(mBluetoothLeService != null && mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                        AlertDialog.Builder close = new AlertDialog.Builder(Main2Activity.this);
                        close.setTitle("温馨提示");
                        close.setMessage("您确定要断开蓝牙连接吗？");
                        close.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mBluetoothLeService.disconnect();
                                connecting_flag = false;//用来断开“正在连接”延时
                                mDeviceAddress = null;
                                mode_choose_ok = 0;
                                change_success = false;
                                //mBluetoothLeService.close();
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
                    Toast.makeText(Main2Activity.this,"蓝牙没有开启,不需要断开",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close_software:
                AlertDialog.Builder out = new AlertDialog.Builder(Main2Activity.this);
                out.setTitle("温馨提示");
                out.setMessage("您确定要关闭软件吗？");
                out.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBluetoothLeService.disconnect();
                        mBluetoothLeService.close();
                        mBluetoothAdapter.disable();
                        // if (locationManager != null){
                        //Toast.makeText(MainActivity.this,"我走了",Toast.LENGTH_SHORT).show();
                        //locationManager.clearTestProviderLocation(android.location.LocationManager.GPS_PROVIDER);
                        // Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, android.provider.Settings.Secure.LOCATION_MODE_OFF);
                        // locationManager = null;
                        // }
                        Main2Activity.this.finish();
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
                break;
            case R.id.mode_choose:
                if(mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED){
                    myModeDialog = new ModeDialog(this, mode_choose_ok,new ModeDialog.Passinfo() {
                        @Override
                        public void passMode(int s,byte[] b) {
                            mode_condition = s;
                            LogUtil.i("main","mode_condition: "+mode_condition);
                            //mMode_name.setVisibility(View.GONE);
                            if (mode_condition == 0){
                                single = false;
                            }
                            mProgressBar.setVisibility(View.VISIBLE);
                            mCharacteristic.setValue(b);
                            mBluetoothLeService.writeCharacteristic(mCharacteristic);
                            change_success = false;
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (change_success != true){
                                        Message message = Message.obtain();
                                        message.what = CHANGE_FLAG;
                                        mHandler.sendMessage(message);
                                    }
                                }
                            },4500);
                        }
                    });
                    myModeDialog.show();
                }else {
                    Toast.makeText(this,"蓝牙连接成功后才可切换",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.just_one:
                if (test_condition == 1){
                    Intent intent1 = new Intent(Main2Activity.this,TestActivity.class);
                    startActivity(intent1);
                }else {
                    Toast.makeText(this,"切换模式后才能进入",Toast.LENGTH_SHORT).show();
                }

        }
        return true;
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
                connecting_flag = false;//用来断开“正在连接”延时
                LogUtil.i("main--connecting_flag2",connecting_flag+"");
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    /*if (mDeviceAddress != null){
                        connect_flag = false;
                        mDeviceAddress = null;//防止自动去连接
                    }*/
                // mBluetoothLeService.disconnect();
                setMessage("连接断开");
                connect_flag = false;
                connecting_flag = false;//用来断开“正在连接”延时

            }
            if(BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)){
                setMessage("正在连接...");

            }
            if(BluetoothLeService.ACTION_GATT_DISCONNECTING.equals(action)){
                setMessage("正在断开...");
            }
            if(BluetoothLeService.ACTION_GATT_SERVICES_UNDISCOVERED.equals(action)){
                setMessage("服务未找到，请重试");
            }
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
               // LogUtil.i("MainActivity----","---------------------------------");
                BluetoothGattService service = mBluetoothLeService.getSupportedGattServices();
                UUID uuid = service.getUuid();//这里的到的UUID是service的，所有不需要使用
                //LogUtil.i("MainActivity----",uuid.toString());
                mCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                int charaProp = mCharacteristic.getProperties();//得到此Characteristic的属性
                if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){//读取属性
                    mBluetoothLeService.readCharacteristic(mCharacteristic);
                }
                if((charaProp | BluetoothGattCharacteristic.PERMISSION_WRITE) > 0){//写入属性
                    ///mCharacteristic.setValue(mSend);
                    //mBluetoothLeService.writeCharacteristic(mCharacteristic);
                }
                //当要读取从设备的数据时，需要设置这个通知Notification为true。
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){//支持通知属性
                    mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
                }

            }
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                flag = true;
                time_count = 0;
                byte[] result = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                mList.add(result);
                //Log.i("MainActivity-mList---",mList.size()+"");
                //Log.i("MainActivity-flag---",flag+"");
                //Log.i("MainActivity-c---",c+"");
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
                    Toast.makeText(Main2Activity.this, "您没有开启蓝牙", Toast.LENGTH_SHORT).show();
                    Main2Activity.this.finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    //启动服务并绑定
                    setMessage("正在连接...");
                    connecting_flag = true;
                    LogUtil.i("main--connecting_flag",connecting_flag+"");
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    if(mBluetoothLeService == null){
                        mBluetoothLeService = new BluetoothLeService();
                    }
                    connect(data);
                }
                break;
        }
    }

    private void connect(Intent intent) {
        //得到需要连接设备的地址
        mDeviceAddress = intent.getExtras().getString(DeviceListActivity.EXTRAS_DEVICE_ADDRESS);
        //连接蓝牙
        mBluetoothLeService.connect(mDeviceAddress);
    }

    public void byteToFloat2(byte[] b){
        if(b.length>0){
            mFloatData.clear();
            mInfoDataList.clear();
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
                    if(tem > 600f){
                        tem=(float)(Math.round(tem*100))/100;//四舍五入
                    }else{
                        tem=(float)(Math.round(tem*100000))/100000;//四舍五入
                    }
                    mFloatData.add(tem+"");
                }
                c = c+4;//往后截取四个字节
            }
            int num = 0;
            for (int i = 0; i < mFloatData.size()/3; i++) {
                InfoData infoData = new InfoData("通道"+(i+1),mFloatData.get(num),mFloatData.get(num+1),mFloatData.get(num+2));
                mInfoDataList.add(infoData);
                if (num < mFloatData.size()){
                    num = num + 3;
                }else {
                    num = 0;
                }
            }
            InfoData rainInfo = new InfoData("雨量",(b[b.length-2]&0xff)+"",((float)(b[b.length-1]&0xff))/10+"","0.0");
            mInfoDataList.add(rainInfo);
            mAdapter.notifyDataSetChanged();
            if(mFloatData.size()>=3){//mFloatData.size()表示目前含有的数据量，一般是3的整数倍，因为每3个数据表示一个通道的三个数据
                saveData(mFloatData.size());
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
            LogUtil.i("MainActivity-mList---",mList.size()+"");
            //将得到的byte[]通过Message传出去
            Message message = mHandler.obtainMessage(RECEIVE_DATA, count, -1, temp);
           /* for (int i = 0; i < temp.length; i++) {
                LogUtil.i("main",getClass().getSimpleName()+"---"+i+"---"+(temp[i]&0xff));
            }*/
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
        /*for (int i = 0; i < (a/3); i++) {//根据有多少TextView有数据来确定要生成多少个Bean，一般一个bean对应三个TextView
            Bean bean = new Bean();
            bean.setName("通道"+String.valueOf(i+1));//设置名字，数据库的查询是以名字来查询的
            z = m + n + h;
            bean.setData1(Float.parseFloat(mFloatData.get(z)));//设置数据1
            bean.setData2(Float.parseFloat(mFloatData.get(z+1)));//设置数据2
            bean.setData3(Float.parseFloat(mFloatData.get(z+2)));//设置数据3
            bean.setNow(date);//设置时间
            bean.setTimeDetail(t);//存放格式化的时间
            mBeanList.add(bean);
            h++;//让他们自增，算出来的角标刚好吻合
            m++;
            n++;

        }*/
        for (int i = 0; i < mInfoDataList.size(); i++) {
            Bean bean = new Bean();
            bean.setName(mInfoDataList.get(i).getName());
            bean.setData1(Float.parseFloat(mInfoDataList.get(i).getData1()));//设置数据1
            bean.setData2(Float.parseFloat(mInfoDataList.get(i).getData2()));//设置数据2
            bean.setData3(Float.parseFloat(mInfoDataList.get(i).getData3()));//设置数据3
            bean.setNow(date);//设置时间
            bean.setTimeDetail(t);//存放格式化的时间
            mBeanList.add(bean);
        }
        mCommUtils.insertMultBean(mBeanList);//批量插入数据
    }
    private void notice(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(tem <= 0){
                    mFresh.setRefreshing(false);
                    Toast.makeText(Main2Activity.this,"设备无响应",Toast.LENGTH_SHORT).show();
                }
            }
        },4000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i("main",getClass().getSimpleName()+"---onStop");

    }
}
