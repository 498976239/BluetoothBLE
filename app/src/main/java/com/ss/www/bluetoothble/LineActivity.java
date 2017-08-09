package com.ss.www.bluetoothble;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.ss.www.bluetoothble.entity.Bean;
import com.ss.www.bluetoothble.utils.ArraysUtil;
import com.ss.www.bluetoothble.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
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

public class LineActivity extends AppCompatActivity {
    private List<Bean> mList ;
    private RadioGroup line_mRadioGroup;
    private float[] data1;
    private float[] data2;
    private float[] data3;
    private Toolbar mToolbar;
    private LineChartView lineChart;
    private Line mLine;
    private  Axis axisX;
    private Axis axisY ;
    private LineChartData mLineChartData;
    private List<PointValue> mPointValues ;
    private List<Line> mLines;
    private List<AxisValue> mAxisXValues ;
    private List<AxisValue> AxisYValues;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        mList = new ArrayList<>();
        mPointValues = new ArrayList<>();
        mLines = new ArrayList<>();
        mAxisXValues = new ArrayList();
        AxisYValues = new ArrayList<>();
        mLine = new Line();
        axisX = new Axis();
        axisY = new Axis();
        mToolbar = (Toolbar) findViewById(R.id.lineToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("历史曲线");
        lineChart = (LineChartView) findViewById(R.id.chart);
        mLineChartData = new LineChartData();
        line_mRadioGroup = (RadioGroup) findViewById(R.id.rg_line);
        mList = (List<Bean>) getIntent().getSerializableExtra(QueryActivity.MY_DATA);//拿到传过来的数据
        data1 = new float[mList.size()];
        data2 = new float[mList.size()];
        data3 = new float[mList.size()];
        for (int i = 0; i < mList.size(); i++) {
            data1[i] = mList.get(i).getData1();
            data2[i] = mList.get(i).getData2();
            data3[i] = mList.get(i).getData3();
        }
        LogUtil.i("main--data1-max",ArraysUtil.getMax(data1)+"");
        LogUtil.i("main--data1-min",ArraysUtil.getMin(data1)+"");
        LogUtil.i("main--data2-max",ArraysUtil.getMax(data2)+"");
        LogUtil.i("main--data2-min",ArraysUtil.getMin(data2)+"");
        LogUtil.i("main--data3-max",ArraysUtil.getMax(data3)+"");
        LogUtil.i("main--data3-min",ArraysUtil.getMin(data3)+"");
        line_mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.line_choose_data1:
                        clear();
                        initAxis();
                        //得到X坐标的标号
                        getAxisXLables(mList);
                       //得到Y轴的标号
                        for(int i = 0; i < mList.size(); i++){
                            AxisValue value = new AxisValue(mList.get(i).getData1());
                            AxisYValues.add(value);
                        }
                        axisY.setValues(AxisYValues);
                        mLineChartData.setAxisYLeft(axisY);
                       //得到曲线上的点
                        for (int i = 0; i < mList.size(); i++) {
                            mPointValues.add(new PointValue(i,mList.get(i).getData1()));
                        }
                        mLine.setValues(mPointValues);
                        initLine(ArraysUtil.getMin(data1),ArraysUtil.getMax(data1));
                        break;
                    case R.id.line_choose_data2:
                        clear();
                        //得到X坐标的标号
                        initAxis();
                        //得到X坐标的标号
                        getAxisXLables(mList);
                        //得到Y轴的标号
                        for(int i = 0; i < mList.size(); i++){
                            AxisValue value = new AxisValue(mList.get(i).getData2());
                            AxisYValues.add(value);
                        }
                        axisY.setValues(AxisYValues);
                        mLineChartData.setAxisYLeft(axisY);
                        //得到曲线上的点
                        for (int i = 0; i < mList.size(); i++) {
                            mPointValues.add(new PointValue(i,mList.get(i).getData2()));
                        }
                        mLine.setValues(mPointValues);
                        initLine(ArraysUtil.getMin(data2),ArraysUtil.getMax(data2));
                        break;
                    case R.id.line_choose_data3:
                        clear();
                        //得到X坐标的标号
                        initAxis();
                        //得到X坐标的标号
                        getAxisXLables(mList);
                        //得到Y轴的标号
                        for(int i = 0; i < mList.size(); i++){
                          AxisValue value = new AxisValue(mList.get(i).getData3());
                            AxisYValues.add(value);
                        }
                        axisY.setValues(AxisYValues);
                        mLineChartData.setAxisYLeft(axisY);
                        //得到曲线上的点
                        for (int i = 0; i < mList.size(); i++) {
                            mPointValues.add(new PointValue(i,mList.get(i).getData3()));
                        }
                        mLine.setValues(mPointValues);
                        initLine(ArraysUtil.getMin(data3),ArraysUtil.getMax(data3));
                        break;
                }
            }
        });

    }

    /**
     * @param f1 要显示的bottom范围
     * @param f2 要显示的top范围
     */
    private void initLine(float f1 , float f2) {
        mLine.setColor(Color.BLUE);
        mLine.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        mLine.setCubic(false);//选择是折线（false）还是曲线（true）
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(2);//设置小数点
        mLine.setFormatter(chartValueFormatter);
        mLine.setHasLines(true);//是否显示线
        //line.setStrokeWidth(4);//：线的粗细
        mLine.setPointRadius(4);//点的半径
        //line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        mLine.setHasLabels(true);//曲线的数据坐标是否加上备注
        mLine.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        mLines.add(mLine);
        mLineChartData.setLines(mLines);
        mLineChartData.setValueLabelTextSize(10);//设置标签文字字号，默认为12sp
        mLineChartData.setValueLabelBackgroundEnabled(false);//设置是否显示标签的背景
        mLineChartData.setValueLabelsTextColor(Color.parseColor("#FF4081"));
        lineChart.setInteractive(true);//设置该图表是否可交互。如不可交互，则图表不会响应缩放、滑动、选择或点击等操作。默认值为true，可交互。
        lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);//设置缩放类型，可选的类型包括ZoomType.HORIZONTAL_AND_VERTICAL, ZoomType.HORIZONTAL, ZoomType.VERTICAL，默认值为HORIZONTAL_AND_VERTICAL。
        lineChart.setZoomEnabled(true);//设置是否可缩放。
        lineChart.setMaxZoom((float) 30);//设置最大缩放比例。默认值20。
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setLineChartData(mLineChartData);
        Viewport viewport = new Viewport(lineChart.getMaximumViewport());
        viewport.bottom = f1 - 50f;
        viewport.top = f2 + 50f;
        viewport.left = 0;
        viewport.right = mList.size();
        lineChart.setMaximumViewport(viewport);
        lineChart.setCurrentViewport(viewport);
    }

    private void initAxis(){
        axisX.setHasTiltedLabels(false);//设置标签是斜着的还是水平的
        axisX.setHasSeparationLine(true);// 设置是否有分割线
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisY.setMaxLabelChars(5);//可以显示的标签长度
        axisY.setHasTiltedLabels(false);
    }
    private void clear(){
        if(mLines != null){
            mLines.clear();
        }
        if (mAxisXValues != null){
            mPointValues.clear();
        }
        if(AxisYValues != null){
            AxisYValues.clear();
        }
        if(mPointValues != null){
            mPointValues.clear();
        }
    }

    /**设置X 轴的显示
     * @param mList
     */
    private void getAxisXLables(List<Bean> mList){
        for (int i = 0; i < mList.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(mList.get(i).getTimeDetail()));
        }
        axisX.setValues(mAxisXValues);
        mLineChartData.setAxisXBottom(axisX);
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
