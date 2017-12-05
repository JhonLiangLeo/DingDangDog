package com.app.debrove.tinpandog.groups;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static Bitmap mBitmap;

    /**
     * 加载图片至本地并储存
     * @param url 图片的网络位置或其本地路径
     * @return 图片于本地存储的位置
     */
    public static String saveImage(final String url, Context context){
        try {
            mBitmap=Glide.with(context).load(url).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
            imagePath=storeImage(mBitmap);
            mBitmap=null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    private static final String TAG = "ImageLoader";
    private static String storeImage(Bitmap bitmap){
        File imageDir=new File(imageDirPath);
        if(!imageDir.exists())
            imageDir.mkdir();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-mm-dd.hh:mm:ss");
        Date date=new Date(System.currentTimeMillis());
        File image=new File(imageDir+"/"+simpleDateFormat.format(date)+".png");
        FileOutputStream fileOutputStream=null;
        try{
            fileOutputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if(fileOutputStream!=null)
                    fileOutputStream.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        Log.e(TAG,"path="+image.getAbsolutePath());
        return image.getAbsolutePath();
    }
}
