package com.pytorch.demo.speechrecognition;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


import org.pytorch.LiteModuleLoader;

import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.PyObject;
//import com.chaquo.python.Kwarg;

import com.pytorch.demo.speechrecognition.R;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;


public class MainActivity extends AppCompatActivity implements Runnable {
    private static final String TAG = MainActivity.class.getName();

    private Module mModuleEncoder;
    private TextView mtvEmoState;
    public TextView mtvTest;
    private ImageButton mButton;
    private ImageButton mImgbExtractFeature;
    private ImageButton mImgbCheckFeature;
    private ImageView mImgvEmoState;
    private ProgressBar mprobarEmoInference;
    private boolean mButtonisPlay=false;


    private int EmoState=EMO_NEUTRAL;
    private float EmoInference=0;

    private final static int EMO_NEUTRAL=1;
    private final static int EMO_ANGER=2;
    public final static int REQUEST_RECORD_AUDIO = 13;
    public final static int AUDIO_LEN_IN_SECOND = 5;
    public final static int SAMPLE_RATE = 16000;//22050;//16000;
    public final static int RECORDING_LENGTH = SAMPLE_RATE * AUDIO_LEN_IN_SECOND;

    private final static String INTERNAL_FILESAVEPATH="assets";

    private final static int ChangeMicState_Drop_InRequest=-1;
    private final static int ChangeMicState_Changed=1;

    private final static int MicOnNoticeMode_Audio=1;
    private final static int MicOnNoticeMode_Vibration=2;

    private final static int VoicePrint_RecordTime=2500;
    public static float VoicePrint_ScoreThreshold= (float) 0.5;

    private final static int MicTimerOn_ExceedTime_Max=12;
    private final static int MicTimerOn_ExceedTime_Min=8;
    private static int MicTimerOn_ExceedTime=MicTimerOn_ExceedTime_Max;


    private final static int HOP_LENGTH=512;
    private final static int FRAME_LENGTH=2048;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private int mStart = 1;
    private HandlerThread mTimerThread;
    private Handler mTimerHandler;


    public final static int MIC_ON=1;
    public final static int MIC_OFF=2;

    public final static int MIC_INREQUEST=1;
    public final static int MIC_NOREQUEST=2;

    private final static int Announce_Mode=0;//最终模式，同时包括倒置开麦和拍拍开麦
    private final static int GreatMeeting_Mode=1;//倒置麦克风开麦
    private final static int HandFree_mode=2;//拍拍开麦

    private SensorManager sm;
    private Sensor mSensorOrientation;
    private SensListener sensListener=new SensListener();

    private Vibrator vibrator;
    private SensorManager sensorManager;
    private SpeechRecognizer speechRecognizer;
    private int samplingPeriod = 10000;

    private static int seqLength = 60;
    private float[][][] input = new float[1][seqLength][6];

    private long[] lastTime = new long[2];
    private long firstTapTime = 0;
    private int skipNum = seqLength;
    private boolean secondFlag = false;

    private MappedByteBuffer firstModel;
    private MappedByteBuffer secondModel;
    private Interpreter firstInterpreter;
    private Interpreter secondInterpreter;
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private float[][] output = new float[1][3];

    private List<Long> lastRecognizedTime = new ArrayList<>();

    private int MicInstantMode=Announce_Mode;
    public static int MicStateRequest=MIC_NOREQUEST;
    public static int MicState=MIC_OFF;
    private static boolean MicReveseOn=false;//仅在倒置开麦的情况下触发正置关麦

    private TextView mtvMicInstantMode;

    private SoundPool soundPool;

    Intent speechRecognizerIntent;
    private HandlerThread TimerThread;
    private Handler TimerHandler;
    private int Timer_time=0;

    Thread SensorService_thread;
    Thread FloatingWindow_thread;
    Thread SpeechDetector_thread;
    Thread VoicePrint_thread;
    //Thread MicOnTimer_thread;

    private HandlerThread MicOnTimerThread;
    private Handler MicOnTimerHandler;
    private int MicOnTimer_time=0;
    private int MicOnTimerState=MicOnTimerState_INACTIVE;
    private final static int MicOnTimerState_INACTIVE=1;
    private final static int MicOnTimerState_ACTIVE=2;

    private static int MicOnNoticeMode=MicOnNoticeMode_Audio;//way to notice when mic on

    //AudioManager audiomanage = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

    Handler FloatingWindow_handler=new Handler(Looper.getMainLooper());

    private int VoicePrintFlag=0;
    public static int VoicePrint_CREATEFEATURE1=0;
    public static int VoicePrint_CREATEFEATURE2=1;
    public static int VoicePrint_CHECKFEATURE=2;
    public static int VoicePrint_CHECKGROUP=3;

    private VoicePrint voicePrint=new VoicePrint();

    private String recordPath = "";
    final RecordManager recordManager = RecordManager.getInstance();
    //final RecordManager recordManager2 = RecordManager.getInstance();

    private static boolean VoicePrintIsRecording = false;
    private static boolean feature1Created = false;
    private static boolean feature2Created = false;

    //private TMAccessibilityService TMcontrol=new TMAccessibilityService();



    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerHandler.postDelayed(mRunnable, 1000);
            Log.i(TAG,"recorder timer");

