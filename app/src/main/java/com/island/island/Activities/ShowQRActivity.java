package com.island.island.Activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.island.island.R;

import org.island.messaging.MessageLayer;

// https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/encode/QRCodeEncoder.java

public class ShowQRActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildQrCode(MessageLayer.getEncodedString(getApplicationContext()));
    }

    private void buildQrCode(String content)
    {
        final int DIMEN = 250;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DIMEN, DIMEN);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if(bitMatrix == null)
        {
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(DIMEN, DIMEN, Bitmap.Config.RGB_565);
        for (int x = 0; x < DIMEN; x++)
        {
            for (int y = 0; y < DIMEN; y++)
            {
                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        ImageView qrImageView = (ImageView) findViewById(R.id.qr_image_view);
        qrImageView.setImageBitmap(bmp);
    }
}
