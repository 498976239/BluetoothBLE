package com.ss.www.bluetoothble.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import com.ss.www.bluetoothble.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by SS on 17-7-10.
 */
public class LineDialog extends Dialog {
    private NumberPicker line_mNumberPicker;
    private String str;
    private Calendar mCalendar;
    private Date startDate,endDate;
    private Button mButton_choose,mButton_choose2;
    public interface PassData{
        void passCondition(int s);
    }
    private RadioGroup mRadioGroup;
    private Button mButton;
    private Context mContext;
    private int line_condition;
    private PassData mPassData;//定义接口回调用来传值

    public LineDialog(Context context, PassData mPassData) {
        super(context);
        this.mContext = context;
        this.mPassData = mPassData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View v = LayoutInflater.from(mContext).inflate(R.layout.line_dialog,null);
        this.setContentView(v);
        String[] s = {"通道1","通道2","通道3","通道4","通道5","通道6","通道7","通道8","通道9",
                "通道10","通道11","通道12","通道13","通道14","通道15","通道16","通道17","通道18",
                "通道19","通道20","通道21","通道22","通道23","通道24","通道25","通道26","通道27",
                "通道28","通道29","通道30","通道31","通道32","通道33","通道34","通道35","通道36"};
        line_mNumberPicker = (NumberPicker) v.findViewById(R.id.pick_line);
        mButton = (Button) v.findViewById(R.id.line_ok);
        mButton_choose = (Button) v.findViewById(R.id.line_date);
        mButton_choose2 = (Button) v.findViewById(R.id.line_date2);
        line_mNumberPicker.setDisplayedValues(s);//设置数据选择器的内容
        line_mNumberPicker.setMinValue(0);
        line_mNumberPicker.setMaxValue(s.length-1);
        str = line_mNumberPicker.getDisplayedValues()[line_mNumberPicker.getValue()];//为数据选择器设置默认值
        mCalendar = Calendar.getInstance();//获取现在的时间值
        int year =  mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day =  mCalendar.get(Calendar.DAY_OF_MONTH);
        startDate = mCalendar.getTime();//为起始时间设置初始值，为当前
        endDate = mCalendar.getTime();//为结束时间设置初始值
        mButton_choose.setText(year+"-"+(month+1)+"-"+day);
        mButton_choose2.setText(year+"-"+(month+1)+"-"+day);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPassData.passCondition(line_condition);
                dismiss();
            }
        });
    }
}
