//package com.pytorch.demo.speechrecognition;
//
//import android.app.IntentService;
//import android.content.Context;
//import android.content.Intent;
//import android.os.IBinder;
//
//import androidx.core.app.JobIntentService;
//
//public class SensorIntentService extends JobIntentService {
//
//    public static final int JOB_ID = 1;
//
//    public static void enqueueWork(Context context, Intent work) {
//        enqueueWork(context, SensorIntentService.class, JOB_ID, work);
//    }
//
//
//    /**
//     * 复写onHandleIntent()方法
//     * 根据 Intent实现 耗时任务 操作
//     **/
//    @Override
//    protected void onHandleWork(Intent intent) {
//
//        // 根据 Intent的不同，进行不同的事务处理
//        String taskName = intent.getExtras().getString("taskName");
//        switch (taskName) {
//            case "task1":
//                Log.i("myIntentService", "do task1");
//                break;
//            case "task2":
//                Log.i("myIntentService", "do task2");
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        Log.i("myIntentService", "onCreate");
//        super.onCreate();
//    }
//    /**
//     * 复写onStartCommand()方法
//     * 默认实现 = 将请求的Intent添加到工作队列里
//     **/
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("myIntentService", "onStartCommand");
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        Log.i("myIntentService", "onDestroy");
//        super.onDestroy();
//    }
//}
