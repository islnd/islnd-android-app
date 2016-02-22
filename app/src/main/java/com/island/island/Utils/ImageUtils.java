package com.island.island.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by poo on 2/10/2016.
 */
public class ImageUtils
{
    private static String IMAGE_DIR = "images";

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmap, String filePath)
    {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File (directory, filePath);
        FileOutputStream outputStream = null;

        try
        {
            outputStream = new FileOutputStream(bitmapPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromInternalStorage(Context context, String filePath)
    {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File (directory, filePath);
        Bitmap bitmap = null;

        try
        {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getBitmapFromAssets(Context context, String filePath)
    {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try
        {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromByteArray(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
