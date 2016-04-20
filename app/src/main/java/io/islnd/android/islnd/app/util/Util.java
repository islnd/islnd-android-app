package io.islnd.android.islnd.app.util;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.BootReceiver;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SyncAlarm;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Comment;
import io.islnd.android.islnd.app.models.CommentViewModel;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.preferences.AppearancePreferenceFragment;
import io.islnd.android.islnd.app.preferences.NotificationsPreferenceFragment;
import io.islnd.android.islnd.app.preferences.ServerPreferenceFragment;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class Util {
    
    private static final String TAG = Util.class.getSimpleName();

    public static String smartTimestampFromUnixTime(Context context, long unixTimeMillis) {
        // currentTimeMillis is already in UTC!
        long currentTime = ServerTime.getCurrentTimeMillis(context) / 1000;
        long timeDiff = currentTime - unixTimeMillis / 1000;

        String timestamp = "";

        // Under 1 minute
        if (timeDiff < 60) {
            timestamp = timeDiff + (timeDiff == 1 ? " sec" : " secs");
        }
        // Under one hour
        else if (timeDiff >= 60 && timeDiff < 3600) {
            long minutes = timeDiff / 60;
            timestamp = minutes + (minutes == 1 ? " min" : " mins");
        }
        // Under 24 hours
        else if (timeDiff >= 3600 && timeDiff < 86400) {
            long hours = timeDiff / 3600;
            timestamp = hours + (hours == 1 ? " hr" : " hrs");
        }
        // Display date of post
        else {
            TimeZone timeZone = TimeZone.getDefault();

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            dateFormat.setTimeZone(timeZone);

            dateFormat.applyPattern("MMM d, yyyy");
            timestamp = dateFormat.format(new Date(unixTimeMillis));
        }

        return timestamp;
    }

    public static String numberOfCommentsString(int numberOfComments) {
        String comments = numberOfComments == 1 ? " Comment" : " Comments";
        return numberOfComments + comments;
    }

    public static boolean getUsesApiKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.uses_api_key), false);
    }

    public static void setUsesApiKey(Context context, boolean usesApiKey) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(context.getString(R.string.uses_api_key), usesApiKey);
        editor.commit();
    }

    public static boolean getHasCreatedAccount(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.has_created_account), false);
    }

    public static void setHasCreatedAccount(Context context, boolean hasCreatedAccount) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(context.getString(R.string.has_created_account), hasCreatedAccount);
        editor.apply();
    }

    public static void applyAppTheme(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPref.getString(AppearancePreferenceFragment.PREFERENCE_THEME_KEY, "1");

        switch (value) {
            case "1": // Light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "2": // Dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "3": // DayNight Auto
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
    }

    public static void setGroupKey(Context context, SecretKey groupKey) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        String encodedGroupKey = CryptoUtil.encodeKey(groupKey);
        editor.putString(context.getString(R.string.group_key), encodedGroupKey);
        editor.commit();
    }

    public static Key getGroupKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodeSymmetricKey(
                sharedPref.getString(context.getString(R.string.group_key), ""));
    }

    public static PublicKey getPublicKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodePublicKey(
                sharedPref.getString(context.getString(R.string.public_key), ""));
    }

    public static PrivateKey getPrivateKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodePrivateKey(
                sharedPref.getString(context.getString(R.string.private_key), ""));
    }

    public static int getEventId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(context.getString(R.string.event_id), 0);
    }

    public static void setEventId(Context context, int eventId) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.event_id), eventId);
        editor.commit();
    }

    public static int getMessageId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(context.getString(R.string.message_id), 0);
    }

    public static void setMessageId(Context context, int eventId) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.message_id), eventId);
        editor.commit();
    }

    public static String getApiKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(ServerPreferenceFragment.PREFERENCE_API_KEY_KEY, "");
    }

    public static void setApiKey(Context context, String apiKey) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ServerPreferenceFragment.PREFERENCE_API_KEY_KEY, apiKey);
        editor.commit();
    }

    public static void deleteSharedPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    public static void deleteDataAndPreferences(Context context) {
        Log.v(TAG, "deleting all data and preferences...");
        DataUtils.deleteAll(context);
        deleteSharedPreferences(context);
    }

    public static void restartActivity(AppCompatActivity context) {
        context.finish();
        context.startActivity(context.getIntent());
    }

    public static void restartApp(AppCompatActivity context) {
        Intent intent = context
                .getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.finish();
        context.startActivity(intent);
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getDpFromResource(Context context, int res) {
        return (int) Util.dpFromPx(context, context.getResources().getDimension(res));
    }

    public static List<CommentViewModel> buildCommentViewModels(Context context, List<Comment> comments) {
        List<CommentViewModel> commentViewModels = new ArrayList<>();
        for (Comment comment : comments) {
            commentViewModels.add(buildCommentViewModel(null, comment));
        }

        return commentViewModels;
    }

    public static CommentViewModel buildCommentViewModel(Context context, Comment comment) {
        throw new UnsupportedOperationException("working on it");
    }

    public static void buildQrCode(ImageView qrImageView, String content) {
        final int DIMEN = 500;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DIMEN, DIMEN);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if (bitMatrix == null) {
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(DIMEN, DIMEN, Bitmap.Config.RGB_565);
        for (int x = 0; x < DIMEN; x++) {
            for (int y = 0; y < DIMEN; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        qrImageView.setImageBitmap(bmp);
    }

    public static Profile buildDefaultProfile(Context context, String displayName) {
        // TODO: default image Uris will probably be assets...
        return new Profile(
                "",
                ImageUtil.getDefaultProfileImageUri(context),
                ImageUtil.getDefaultHeaderImageUri(context));
    }

    public static Account getSyncAccount(Context context) {
        return new Account(
                context.getString(R.string.sync_account),
                context.getString(R.string.sync_account_type));
    }

    public static void enableIncrementalSyncs(Context context) {
        BootReceiver.enableReceiver(context);
        SyncAlarm.enableReceiver(context);
        applySyncInterval(context);
    }

    public static void disableIncrementalSyncs(Context context) {
        BootReceiver.disableReceiver(context);
        SyncAlarm.disableReceiver(context);
        SyncAlarm.cancelAlarm(context);
    }

    public static String[] partition(String s, int partitions) {
        String[] result = new String[partitions];

        int start = 0;
        int length = s.length() / partitions;
        for (int i = 0; i < partitions; i++) {
            if (i == partitions - 1) {
                result[i] = s.substring(start);
            } else {
                result[i] = s.substring(start, start + length);
                start += length;
            }
        }

        return result;
    }

    public static String formatWithColons(String s) {
        if (s.length() < 3) {
            return s;
        }

        StringBuilder withColons = new StringBuilder(s.length() * 2);
        final boolean isEven = s.length() % 2 == 0;

        int startIndex = 0;
        if (isEven) {
            withColons.append(s.charAt(0));
            withColons.append(s.charAt(1));
            startIndex = 2;
        } else {
            withColons.append(s.charAt(0));
            startIndex = 1;
        }

        for (int i = startIndex; i < s.length(); i+=2) {
            withColons.append(":");
            withColons.append(s.charAt(i));
            withColons.append(s.charAt(i + 1));
        }

        return withColons.toString();
    }

    public static boolean getHasRequestSmsInvitePermission(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.has_requested_sms_invite_permission), false);
    }

    public static void setHasRequestSmsInvitePermission(Context context, boolean hasRequestedPermission) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(context.getString(R.string.has_requested_sms_invite_permission), hasRequestedPermission);
        editor.apply();
    }

    public static void setSyncIntervalFromPreference(Context context, String arrayValue) {
        switch (arrayValue) {
            case "1":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_FIVE_MINUTES);
                break;
            case "2":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_TEN_MINUTES);
                break;
            case "3":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_FIFTEEN_MINUTES);
                break;
            case "4":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_THIRTY_MINUTES);
                break;
            case "5":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_ONE_HOUR);
                break;
            case "6":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_TWO_HOURS);
                break;
            case "7":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_FOUR_HOURS);
                break;
            case "8":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_TWELVE_HOURS);
                break;
            case "9":
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_TWENTY_FOUR_HOURS);
                break;
            default:
                SyncAlarm.setAlarm(context, SyncAlarm.SYNC_INTERVAL_THIRTY_MINUTES);
        }
    }

    public static void applySyncInterval(Context context) {
        if (!getNotificationsEnabled(context)) {
            Log.d(TAG, "applySyncInterval: notifications disabled");
            return;
        }

        String value = android.support.v7.preference.PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(
                        NotificationsPreferenceFragment.PREF_SYNC_INTERVAL_KEY,
                        context.getString(R.string.pref_sync_interval_default_value));
        setSyncIntervalFromPreference(context, value);
    }

    public static boolean getNotificationsEnabled(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(NotificationsPreferenceFragment.PREF_ENABLE_NOTIFICATIONS_KEY, true);
    }
}
