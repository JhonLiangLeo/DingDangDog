package com.app.debrove.tinpandog.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.app.debrove.tinpandog.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by NameTooLong on 2017/10/26.
 *
 * 二位码生成实现，产生二维码不保存
 */

public class QrcodeActivity extends Activity {
    /**
     * 二位码中包含的信息
     */
    private String user_information="Zhang San";
    /**
     * 生成的二维码中间logo图片的路径
     * 若为null，则生成二维码无logo
     */
    private String mLogoImagePath;
    private int width_qrcode=300,height_qrcode=300;

    private ImageView mImage_qrcode;
    private Button mButton_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        mImage_qrcode=(ImageView)findViewById(R.id.qrcode_activity_image_qrcode);
        mButton_scan=(Button)findViewById(R.id.qrcode_activity_button_scan);
        mButton_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(QrcodeActivity.this,ScanActivity.class);
                QrcodeActivity.this.startActivity(intent);
            }
        });

        createQrcodeImage(mLogoImagePath);
    }

    /**
       *生成所需的二维码图片，并显示在mImage_qrcode上
        @param imagePath 二维码中间logo的路径，若为null，则
         生成的二维码中间无logo
     */
    private void createQrcodeImage(String imagePath){
        new AsyncTask<String,Bitmap,Void>(){
            @Override
            protected Void doInBackground(String... params) {
                Bitmap bitmap=generateBitmap(params[0],width_qrcode,height_qrcode);
                if(params[1]!=null){
                    Bitmap logo= BitmapFactory.decodeFile(params[1]);
                    bitmap=addLogo(bitmap,logo);
                }
                publishProgress(bitmap);
                return null;
            }

            @Override
            protected void onProgressUpdate(Bitmap... values) {
                mImage_qrcode.setImageBitmap(values[0]);
            }

            private Bitmap generateBitmap(String content, int width, int height) {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                Map<EncodeHintType, String> hints = new HashMap<>();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                try {
                    BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
                    int[] pixels = new int[width * height];
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            if (encode.get(j, i)) {
                                pixels[i * width + j] = 0x00000000;
                            } else {
                                pixels[i * width + j] = 0xffffffff;
                            }
                        }
                    }
                    return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
                int qrBitmapWidth = qrBitmap.getWidth();
                int qrBitmapHeight = qrBitmap.getHeight();
                int logoBitmapWidth = logoBitmap.getWidth();
                int logoBitmapHeight = logoBitmap.getHeight();
                Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(blankBitmap);
                canvas.drawBitmap(qrBitmap, 0, 0, null);
                canvas.save(Canvas.ALL_SAVE_FLAG);
                float scaleSize = 1.0f;
                while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
                    scaleSize *= 2;
                }
                float sx = 1.0f / scaleSize;
                canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
                canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
                canvas.restore();
                return blankBitmap;
            }
        }.execute(user_information,imagePath);
    }
}
