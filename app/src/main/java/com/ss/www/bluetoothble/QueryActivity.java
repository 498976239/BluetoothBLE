package com.ss.www.bluetoothble;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ss.www.bluetoothble.adapter.MyRecyclerAdapter;
import com.ss.www.bluetoothble.dbmanager.CommUtils;
import com.ss.www.bluetoothble.dialog.MyDialog;
import com.ss.www.bluetoothble.entity.Bean;
import com.ss.www.bluetoothble.utils.LogUtil;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class QueryActivity extends AppCompatActivity {
    private static final int QUERY_DETAIL = 0;
    private static final int QUERY_TODAY = 1;
    private static final int QUERY_WEEK = 2;
    private static final int QUERY_ALL = 3;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    public static final String LINE_CONDITION_RESULT = "line_result";
    public static final String MY_DATA = "history_line";
    private Button query_btn;
    private int query_condition;
    private int add_dec_condition;//用来做查询加减通道的值
    private Button mLast;
    private Button mNext;
    private Button mRain_btn;
    private Button line_btn;
    private EditText mInput;
    private List<Bean> list = new ArrayList<>();
    private CommUtils mCommonUtils;
    private String str;
    private String query_condition_name;
    private String editTextStartTime;
    private String editTextEndTime;
    private Date startDate,endDate;
    private Toolbar mQueryActivityToolBar;
    private MyDialog myDialog;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter myRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        initViews();
        showDialog();
        setSupportActionBar(mQueryActivityToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("数据查询");
        mCommonUtils = new CommUtils(this);
        myRecyclerAdapter = new MyRecyclerAdapter(this,list);
        LinearLayoutManager linearlayoutmanager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearlayoutmanager);
        mRecyclerView.setAdapter(myRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        myRecyclerAdapter.setLongItemClickListener(new MyRecyclerAdapter.OnRecyclerViewLongItemClickListener() {
            @Override
            public void onLongItemClick(View view, int position) {
                //list.remove(position);
                mCommonUtils.delectBean(list.get(position));
                list.remove(position);
               // myRecyclerAdapter.notifyItemRemoved(position);
                //myRecyclerAdapter.notifyItemChanged(position);
                //myRecyclerAdapter.notify();
                myRecyclerAdapter.notifyDataSetChanged();
                LogUtil.i("main","position"+"---"+position);

            }
        });

        query_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (query_condition){
                    //按详细条件查询
                    case QUERY_DETAIL:
                        LogUtil.i("main",getClass().getSimpleName()+"-d--"+str.length());
                        if (mInput.getText().toString()!= null&&mInput.getText().toString().length()>0){
                            str = "通道"+mInput.getText().toString();
                            LogUtil.i("main",getClass().getSimpleName()+"---"+mInput.getText().toString());
                            add_dec_condition = Integer.parseInt(mInput.getText().toString());
                        }else {
                            if (str.length() == 3){
                                add_dec_condition = Integer.parseInt(str.substring(str.length()-1));
                            }
                            if (str.length() > 3){
                                add_dec_condition = Integer.parseInt(str.substring(str.length()-2));
                            }
                        }
                        LogUtil.i("main","add_dec_condition2--"+str.substring(str.length()-2,str.length()-1));
                        LogUtil.i("main","add_dec_condition--"+add_dec_condition);
                        mQueryActivityToolBar.setSubtitle(str);
                        if(str != null&&startDate!=null&&endDate!=null){
                            list.clear();
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTime(startDate);
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            //毫秒可根据系统需要清除或不清除
                            cal.set(Calendar.MILLISECOND, 0);
                            long starting = cal.getTimeInMillis();
                            startDate = new Date(starting);
                            ////////////////////////////////////////////////////
                            GregorianCalendar cal2 = new GregorianCalendar();
                            cal2.setTime(endDate);
                            cal2.set(Calendar.HOUR_OF_DAY, 0);
                            cal2.set(Calendar.MINUTE, 0);
                            cal2.set(Calendar.SECOND, 0);
                            //毫秒可根据系统需要清除或不清除
                            cal2.set(Calendar.MILLISECOND, 0);
                            long ending = cal2.getTimeInMillis()+24*3600*1000;
                            endDate = new Date(ending);
                            List<Bean> been = mCommonUtils.queryCondition(str,startDate,endDate);
                            if(been.size()>0){
                                for (int i = been.size(); i >0 ; i--) {
                                    list.add(been.get(i-1));
                                }
                            }else {
                                Toast.makeText(QueryActivity.this,"此条件下，没有数据",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(QueryActivity.this,"请重新选择查询条件",Toast.LENGTH_SHORT).show();
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                        break;
                    case QUERY_TODAY:
                        //查询今天的
                        list.clear();
                        Date now = new Date();
                        GregorianCalendar cal_today = new GregorianCalendar();
                        cal_today.setTime(now);
                        cal_today.set(Calendar.HOUR_OF_DAY, 0);
                        cal_today.set(Calendar.MINUTE, 0);
                        cal_today.set(Calendar.SECOND, 0);
                        //毫秒可根据系统需要清除或不清除
                        cal_today.set(Calendar.MILLISECOND, 0);
                        long today_start = cal_today.getTimeInMillis();
                        Date start = new Date(today_start);
                        List<Bean> been_today = mCommonUtils.queryCondition(start, now);
                        if(been_today.size()>0){
                            for (int i = been_today.size(); i > 0 ; i--) {
                                list.add(been_today.get(i-1));
                            }
                        }else {
                            Toast.makeText(QueryActivity.this,"此条件下，没有数据",Toast.LENGTH_SHORT).show();
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                        query_condition_name="今天";
                        break;
                    case QUERY_WEEK:
                        list.clear();
                        Date rightnow = new Date();
                        GregorianCalendar cal_week = new GregorianCalendar();
                        cal_week.setTime(rightnow);
                        cal_week.set(Calendar.HOUR_OF_DAY, 0);
                        cal_week.set(Calendar.MINUTE, 0);
                        cal_week.set(Calendar.SECOND, 0);
                        //毫秒可根据系统需要清除或不清除
                        cal_week.set(Calendar.MILLISECOND, 0);
                        long week_end = cal_week.getTimeInMillis();
                        Date week_finish = new Date(week_end);
                        long week_start = week_end/(24*3600*1000*7);
                        Date week_action = new Date(week_start);
                        List<Bean> been_week = mCommonUtils.queryCondition(week_action, week_finish);
                        if(been_week.size()>0){
                            for (int i = been_week.size(); i >0 ; i--) {
                                list.add(been_week.get(i-1));
                            }
                        }else {
                            Toast.makeText(QueryActivity.this,"此条件下，没有数据",Toast.LENGTH_SHORT).show();
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                        query_condition_name="周";
                        break;
                    case QUERY_ALL:
                        list.clear();
                        List<Bean> been_all = mCommonUtils.allLoad();
                        if (been_all.size()>0){
                            for (int i = been_all.size(); i >0 ; i--) {
                                list.add(been_all.get(i-1));
                            }
                        }else {
                            Toast.makeText(QueryActivity.this,"此条件下，没有数据",Toast.LENGTH_SHORT).show();
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                        query_condition_name="全部";
                        break;
                }
            }
        });
        mLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_dec_condition = add_dec_condition - 1;
                if (add_dec_condition <= 0){
                    add_dec_condition = 1;
                    mLast.setVisibility(View.GONE);
                    Toast.makeText(QueryActivity.this,"已经是第一通道了",Toast.LENGTH_SHORT).show();
                }else {
                    mLast.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                    str = "通道"+String.valueOf(add_dec_condition);
                    mQueryActivityToolBar.setSubtitle(str);
                    list.clear();
                    List<Bean> been = mCommonUtils.queryCondition(str,startDate,endDate);
                    if(been.size()>0) {
                        for (int i = been.size(); i > 0; i--) {
                            list.add(been.get(i - 1));
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(QueryActivity.this,"没有数据",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_dec_condition = add_dec_condition + 1;
                if (add_dec_condition >= 100){
                    add_dec_condition = 99;
                    mNext.setVisibility(View.GONE);
                    Toast.makeText(QueryActivity.this,"已经是最后一个通道了",Toast.LENGTH_SHORT).show();
                }else {
                    mLast.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                    str = "通道"+String.valueOf(add_dec_condition);
                    mQueryActivityToolBar.setSubtitle(str);
                    list.clear();
                    List<Bean> been = mCommonUtils.queryCondition(str,startDate,endDate);
                    if(been.size()>0) {
                        for (int i = been.size(); i > 0; i--) {
                            list.add(been.get(i - 1));
                        }
                        myRecyclerAdapter.notifyDataSetChanged();
                    }else {
                        list.clear();
                        Toast.makeText(QueryActivity.this,"没有数据",Toast.LENGTH_SHORT).show();
                        myRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        line_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLineActivity();
            }
        });
        mRain_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQueryActivityToolBar.setSubtitle("雨量");
                list.clear();
                List<Bean> been = mCommonUtils.queryCondition("雨量",startDate,endDate);
                if(been.size()>0) {
                    for (int i = been.size(); i > 0; i--) {
                        list.add(been.get(i - 1));
                    }
                    myRecyclerAdapter.notifyDataSetChanged();
                }else {
                    list.clear();
                    Toast.makeText(QueryActivity.this,"没有数据",Toast.LENGTH_SHORT).show();
                    myRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.data_recyclerview);
        mQueryActivityToolBar = (Toolbar) findViewById(R.id.date_query_toolbar);
        query_btn = (Button) findViewById(R.id.data_query_btn);
        mRain_btn = (Button) findViewById(R.id.rain_btn);
        mLast = (Button) findViewById(R.id.last_btn);
        mNext = (Button) findViewById(R.id.next_btn);
        mInput = (EditText) findViewById(R.id.chan_no);
        line_btn = (Button) findViewById(R.id.line_btn);
        //mShow = (TextView) findViewById(R.id.chan_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.openChoose:
                showDialog();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.export_data:
                if(list.size()>0){
                    AlertDialog.Builder export = new AlertDialog.Builder(QueryActivity.this);
                    export.setTitle("输入文件名");
                    final View v = getLayoutInflater().inflate(R.layout.export_data,null);
                    final EditText editText= (EditText) v.findViewById(R.id.mEditText);
                    if(editTextStartTime!=null&&editTextEndTime!=null){
                        editText.setText(str+"--"+editTextStartTime+"--"+editTextEndTime);
                    }else {
                        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        editText.setText(query_condition_name+":"+mSimpleDateFormat.format(new Date()));
                    }

                    export.setView(v);
                    export.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String s= editText.getText().toString();
                            if(s.length()>=1){
                                QueryActivityPermissionsDispatcher.ExportToExcelWithCheck(QueryActivity.this,s);

                            }else{
                                Toast.makeText(QueryActivity.this,"请输入至少一个字符的文件名",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    export.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    export.create().show();
                }else {
                    Toast.makeText(QueryActivity.this,"无数据不导出",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.line_data:
                showLineActivity();
                break;
            case R.id.deleteAll:
                AlertDialog.Builder delete = new AlertDialog.Builder(QueryActivity.this);
                delete.setTitle("温馨提示");
                delete.setMessage("该操作会删除数据库所有数据，删除后不可恢复！");
                delete.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       mCommonUtils.delectAllBean();
                        list.clear();
                        myRecyclerAdapter.notifyDataSetChanged();
                    }
                });
                delete.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                delete.create().show();
                break;


        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQueryActivityToolBar.setSubtitle(str);
        LogUtil.i("main","执行了onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.i("main","onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i("main","onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
    //将数据导出成为Excel
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
     void ExportToExcel(String str){
        if(list.size()>0){
            FileOutputStream fileOut = null;
            File external = Environment.getExternalStorageDirectory();
            Workbook wb = new HSSFWorkbook();//创建一个工作簿
            Sheet sheet = wb.createSheet("采集数据");//创建一个sheet页
            Row row = sheet.createRow(0);//创建第一行
            row.createCell(0).setCellValue("通道号");//设置第一列并命名
            row.createCell(1).setCellValue("数据1");
            row.createCell(2).setCellValue("数据2");
            row.createCell(3).setCellValue("数据3");
            row.createCell(4).setCellValue("日期");
            for (int i = 0; i < list.size(); i++) {
                Row row1 = sheet.createRow(i + 1);
                String name = list.get(i).getName();
                float data1 = list.get(i).getData1();
                float data2 = list.get(i).getData2();
                float data3 = list.get(i).getData3();
                String timeDetail = list.get(i).getTimeDetail();
                row1.createCell(0).setCellValue(name);//将name写入单元格
                row1.createCell(1).setCellValue(data1+"");
                row1.createCell(2).setCellValue(data2+"");
                row1.createCell(3).setCellValue(data3+"");
                row1.createCell(4).setCellValue(timeDetail);
            }
            File saveFile  = new File(external,str+".xls");
            try {
                fileOut = new FileOutputStream(saveFile);
                try {
                    wb.write(fileOut);
                    fileOut.close();
                    Toast.makeText(this,"导出成功",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }finally {
                if(fileOut!= null){
                    try {
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            Toast.makeText(this,"没有可以导出的数据",Toast.LENGTH_SHORT).show();
        }
    }
    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showWhy(final PermissionRequest request){
        new AlertDialog.Builder(this)
                .setMessage("导出数据需要写操作权限")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .show();
    }
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDenied(){
        Toast.makeText(QueryActivity.this,"将无法导出数据",Toast.LENGTH_SHORT).show();
    }
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNotAsk(){
        new AlertDialog.Builder(this)
                .setMessage("该功能需要写操作权限， 不开启将无法正常工作！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    /**
     * 查询时弹出来的对话框
     */
    private void showDialog(){
        myDialog = new MyDialog(this, new MyDialog.PassData() {
            @Override
            public void passString(String s,Date d1,Date d2,int i) {
                str = s;
                mQueryActivityToolBar.setSubtitle(str);
                if (str.length() == 3){
                    add_dec_condition = Integer.parseInt(str.substring(str.length()-1));
                }
                if (str.length() > 3){
                    add_dec_condition = Integer.parseInt(str.substring(str.length()-2));
                }
                startDate = d1;
                endDate = d2;
                query_condition = i;
                SimpleDateFormat  mSimpleDateFormat = new SimpleDateFormat("yy-MM-dd");
                if(d1!=null&&d2!=null){
                    editTextStartTime= mSimpleDateFormat.format(d1);
                    editTextEndTime =  mSimpleDateFormat.format(d2);
                }
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                //毫秒可根据系统需要清除或不清除
                cal.set(Calendar.MILLISECOND, 0);
                long starting = cal.getTimeInMillis();
                startDate = new Date(starting);
                ////////////////////////////////////////////////////
                GregorianCalendar cal2 = new GregorianCalendar();
                cal2.setTime(endDate);
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.set(Calendar.MINUTE, 0);
                cal2.set(Calendar.SECOND, 0);
                //毫秒可根据系统需要清除或不清除
                cal2.set(Calendar.MILLISECOND, 0);
                long ending = cal2.getTimeInMillis()+24*3600*1000;
                endDate = new Date(ending);
                if (i == 0){
                    mInput.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                    mLast.setVisibility(View.VISIBLE);
                }else {
                    mInput.setVisibility(View.GONE);
                    mNext.setVisibility(View.GONE);
                    mLast.setVisibility(View.GONE);
                    mQueryActivityToolBar.setSubtitle("");
                }

                  LogUtil.i("main--str--",str);
                  LogUtil.i("main--str2--",str.substring(str.length()-1));
                  LogUtil.i("main--str3--",Integer.parseInt(str.substring(str.length()-1))+"");
                // Log.i("main--d1--",d1.toString());
                // Log.i("main--d2--",d2.toString());
                LogUtil.i("main--condition--",query_condition+"");
                LogUtil.i("main---startDate",startDate+"");
            }
        });
        myDialog.show();
        Window dialogWindow = myDialog.getWindow();
        WindowManager m = this.getWindowManager();
        //获取屏幕
        WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH= size.x;//获取屏幕的宽度
        SCREEN_HEIGHT= size.y;//获取屏幕的高度
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (SCREEN_WIDTH*0.95); // 宽度设置为屏幕，根据实际情况调整
        p.height = (int) (SCREEN_HEIGHT*0.7); // 高度设置为屏幕，根据实际情况调整
        dialogWindow.setAttributes(p);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        QueryActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);
    }
    private void showLineActivity(){
        if(list.size()>0){
            if(query_condition == 0){
                Intent intent = new Intent(this,LineActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MY_DATA,(Serializable) list);
                intent.putExtras(bundle);
                startActivity(intent);
            }else{
                Toast.makeText(this,"只能选择一个具体的通道数",Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(this,"请选择需要曲线显示的数据",Toast.LENGTH_SHORT).show();
        }
    }
}
