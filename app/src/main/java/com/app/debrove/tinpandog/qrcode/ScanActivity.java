package com.app.debrove.tinpandog.qrcode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.debrove.tinpandog.R;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;

/**
 * Created by NameTooLong on 2017/10/26.
 *
 * 1.二维码扫描实现
 * 2.可以从本地图片或直接使用相机扫描
 */

public class ScanActivity extends Activity implements QRCodeView.Delegate{
    private Button mButton_scanFromAlbum;
    private QRCodeView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mQRCodeView=(ZBarView)findViewById(R.id.scan_activity_zbarview);
        mQRCodeView.setDelegate(this);
        if(ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(ScanActivity.this,new String[]{Manifest.permission.CAMERA},OPEN_CAMERA);
        else
            mQRCodeView.showScanRect();

        mButton_scanFromAlbum=(Button)findViewById(R.id.scan_activity_button_scanFromAlbum);
        mButton_scanFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ScanActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},ASK_PERMISSION);
                else
                    openAlbum();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mQRCodeView.stopSpot();
        mQRCodeView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQRCodeView.onDestroy();
    }

    /**
       *相机打开失误问题处理
     */
    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG,"open camera error");
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        onScanSuccess(result);
    }

    private static final String TAG = "ScanActivity";
    /**
      *处理二维码扫描生成的结果
      *@param mess 二维码扫描得到的字符串信息
      */
    private void onScanSuccess(String mess){
        Log.e(TAG,"mess="+mess);
        Toast.makeText(this,mess,Toast.LENGTH_SHORT).show();
    }

    private void spotImageFromAlbum(final String imagePath){
        if(imagePath==null)
            Toast.makeText(this,"Can't get image path correctly",Toast.LENGTH_SHORT).show();
        else {
            new AsyncTask<String,String,Void>(){
                @Override
                protected Void doInBackground(String... params) {
                    String message= QRCodeDecoder.syncDecodeQRCode(params[0]);
                    publishProgress(message);
                    return null;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    onScanSuccess(values[0]);
                }
            }.execute(imagePath);
        }
    }

    private static final int CHOOSE_FROM_ALBUM=0;
    private static final int ASK_PERMISSION=1;
    private static final int OPEN_CAMERA=2;

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_FROM_ALBUM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode){
            case ASK_PERMISSION:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(this,"Permission denied!",Toast.LENGTH_SHORT).show();
                break;
            case OPEN_CAMERA:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    mQRCodeView.showScanRect();
                else
                    Toast.makeText(this,"Permission denied!",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_FROM_ALBUM:
                if(Build.VERSION.SDK_INT>=19)
                    handleImageOnKitKat(data);
                else
                    handleImageBeforeKitKat(data);
                break;
        }
    }

    @TargetApi(21)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme()))
            imagePath=getImagePath(uri,null);
        else if("file".equalsIgnoreCase(uri.getScheme()))
            imagePath=uri.getPath();
        spotImageFromAlbum(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String imagePath=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst())
                imagePath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return imagePath;
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        spotImageFromAlbum(imagePath);
    }
}
