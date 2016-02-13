package com.island.island.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.island.island.Database.IdentityDatabase;
import com.island.island.R;

import org.apache.commons.lang3.SerializationUtils;
import org.island.messaging.Crypto;

import java.security.Key;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by poo on 1/26/16.
 */
public class Utils
{
    public static String smartTimestampFromUnixTime(long unixTime)
    {
        // currentTimeMillis is already in UTC!
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - unixTime;

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
            timestamp = dateFormat.format(new Date(unixTime * 1000));
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

    public static void setUser(Context context, String userName)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.user_name), userName);
        editor.commit();
    }

    public static String convertToString(Key key) {
        byte[] data = SerializationUtils.serialize(key);
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static Key convertFromString(String s) {
        byte[] data = Base64.decode(s, Base64.NO_WRAP);
        return (Key) SerializationUtils.deserialize(data);
    }
}
