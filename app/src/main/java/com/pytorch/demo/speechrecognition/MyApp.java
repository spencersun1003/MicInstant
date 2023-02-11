package com.pytorch.demo.speechrecognition;

import com.chaquo.python.android.PyApplication;
import android.util.Log;

public class MyApp extends PyApplication {
    public static MyApp mApp;

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;
        Log.w("sybTest", "TEST-----------------");
    }

    public static MyApp getInstance() {
        return mApp;
    }
}