package com.pytorch.demo.speechrecognition;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.pytorch.demo.speechrecognition.iflytek.QueryFeatureList;
import com.pytorch.demo.speechrecognition.iflytek.SearchFeature;
import com.pytorch.demo.speechrecognition.iflytek.SearchOneFeature;
import com.pytorch.demo.speechrecognition.iflytek.CreateFeature;
import com.pytorch.demo.speechrecognition.iflytek.CreateGroup;

import java.io.File;
import java.util.Random;

public class VoicePrint {
    private static String requestUrl = "https://api.xf-yun.com/v1/private/s782b4996";

    //控制台获取以下信息
    private static String APPID = "3f0f566d";
    private static String apiSecret = "NjljY2Y2OTU0NTVlZTFhMDE5YTU0NWRi";
    private static String apiKey = "e860d30ff1c5f572c07731f715464b32";


    private String GroupID="";
    private String FeatureID="";
    private String FeatureID2="";
    VoicePrint(){
        Random r = new Random();
        GroupID=String.valueOf("test_2022_6_2"+r.nextInt(10000));
        FeatureID=String.valueOf("test_2022_6_2"+r.nextInt(10000));
        FeatureID2=String.valueOf("test_2022_6_2"+r.nextInt(10000));
    }

    //音频存放位置(比对功能请注意更换音频)
    private static String AUDIO_PATH = (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/myEmovo/record/record.wav")).getAbsolutePath();//Environment.DIRECTORY_DOWNLOADS+"/myEmovo/record/record.wav";//"audioExample/讯飞开放平台.mp3";

    public void vpCreateGroup(){
        CreateGroup.doCreateGroup(requestUrl,APPID,apiSecret,apiKey,GroupID,FeatureID);
    }

    public boolean vpCreateFeature(String audio_path,int Featureselector){
        String iFeatureID="";
        if(Featureselector==MainActivity.VoicePrint_CREATEFEATURE1){
            iFeatureID=FeatureID;
        }
        else if (Featureselector==MainActivity.VoicePrint_CREATEFEATURE2){
            iFeatureID=FeatureID2;
        }
        else {
            iFeatureID=FeatureID;
        }
        return CreateFeature.doCreateFeature(requestUrl,APPID,apiSecret,apiKey,GroupID,iFeatureID,audio_path);
    }

    public void vpQueryFeatureList(){
        QueryFeatureList.doQueryFeatureList(requestUrl,APPID,apiSecret,apiKey);
    }

    public Float vpSearchOneFeature(String audio_path){
        return SearchOneFeature.doSearchOneFeature(requestUrl,APPID,apiSecret,apiKey,GroupID,FeatureID,audio_path);
    }
    public boolean vpSearchFeature(String audio_path){
        return SearchFeature.doSearchFeature(requestUrl,APPID,apiSecret,apiKey,GroupID,FeatureID,audio_path);
    }

    


    public void main(String[] args) {
        /**1.创建声纹特征库*/
        CreateGroup.doCreateGroup(requestUrl,APPID,apiSecret,apiKey,GroupID,FeatureID);
        /**2.添加音频特征*/
        //CreateFeature.doCreateFeature(requestUrl,APPID,apiSecret,apiKey,AUDIO_PATH);
        /**3.查询特征列表*/
        //QueryFeatureList.doQueryFeatureList(requestUrl,APPID,apiSecret,apiKey);
        /**4.特征比对1:1*/
        //SearchOneFeature.doSearchOneFeature(requestUrl,APPID,apiSecret,apiKey,AUDIO_PATH);
        /**5.特征比对1:N*/
        //SearchFeature.doSearchFeature(requestUrl,APPID,apiSecret,apiKey,AUDIO_PATH);
        /**6.更新音频特征*/
        //UpdateFeature.doUpdateFeature(requestUrl,APPID,apiSecret,apiKey,AUDIO_PATH);
        /**7.删除指定特征*/
        //DeleteFeature.doDeleteFeature(requestUrl,APPID,apiSecret,apiKey);
        /**8.删除声纹特征库*/
        //DeleteGroup.doDeleteGroup(requestUrl,APPID,apiSecret,apiKey);
    }



}
