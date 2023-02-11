package com.pytorch.demo.speechrecognition;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.pytorch.demo.speechrecognition.R;

public class FloatWindow extends Service {
    private static final String TAG = FloatWindow.class.getName();
    private static final String vid_Main_JoinMeeting="h0";
    private static final String vid_Main_FastMeeting="gy";
    private static final String vid_FastMeeting_Video="i9";
    private static final String vid_FastMeeting_Beauty="i8";
    private static final String vid_FastMeeting_BeautySet="jb";//"b20";
    private static final String vid_FastMeeting_PersonalMeetingNum="i_";
    private static final String vid_FastMeeting_StartMeeting="fw";//"a4b";//"fw";
    private static final String vid_FastMeeting_Cancel="b1w";
    private static final String vid_JoinMeeting_cb="ay5";
    private static final String vid_JoinMeeting_vgBeauty="a2j";
    private static final String vid_JoinMeeting_vgMicon="b01";

    public static FloatWindow mService;


    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams layoutParams_JoinMeeting;

    protected FrameLayout mFastMeeting;
    protected FrameLayout mJoinMeeting;
    protected FrameLayout minMeeting;
    private FrameLayout displayedView;


    private Switch vFastMeeting_mswMultiMic;
    private Switch vFastMeeting_mswVideo;
    private Switch vFastMeeting_mswBeauty;
    private Switch vFastMeeting_mswPersonalNum;
    private Button vFastMeeting_mbStartMeeting;
    private Button vFastMeeting_mbBeautySet;
    private Button vFastMeeting_mbCancel;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mService=this;
        //MainActivity.mFloatWindow
        initFloatWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("InflateParams")
    private void initFloatWindow() {
        Log.i(TAG,"initFloatWindow");
        if (Settings.canDrawOverlays(this)) {
            windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

            layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            //lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //改成下面这句话就可以弹出输入法了
            // lp.flags =WindowManager. LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.flags |=WindowManager. LayoutParams.FLAG_LAYOUT_NO_LIMITS|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.gravity |= Gravity.FILL_HORIZONTAL|Gravity.TOP;

            layoutParams_JoinMeeting = new WindowManager.LayoutParams();
            layoutParams_JoinMeeting.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//TYPE_APPLICATION_SUB_PANEL;
            layoutParams_JoinMeeting.format = PixelFormat.TRANSLUCENT;
            //lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //改成下面这句话就可以弹出输入法了
            // lp.flags =WindowManager. LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams_JoinMeeting.flags |=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams_JoinMeeting.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams_JoinMeeting.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //layoutParams_JoinMeeting.verticalMargin=45;
            layoutParams_JoinMeeting.gravity |= Gravity.FILL_HORIZONTAL|Gravity.TOP;


            mFastMeeting = new FrameLayout(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            inflater.inflate(R.layout.floatwindow_tm_fastmeeting, mFastMeeting);

            mJoinMeeting = new FrameLayout(this);
            inflater.inflate(R.layout.floatwindow_tm_joinmeeting, mJoinMeeting);

            minMeeting = new FrameLayout(this);
            inflater.inflate(R.layout.floatwindow_tm_inmeeting, minMeeting);
            //ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_statistic, null,false);

            setView();
            Log.i(TAG,"initFloatWindow Done.");
        }
    }

    public void showFloatWindow(FrameLayout View,int WindowPosition_y){
        if(displayedView!=View) {
            displayedView = View;
            removeFloatWindow();
            if(View==mFastMeeting){
                windowManager.addView(View, layoutParams);
                SynchronizeSwitchState(vFastMeeting_mswVideo,vid_FastMeeting_Video);
                SynchronizeSwitchState(vFastMeeting_mswBeauty,vid_FastMeeting_Beauty);
                SynchronizeSwitchState(vFastMeeting_mswPersonalNum,vid_FastMeeting_PersonalMeetingNum);
            }
            else if(View==mJoinMeeting){
                layoutParams_JoinMeeting.y=WindowPosition_y;
                //layoutParams_JoinMeeting.token = WindowToken; // 必须要
                windowManager.addView(View, layoutParams_JoinMeeting);
            }
            else {
                windowManager.addView(View, layoutParams);
            }

        }
    }

    public void removeFloatWindow(){
        if (displayedView!=null){
            try{
                windowManager.removeView(displayedView);
                displayedView=null;
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }


    private void setView(){
        vFastMeeting_mswVideo=mFastMeeting.findViewById(R.id.swVideo);
        vFastMeeting_mswVideo.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_Video);
            }
        });

        vFastMeeting_mswBeauty=mFastMeeting.findViewById(R.id.swBeauty);

        vFastMeeting_mswBeauty.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_Beauty);
            }
        });

        vFastMeeting_mbBeautySet=mFastMeeting.findViewById(R.id.bBeautySet);
        vFastMeeting_mbBeautySet.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_BeautySet);
                removeFloatWindow();
            }
        });

        vFastMeeting_mswPersonalNum=mFastMeeting.findViewById(R.id.swPersonalNum);

        vFastMeeting_mswPersonalNum.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_PersonalMeetingNum);
            }
        });

        vFastMeeting_mbStartMeeting=mFastMeeting.findViewById(R.id.bStartMeeting);
        vFastMeeting_mbStartMeeting.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_StartMeeting);
                //TmAccessibilityService.mService.ClickViewbyText("进入会议");
                //removeFloatWindow();
            }

        });

        vFastMeeting_mbCancel=mFastMeeting.findViewById(R.id.bCancel);
        vFastMeeting_mbCancel.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onCheckedChanged");
                removeFloatWindow();
                TmAccessibilityService.mService.ClickView(vid_FastMeeting_Cancel);
                //TmAccessibilityService.mService.ClickViewbyText("进入会议");

            }

        });

    }



    private void SynchronizeSwitchState(Switch mSwitch,String ViewID){
        mSwitch.setEnabled(false);
        mSwitch.setChecked(GetSwitchStateinBool(ViewID));
        mSwitch.setEnabled(true);
    }
    private boolean GetSwitchStateinBool(String ViewID){
        String SwitchState=TmAccessibilityService.mService.GetSwitchState(ViewID);
        if (SwitchState.equals("未勾选")){
            return false;
        }
        else if (SwitchState.equals("已勾选")){
            return true;
        }
        else {
            Log.i(TAG,"Warning: Wrong Switch State!"+SwitchState);
            return false;
        }

    }

    public static boolean isStart(){
        return mService!=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService = null;

    }







}
