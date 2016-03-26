package io.islnd.android.islnd.app.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.islnd.android.islnd.app.R;

public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();
    private static final String IMAGE_DIR = "images";
    private static final String JPG_EXT = ".jpg";

    private static final String DEFAULT_HEADER_ASSET = "default_header.jpg";
    private static final String DEFAULT_PROFILE_ASSET = "default_profile.jpg";

    private static final String SLASH = "/";

    private static final int COMPRESSION = 65;

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmap, String filePath) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File(directory, filePath);

        try {
            FileOutputStream outputStream = new FileOutputStream(bitmapPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, outputStream);
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Uri saveBitmapToInternalStorage(Context context, Bitmap bitmap) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File(directory, getCurrentTimeJpgString());

        try {
            FileOutputStream outputStream = new FileOutputStream(bitmapPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, outputStream);
            try {
                outputStream.close();
                return Uri.fromFile(bitmapPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromInternalStorage(Context context, String filePath) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File(directory, filePath);
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Uri saveBitmapToInternalFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "Failed to get bitmap from uri.");
        }

        Uri newUri = null;
        if (bitmap != null) {
            newUri = saveBitmapToInternalStorage(context, bitmap);
        }
        return newUri;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getSampledBitmapFromUri(Context context, Uri uri, int reqWidth,
                                                 int reqHeight) {
        Bitmap bitmap = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),
                    null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),
                    null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static String getCurrentTimeJpgString() {
        return System.currentTimeMillis() + JPG_EXT;
    }

    public static Bitmap getBitmapFromAssets(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Uri getDefaultProfileImageUri(Context context) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File image = new File(directory, DEFAULT_PROFILE_ASSET);

        if (!image.exists()) {
            saveBitmapToInternalStorage(
                    context,
                    getBitmapFromAssets(context, IMAGE_DIR + SLASH + DEFAULT_PROFILE_ASSET),
                    DEFAULT_PROFILE_ASSET);
        }
        return Uri.fromFile(image);
    }

    public static Uri getDefaultHeaderImageUri(Context context) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File image = new File(directory, DEFAULT_HEADER_ASSET);

        if (!image.exists()) {
            saveBitmapToInternalStorage(
                    context,
                    getBitmapFromAssets(context, IMAGE_DIR + SLASH + DEFAULT_HEADER_ASSET),
                    DEFAULT_HEADER_ASSET);
        }
        return Uri.fromFile(image);
    }

    public static byte[] getByteArrayFromUri(Context context, Uri uri) {
        Bitmap bitmap = getBitmapFromUri(context, uri);
        return getByteArrayFromBitmap(bitmap);
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromByteArray(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static Uri saveBitmapToInternalFromByteArray(Context context, byte[] byteArray) {
        return saveBitmapToInternalStorage(context, getBitmapFromByteArray(byteArray));
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void setProfileImageSampled(Context context, ImageView imageView, Uri uri) {
        int profileDimen = Util.getDpFromResource(context, R.dimen.profile_profile_image);
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                profileDimen,
                profileDimen));
    }

    public static void setHeaderImageSampled(Context context, ImageView imageView, Uri uri) {
        int headerHeightDimen = Util.getDpFromResource(context, R.dimen.profile_header_height);
        int headerWidthDimen = 2 * headerHeightDimen;
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                headerWidthDimen,
                headerHeightDimen));
    }

    public static void setPostProfileImageSampled(Context context, ImageView imageView, Uri uri) {
        int profileDimen = Util.getDpFromResource(context, R.dimen.post_profile_image);
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                profileDimen,
                profileDimen));
    }

    public static void setCommentProfileImageSampled(Context context, ImageView imageView, Uri uri) {
        int profileDimen = Util.getDpFromResource(context, R.dimen.comment_profile_image);
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                profileDimen,
                profileDimen));
    }

    public static void setViewFriendImageSampled(Context context, ImageView imageView, Uri uri) {
        int profileDimen = Util.getDpFromResource(context, R.dimen.friend_profile_image);
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                profileDimen,
                profileDimen));
    }

    public static void setNavProfileImageSampled(Context context, ImageView imageView, Uri uri) {
        int profileDimen = Util.getDpFromResource(context, R.dimen.nav_profile_image);
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                profileDimen,
                profileDimen));
    }

    public static void setNavHeaderImageSampled(Context context, ImageView imageView, Uri uri) {
        int headerHeightDimen = Util.getDpFromResource(context, R.dimen.nav_header_height);
        int headerWidthDimen = 2 * headerHeightDimen;
        imageView.setImageBitmap(ImageUtil.getSampledBitmapFromUri(
                context,
                uri,
                headerWidthDimen,
                headerHeightDimen));
    }
}
