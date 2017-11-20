package com.app.debrove.tinpandog.groups;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by NameTooLong on 2017/11/4.
 *
 * 用于加载网络及本地图片的静态类，
 * 并在指定位置保存其副本
 */

public class ImageLoader {
    private static String imagePath;
    private static final String imageDirPath="/data/data/com.app.debrove.tinpandog/files/image";

    /**
     * 从网络中加载图片
     * @param url 图片的网络位置
     * @return 图片于本地存储的位置
     */
    public static String saveImageFromNet(final String url){
        new Thread(){
            @Override
            public void run() {
                byte imageByte[]=null;
                try {
                    URL url1=new URL(url);
                    HttpURLConnection connection=(HttpURLConnection)url1.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    InputStream inputStream=connection.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    byte buffer[]=new byte[1024];
                    int len=0;
                    while ((len=inputStream.read(buffer))!=-1)
                        byteArrayOutputStream.write(buffer,0,len);
                    byteArrayOutputStream.close();
                    inputStream.close();
                    imageByte=byteArrayOutputStream.toByteArray();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                imagePath=storeImage(imageByte);
            }
        }.start();
        return imagePath;
    }

    /**
     * 存储本地图片的副本，避免用户删除
     * @param imagePath 图片的路径
     * @return 图片副本的路径
     */
    public static String saveImageFromLocal(String imagePath){
        Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return storeImage(byteArrayOutputStream.toByteArray());
    }

    private static final String TAG = "ImageLoader";
    private static String storeImage(byte imageByte[]){
        File imageDir=new File(imageDirPath);
        if(!imageDir.exists())
            imageDir.mkdir();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-mm-dd.hh:mm:ss");
        Date date=new Date(System.currentTimeMillis());
        File image=new File(imageDir+"/"+simpleDateFormat.format(date)+".png");
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(image);
            fileOutputStream.write(imageByte);
            fileOutputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return image.getAbsolutePath();
    }
}
