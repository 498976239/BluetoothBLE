package com.ss.www.bluetoothble;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ss.www.bluetoothble.adapter.MyRecyclerAdapter;
import com.ss.www.bluetoothble.dbmanager.CommUtils;
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

public class QueryActivity extends AppCompatActivity {
    private static final int QUERY_DETAIL = 0;
    private static final int QUERY_TODAY = 1;
    private static final int QUERY_WEEK = 2;
    private static final int QUERY_ALL = 3;
    public static final String LINE_CONDITION_RESULT = "line_result";
    public static final String MY_DATA = "history_line";
    private Button query_btn;
    private int query_condition;
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
        mQueryActivityToolBar = (Toolbar) findViewById(R.id.date_query_toolbar);
        setSupportActionBar(mQueryActivityToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("数据查询");
        mCommonUtils = new CommUtils(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.data_recyclerview);
        myRecyclerAdapter = new MyRecyclerAdapter(this,list);
        LinearLayoutManager linearlayoutmanager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearlayoutmanager);
        mRecyclerView.setAdapter(myRecyclerAdapter);
        query_btn = (Button) findViewById(R.id.data_query_btn);
        query_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (query_condition){
                    //按详细条件查询
                    case QUERY_DETAIL:
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
                                ExportToExcel(s);
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

                break;


        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDialog();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
    //将数据导出成为Excel
    private void ExportToExcel(String str){
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

    /**
     * 查询时弹出来的对话框
     */
    private void showDialog(){
        myDialog = new MyDialog(this, new MyDialog.PassData() {
            @Override
            public void passString(String s,Date d1,Date d2,int i) {
                str = s;
                startDate = d1;
                endDate = d2;
                query_condition = i;
                SimpleDateFormat  mSimpleDateFormat = new SimpleDateFormat("yy-MM-dd");
                if(d1!=null&&d2!=null){
                    editTextStartTime= mSimpleDateFormat.format(d1);
                    editTextEndTime =  mSimpleDateFormat.format(d2);
                }

                //  Log.i("main--str--",str);
                // Log.i("main--d1--",d1.toString());
                // Log.i("main--d2--",d2.toString());
                LogUtil.i("main--condition--",query_condition+"");
                LogUtil.i("main---startDate",startDate+"");
            }
        });
        myDialog.show();
    }


}
