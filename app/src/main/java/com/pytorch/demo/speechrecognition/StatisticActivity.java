package com.pytorch.demo.speechrecognition;
import java.util.Calendar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.pytorch.demo.speechrecognition.R;

public class StatisticActivity extends Activity {

    int year = 0;
    int monthOfYear = 0;
    int dayOfMonth = 0;
    int minute = 0;
    int houre = 0;
    TextView showDate = null;
    TextView showtime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        showDate(year, monthOfYear + 1, dayOfMonth);
        showTime(houre, minute);
    }

    private void initView() {
        // 日期控件对象
        DatePicker date = (DatePicker) findViewById(R.id.main_dp);
        // 获得日历对象
        Calendar c = Calendar.getInstance();
        // 获取当前年份
        year = c.get(Calendar.YEAR);
        // 获取当前月份
        monthOfYear = c.get(Calendar.MONTH);
        // 获取当前月份的天数
        dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        // 获取当前的小时数
        houre = c.get(Calendar.HOUR_OF_DAY);
        // 获取当前的分钟数
        minute = c.get(Calendar.MINUTE);

        // 时间显示的文本对象
//        showDate = (TextView) findViewById(R.id.main_tv_showdate);
//
//        // 为日期设置监听事件
//        date.init(year, monthOfYear, dayOfMonth, new OnDateChangedListener() {
//
//            @Override
//            public void onDateChanged(DatePicker view, int year,
//                                      int monthOfYear, int dayOfMonth) {
//                MainActivity.this.year = year;
//                MainActivity.this.monthOfYear = monthOfYear;
//                MainActivity.this.dayOfMonth = dayOfMonth;
//                showDate(year, monthOfYear + 1, dayOfMonth);
//
//            }
//
//        });

        // 显示时间的文本控件
        //showtime = (TextView) findViewById(R.id.main_tv_showtime);

        // 时间显示的控件
        //TimePicker time = (TimePicker) findViewById(R.id.main_tp_showTime);
        // 为时间控件设置监听事件
//        time.setOnTimeChangedListener(new OnTimeChangedListener() {
//
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                showTime(hourOfDay, minute);
//            }
//        });

    }

    //显示日期的方法
    private void showDate(int year, int monthOfYear, int dayOfMonth) {
        showDate.setText("日期是：" + year + "年" + monthOfYear + "月" + dayOfMonth
                + "日");

    }

    //显示时间的方法
    private void showTime(int houre2, int minute2) {
        showtime.setText("时间是：" + houre2 + "时" + minute2 + "分");

    }

}