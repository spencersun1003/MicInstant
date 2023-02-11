//package com.pytorch.demo.speechrecognition;
//
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.widget.TextView;
//
//import androidx.annotation.LayoutRes;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class SensorListener extends AppCompatActivity implements SensorEventListener {
//
//
//    private SensorManager sm;
//    private Sensor mSensorOrientation;
//    private MainActivity context;
//
//    public interface datachanged {
//
//        public void netFall();
//
//    }
//
//
//
//
//
//    public void settvTest(MainActivity context){
//        this.context=context;
//
//        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
//        // 获取方向传感器
//        mSensorOrientation = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
//        //注册数值变化监听器
//        sm.registerListener(this, mSensorOrientation,SensorManager.SENSOR_DELAY_UI);
//    }
//
//
//    // 传感器数值变化会调用此方法
//
//    @Override
//    public void onSensorChanged( SensorEvent event) {
//
//        context.runOnUiThread(new Runnable() {
//            @Override
//
//            public void run() { context.mtvTest.setText((String.valueOf((float) (Math.round(event.values[0] * 100)) / 100)));}
//
//            });
//
//
//    }
//
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//
//}