            MainActivity.this.runOnUiThread(
                    () -> {
                        mtvTest.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND - mStart));
                        mStart += 1;
                    });
        }
    };


    private Runnable SensorService_runnable =new Runnable() {
        @Override
        public void run() {
            initSensor();
        }
    };


    Runnable FloatingWindow_runnable =new Runnable() {
        @Override
        public void run(){
            //TmAccessibilityService.initFloatingWindow();
            try {
                startService(new Intent(MainActivity.this, FloatWindow.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    Runnable MicOnTimer_runnable=new Runnable() {
        @Override
        public void run() {
            MicOnTimerHandler.postDelayed(MicOnTimer_runnable, 1000);
            if(MicState==MIC_ON&&!MicReveseOn&&MicOnTimerState==MicOnTimerState_INACTIVE) {
                MicOnTimerState=MicOnTimerState_ACTIVE;
                Log.i(TAG,"Start when mic is on!");
                MicTimerOn_ExceedTime=MicTimerOn_ExceedTime_Max;
                StartRecordManager(recordManager);
                VoicePrintIsRecording = true;
            }
            if (MicOnTimerState == MicOnTimerState_ACTIVE) {
                if (!VoicePrintIsRecording){
                    StartRecordManager(recordManager);
                }
                MicOnTimer_time += 1;
                Log.i(TAG, "MicOnTimer running:" + MicOnTimer_time);
//                if (MicOnTimer_time == 2) {
//                    //MicOnTimer_time = 0;
//                    if(recording == true) {
//                        Log.i(TAG, "stop when mictimer zero!");
//                        recordManager.stop();
//                        recording = false;
//                    }
                if (MicOnTimer_time>MicTimerOn_ExceedTime&&!MicReveseOn) {

                    changeMicState(MIC_OFF);
                    Utils.writeTxtToFile(Utils.GetSystemTime()+" "+"MicOnTimer_runnable::No voice print detected, switch mic off","/logs","/log.txt");
                }
                    //stopMicOnTimer();
                    //speechRecognizer.stopListening();
//                }
//                else if (MicOnTimer_time == 4){
//                    MicOnTimer_time = 0;
//                    if(recording == false) {
//                        StartRecordManager(recordManager);
//                        Log.i(TAG, "MicOnTimer triggered:" + MicOnTimer_time);
//                    }
//                    recording = true;
//                }
            }
        }
    };
    public void ChaneMicOnTimerState(int MicOnTimerState) {
        MicOnTimer_time = 0;
        MicTimerOn_ExceedTime=MicTimerOn_ExceedTime_Max;
         if (MicOnTimerState == MicOnTimerState_INACTIVE && this.MicOnTimerState == MicOnTimerState_ACTIVE&& VoicePrintIsRecording == true){
                Log.i(TAG,"Stop when micontimer is off!");
                recordManager.stop();
             VoicePrintIsRecording = false;
            }
        this.MicOnTimerState=MicOnTimerState;
        Log.i(TAG, "MicOnTimer state is "+ MicOnTimerState);

    }
    protected void stopMicOnTimer() {
        if (MicOnTimerThread!=null) {
            MicOnTimerThread.quitSafely();
            try {
                MicOnTimerThread.join();
                MicOnTimerThread = null;
                MicOnTimerHandler = null;
                MicOnTimer_time = 0;
                Log.e(TAG, "MicOnTimer stop");
            } catch (InterruptedException e) {
                Log.e(TAG, "Error on stopping background thread", e);
            }
        }
    }


    protected boolean CheckMicOnTimerRunning(){
        if(MicOnTimerThread!=null){
            return true;
        }
        else {
            return false;
        }
    }
    private void startMicOnTimer(){
        //Thread thread = new Thread(MainActivity.this);
        //thread.start();
        MicOnTimerThread = new HandlerThread("ClockTimer");
        MicOnTimerThread.start();
        MicOnTimerHandler = new Handler(MicOnTimerThread.getLooper());
        MicOnTimerHandler.postDelayed(MicOnTimer_runnable, 1000);
        Log.e(TAG, "startMicOnTimer");
    }


    protected void stopTimerThread() {
        mTimerThread.quitSafely();
        try {
            mTimerThread.join();
            mTimerThread = null;
            mTimerHandler = null;
            mStart = 1;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error on stopping background thread", e);
        }
    }


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.imgbtnSorP);
        mButton.setImageResource(R.drawable.icon2);
        mImgvEmoState=findViewById(R.id.imgvEmoState);
        mImgbExtractFeature=findViewById(R.id.imgbtnExtractFeature);
        mImgbCheckFeature=findViewById(R.id.imgbtnCheckFeature);
        mtvEmoState = findViewById(R.id.tvState);
        mtvTest=findViewById(R.id.tvTest);
        mprobarEmoInference=findViewById(R.id.probarEmoInference);

        mtvMicInstantMode = findViewById(R.id.tvMicInstantMode);
        mprobarEmoInference.setProgress(1);
//        Intent intent = new Intent();
//        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//        intent.setData(Uri.parse("package:" +this.getPackageName()));
//        startActivityForResult(intent, 0x1000);



        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO})
                .start();

        mImgbCheckFeature.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.i(TAG,"Feature ready!");
                VoicePrintFlag = VoicePrint_CREATEFEATURE2;
                TmAccessibilityService.mService.SetMicMute(false);
                StartRecordManager(recordManager);
                toast("Record voice print - User 2...");
            }
        });

        mImgbExtractFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"Feature ready!");
                VoicePrintFlag = VoicePrint_CREATEFEATURE1;
                TmAccessibilityService.mService.SetMicMute(false);
                StartRecordManager(recordManager);
                toast("Record voice print - User 1...");
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                //mButton.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND));
//                mButton.setEnabled(false);
//
//                Thread thread = new Thread(MainActivity.this);
//                thread.start();
//
//                mTimerThread = new HandlerThread("Timer");
//                mTimerThread.start();
//                mTimerHandler = new Handler(mTimerThread.getLooper());
//                mTimerHandler.postDelayed(mRunnable, 1000);
//                if(mButtonisPlay){
//                    ((ImageButton)v).setImageResource(R.drawable.icon2);
//                    mtvTest.setText("Stop Run");
//                }
//                else{
//                    ((ImageButton)v).setImageResource(R.drawable.icon1);
//                    mtvTest.setText("Recording");
//                }
//                mButtonisPlay = !mButtonisPlay;
                if (MicInstantMode==Announce_Mode){
                    MicInstantMode=GreatMeeting_Mode;
                    mtvMicInstantMode.setText("       GreatMeeting");
                }
                else if(MicInstantMode==GreatMeeting_Mode){
                    MicInstantMode=HandFree_mode;
                    mtvMicInstantMode.setText("       HandFree");
                }
                else if(MicInstantMode==HandFree_mode){
                    MicInstantMode=Announce_Mode;
                    mtvMicInstantMode.setText("   AnnounceMode");
                }
                mButtonisPlay = !mButtonisPlay;
            }
        });
