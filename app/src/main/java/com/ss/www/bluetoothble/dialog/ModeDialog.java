package com.ss.www.bluetoothble.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ss.www.bluetoothble.Main2Activity;
import com.ss.www.bluetoothble.R;
import com.ss.www.bluetoothble.utils.ArraysUtil;

/**
 * Created by 小松松 on 2017/11/19.
 */

public class ModeDialog extends Dialog {
    private RadioGroup mRadioGroup;
    private Button sure_btn,cancel_btn;
    private Context mContext;
    private int Mode_condition;
    private int jugde_condition;//用来判定选中的模式是否已经在执行
    private byte[] end ;
    public interface Passinfo{
        void passMode(int s,byte[] b);
    }
    private Passinfo pi;

    public ModeDialog(@NonNull Context context, int jugde_condition, Passinfo pi) {
        super(context);
        this.mContext = context;
        this.pi = pi;
        this.jugde_condition = jugde_condition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = LayoutInflater.from(mContext).inflate(R.layout.mode_layout,null);
        this.setContentView(v);
        mRadioGroup = v.findViewById(R.id.radio_choose);
        sure_btn = v.findViewById(R.id.mode_sure);
        cancel_btn = v.findViewById(R.id.mode_cancel);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.average:
                        Mode_condition = 0;
                        byte[] b = {0x01,(byte)0xa3,(byte)0xfa,(byte)0xfa,0,0x10,0,0};
                        byte[] orderCRC = ArraysUtil.intToByteArray(ArraysUtil.getCrc16(b));
                        end = new byte[]{0x01, (byte) 0xa3, (byte)0xfa,(byte)0xfa,0,0x10,0,0,orderCRC[2], orderCRC[3]};
                        break;
                    case R.id.special:
                        Mode_condition = 1;
                        //默认对通道1进行采集
                        byte[] b1 = {0x01,(byte)0xa3,(byte)0xfa,(byte)0xfa,0,0x10,85,1};
                        byte[] orderCRC1 = ArraysUtil.intToByteArray(ArraysUtil.getCrc16(b1));
                        end = new byte[]{0x01, (byte) 0xa3, (byte)0xfa,(byte)0xfa,0,0x10,85,1, orderCRC1[2], orderCRC1[3]};
                        break;
                }
            }
        });
        if (jugde_condition == 0){
            mRadioGroup.check(R.id.average);
        }else {
            mRadioGroup.check(R.id.special);
        }

        sure_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (jugde_condition == Mode_condition){
                 //   Toast.makeText(getContext(),"已是该模式，无需切换",Toast.LENGTH_SHORT).show();
               // }else {
                    pi.passMode(Mode_condition,end);
                    dismiss();
               // }

            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
