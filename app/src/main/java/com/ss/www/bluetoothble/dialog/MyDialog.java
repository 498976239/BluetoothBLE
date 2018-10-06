package com.ss.www.bluetoothble.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import com.ss.www.bluetoothble.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by SS on 17-6-24.
 */
public class MyDialog extends Dialog {
    private NumberPicker mNumberPicker;
    private RadioGroup mRadioGroup;
    private int conditon_query;
    private Button mButton_choose,mButton_choose2;
    private Calendar mCalendar;
    private String[] s;
    private Date startDate,endDate;
    //回调接口
    public interface PassData{
        void passString(String s,Date d1,Date d2,int i);
    }
    private String str;
    private PassData mPassData;//定义接口回调用来传值
    private Context mContext;
    public MyDialog(Context context,PassData mPassData) {
        super(context);
        this.mContext = context;
        this.mPassData = mPassData;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View v = LayoutInflater.from(mContext).inflate(R.layout.dialog,null);
        this.setContentView(v);
        s = new String[99];
        for (int i = 0; i < s.length; i++) {
            s[i] = "通道"+(i+1);
        }
        mNumberPicker = (NumberPicker) v.findViewById(R.id.pick);
        mRadioGroup = (RadioGroup) v.findViewById(R.id.rg);
        mButton_choose = (Button) v.findViewById(R.id.choose_date);
        mButton_choose2 = (Button) v.findViewById(R.id.choose_date2);
        mNumberPicker.setDisplayedValues(s);//设置数据选择器的内容
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(s.length-1);
        str = mNumberPicker.getDisplayedValues()[mNumberPicker.getValue()];//为数据选择器设置默认值
        mCalendar = Calendar.getInstance();//获取现在的时间值
        int year =  mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day =  mCalendar.get(Calendar.DAY_OF_MONTH);
        startDate = mCalendar.getTime();//为起始时间设置初始值，为当前
        endDate = mCalendar.getTime();//为结束时间设置初始值
        mButton_choose.setText(year+"-"+(month+1)+"-"+day);
        mButton_choose2.setText(year+"-"+(month+1)+"-"+day);
        mNumberPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                switch (i) {
                    case SCROLL_STATE_IDLE:
                        String[] displayedValues = mNumberPicker.getDisplayedValues();
                        str = displayedValues[mNumberPicker.getValue()];
                        mRadioGroup.clearCheck();
                        conditon_query =0;


                        break;
                    case SCROLL_STATE_FLING:
                        str = null;
                }
            }
        });
        Button mButton = (Button) v.findViewById(R.id.sure);
        //确定选中的值，并传递给主程序
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPassData.passString(str,startDate,endDate,conditon_query);
                dismiss();
            }
        });
        //选择日期
        mButton_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRadioGroup.clearCheck();
                conditon_query =0;
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        setCalendar(i,i1,i2);
                    }
                }, year, month, day);
                dialog.show();

            }
        });
//选择日期
        mButton_choose2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRadioGroup.clearCheck();
                conditon_query =0;
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        setCalendar2(i,i1,i2);
                    }
                }, year, month, day);
                dialog.show();

            }
        });
        //单选框选择
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.today:
                        conditon_query = 1;
                        break;
                    case R.id.five_day:
                        conditon_query = 2;
                        break;
                    case R.id.all:
                        conditon_query = 3;
                        break;
                }
            }
        });
    }
    //为结束时间赋值
    private void setCalendar2(int year, int month, int day) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,year);
        mCalendar.set(Calendar.MONTH,month);
        mCalendar.set(Calendar.DAY_OF_MONTH,day);
        endDate = mCalendar.getTime();
        mButton_choose2.setText(year+"-"+(month+1)+"-"+day);

    }
    //为起始时间赋值
    public void setCalendar(int year,int month,int day){
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,year);
        mCalendar.set(Calendar.MONTH,month);
        mCalendar.set(Calendar.DAY_OF_MONTH,day);
        startDate = mCalendar.getTime();
        mButton_choose.setText(year+"-"+(month+1)+"-"+day);
    }



}