//        mImgbExtractFeature.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                startVoicePrint(VoicePrint_CREATEFEATURE);
//                mButtonisPlay = !mButtonisPlay;
//
//            }
//        });
//        mImgbCheckFeature.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                startVoicePrint(VoicePrint_CHECKFEATURE);
//                mButtonisPlay = !mButtonisPlay;
//            }
//        });
        //requestMicrophonePermission();

        //initSensor();
        //initSpeechDetector();

        Log.i(TAG, "Main::oncreate done ");

        Utils.writeTxtToFile(Utils.GetSystemTime()+" "+"Main::oncreate done ","MainActivity.this.getFilesDir()","/log.txt");
        //startTimer();

        //initAccessibility(this.getApplicationContext(),"TMAccessibilityService");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0x1000){
            if (Environment.isExternalStorageManager()) {
                Log.i(TAG, "quanxianchenggong");
                initVoicePrint();
                initRecord();
                startMicOnTimer();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utils.writeTxtToFile("onResume ","/logs","log.txt");
        if (!TmAccessibilityService.isStart()) {
            try {
                this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                e.printStackTrace();
            }
        }
        else {
            InitafterTmConnected();
        }
    }
    public void InitafterTmConnected(){
        if(SensorService_thread==null) {
            requestStoragePermission();
            initSoudpool();
            SensorService_thread = new Thread(SensorService_runnable);
            SensorService_thread.start();
        }
        if(FloatingWindow_thread==null){
            if(Settings.canDrawOverlays(this)){
                Log.i(TAG,"start floating window...");
                FloatingWindow_thread=new Thread(FloatingWindow_runnable);
                FloatingWindow_thread.start();
                //SpeechDetector_thread=new Thread(SpeechDetector_runnable);
                //SpeechDetector_thread.start();
                //initSpeechDetector();
                //SpeechDetect();
                //System.out.println(FloatWindow.isStart());
                TmAccessibilityService.mService.SetMicMute(false);
                initVoicePrint();
                initRecord();
                startMicOnTimer();
                if((!feature1Created)&&(!feature2Created)){
                    toast("No voice recorded, please record to proceed");
                }
                else {
                    startApp("com.tencent.wemeet.app");
                    Utils.writeTxtToFile(" start com.tencent.wemeet.app1", "/logs", "log.txt");
                }


//                    TmAccessibilityService.mService.startFloatingWindow();
            }
            else {
                requestFloatingWindow();
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void startApp(String packname){
        PackageManager packageManager = getPackageManager();
        if (checkPackInfo(packname)) {
            Intent intent = packageManager.getLaunchIntentForPackage(packname);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "没有安装" + packname, 1).show();
        }
    }
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }


    private void requestFloatingWindow(){
        Toast.makeText(this, "Please permit float window...", Toast.LENGTH_SHORT);
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 0);
    }


    private void initSoudpool(){
        soundPool= new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        soundPool.load(this,R.raw.sound_micon,1);
        soundPool.load(this,R.raw.sound_micoff,2);
    }

    public void initSensor(){
        //倒置控制
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 获取方向传感器
        mSensorOrientation = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //注册数值变化监听器
        sm.registerListener(sensListener, mSensorOrientation,SensorManager.SENSOR_DELAY_UI);

        //PatPat控制
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(gyroListener, gyroSensor, samplingPeriod);
        Sensor linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(linearAccListener, linearAccSensor, samplingPeriod);

        tfliteOptions.setNumThreads(4);
        try {
            firstModel = FileUtil.loadMappedFile(getApplicationContext(), String.format("first.tflite"));
            //firstModel = FileUtil.loadMappedFile(assetFilePath(getApplicationContext(), "Model2.ptl"));
            firstInterpreter = new Interpreter(firstModel, tfliteOptions);
        } catch (IOException e) {
            Log.e("Load Model", "model load fail");
            e.printStackTrace();
            System.out.println("first model load fail");
        }
        try {
            secondModel = FileUtil.loadMappedFile(getApplicationContext(), String.format("second.tflite"));
            secondInterpreter = new Interpreter(secondModel, tfliteOptions);
        } catch (IOException e) {
            Log.e("Load Model", "model load fail");
            e.printStackTrace();
            System.out.println("second model load fail");
        }
    }

    private class SensListener extends AppCompatActivity implements SensorEventListener {
        private float Mx,My,Mz;

        // 传感器数值变化会调用此方法

        @Override
        public void onSensorChanged( SensorEvent event) {
            Mx=(float) (Math.round(event.values[0] * 100)) / 100;
            My=(float) (Math.round(event.values[1] * 100)) / 100;//手机纵向地磁加速度
            Mz=(float) (Math.round(event.values[2] * 100)) / 100;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mtvTest.setText(String.valueOf(Mx)+"\n"+String.valueOf(My)+"\n"+String.valueOf(Mz));
                }

            });
            if(MicInstantMode==GreatMeeting_Mode || MicInstantMode==Announce_Mode){
                CheckGesture();
            }

        }

        private void CheckGesture(){
            //boolean make_a_change = false;
            if (My<-6 && Math.abs(My)>(Math.abs(Mx+Mz)*0.5)){
                if(!MicReveseOn) {
                    MicReveseOn=true;
                    changeMicState(MIC_ON);
                    Utils.writeTxtToFile(Utils.GetSystemTime()+" "+"CheckGesture::Reverse detected, switch mic on. ","/logs","/log.txt");
                    Log.i(TAG, "CheckGesture:Phone reverted");
                }
                if (CheckMicOnTimerRunning()) {
                    ChaneMicOnTimerState(MicOnTimerState_INACTIVE);
                }
                MicReveseOn=true;
            }
            else if(My>6 && Math.abs(My)>(Math.abs(Mx+Mz)*0.5) ){
                if(MicState==MIC_ON &&MicReveseOn) {
                    changeMicState(MIC_OFF);
                    Utils.writeTxtToFile(Utils.GetSystemTime()+" "+"CheckGesture::Reverse cancelled, switch mic off. ","/logs","/log.txt");
                    Log.i(TAG, "CheckGesture:Phone returned");
                }
                MicReveseOn=false;

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

    private int changeMicState(int Micstate){
        if (MicStateRequest==MIC_NOREQUEST) {
            MicStateRequest=MIC_INREQUEST;
            int res;
            String MicHint = "";
            //startTimer();
            if (Micstate == MicState) {
                Log.i(TAG,"changeMicState: request already done, drop this request...now Mic state:"+String.valueOf(MicState));

            } else {
                if (Micstate == MIC_ON) {

                    res = R.drawable.mic_on;
                    if (TmAccessibilityService.mService.CheckMicOn() == 0) {
                        TmAccessibilityService.mService.ClickView(TmAccessibilityService.mService.vid_InMeeting_Mic);
                    }
                    TmAccessibilityService.mService.SetMicMute(false);
                    ChaneMicOnTimerState(MicOnTimerState_ACTIVE);

                    MicState = MIC_ON;
                    if (MicOnNoticeMode==MicOnNoticeMode_Audio) {
                        soundPool.play(1, 1, 1, 0, 0, 1);
                    }
                    else if(MicOnNoticeMode==MicOnNoticeMode_Vibration){
                        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    // recordManager.start();
                    MicHint = "Microphone ON!";

                } else if (Micstate == MIC_OFF) {

                    res = R.drawable.mic_off;
                    if (TmAccessibilityService.mService.CheckMicOn() == 1) {
                        TmAccessibilityService.mService.ClickView(TmAccessibilityService.mService.vid_InMeeting_Mic);
                        //audiomanage.setMicrophoneMute(true);
                    }
                    TmAccessibilityService.mService.SetMicMute(true);
                    ChaneMicOnTimerState(MicOnTimerState_INACTIVE);
                    MicState = MIC_OFF;
                    MicReveseOn=false;
                    if (MicOnNoticeMode==MicOnNoticeMode_Audio) {
                        soundPool.play(2, 1, 1, 0, 0, 1);
                    }
                    else if(MicOnNoticeMode==MicOnNoticeMode_Vibration){
                        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    MicHint = "Microphone OFF!";
                    // recordManager.stop();
                } else {
                    MicState = MIC_OFF;
                    res = R.drawable.mic_off;
                    if (TmAccessibilityService.mService.CheckMicOn() == 1) {
                        TmAccessibilityService.mService.ClickView(TmAccessibilityService.mService.vid_InMeeting_Mic);
                    }
                    TmAccessibilityService.mService.SetMicMute(true);

                    ChaneMicOnTimerState(MicOnTimerState_INACTIVE);
                    // recordManager.stop();
                    MicReveseOn=false;
                    MicHint = "Microphone OFF!";
                }
                mImgvEmoState.setImageResource(res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(Micstate==MIC_ON) {
                            String MicHint = "Microphone ON!";
                            mtvEmoState.setText(MicHint);
                        }
                        else {
                            String MicHint = "Microphone OFF!";
                            mtvEmoState.setText(MicHint);
                        }
                    }
                });

            }
            MicStateRequest=MIC_NOREQUEST;
            return ChangeMicState_Changed;
        }
        else {
            Log.i(TAG,"changeMicState: MicStateRequest==MIC_INREQUEST, drop this request...");
            return ChangeMicState_Drop_InRequest;
        }

    }

    private void initRecord() {
        if(Build.VERSION.SDK_INT==29){
            VoicePrint_ScoreThreshold= (float) 0.2;
        }
        recordManager.init(MyApp.getInstance(), true);
        recordManager.changeFormat(RecordConfig.RecordFormat.MP3);
//        String recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
//                Environment.getExternalStorageDirectory().getAbsolutePath());
//        String recordDir = String.format(Locale.getDefault(), "%s",
//                MainActivity.this.getExternalFilesDir("assets").getAbsolutePath());
        String recordDir = Objects.requireNonNull(MainActivity.this.getExternalFilesDir(INTERNAL_FILESAVEPATH)).getAbsolutePath();
        Log.i(TAG,"recordManager save path: "+recordDir);
//        String recordDir = String.format(MainActivity.this.getFilesDir().getAbsolutePath());
        recordManager.changeRecordDir(recordDir);
        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                recordPath = result.getAbsolutePath();
                Log.i(TAG, "录音文件： " + recordPath);
                startVoicePrint();
            }
        });
    }

    public void StartRecordManager(RecordManager irecordmanager){
        VoicePrintIsRecording=true;
        Thread ithread =new Thread(new Runnable() {
            @Override
            public void run() {
                irecordmanager.start();
                try {
                    Thread.sleep(VoicePrint_RecordTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                irecordmanager.stop();
                VoicePrintIsRecording=false;
                //ithread.stop();

            }
        });
        ithread.start();
    }

    private Runnable SpeechDetector_runnable =new Runnable() {
        @Override
        public void run() {
            initSpeechDetector();
        }
    };

    private void initSpeechDetector(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }
            @Override
            public void onBeginningOfSpeech() {
                System.out.println("start detecting...");
            }
            @Override
            public void onRmsChanged(float v) {
            }
            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }
            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data.size()<1){
                    Log.i(TAG,"no speech detected!");
                }
                else {
                    for (int i = 0; i < data.size(); i++) {
                        System.out.println(data.get(i));
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
    }

    private void SpeechDetect(){

        speechRecognizer.startListening(speechRecognizerIntent);
        startTimer();

    }

    private Runnable TimerRunnable = new Runnable() {
        @Override
        public void run() {
            TimerHandler.postDelayed(TimerRunnable, 1000);
            Timer_time+=1;
            Log.e(TAG, "Timer running");
            if(Timer_time>5){
                stopTimer();
                Log.i(TAG,"Timer:"+Timer_time);
                //speechRecognizer.stopListening();
            }
        }
    };
    protected void stopTimer() {
        TimerThread.quitSafely();
        try {
            TimerThread.join();
            TimerThread = null;
            TimerHandler = null;
            speechRecognizer.stopListening();
            Timer_time = 0;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error on stopping background thread", e);
        }
    }
    private void startTimer(){
        //Thread thread = new Thread(MainActivity.this);
        //thread.start();

        TimerThread = new HandlerThread("ClockTimer");
        TimerThread.start();
        TimerHandler = new Handler(TimerThread.getLooper());
        TimerHandler.postDelayed(TimerRunnable, 1000);
        Log.e(TAG, "startTimer");
    }

    private void initAccessibility(Context ct, String serviceClass){
        boolean haspermisssion=false;
        int ok = 0;
        try {
            ok = Settings.Secure.getInt(ct.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
        if (ok == 1) {
            String settingValue = Settings.Secure.getString(ct.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    if (accessibilityService.contains(serviceClass)) {
                        haspermisssion=true;
                    }
                }
            }
        }

        if (!haspermisssion){
            // jump to setting permission
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ct.startActivity(intent);
        }
    }

    //control tencent meeting
//    public static class TMAccessibilityService extends AccessibilityService{
//        private final String TAG = MainActivity.class
//                .getSimpleName();
//        private final String packageName="com.tencent.wemeet.app:id/";
//
//        @Override
//        public void onAccessibilityEvent(AccessibilityEvent event) {
//            Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());
//
//            //TYPE_WINDOW_STATE_CHANGED == 32
//            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
//                    .getEventType()) {
//                AccessibilityNodeInfo nodeInfo = event.getSource();
//                Log.i(TAG, "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo);
//                if (nodeInfo == null) {
//                    return;
//                }
//
//                List<AccessibilityNodeInfo> list = nodeInfo
//                        .findAccessibilityNodeInfosByViewId(packageName+"i9");
//                for (AccessibilityNodeInfo node : list) {
//                    Log.i(TAG, "ACC::onAccessibilityEvent: left_button " + node);
//                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//
//                list = nodeInfo
//                        .findAccessibilityNodeInfosByViewId("android:id/button1");
//                for (AccessibilityNodeInfo node : list) {
//                    Log.i(TAG, "ACC::onAccessibilityEvent: button1 " + node);
//                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//
//        }
//
//        @Override
//        public void onServiceConnected() {
//            Log.i(TAG, "ACC::onServiceConnected: ");
//        }
//        @Override
//        public void onInterrupt() {
//            // TODO Auto-generated method stub
//        }
//    };



    private SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (MicInstantMode==HandFree_mode|| MicInstantMode==Announce_Mode){
                addSensorData(0, event.values[0], event.values[1], event.values[2], event.timestamp);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener linearAccListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (MicInstantMode==HandFree_mode|| MicInstantMode==Announce_Mode) {
                addSensorData(1, event.values[0], event.values[1], event.values[2], event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void addSensorData(int idx, float x, float y, float z, long timestamp) {
        if (timestamp < lastTime[idx] + 3 * 1e6)
            return;
        lastTime[idx] = timestamp;
        for (int i = 0; i < seqLength - 1; i++)
            System.arraycopy(input[0][i + 1], 3 * idx, input[0][i], 3 * idx, 3);
        input[0][seqLength - 1][3 * idx] = x;
        input[0][seqLength - 1][3 * idx + 1] = y;
        input[0][seqLength - 1][3 * idx + 2] = z;
        if (idx == 1) {
            if (skipNum > 0)
                skipNum--;
                // 进行识别
            else {
                recognizeFirst();
                recognizeSecond();
            }
        }

    }

    private void recognizeFirst() {
        int offset = 5;
        float value = input[0][seqLength - offset][5];
        if (value < 0.5)
            return;
        for (int i = 1; i <= 10; i++)
            if (value < input[0][seqLength - offset - i][5])
                return;
        for (int i = 1; i < offset; i++)
            if (value < input[0][seqLength - offset + i][5])
                return;
        float[][][] firstInput = new float[1][10][6];
        float[][] firstOutput = new float[1][2];
        for (int i = -5; i < 5; i++)
            System.arraycopy(input[0][seqLength - offset + i], 0, firstInput[0][5 + i], 0, 6);
        firstInterpreter.run(firstInput, firstOutput);
        if (firstOutput[0][1] < 0.99)
            return;
        if (!secondFlag) {
            secondFlag = true;
            skipNum = 8;
        }
        firstTapTime = lastTime[1];
    }

    private void recognizeSecond() {
        if (!secondFlag || skipNum > 0)
            return;
        if (lastTime[1] - firstTapTime > 600 * 1e6) {
            secondFlag = false;
            Log.e("Recognize", "!!!-1!!!");
            return;
        }
        boolean make_a_change = false;
        secondInterpreter.run(input, output);
        if (output[0][1] > output[0][0] && output[0][1] > output[0][2]) {
            //Toast.makeText(this, "TapTap", Toast.LENGTH_SHORT).show();
            //mtvTest.setText(TestText);
            if(!MicReveseOn) {
//                if(MicState == MIC_OFF){
//                    make_a_change = true;
//                }
                changeMicState(MIC_ON);
//                try {
//                    TmAccessibilityService.mService.toFileRecorder_byAR(MainActivity.this.getExternalFilesDir(INTERNAL_FILESAVEPATH));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }

                Utils.writeTxtToFile(Utils.GetSystemTime()+" "+"PatPat recognizeSecond::Patpat detected, switch mic on. ","/logs","/log.txt");
            }

            //startTimer();
            Log.e("Recognize", "!!!TapTap!!!");
        }
        else
            return;
        secondFlag = false;
        skipNum = 30;
        for (int i = 0; i < seqLength; i++)
            for (int j = 0; j < 6; j++)
                input[0][i][j] = 0;
        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
//        if(make_a_change == true){
//            Log.i(TAG,"begin recording when taptap!");
//            //recordManager.start();
//            //recording = true;
//        }
    }

    @Override
    protected void onDestroy() {
        stopTimerThread();
        TmAccessibilityService.mService.SetMicMute(false);
        if (sensorManager != null) {
            sensorManager.unregisterListener(gyroListener);
            sensorManager.unregisterListener(linearAccListener);
        }

        if (firstInterpreter != null) {
            firstInterpreter.close();
            firstInterpreter = null;
        }
        firstModel = null;

        if (secondInterpreter != null) {
            secondInterpreter.close();
            secondInterpreter = null;
        }
        secondModel = null;

        if (vibrator != null)
            vibrator.cancel();
        super.onDestroy();
    }


    //权限申请
    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, REQUEST_EXTERNAL_STORAGE);
//            requestPermissions(
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, assetName + ": " + e.getLocalizedMessage());
        }
        return null;
    }

    //刷新文件目录，使指定uri文件可见
    private void refreshFilelist(File file){
        Intent intent =
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    private void saveFile(String data,String path,String fileName) throws IOException {


        File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fullpath=new File(dir,"/myEmovo/log");
        if (!fullpath.exists()){
            fullpath.mkdirs();
        }

        File file = new File(fullpath,fileName);
        try {
            if (!file.exists()){
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        FileOutputStream fos = new FileOutputStream( file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        writer.write(data);
        writer.flush();
        writer.close();
        MediaScannerConnection.scanFile(this,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }


    private void log(float result){
        String.valueOf(result);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };


//    private static void verifyStoragePermissions(Activity activity) {
//        try {
//        //检测是否有写的权限
//        int permission = ActivityCompat.checkSelfPermission(activity,"android.permission.WRITE_EXTERNAL_STORAGE");
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//            // 没有写的权限，去申请写的权限，会弹出对话框
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            }
//
//        }


    private void showTranslationResult(String result) {
        mtvTest.setText(result);
    }
    private void changeEmoState(int EmoState){
        int res;
        String EmoHint="";
        if (EmoState==EMO_NEUTRAL) {
            res = R.drawable.emoji_neutral;
            EmoHint="I am satisfied!";
        }
        else if(EmoState==EMO_ANGER){
            res=R.drawable.emoji__anger;
            EmoHint="I am angry!";
        }
        else {
            res=R.drawable.emoji__anger;
            EmoHint="I am satisfied!";
        }
        mImgvEmoState.setImageResource(res);
        mtvEmoState.setText(EmoHint);

    }



    private void realtimeRecorder(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            throw new IllegalStateException();
            //return;
        }
        record.startRecording();

        long shortsRead = 0;
        int recordingOffset = 0;
        float[] audioBuffer = new float[bufferSize / 2];
        float[] recordingBuffer = new float[RECORDING_LENGTH];

        while (shortsRead < RECORDING_LENGTH/audioBuffer.length*audioBuffer.length) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length,AudioRecord.READ_NON_BLOCKING);
            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }

        record.stop();
        record.release();
        stopTimerThread();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvTest.setText("Recognizing...");
            }
        });

