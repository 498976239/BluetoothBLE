package com.ss.www.bluetoothble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.www.bluetoothble.dispaly.InfoData;
import com.ss.www.bluetoothble.service.BluetoothLeService;
import com.ss.www.bluetoothble.utils.ArraysUtil;
import com.ss.www.bluetoothble.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class TestActivity extends AppCompatActivity {
    private byte channal_number;//通道编号
    private static final int ERROR = 1;
    private static final int CHANGE_BACKGOUND = 2;
    private ProgressBar mProgressBar;
    private LineChartView lineChart;
    private LineChartData mLineChartData;
    private List<PointValue> mPointValues ;
    private List<Line> mLines;
    private List<Float> mFloat;
    private Axis axisX;
    private Axis axisY ;
    private Line mLine;
    private List<InfoData> mList;
    private float high_limit;
    private float low_limit;
    private int position;//用来记录X坐标的位置
    private int a;//用来标识是否是动态曲线
    private List<AxisValue> mAxisXValues ;
    private List<AxisValue> AxisYValues;
    private int point_count;//用来记录曲线上的点的个数
    private TextView tv_data1;
    private TextView tv_data2;
    private TextView tv_data3;
    private TextView mText1;
    private TextView mText2;
    private boolean firstEnter;
    private TextView mText3;
    private TextView mText4;
    private float[] data_result = new float[3];//用来存放三个需要显示的数据
    private byte[] temp_bytes = new byte[16];//用来比较数据是否相同
    private TextView tv_name;
    private boolean show_ok;//请求后收到数据标志
    private int wrong_count;
    private EditText mEditText;
    private TextView wrong_info;
    private TextView count;
    private Button btn_last;
    private Button btn_next;
    private CardView cv1,cv2,cv3;
    private Button btn_sure;
    private Toolbar mToolbar;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ERROR:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(TestActivity.this,"设备无响应",Toast.LENGTH_SHORT).show();
                    break;
                case CHANGE_BACKGOUND:
                    //蓝色
                    cv1.setCardBackgroundColor(0xFF67A7F4);
                    cv2.setCardBackgroundColor(0xFF67A7F4);
                    cv3.setCardBackgroundColor(0xFF67A7F4);
                    break;
            }
        }
    };
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mCharacteristic;
    private IntentFilter intentFilter;
    private final String TAG = getClass().getSimpleName();
    //管理服务生命周期的代码
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("main",TAG+"---onCreate");
        setContentView(R.layout.activity_test);
        initViews();
        //mToolbar.setSubtitle("通道"+channal_number+"正在采集");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("测试界面");
        mList = new ArrayList<>();
        mPointValues = new ArrayList<>();
        mLines = new ArrayList<>();
        mAxisXValues = new ArrayList();
        AxisYValues = new ArrayList<>();
        mFloat = new ArrayList<>();
        axisX = new Axis();
        axisY = new Axis();
        mLine = new Line();
        if(mBluetoothLeService == null){
            mBluetoothLeService = new BluetoothLeService();
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //蓝色
                cv1.setCardBackgroundColor(0xFF67A7F4);
                cv2.setCardBackgroundColor(0xFF67A7F4);
                cv3.setCardBackgroundColor(0xFF67A7F4);
                show_ok = false;
                firstEnter = true;//表示已经操作过了，不是第一次进来
                a = 0;
                point_count = 0;
                position = 0;
                if (mList!=null){
                    mList.clear();
                }
                if (mPointValues!=null){
                    mPointValues.clear();
                }
                count.setText("已采集 "+mPointValues.size()+" 点");
                initLine3();//用来清空曲线上的点
                wrong_info.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                if (mEditText.getText().toString()!=null&&mEditText.getText().toString().length() >0&&mEditText.getText().toString().length() < 3){
                    channal_number = Byte.parseByte(mEditText.getText().toString());
                }
                byte[] b = {0x01,(byte)0xa3,(byte)0xfa,(byte)0xfa,0,0x10,85,channal_number};
                byte[] orderCRC = ArraysUtil.intToByteArray(ArraysUtil.getCrc16(b));
                byte[] end = new byte[]{0x01, (byte) 0xa3, (byte)0xfa,(byte)0xfa,0,0x10,85,channal_number,orderCRC[2], orderCRC[3]};
                BluetoothGattService service = mBluetoothLeService.getSupportedGattServices();
                mCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                mCharacteristic.setValue(end);
                mBluetoothLeService.writeCharacteristic(mCharacteristic);
                tv_name.setText("通道"+(channal_number&0xff));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (show_ok == false){
                            Message message = Message.obtain();
                            message.what = ERROR;
                            mHandler.sendMessage(message);
                        }
                    }
                },4500);
            }
        });
        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().toString()!=null){
                    mEditText.setText("");
                }
                if (channal_number <= 1){
                    channal_number = 96;
                }else {
                    channal_number--;

                }
                tv_name.setText("通道"+(channal_number&0xff));
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().toString()!=null){
                    mEditText.setText("");
                }
                if (channal_number >= 96){
                    channal_number =1;
                }else {
                    channal_number++;
                }
                tv_name.setText("通道"+(channal_number&0xff));

            }
        });
        lineChart.setClickable(true);
        lineChart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                a++;
                if (a==1){
                    if (mPointValues.size()>0){
                        initLine2();
                        float[] data = new float[mPointValues.size()];
                        float average = 0;
                        for (int i = 0; i < mPointValues.size(); i++) {
                            data[i] = mPointValues.get(i).getY();
                            average = data[i] + average;
                        }
                        mText1.setVisibility(View.VISIBLE);
                        mText2.setVisibility(View.VISIBLE);
                        mText3.setVisibility(View.VISIBLE);
                        mText4.setVisibility(View.VISIBLE);
                        mText1.setText("最大值:"+ArraysUtil.getMax(data)+" ");
                        mText2.setText("最小值:"+ArraysUtil.getMin(data)+" ");
                        mText3.setText("平均值:"+((float)(Math.round((average/mPointValues.size())*100))/100)+" ");
                        mText4.setText("差值:"+((float)(Math.round((ArraysUtil.getMax(data)-ArraysUtil.getMin(data))*100))/100)+" ");
                    }

                }
                if (a==2){
                    a=0;
                    mText1.setVisibility(View.GONE);
                    mText2.setVisibility(View.GONE);
                    mText3.setVisibility(View.GONE);
                    mText4.setVisibility(View.GONE);
                    initLine();
                }
                Log.i("main","change----"+a);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i("main",TAG+"---onResume");
        if (show_ok==true){
            mToolbar.setSubtitle("通道"+channal_number+"正在采集");
        }
        registerReceiver(mBroadcastReceiver, intentFilter);//注册广播
        //绑定服务
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("main",TAG+"---onDestroy");
        unregisterReceiver(mBroadcastReceiver);//注销广播
        unbindService(mServiceConnection);//解绑服务

    }

    private void initViews() {
        tv_data1 = (TextView) findViewById(R.id.test_data1);
        tv_data2 = (TextView) findViewById(R.id.test_data2);
        tv_data3 = (TextView) findViewById(R.id.test_data3);
        tv_name = (TextView) findViewById(R.id.test_text_name);
        wrong_info = (TextView) findViewById(R.id.wrong_info);
        mEditText = (EditText) findViewById(R.id.test_edit);
        btn_last = (Button) findViewById(R.id.test_last_btn);
        btn_next = (Button) findViewById(R.id.test_next_btn);
        btn_sure = (Button) findViewById(R.id.test_sure_btn);
        mToolbar = (Toolbar) findViewById(R.id.text_ToolBar);
        mProgressBar = (ProgressBar) findViewById(R.id.test_ProgressBar);
        cv1 = (CardView) findViewById(R.id.test_card1);
        cv2 = (CardView) findViewById(R.id.test_card2);
        cv3 = (CardView) findViewById(R.id.test_card3);
        lineChart = (LineChartView) findViewById(R.id.test_chartView);
        mText1 = (TextView) findViewById(R.id.test_max);
        mText2 = (TextView) findViewById(R.id.test_min);
        mText3 = (TextView) findViewById(R.id.test_average);
        mText4 = (TextView) findViewById(R.id.test_decrease);
        count = (TextView) findViewById(R.id.test_count);

    }
         
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                byte[] result = new byte[data.length-2];//除去校验位的数据
                System.arraycopy(data,0,result,0,data.length-2);//获取除去CRC校验码之后的数据信息
                byte[] myCRC =  ArraysUtil.intToByteArray(ArraysUtil.getCrc16(result));//生成CRC校验码,和获取的刚好相反
                LogUtil.i("main",TAG+"--myCRC[3]--"+myCRC[3]);
                LogUtil.i("main",TAG+"--data[data.length-1]--"+data[data.length-1]);
                LogUtil.i("main",TAG+"--myCRC[2]--"+myCRC[2]);
                LogUtil.i("main",TAG+"--data[data.length-2]--"+data[data.length-2]);
                if(myCRC[3]==data[data.length-2]&&myCRC[2]==data[data.length-1]){
                    show_ok = true;
                    mProgressBar.setVisibility(View.GONE);
                    if (result.length == 7){
                        if (((result[0]&0xff)==1)&&((result[1]&0xff)==163)&&((result[2]&0xff)==250)&&((result[3]&0xff)==250)&&((result[4]&0xff)==0)&&((result[5]&0xff)==9)&&((result[6]&0xff)==127)){
                            show_ok = true;
                            mToolbar.setSubtitle("通道"+channal_number+"正在采集");
                            mProgressBar.setVisibility(View.GONE);
                            tv_data1.setText("0000.00");
                            tv_data2.setText("0000.00");
                            tv_data3.setText("0000.00");
                        }
                        if (((result[0]&0xff)==1)&&((result[1]&0xff)==163)&&((result[2]&0xff)==250)&&((result[3]&0xff)==250)&&((result[4]&0xff)==0)&&((result[5]&0xff)==9)&&((result[6]&0xff)==255)){
                            show_ok = true;
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(TestActivity.this,"请求失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (result.length == 14){
                        LogUtil.i("main",TAG+"--equals--"+Arrays.equals(data,temp_bytes));
                        if ((result[10]==-1)&&(result[11]==-1)&&(result[12]==-1)&&(result[13]==-1)){
                            wrong_count ++;
                            if (wrong_count >= 3){
                                wrong_info.setVisibility(View.VISIBLE);
                                tv_data1.setText("0000.00");
                                tv_data2.setText("0000.00");
                                tv_data3.setText("0000.00");
                                //红色
                                cv1.setCardBackgroundColor(0xFFF70404);
                                cv2.setCardBackgroundColor(0xFFF70404);
                                cv3.setCardBackgroundColor(0xFFF70404);
                            }
                        }else {
                            wrong_count = 0;
                            wrong_info.setVisibility(View.GONE);
                            //绿色
                            cv1.setCardBackgroundColor(0xFF14CE0A);
                            cv2.setCardBackgroundColor(0xFF14CE0A);
                            cv3.setCardBackgroundColor(0xFF14CE0A);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Message message = Message.obtain();
                                    message.what = CHANGE_BACKGOUND;
                                    mHandler.sendMessage(message);
                                }
                            },500);
                            int c = 2;
                            float tem;
                            byte[] child;
                            channal_number = (byte)(result[1]&0xff);
                            LogUtil.i("main",TAG+"channal_number----------------"+channal_number);
                            if (!firstEnter){
                                mToolbar.setSubtitle("通道"+channal_number+"正在采集");
                                tv_name.setText("通道"+(channal_number&0xff));
                            }
                            for (int i = 0; i < (result.length - 2)/4; i++) {
                                child = new byte[4];
                                System.arraycopy(result,c,child,0,4);
                                tem = Float.intBitsToFloat(ArraysUtil.bytesToInt(child));//将小的byte[]转换成float
                                if(tem > 600f){
                                    tem=(float)(Math.round(tem*100))/100;//四舍五入
                                }else{
                                    tem=(float)(Math.round(tem*100000))/100000;//四舍五入
                                }
                                data_result[i] =  tem;
                                c = c+4;
                            }
                            for (int i = 0; i < data_result.length; i++) {
                                LogUtil.i("main",TAG+"--data_result--"+data_result[i]);
                            }
                            InfoData infoData = new InfoData();//用来做曲线显示的
                            infoData.setData1(data_result[0]+"");
                            infoData.setData1(data_result[1]+"");
                            mList.add(infoData);
                            //曲线上的点
                            mPointValues.add(new PointValue(point_count,data_result[0]));
                            count.setText("已采集 "+mPointValues.size()+" 点");
                            point_count++;//曲线上的点个数累积
                            float[] data2 = new float[mPointValues.size()];
                            for (int i = 0; i < mPointValues.size(); i++) {
                                data2[i] = mPointValues.get(i).getY();
                            }
                            high_limit = ArraysUtil.getMax(data2);
                            low_limit = ArraysUtil.getMin(data2);
                            if (a==0){
                                //lineChart.setVisibility(View.VISIBLE);
                                initLine();
                            }
                            tv_data1.setText(data_result[0]+"");
                            tv_data2.setText(data_result[1]+"");
                            tv_data3.setText(data_result[2]+"");
                        }
                    }
                }else {
                    show_ok = true;
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(TestActivity.this,"校验未通过",Toast.LENGTH_SHORT).show();
                }
                LogUtil.i("main",TAG+"--result.length--"+result.length);
                for (int i = 0; i <result.length ; i++) {
                    LogUtil.i("main",TAG+"--result--"+(result[i]&0xff));
                }

            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
