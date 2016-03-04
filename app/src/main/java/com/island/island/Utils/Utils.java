package com.island.island.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.island.island.Database.FriendDatabase;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Profile;
import com.island.island.Models.ProfileWithImageData;
import com.island.island.Models.Comment;
import com.island.island.R;

import org.island.messaging.crypto.CryptoUtil;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils
{
    private static final String TAG = Utils.class.getSimpleName();

    public static String smartTimestampFromUnixTime(long unixTimeMillis)
    {
        // currentTimeMillis is already in UTC!
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - unixTimeMillis / 1000;

        String timestamp = "";

        // Under 1 minute
        if(timeDiff < 60)
        {
            timestamp = timeDiff + (timeDiff == 1 ? " sec" : " secs");
        }
        // Under one hour
        else if(timeDiff >= 60 && timeDiff < 3600)
        {
            long minutes = timeDiff / 60;
            timestamp = minutes + (minutes == 1 ? " min" : " mins");
        }
        // Under 24 hours
        else if(timeDiff >= 3600 && timeDiff < 86400)
        {
            long hours = timeDiff / 3600;
            timestamp = hours + (hours == 1 ? " hr" : " hrs");
        }
        // Display date of post
        else
        {
            TimeZone timeZone = TimeZone.getDefault();

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            dateFormat.setTimeZone(timeZone);

            dateFormat.applyPattern("MMM d, yyyy");
            timestamp = dateFormat.format(new Date(unixTimeMillis));
        }

        return timestamp;
    }

    public static String numberOfCommentsString(int numberOfComments)
    {
        return numberOfComments + " comments";
    }

    public static boolean isUser(Context context, String userName)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String setUser = sharedPref.getString(context.getString(R.string.user_name), "");

        return userName.equals(setUser);
    }

    public static String getUser(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.user_name), "");
    }

    public static String getPseudonym(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pseudonym), "");
    }

    public static String getPseudonymSeed(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pseudonym_seed), "");
    }

    public static Key getGroupKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodeSymmetricKey(
                sharedPref.getString(context.getString(R.string.group_key), ""));
    }

    public static Key getPrivateKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodePrivateKey(
                sharedPref.getString(context.getString(R.string.private_key), ""));
    }

    public static void setUser(Context context, String userName)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.user_name), userName);
        editor.commit();
    }

    public static void printAvailableMemory(Context context, String tag) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v(tag, memoryClass + "mb available");
    }

    public static Profile saveProfileWithImageData(Context context, ProfileWithImageData profile) {
        Uri savedProfileImageUri = ImageUtils.saveBitmapToInternalFromByteArray(context,
                profile.getProfileImageByteArray());
        Uri savedHeaderImageUri = ImageUtils.saveBitmapToInternalFromByteArray(
                context,
                profile.getHeaderImageByteArray());

        return new Profile(
                profile.getUsername(),
                profile.getAboutMe(),
                savedProfileImageUri,
                savedHeaderImageUri,
                profile.getVersion()
        );
    }

    public static String getApiKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.api_key), "");
    }

    public static void setApiKey(Context context, String apiKey) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.api_key), apiKey);
        editor.commit();
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getDpFromResource(Context context, int res) {
        return (int) Utils.dpFromPx(context, context.getResources().getDimension(res));
    }

    public static List<CommentViewModel> buildCommentViewModels(Context context, List<Comment> comments) {
        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
        List<CommentViewModel> commentViewModels = new ArrayList<>();
        for (Comment comment : comments) {
            commentViewModels.add(buildCommentViewModel(friendDatabase, comment));
        }

        return commentViewModels;
    }

    public static CommentViewModel buildCommentViewModel(Context context, Comment comment) {
        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
        return buildCommentViewModel(friendDatabase, comment);
    }

    private static CommentViewModel buildCommentViewModel(FriendDatabase friendDatabase, Comment comment) {
        String username = friendDatabase.getUsername(comment.getCommentUserId());
        return new CommentViewModel(
                username,
                comment.getCommentUserId(),
                comment.getCommentId(),
                comment.getContent(),
                comment.getTimestamp());
    }

    public static void buildQrCode(ImageView qrImageView, String content) {
        final int DIMEN = 250;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DIMEN, DIMEN);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if(bitMatrix == null) {
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(DIMEN, DIMEN, Bitmap.Config.RGB_565);
        for (int x = 0; x < DIMEN; x++) {
            for (int y = 0; y < DIMEN; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        qrImageView.setImageBitmap(bmp);
    }
}