//        int min = (int) Collections.min(Arrays.asList(recordingBuffer));
//        int max = (int) Collections.max(Arrays.asList(recordingBuffer));
//        System.out.println("最小值: " + min);
//        System.out.println("最大值: " + max);

        //send data to chaquo preprocess module
        Python py=Python.getInstance();
        PyObject data=py.getModule("DataPre").callAttr("Preprocess",recordingBuffer);
        float[] inputfloat =data.toJava(float[].class);
        //recognize
        final String result = recognize(inputfloat,(int)562);

        try {
            saveFile(result,"/logs","log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetProgress(mprobarEmoInference,mprobarEmoInference.getProgress(), (int) (EmoInference*100));
                showTranslationResult(result);
                changeEmoState(EmoState);
                mButton.setEnabled(true);
                mtvTest.setText("Recording");

            }
        });

    }

    private void initVoicePrint(){
        Thread initVPthread=new Thread(new Runnable() {
            @Override
            public void run() {
                voicePrint.vpCreateGroup();
            }
        });
        initVPthread.start();

    }

    private Runnable VoicePrint_runnable =new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"VoicePrint_runnable: start..");
            Looper.prepare();
            //toFileRecorder_byAR(MainActivity.this.getExternalFilesDir(INTERNAL_FILESAVEPATH));
            String currentPath = recordPath;
            Log.i(TAG,"操作文件 "+currentPath);
            if (VoicePrintFlag==VoicePrint_CREATEFEATURE1){
                feature1Created = voicePrint.vpCreateFeature(currentPath,VoicePrint_CREATEFEATURE1);
                VoicePrintFlag=VoicePrint_CHECKFEATURE;
                if (feature1Created){
                    toast("Voice print1 recorded!");
                }
                else {
                    toast("Failed to record voice print1...");
                }
            }
            else if (VoicePrintFlag==VoicePrint_CREATEFEATURE2){
                feature2Created = voicePrint.vpCreateFeature(currentPath,VoicePrint_CREATEFEATURE2);
                VoicePrintFlag=VoicePrint_CHECKFEATURE;
                if (feature2Created){
                    toast("Voice print2 recorded!");
                }
                else {
                    toast("Failed to record voice print2...");
                }

            }
            else if(VoicePrintFlag==VoicePrint_CHECKFEATURE){
                if (feature1Created&&feature2Created){
                    boolean FoundVoicePrint = voicePrint.vpSearchFeature(currentPath);
                    Log.i(TAG,"score=="+String.valueOf(FoundVoicePrint));
                    if(FoundVoicePrint){
                        if(MicState == MIC_ON && !MicReveseOn){
                            MicOnTimer_time=0;
                            MicTimerOn_ExceedTime=MicTimerOn_ExceedTime_Min;
                            //changeMicState(MIC_OFF);
                            Log.i(TAG,"User speech detected...continue Mic on");
                        }
                    }
                }
                else {
                    float score = voicePrint.vpSearchOneFeature(currentPath);
                    Log.i(TAG, "score==" + String.valueOf(score));
                    if (score > VoicePrint_ScoreThreshold) {
                        if (MicState == MIC_ON && !MicReveseOn) {
                            MicOnTimer_time = 0;
                            MicTimerOn_ExceedTime=MicTimerOn_ExceedTime_Min;
                            //changeMicState(MIC_OFF);
                            Log.i(TAG, "User speech detected...continue Mic on");
                        }
                    }

                }
            }
            else {
                Log.i(TAG,"Warning:in VoicePrint_runnable empty VoicePrintFlag!");
            }


            //VoicePrintIsRecording=false;
        }
    };

    private void startVoicePrint(){
        // this.VoicePrintFlag=VoicePrintFlag;
        VoicePrint_thread=new Thread(VoicePrint_runnable);
        VoicePrint_thread.start();
    }

    private void toFileRecorder_byAR(File Path) throws FileNotFoundException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            throw new IllegalStateException();
            //return;
        }
        record.startRecording();

        long shortsRead = 0;
        int recordingOffset = 0;
        short[] audioBuffer = new short[bufferSize / 2];
        short[] recordingBuffer = new short[RECORDING_LENGTH];

        while (shortsRead < RECORDING_LENGTH/audioBuffer.length*audioBuffer.length) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length,AudioRecord.READ_NON_BLOCKING);
            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }

        record.stop();
        record.release();
        //stopTimerThread();

        Wave wavFile= new Wave(SAMPLE_RATE, (short) 1,recordingBuffer,0,recordingBuffer.length-1);
        File fullpath =null;
        if(Build.VERSION.SDK_INT ==29 ) {
            fullpath=new File(Path,"/MicInstant");
        }
        else {
            fullpath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "/MicInstant");
        }
        Utils.makeRootDirectory(fullpath);
        fullpath=new File(fullpath,"/record");
        Utils.makeRootDirectory(fullpath);
        File dir=new File(fullpath,"record.wav");
        //System.out.println(dir);
        //File dir=new File("/data/data/com.pytorch.demo.speechrecognition/files/chaquopy/AssetFinder/app","record.wav");
        if (!dir.exists()){
            System.out.println("warning:dir not exits!");
        }
        wavFile.wroteToFile(dir);
        FileInputStream ios=new FileInputStream(dir);