//动态变化的曲线
    private void initLine() {
        mLines.clear();
        mLine.setValues(mPointValues);
        mLine.setColor(Color.BLUE);
        mLine.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        mLine.setCubic(true);//选择是折线（false）还是曲线（true）
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(2);//设置小数点
        mLine.setFormatter(chartValueFormatter);
        AxisValueFormatter axisValueFormatter = new SimpleAxisValueFormatter(2);//设置Y轴小数点
        axisY.setFormatter(axisValueFormatter);
        axisY.setHasSeparationLine(true);
        axisY.setHasLines(true);
        mLine.setHasLines(true);//是否显示线
        mLine.setPointRadius(2);//点的半径
        //mLine.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效
        mLine.setHasLabels(true);//曲线的数据坐标是否加上备注
        mLine.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        mLines.add(mLine);
        mLineChartData = new LineChartData();
        mLineChartData.setLines(mLines);
        mLineChartData.setValueLabelTextSize(10);//设置标签文字字号，默认为12sp
        mLineChartData.setValueLabelBackgroundEnabled(false);//设置是否显示标签的背景
        mLineChartData.setValueLabelsTextColor(Color.parseColor("#FF4081"));
        //lineChart.setScaleY(0.5f);//视图的缩放宽度。值为1意味着不使用缩放。
        lineChart.setInteractive(true);//设置该图表是否可交互。如不可交互，则图表不会响应缩放、滑动、选择或点击等操作。默认值为true，可交互。
        lineChart.setZoomType(ZoomType.HORIZONTAL);//设置缩放类型，可选的类型包括ZoomType.HORIZONTAL_AND_VERTICAL, ZoomType.HORIZONTAL, ZoomType.VERTICAL，默认值为HORIZONTAL_AND_VERTICAL。
        lineChart.setZoomEnabled(true);//设置是否可缩放。
        lineChart.setScrollEnabled(true);
        lineChart.setMaxZoom((float) 20);//设置最大缩放比例。默认值20。
        lineChart.setSaveFromParentEnabled(true);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setLineChartData(mLineChartData);
        if (a==0){
            float xAxisValue = mPointValues.get(position).getX();
            Viewport port = new Viewport();
            port.top = high_limit+5;//Y轴上限，固定(不固定上下限的话，Y轴坐标值可自适应变化)
            port.bottom = low_limit-5;//Y轴下限，固定
            if (xAxisValue > 5){
                port.left = xAxisValue-5;//X轴左边界，变化
                port.right = xAxisValue;//X轴右边界，变化
                //port = initViewPort(0,mPointValues.size());
            }
            else {
                port.left = 0;//X轴左边界，变化
                port.right = 15;//X轴右边界，变化
            }
            lineChart.setMaximumViewport(port);
            lineChart.setCurrentViewport(port);
            if (position < mPointValues.size()-1)
                position++;

        }
    }

    private void initLine2(){
        mLines.clear();
        mLine.setValues(mPointValues);
        mLine.setColor(Color.BLUE);
        mLine.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        mLine.setCubic(true);//选择是折线（false）还是曲线（true）
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(2);//设置小数点
        mLine.setFormatter(chartValueFormatter);
        AxisValueFormatter axisValueFormatter = new SimpleAxisValueFormatter(2);//设置Y轴小数点
        axisY.setFormatter(axisValueFormatter);
        axisY.setHasSeparationLine(true);
        axisY.setHasLines(true);
        //axisY.setInside(true);
        //axisY.setTypeface();
        mLine.setHasLines(true);//是否显示线
        //line.setStrokeWidth(4);//：线的粗细
        mLine.setPointRadius(2);//点的半径
        //mLine.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效
        mLine.setHasLabels(false);//曲线的数据坐标是否加上备注
        mLine.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        mLines.add(mLine);
        mLineChartData = new LineChartData();
        mLineChartData.setLines(mLines);
        mLineChartData.setValueLabelTextSize(10);//设置标签文字字号，默认为12sp
        mLineChartData.setValueLabelBackgroundEnabled(false);//设置是否显示标签的背景
        mLineChartData.setValueLabelsTextColor(Color.parseColor("#FF4081"));
        //lineChart.setScaleY(0.5f);//视图的缩放宽度。值为1意味着不使用缩放。
        lineChart.setInteractive(true);//设置该图表是否可交互。如不可交互，则图表不会响应缩放、滑动、选择或点击等操作。默认值为true，可交互。
        lineChart.setZoomType(ZoomType.HORIZONTAL);//设置缩放类型，可选的类型包括ZoomType.HORIZONTAL_AND_VERTICAL, ZoomType.HORIZONTAL, ZoomType.VERTICAL，默认值为HORIZONTAL_AND_VERTICAL。
        lineChart.setZoomEnabled(true);//设置是否可缩放。
        lineChart.setScrollEnabled(true);
        lineChart.setMaxZoom((float) 20);//设置最大缩放比例。默认值20。
        lineChart.setSaveFromParentEnabled(true);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setLineChartData(mLineChartData);
        Viewport port = new Viewport();
        port.top = high_limit+5;//Y轴上限，固定(不固定上下限的话，Y轴坐标值可自适应变化)
        port.bottom = low_limit-5;//Y轴下限，固定
        port.left = 0;//X轴左边界，变化
        port.right = mPointValues.size()-1;//X轴右边界，变化
        lineChart.setMaximumViewport(port);
        lineChart.setCurrentViewport(port);
    }
    private void initLine3(){
        mLines.clear();
        mLine.setValues(mPointValues);
        mLine.setColor(Color.BLUE);
        mLine.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        mLine.setCubic(true);//选择是折线（false）还是曲线（true）
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(2);//设置小数点
        mLine.setFormatter(chartValueFormatter);
        AxisValueFormatter axisValueFormatter = new SimpleAxisValueFormatter(2);//设置Y轴小数点
        axisY.setFormatter(axisValueFormatter);
        axisY.setHasSeparationLine(true);
        axisY.setHasLines(true);
        //axisY.setInside(true);
        //axisY.setTypeface();
        mLine.setHasLines(true);//是否显示线
        //line.setStrokeWidth(4);//：线的粗细
        mLine.setPointRadius(2);//点的半径
        //mLine.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效
        mLine.setHasLabels(false);//曲线的数据坐标是否加上备注
        mLine.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        mLines.add(mLine);
        mLineChartData = new LineChartData();
        mLineChartData.setLines(mLines);
        mLineChartData.setValueLabelTextSize(10);//设置标签文字字号，默认为12sp
        mLineChartData.setValueLabelBackgroundEnabled(false);//设置是否显示标签的背景
        mLineChartData.setValueLabelsTextColor(Color.parseColor("#FF4081"));
        //lineChart.setScaleY(0.5f);//视图的缩放宽度。值为1意味着不使用缩放。
        lineChart.setInteractive(true);//设置该图表是否可交互。如不可交互，则图表不会响应缩放、滑动、选择或点击等操作。默认值为true，可交互。
        lineChart.setZoomType(ZoomType.HORIZONTAL);//设置缩放类型，可选的类型包括ZoomType.HORIZONTAL_AND_VERTICAL, ZoomType.HORIZONTAL, ZoomType.VERTICAL，默认值为HORIZONTAL_AND_VERTICAL。
        lineChart.setZoomEnabled(true);//设置是否可缩放。
        lineChart.setScrollEnabled(true);
        lineChart.setMaxZoom((float) 20);//设置最大缩放比例。默认值20。
        lineChart.setSaveFromParentEnabled(true);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setLineChartData(mLineChartData);
        Viewport port = new Viewport();
        port.top = 5;//Y轴上限，固定(不固定上下限的话，Y轴坐标值可自适应变化)
        port.bottom = 5;//Y轴下限，固定
        port.left = 0;//X轴左边界，变化
        port.right = 10;//X轴右边界，变化
        lineChart.setMaximumViewport(port);
        lineChart.setCurrentViewport(port);
    }

}
