package com.ss.www.bluetoothble.service;
//BLE是采用Service在后台运行和activity进行通讯，当有数据改变的时候，就会发送广播给activity
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ss.www.bluetoothble.MainActivity;
import com.ss.www.bluetoothble.R;
import com.ss.www.bluetoothble.utils.LogUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private BluetoothGatt mBluetoothGatt;//蓝牙协议，发现蓝牙服务，根据特征值处理数据交互
    private BluetoothManager mBluetoothManager;//用来获取蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器，处理系统蓝牙是否打开，搜索设备
    private String mBluetoothDeviceAddress;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_ERROR = 4;
    private int mConnectionState = STATE_DISCONNECTED;
    public final static String ACTION_GATT_CONNECTED = "com.ss.www.bluetoothble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.ss.www.bluetoothble.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING = "com.ss.www.bluetoothble.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTING = "com.ss.www.bluetoothble.ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.ss.www.bluetoothble.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_SERVICES_UNDISCOVERED = "com.ss.www.bluetoothble.ACTION_GATT_SERVICES_UNDISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.ss.www.bluetoothble.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.ss.www.bluetoothble.EXTRA_DATA";
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                LogUtil.i("Main--Service", "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                broadcastUpdate(intentAction);
            }
            if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
            if(newState == BluetoothProfile.STATE_CONNECTING){
                intentAction = ACTION_GATT_CONNECTING;
                mConnectionState = STATE_CONNECTING;
                broadcastUpdate(intentAction);
            }
            if(newState == BluetoothProfile.STATE_DISCONNECTING){
                intentAction = ACTION_GATT_DISCONNECTING;
                mConnectionState = STATE_DISCONNECTING;
                broadcastUpdate(intentAction);
            }
        }
//当发现服务之后，发送广播，在activity里面配置好可以读取数据的权限
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }else{
                broadcastUpdate(ACTION_GATT_SERVICES_UNDISCOVERED);
                LogUtil.i("main----","没有找到服务");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // stringBuilder.setLength(0);
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };
//封装的用来发送广播的方法
    private void broadcastUpdate(String intentAction) {
        Intent intent = new Intent(intentAction);
        sendBroadcast(intent);
    }
    //封装的用来发送广播的重载方法
    private void broadcastUpdate(String intentAction,BluetoothGattCharacteristic characteristic){
        Intent intent = new Intent(intentAction);
        byte[] data = characteristic.getValue();
        //byte[] temp_data = Arrays.copyOf(data,data.length);
        //byte[] theLastResult = ArrayUtils.addAll(temp_data);
        LogUtil.i("main--count",data.length+"");
        intent.putExtra(EXTRA_DATA,data);
       // intent.putExtra(EXTRA_DATA,stringBuilder.toString());
        //StringBuilder stringBuilder = new StringBuilder(data.length);
        // for(byte byteChar : data)
        //stringBuilder.append(byteChar);
        //intent.putExtra(EXTRA_DATA,stringBuilder.toString());
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }

        return true;
    }

    /**连接蓝牙是否成功
     * @param address
     * @return
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        // Previously connected device.  Try to reconnect. (先前连接的设备。 尝试重新连接),不知道要不要使用，暂时关闭吧
        //屏蔽之后，后台运行，再切回控制界面，连接断开
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTED;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }//连接前先关闭之前 的
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     *
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mConnectionState = STATE_DISCONNECTED;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mConnectionState = STATE_DISCONNECTED;
    }
    public int getState(){
        return mConnectionState;
    }

    /**
     * @param characteristic
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }
    public BluetoothGattService getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder builder = new Notification.Builder(this);
        /*PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        builder.setContentIntent(contentIntent);*/
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("Foreground Service Start");
        builder.setContentTitle("Foreground Service");
        builder.setContentText("正在采集...");
        Notification notification = builder.build();
        startForeground(1, notification);
    }
}