//        int min = (int) Collections.min(Arrays.asList(recordingBuffer));
//        int max = (int) Collections.max(Arrays.asList(recordingBuffer));
//        System.out.println("最小值: " + min);
//        System.out.println("最大值: " + max);

//        Python py=Python.getInstance();
//        PyObject data=py.getModule("DataPre").callAttr("Preprocess2",ios);
//        float[] inputfloat =data.toJava(float[].class);
//
//        final String result = recognize(inputfloat,(int)562);
//        //save(result,"log.txt");
//        try {
//            saveFile(result,"/logs","log.txt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                showTranslationResult(result);
//                mButton.setEnabled(true);
//                //mTextView.setText("Start");
//            }
//        });

    }

    //
    private class MyTimerTask extends TimerTask {
        private MediaRecorder recorder=null;
        private File filedir=null;
        MyTimerTask(MediaRecorder recorder,File filedir){
            this.recorder=recorder;
            this.filedir=filedir;
        }
        public void run() {
            recorder.stop();
            recorder.release();

            Python py=Python.getInstance();
            PyObject data=py.getModule("DataPre").callAttr("Preprocess2",filedir);
            float[] inputfloat =data.toJava(float[].class);


            final String result = recognize(inputfloat,(int)562);
            //save(result,"log.txt");
            try {
                saveFile(result,"/logs","log.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTranslationResult(result);
                    mButton.setEnabled(true);
                    mtvTest.setText("Start");
                }
            });



        }
    }
    //先将录音存为音频，再用librosa直接读取
    private void toFileRecorder_byMR(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        final MediaRecorder recorder = new MediaRecorder();
        //ContentValues values = new ContentValues(3);
        //values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //recorder.setMaxDuration(AUDIO_LEN_IN_SECOND);
        File fullpath=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/myEmovo/record");
        if (!fullpath.exists()){
            fullpath.mkdirs();
        }

        File dir=new File(fullpath,"record.m4a");
        recorder.setOutputFile(dir);
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
            while (true){
                System.out.println("prepare wrong!");
            }

        }
        recorder.start();

        Timer timer = new Timer();
        timer.schedule(new MyTimerTask(recorder,dir), 2000, 5000);
    }

    public void run() {
        Log.i(TAG,"recorder running...");
        //toFileRecorder_byMR();
        //realtimeRecorder();
//        try {
//            toFileRecorder_byAR();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    }

    private String recognize(float[] floatInputBuffer,int shape0) {
        if (mModuleEncoder == null) {

            mModuleEncoder = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "Model2.ptl"));
        }
        int Length=floatInputBuffer.length;
        double wav2vecinput[] = new double[Length];
        for (int n = 0; n < Length; n++)
            wav2vecinput[n] = floatInputBuffer[n];

        FloatBuffer inTensorBuffer = Tensor.allocateFloatBuffer(Length);
        for (double val : wav2vecinput)
            inTensorBuffer.put((float)val);

        Tensor inTensor = Tensor.fromBlob(inTensorBuffer, new long[]{(int)Length/shape0,(int)Length/shape0, shape0});
        Tensor ResultTensor=mModuleEncoder.forward(IValue.from(inTensor)).toTensor();
        float[] ResultFloat=ResultTensor.getDataAsFloatArray();
        float result =ResultFloat[0]/(ResultFloat[0]+ResultFloat[1]);
        this.EmoInference=result;
        if (result<0.5){
            this.EmoState=EMO_NEUTRAL;
        }
        else if (result>0.5){
            this.EmoState=EMO_ANGER;
        }


        return String.valueOf(result);
    }

    public void SetProgress(final ProgressBar view, int startprogress, int endprogress) {//进度条的控件，以及起始的值

        view.setVisibility(View.VISIBLE);


        ValueAnimator animator = ValueAnimator.ofInt(startprogress, endprogress).setDuration(800);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setProgress((int) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }

    public void toast(CharSequence cs) {
        Toast.makeText(this, cs, Toast.LENGTH_SHORT).show();
    }



}