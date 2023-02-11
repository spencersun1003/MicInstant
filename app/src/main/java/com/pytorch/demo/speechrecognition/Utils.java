package com.pytorch.demo.speechrecognition;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;


public class Utils {

    //集合是否是空的
    public static boolean isEmptyArray(Collection list) {
        return list == null || list.size() == 0;
    }

    public static <T> boolean isEmptyArray(T[] list) {
        return list == null || list.length == 0;
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir=null;
        File fullpath=null;
        System.out.println("SDK ver==="+String.valueOf(Build.VERSION.SDK_INT));
        if(Build.VERSION.SDK_INT ==29 ) {
            dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"/MicInstant");
            //dir=new File("我的手机/Documents/MicInstant");
            fullpath=dir;
            Log.i("Utils","Using file for Harmony");
        }
        else {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            fullpath=new File(dir,"/MicInstant");
        }
        makeRootDirectory(dir);
        System.out.println("dir:"+dir+File.separator);
        makeRootDirectory(fullpath);
        fullpath=new File(fullpath,"/log");
        makeRootDirectory(fullpath);
        //生成文件夹之后，再生成文件，不然会出错
        File file=makeFilePath(fullpath,fileName);

        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + fullpath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
        System.out.println("writeTxtToFile done");
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile2(String strcontent, File filePath, String fileName) {
        //File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir=null;
        File fullpath=null;
        System.out.println("SDK ver==="+String.valueOf(Build.VERSION.SDK_INT));
        if(Build.VERSION.SDK_INT ==29 ) {
            dir=new File(filePath,"/MicInstant");
            //dir=new File("我的手机/Documents/MicInstant");
            fullpath=dir;
            Log.i("Utils","Using file for Harmony");
        }
        else {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            fullpath=new File(dir,"/MicInstant");
        }
        makeRootDirectory(dir);
        System.out.println("dir:"+dir+File.separator);
        makeRootDirectory(fullpath);
        fullpath=new File(fullpath,"/log");
        makeRootDirectory(fullpath);
        //生成文件夹之后，再生成文件，不然会出错
        File file=makeFilePath(fullpath,fileName);

        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + fullpath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
        System.out.println("writeTxtToFile2 done");
    }
//生成文件

    public static File makeFilePath(File fullpath, String fileName) {
        File file = null;
        //makeRootDirectory(fullpath);
        try {
            file = new File(fullpath ,fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    public static void makeRootDirectory(File fullpath) {
        try {

            if (!fullpath.exists()) {
                fullpath.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    //读取指定目录下的所有TXT文件的文件内容
    private String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }

    public static String GetSystemTime(){
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss", Locale.CHINA);
        Date curDate =  new Date(System.currentTimeMillis());
        //获取当前时间
        String   Time   =   formatter.format(curDate);
        return Time;
    }

}
